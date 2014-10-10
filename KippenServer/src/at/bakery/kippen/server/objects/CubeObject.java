package at.bakery.kippen.server.objects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.BatteryData;
import at.bakery.kippen.common.data.CubeOrientationData;
import at.bakery.kippen.common.data.MoveData;
import at.bakery.kippen.common.data.ShakeData;
import at.bakery.kippen.common.data.WifiLevelsData;
import at.bakery.kippen.server.EventTypes;
import at.bakery.kippen.server.KippenServer;
import at.bakery.kippen.server.command.Command;
import at.bakery.kippen.server.outlets.AbstractKippOutlet;
import at.bakery.kippen.server.outlets.CsvKippOutlet;

public class CubeObject extends AbstractKippenObject {
	static Logger log = Logger.getLogger(CubeObject.class.getName());
	private int currentSide = -1;

	private double MOVE_DATA_THRESHHOLD = 0.1;

	private Queue<WifiLevelsData> avgWifiLevel = new LinkedList<>();
	private boolean moveDataWasBelowThreshhold = false;

	public CubeObject(String id) {
		super(id);
		log.setLevel(KippenServer.LOG_LEVEL);
	}

	@Override
	public void processData(AbstractData d) {
		super.processData(d);

		
		if (d instanceof WifiLevelsData) {
			processWifiData((WifiLevelsData) d);
		} else if (d instanceof AccelerationData) {
			processAccelerationData((AccelerationData) d);
		} else if (d instanceof CubeOrientationData) {
			processCubeOrientationData((CubeOrientationData) d);
		} else if (d instanceof ShakeData) {
			processShakeData((ShakeData) d);
		} else if (d instanceof MoveData) {
			System.out.println("moving");
			processMoveData((MoveData) d);
		} else if (d instanceof BatteryData) {
			processBatteryData((BatteryData) d);
		}

		output();
	}

	protected void output() {
		for (AbstractKippOutlet aOutlet : outletObjects) {
			if (aOutlet instanceof CsvKippOutlet) {
				aOutlet.output();
			}
		}
	}

	protected void timeout() {
		super.timeout();
	}

	// 2 seconds delay before a new shake is processed
	private static final long NEW_SHAKE_AFTER = (long) 1e9;
	private long lastShook = System.nanoTime();

	private void processShakeData(ShakeData shakeData) {
		if(shakeData.isShaking() == false) {
			return;
		}
		
		long curTime = System.nanoTime();
		if (curTime - lastShook < NEW_SHAKE_AFTER) {
			// ignore if shake events indifferent
			return;
		}

		lastShook = curTime;
		HashMap<String, String> paramMap = new HashMap<String, String>();

		List<Command> sideChangeEvents = eventsOfObject.get(EventTypes.SHAKE);
		if (sideChangeEvents != null) {
			for (Command c : eventsOfObject.get(EventTypes.SHAKE)) {
				try {
					c.execute(paramMap);
				} catch (Exception e) {
					log.warning("Failed to execute command " + c.getClass().getSimpleName());
				}
			}
		}
	}

	private void processCubeOrientationData(CubeOrientationData data) {
		CubeOrientationData cd = (CubeOrientationData) data;
		if (cd.getOrientation() == CubeOrientationData.Orientation.UNKNOWN) {
			return;
		}
		
		int sideInt = cd.getOrientation().ordinal();
		if (sideInt == currentSide) {
			return;
		}

		String sideString = String.valueOf(sideInt);
		log.log(Level.FINE,"Executing side change with side " + sideString);
		
		HashMap<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clipNumber", sideString);
		
		List<Command> sideChangeEvents = eventsOfObject.get(EventTypes.SIDECHANGE);
		if (sideChangeEvents != null) {
			for(Command c : sideChangeEvents) {
				try {
					c.execute(paramMap);
				} catch (Exception e) {
					log.warning("Failed to execute command " + c.getClass().getSimpleName());
				}
			}

			// set the current side
			currentSide = sideInt;
		}
	}


	private void processWifiData(WifiLevelsData data) {
		WifiLevelsData wd = (WifiLevelsData) data;

		// add the current measurement to the list
		avgWifiLevel.offer(wd);

		// compute average
		if (avgWifiLevel.size() > 0) {
			double avgLevel = 0;

			// for each measurement item ...
			for (WifiLevelsData w : avgWifiLevel) {
				double innerAvgLevel = 0;

				// for each measured wifi in the item ...
				for (Entry<String, Object> val : w.getNetworks()) {
					innerAvgLevel += (double) val.getValue();
				}

				// ... compute average
				avgLevel += (innerAvgLevel / w.getNetworks().size());
			}
			avgLevel /= avgWifiLevel.size();

			// RSSI to meters conversion
			double dist = Math.pow(10, ((27.55 - (67.6 + avgLevel)) / 20.0));
			log.log(Level.FINEST, "~ " + dist + "m (level: " + avgLevel + ")");

			// remember the last to measurements, remove others
			if (avgWifiLevel.size() > 10) {
				avgWifiLevel.poll();
			}
		}

	}

	private void processAccelerationData(AccelerationData data) {}


	private void processBatteryData(BatteryData data) {}

	private void processMoveData(MoveData data) {
		double lengthVector = Math.abs(Math.sqrt(data.getX() * data.getX() + data.getY() * data.getY() + data.getZ()
				* data.getZ()));
		HashMap<String, String> paramMap = new HashMap<String, String>();
		
		
		System.out.println("length " + lengthVector);
		if (lengthVector > MOVE_DATA_THRESHHOLD) {

			NormalizedField normalizer = new NormalizedField(NormalizationAction.Normalize, null, 3.0, 0.0, 1.0, 0.0);
			double normalizedValue = normalizer.normalize(lengthVector);
			// log.log(Level.FINEST, "Length vector: " + lengthVector);
			// log.log(Level.FINEST, "Normalized value: " + normalizedValue);

			paramMap.put("value", String.valueOf(normalizedValue));

			System.out.println(normalizedValue);
			executeCommands(paramMap, EventTypes.MOVE);
			moveDataWasBelowThreshhold = false;
		} else {
			//if we are below threshold set the value to 0
			if (moveDataWasBelowThreshhold == false) {
				paramMap.put("value", String.valueOf(0.0f));
				executeCommands(paramMap, EventTypes.MOVE);
				moveDataWasBelowThreshhold = true;
			}

		}
	}

	private void executeCommands(HashMap<String, String> paramMap, String eventType) {
		List<Command> commands = eventsOfObject.get(eventType);
		if (commands != null) {
			for (Command c : commands) {
				try {
					c.execute(paramMap);
				} catch (Exception e) {
					log.warning("Failed to execute command " + c.getClass().getSimpleName());
				} finally {

				}
			}
		}
	}
}

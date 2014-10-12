package at.bakery.kippen.server.objects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

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

public class CubeObject extends AbstractKippenObject {
	static Logger log = Logger.getLogger(CubeObject.class.getName());
	private int currentSide = -1;

	private Queue<WifiLevelsData> avgWifiLevel = new LinkedList<>();

	public CubeObject(String id, int timeout) {
		super(id, timeout);
		log.setLevel(KippenServer.LOG_LEVEL);
	}

	@Override
	public void processData(AbstractData d) {
		super.processData(d);

		log.log(Level.FINEST, "CUBE processes " + d.getClass().getSimpleName() + " -> " + d.toString());
		if (d instanceof WifiLevelsData) {
			processWifiData((WifiLevelsData) d);
		} else if (d instanceof AccelerationData) {
			processAccelerationData((AccelerationData) d);
		} else if (d instanceof CubeOrientationData) {
			processCubeOrientationData((CubeOrientationData) d);
		} else if (d instanceof ShakeData) {
			processShakeData((ShakeData) d);
		} else if (d instanceof MoveData) {
			processMoveData((MoveData) d);
		} else if (d instanceof BatteryData) {
			processBatteryData((BatteryData) d);
		}

	}

	protected void timeout() {
		super.timeout();
	}

	private long lastShook = System.nanoTime();

	private void processShakeData(ShakeData shakeData) {
		if (shakeData.isShaking() == false) {
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
		log.log(Level.FINE, "Executing side change with side " + sideString);

		HashMap<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clipNumber", sideString);

		List<Command> sideChangeEvents = eventsOfObject.get(EventTypes.SIDECHANGE);
		if (sideChangeEvents != null) {
			for (Command c : sideChangeEvents) {
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

	private void processAccelerationData(AccelerationData data) {
	}

	private void processBatteryData(BatteryData data) {
	}

	private void processMoveData(MoveData data) {
	}

}

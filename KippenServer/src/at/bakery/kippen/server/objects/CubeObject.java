package at.bakery.kippen.server.objects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.logging.Logger;

import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.BatteryData;
import at.bakery.kippen.common.data.CubeOrientationData;
import at.bakery.kippen.common.data.DirectionOrientationData;
import at.bakery.kippen.common.data.MoveData;
import at.bakery.kippen.common.data.SensorTripleData;
import at.bakery.kippen.common.data.ShakeData;
import at.bakery.kippen.common.data.WifiLevelsData;
import at.bakery.kippen.server.EventTypes;
import at.bakery.kippen.server.command.Command;
import at.bakery.kippen.server.outlets.AbstractKippOutlet;
import at.bakery.kippen.server.outlets.CsvKippOutlet;

public class CubeObject extends AbstractKippenObject {
	static Logger log =  Logger.getLogger(CubeObject.class.getName());

	public CubeObject(String id) {
		super(id);
	}

	@Override
	public void processData(AbstractData d) {
		//log.info(d.getClass().getSimpleName() + " -> " + d.toString());
		
		super.processData(d);
		
		if (d instanceof WifiLevelsData) {
			processWifiData((WifiLevelsData) d);
		} else if (d instanceof AccelerationData) {
			processAccelerationData((AccelerationData) d);
		} else if (d instanceof CubeOrientationData) {
			processCubeOrientationData((CubeOrientationData) d);
		} else if (d instanceof DirectionOrientationData) {
			processOrientationData((DirectionOrientationData) d);
		} else if (d instanceof ShakeData) {
			log.info(d.getClass().getSimpleName() + " -> " + d.toString());
			processShakeData();
		} else if(d instanceof MoveData) {
			processMoveData((MoveData)d);
		} else if(d instanceof BatteryData) {
			processBatteryData((BatteryData)d);
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
	
	protected void stop() {
		// FIXME implement ableton stop
		System.out.println("STOP the cube");
	}

	// 2 seconds delay before a new shake is processed
	private static final long NEW_SHAKE_AFTER = (long)2e9;
	private long lastShook = System.nanoTime();
	
	private void processShakeData() {
		long curTime = System.nanoTime();
		if(curTime - lastShook < NEW_SHAKE_AFTER) {
			// ignore if shake events indifferent
			return;
		}
		
		lastShook = curTime;
		HashMap<String, String> paramMap = new HashMap<String, String>();
		
		for(Command c : eventMap.get(EventTypes.SHAKE)) {
			try {
				c.execute(paramMap);
			} catch (Exception e) {
				log.warning("Failed to execute command " + c.getClass().getSimpleName());
			}
		}
	}

	private void processCubeOrientationData(CubeOrientationData data) {
		CubeOrientationData cd = (CubeOrientationData) data;

		if(cd.getOrientation() == CubeOrientationData.Orientation.UNKNOWN) {
			return;
		} 
		
		String side = String.valueOf(cd.getOrientation().ordinal());
		
		log.info("Executing side change with side " + side);
		HashMap<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("clipNumber", side);
		List<Command> sideChangeEvents = eventMap.get(EventTypes.SIDECHANGE);
		
		if (sideChangeEvents != null) {
			for (Command c : sideChangeEvents) {
				try {
					c.execute(paramMap);
				} catch (Exception e) {
					log.warning("Failed to execute command " + c.getClass().getSimpleName());
				}
			}
		}
	}
	
	private Queue<WifiLevelsData> avgWifiLevel = new LinkedList<>();
	
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
					innerAvgLevel += (double)val.getValue();
				}

				// ... compute average
				avgLevel += (innerAvgLevel / w.getNetworks().size());
			}
			avgLevel /= avgWifiLevel.size();

			// RSSI to meters conversion
			double dist = Math.pow(10, ((27.55 - (67.6 + avgLevel)) / 20.0));
			log.info("~ " + dist + "m (level: " + avgLevel + ")");

			// remember the last to measurements, remove others
			if (avgWifiLevel.size() > 10) {
				avgWifiLevel.poll();
			}
		}

	}

	private void processAccelerationData(AccelerationData data) {
		SensorTripleData sd = (SensorTripleData) data;
		//log.info("" + Math.sqrt(sd.getX() * sd.getX() + sd.getY() * sd.getY() + sd.getZ() + sd.getZ()));
	}

	private void processBatteryData(BatteryData data) {}
	private void processOrientationData(DirectionOrientationData data) {}
	private void processMoveData(MoveData data) {}
}
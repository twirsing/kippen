package at.bakery.kippen.server.objects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.BarrelOrientationData;
import at.bakery.kippen.common.data.BatteryData;
import at.bakery.kippen.common.data.MoveData;
import at.bakery.kippen.common.data.SensorTripleData;
import at.bakery.kippen.common.data.ShakeData;
import at.bakery.kippen.common.data.WifiLevelsData;
import at.bakery.kippen.server.EventTypes;
import at.bakery.kippen.server.KippenServer;
import at.bakery.kippen.server.command.Command;

public class BarrelObject extends AbstractKippenObject {
	static Logger log = Logger.getLogger(BarrelObject.class.getName());

	double lastBarrelValue = 0;

	private static double MIN_VALUE_DELTA = 0.01;

	public BarrelObject(String id) {
		super(id);
		log.setLevel(KippenServer.LOG_LEVEL);
	}

	@Override
	public void processData(AbstractData d) {
		super.processData(d);

		log.log(Level.FINEST, "BARREL processes " + d.getClass().getSimpleName() + " -> " + d.toString());

		if (d instanceof WifiLevelsData) {
			processWifiData((WifiLevelsData) d);
		} else if (d instanceof BarrelOrientationData) {
			processOrientationData((BarrelOrientationData) d);
		} else if (d instanceof BatteryData) {
			processBatteryData((BatteryData) d);
		}

	}

	@Override
	protected void timeout() {
		super.timeout();
	}

	private void processOrientationData(BarrelOrientationData data) {
		log.log(Level.FINEST, "Barrel relative orientation " + data.getValue());
		HashMap<String, String> paramMap = new HashMap<String, String>();

		double barrelValue = Double.valueOf(data.getValue());

		if (Math.abs(barrelValue - lastBarrelValue) > MIN_VALUE_DELTA) {

			System.out.println("barrel change");
			paramMap.put("value", String.valueOf(barrelValue));

			for (Command c : eventsOfObject.get(EventTypes.ROLLCHANGE)) {
				try {
					c.execute(paramMap);
				} catch (Exception e) {
					e.printStackTrace();
					log.warning("Failed to execute command " + c.getClass().getSimpleName());
				}
			}

			lastBarrelValue = barrelValue;

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
		SensorTripleData sd = (SensorTripleData) data;
		// log.info("" + Math.sqrt(sd.getX() * sd.getX() + sd.getY() * sd.getY()
		// + sd.getZ() + sd.getZ()));
	}

	private void processBatteryData(BatteryData data) {
	}

	private void processMoveData(MoveData data) {
	}
}

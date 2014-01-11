package at.bakery.kippen.server.objects;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import at.bakery.kippen.common.IData;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.BatteryData;
import at.bakery.kippen.common.data.OrientationData;
import at.bakery.kippen.common.data.OrientationSimpleData;
import at.bakery.kippen.common.data.SensorSingleData;
import at.bakery.kippen.common.data.SensorTripleData;
import at.bakery.kippen.common.data.WifiLevelsData;
import at.bakery.kippen.server.outlets.AbstractKippOutlet;
import at.bakery.kippen.server.outlets.CsvKippOutlet;
import at.bakery.kippen.server.outlets.JframeKippOutlet;


public class CubeKippObject extends AbstractKippObject {

	
	private String lastOrientation;
	private double lastEffAcc;
	private Queue<WifiLevelsData> avgWifiLevel = new LinkedList();
	private double lastAvgWifiLevel = -1;
	private SensorTripleData lastAccData = new SensorTripleData(0, 0, 0);
	

	/**
	 * @param id usually mac address
	 */
	public CubeKippObject(String id) {
		super(id);
	}
	
	@Override
	public void addOutlet(AbstractKippOutlet aKippOutlet) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void output() {
		// TODO Auto-generated method stub
		
		for (AbstractKippOutlet aOutlet : outletObjects) {
		    
			if(aOutlet instanceof CsvKippOutlet) {
				aOutlet.output();
			}
			if(aOutlet instanceof JframeKippOutlet) {
				Map<String, String> outputdata = new HashMap<String, String>();
				outputdata.put("info", id);
				outputdata.put("wifi", dataObjects.get("WifiLevelsData").toString());
				outputdata.put("accaleration", String.valueOf(lastEffAcc));
				outputdata.put("orientation", lastOrientation);
				outputdata.put("distance", lastOrientation);
				
				JframeKippOutlet jf = (JframeKippOutlet) aOutlet;
				jf.output(outputdata);
			}
		}

	}

	@Override
	public void processData(IData d) {
		// TODO Auto-generated method stub
		
		super.processData(d);
		
		if(d instanceof WifiLevelsData) {
			processWifiData((WifiLevelsData)d);			
		
		// ACCELERATION
		} else if(d instanceof AccelerationData) {
			processAccelerationData((AccelerationData)d);
			
		// COMPLEX ORIENTATION
		} else if(d instanceof OrientationSimpleData) {
			SensorTripleData sd = (SensorTripleData)d;
			
			//orientText.setText(sd.x + ", " + sd.y + ", " + sd.z);
			
		// SIMPLE (CUBE) ORIENTATION
		} else if(d instanceof OrientationSimpleData) {
			processOrientationData((OrientationSimpleData)d);
		}
		
		output();
	}
	

	@Override
	protected void processWifiData(WifiLevelsData data) {
		WifiLevelsData wd = (WifiLevelsData)data;
		
		//wifiDistanceText.setText(wd.toString());
		
		// add the current measurement to the list
		avgWifiLevel.offer(wd);
		
		// compute average
		if(avgWifiLevel.size() > 0) {
			double avgLevel = 0;
			
			// for each measurement item ...
			for(WifiLevelsData w : avgWifiLevel) {
				double innerAvgLevel = 0;
				
				// for each measured wifi in the item ...
				for(Integer val : w.getNetworks().values()) {
					innerAvgLevel += val;
				}
				
				// ... compute average
				avgLevel += (innerAvgLevel / w.getNetworks().values().size());
			}
			avgLevel /= avgWifiLevel.size();
			
			// RSSI to meters conversion
			double dist = Math.pow(10, ((27.55 - (67.6 + avgLevel)) / 20.0));
			//metricDistanceText.setText("~ " + dist + "m (level: " + avgLevel + ")");
			
			// remember the last to measurements, remove others
			if(avgWifiLevel.size() > 10) {
				avgWifiLevel.poll();
			}
		}
		
	}
	
	@Override
	protected void processBatteryData(BatteryData data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void processOrientationData(OrientationSimpleData data) {
		SensorSingleData sd = (SensorSingleData)data;
		int deg = (int)sd.value;
		
		// it is NOT flat on the ground
		if(deg != -1) {
			if(deg >= 315 || deg < 45) {
				lastOrientation = "down";
			} else if(deg >= 45 && deg < 135) {
				lastOrientation = "right";
			} else if(deg >= 135 && deg < 225) {
				lastOrientation = "up";
			} else if(deg >= 225 && deg < 315) {
				lastOrientation = "left";
			}
		}
		// it IS flat on the ground
		else {
			if(lastAccData.z < 0) {
				lastOrientation = "top";
			} else {
				lastOrientation = "bottom";
			}
		}
		
	}

	@Override
	protected void processAccelerationData(AccelerationData data) {

		SensorTripleData sd = (SensorTripleData)data;
		
		lastAccData = sd;
		
		lastEffAcc = Math.sqrt(sd.x * sd.x + sd.y * sd.y + sd.z + sd.z); 
		
	}



}

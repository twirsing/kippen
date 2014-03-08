/**
 * 
 */
package at.bakery.kippen.server.objects;

import java.util.HashMap;
import java.util.HashSet;

import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.BatteryData;
import at.bakery.kippen.common.data.DirectionOrientationData;
import at.bakery.kippen.common.data.WifiLevelsData;
import at.bakery.kippen.server.outlets.AbstractKippOutlet;

/**
 * @author thomasw
 *
 */
public abstract class AbstractKippObject {

	protected String id;

	protected HashSet<AbstractKippOutlet> outletObjects = new HashSet<AbstractKippOutlet>();
	
	// FIXME instead of String, Class<AbstractData> could potentially be used !?
	protected HashMap<String, AbstractData> dataObjects = new HashMap<String, AbstractData>();

	/**
	 * @param id usually mac address
	 */
	public AbstractKippObject(String id) {
		this.id = id;
	}
	
	public void addOutlet(AbstractKippOutlet aKippOutlet){
		outletObjects.add(aKippOutlet);
	}
	
	protected abstract void output();
	
	public void processData(AbstractData data){
		dataObjects.put(data.getClass().toString(), data);
		
	}
	
	protected abstract void processWifiData(WifiLevelsData data);
	protected abstract void processBatteryData(BatteryData data);
	protected abstract void processOrientationData(DirectionOrientationData data);
	protected abstract void processAccelerationData(AccelerationData data);
	
	public String getId() {
		return id;
	}

}

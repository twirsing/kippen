/**
 * 
 */
package at.bakery.kippen.server.objects;

import java.util.HashMap;
import java.util.HashSet;

import at.bakery.kippen.common.IData;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.BatteryData;
import at.bakery.kippen.common.data.OrientationData;
import at.bakery.kippen.common.data.OrientationSimpleData;
import at.bakery.kippen.common.data.WifiLevelsData;
import at.bakery.kippen.server.outlets.AbstractKippOutlet;

/**
 * @author thomasw
 *
 */
public abstract class AbstractKippObject {

	protected String id;

	protected HashSet<AbstractKippOutlet> outletObjects = new HashSet<AbstractKippOutlet>();
	
	protected HashMap<String, IData> dataObjects = new HashMap<String, IData>();

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
	
	public void processData(IData d){
		
		dataObjects.put(d.getClass().toString(), d);
		
	}
	
	protected abstract void processWifiData(WifiLevelsData data);
	protected abstract void processBatteryData(BatteryData data);
	protected abstract void processOrientationData(OrientationSimpleData data);
	protected abstract void processAccelerationData(AccelerationData data);
	
	public String getId() {
		return id;
	}

}

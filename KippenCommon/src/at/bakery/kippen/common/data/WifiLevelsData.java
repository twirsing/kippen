package at.bakery.kippen.common.data;

import java.util.Collection;
import java.util.Map.Entry;

import at.bakery.kippen.common.AbstractData;

public class WifiLevelsData extends AbstractData {

	public WifiLevelsData() {
		this(System.nanoTime());
	}
	
	public WifiLevelsData(long ts) {
		super(ts);
	}
	
	public void setNetwork(String networkId, double signalLevel) {
		setDouble(networkId, signalLevel);
	}
	
	public double getNetwork(String networkId) {
		return (Double)getValue("networkId");
	}
	
	public Collection<Entry<String, Object>> getNetworks() {
		return eventData.entrySet();
	}
	
	public boolean hasNetworks() {
		return !eventData.isEmpty();
	}
	
	@Override
	public String toString() {
		return "SENSOR wifi = " + eventData.toString();
	}
}

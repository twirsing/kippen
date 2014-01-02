package at.bakery.kippen.common.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import at.bakery.kippen.common.IData;

public class WifiLevelsData implements IData {

	private static final long serialVersionUID = -6473541860056950929L;

	private String networkId;
	private Integer signalLevel;
	
	private Map<String, Integer> networks = new HashMap<String, Integer>();
	
	public WifiLevelsData() {}
	
	public WifiLevelsData(String networkId, Integer signalLevel) {
		this.networkId = networkId;
		this.signalLevel = signalLevel;
	}
	
	public Map<String, Integer> getNetworks() {
		return networks;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(Entry<String, Integer> entry : networks.entrySet()) {
			sb.append(entry.getValue() + " -- ");
		}
		
		return sb.toString();
	}
}

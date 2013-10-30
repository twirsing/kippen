package at.bakery.kippen.common;

import java.util.HashMap;
import java.util.Map;

public class SensorConfig {
	
	public enum SensorConfigType {
		CENTER_AP_ESSID,
		TRI_AP_ESSID,
		OID
	}
	
	private Map<SensorConfigType, Object> config = new HashMap<SensorConfig.SensorConfigType, Object>();
	
	public void setConfig(SensorConfigType type, Object value) {
		config.put(type, value);
	}
	
	public Object getConfig(SensorConfigType type) {
		return config.get(type);
	}
}

package at.bakery.kippen.common.data;

import at.bakery.kippen.common.AbstractData;

public class ClientConfigData extends AbstractData {
	
	public enum ConfigType {
		MEASURE_AP_ESSID,
		OID
	}
	
	public void setConfig(ConfigType type, Object value) {
		setObject(type.name(), value);
	}
	
	public Object getConfig(ConfigType type) {
		return getValue(type.name());
	}
}

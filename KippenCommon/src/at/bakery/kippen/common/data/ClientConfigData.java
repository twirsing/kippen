package at.bakery.kippen.common.data;

import java.util.HashMap;
import java.util.Map;

import at.bakery.kippen.common.IData;


public class ClientConfigData implements IData {
	
	private static final long serialVersionUID = 2310192954941958562L;

	public enum ConfigType {
		MEASURE_AP_ESSID,
		OID
	}
	
	private Map<ConfigType, Object> config = new HashMap<ClientConfigData.ConfigType, Object>();
	
	public void setConfig(ConfigType type, Object value) {
		config.put(type, value);
	}
	
	public Object getConfig(ConfigType type) {
		return config.get(type);
	}
}

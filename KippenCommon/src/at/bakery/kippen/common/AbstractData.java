package at.bakery.kippen.common;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import at.bakery.kippen.common.json.JSONDataSerializer;

public abstract class AbstractData {
	
	// a timestamp to be used for computations, ordering of events, etc.
	private long timestamp;
	
	// the client id to associate devices
	private String clientId;
	
	// the payload of an event, filled by each event definition differently
	protected Map<String, Object> eventData = new HashMap<String, Object>();

	public AbstractData(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public AbstractData() {
		this(System.nanoTime());
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	protected void setTimestamp(long ts) {
		this.timestamp = ts;
	}
	
	public String getClientId() {
		return clientId;
	}
	
	public void setClientId(String cid) {
		this.clientId = cid;
	}

	protected void setDouble(String name, double ... value) {
		if(value.length <= 0) return;
		eventData.put(name, value.length > 1 ? value : value[0]);
	}
	
	protected void setString(String name, String ... value) {
		if(value.length <= 0) return;
		eventData.put(name, value.length > 1 ? value : value[0]);
	}
	
	protected void setBoolean(String name, boolean ... value) {
		if(value.length <= 0) return;
		eventData.put(name, value.length > 1 ? value : value[0]);
	}
	
	protected void setObject(String name, Object ... value) {
		if(value.length <= 0) return;
		eventData.put(name, value.length > 1 ? value : value[0]);
	}
	
	protected Object getValue(String name) {
		return eventData.get(name);
	}
	
	public Collection<Entry<String, Object>> getEntries() {
		return eventData.entrySet();
	}
}
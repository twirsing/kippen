package at.bakery.kippen.common;

import java.io.Serializable;


public class DataWithTimestamp implements Serializable {
	
	private IData data;
	private long timestamp;
	
	public DataWithTimestamp(IData data, long timestamp) {
		this.data = data;
		this.timestamp = timestamp;
	}
	
	public DataWithTimestamp(IData data) {
		this(data, System.nanoTime());
	}
	
	public IData getData() {
		return data;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
}
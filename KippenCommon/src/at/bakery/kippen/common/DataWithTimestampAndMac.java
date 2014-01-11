package at.bakery.kippen.common;

import java.io.Serializable;


public class DataWithTimestampAndMac implements Serializable {
	
	private IData data;
	private long timestamp;
	private String macAddress;


	public DataWithTimestampAndMac(IData data, long timestamp, String macAddress) {
		this.data = data;
		this.timestamp = timestamp;
		this.macAddress = macAddress;
	}
	
	public DataWithTimestampAndMac(IData data, String macAddress) {
		this(data, System.nanoTime(), macAddress);
	}
	
	public IData getData() {
		return data;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getMacAddress() {
		return macAddress;
	}

}
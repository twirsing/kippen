package nerdproject;

public class Device {
	private int trackNumber;
	private int deviceNumber;
	private String name;
	
	public Device(int trackNumber, int deviceNumber, String name ){
		this.name = name;
		this.deviceNumber = deviceNumber;
		this.trackNumber = trackNumber;
	}
	
	public int getTrackNumber() {
		return trackNumber;
	}
	public void setTrackNumber(int trackNumber) {
		this.trackNumber = trackNumber;
	}
	public int getDeviceNumber() {
		return deviceNumber;
	}
	public void setDeviceNumber(int deviceNumber) {
		this.deviceNumber = deviceNumber;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}

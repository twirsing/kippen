package nerdproject;

public class DeviceParameter {
	private int trackNumber;
	private int deviceNumner;
	private int parameterNumner;
	private float value;
	private String name;

	public DeviceParameter(int trackNumber, int deviceNumner,
			int parameterNumner, float value, String name) {
		this.trackNumber = trackNumber;
		this.deviceNumner = deviceNumner;
		this.parameterNumner = parameterNumner;
		this.value = value;
		this.name = name;
	}

	public int getTrackNumber() {
		return trackNumber;
	}

	@Override
	public String toString() {
		return "DeviceParameter [trackNumber=" + trackNumber
				+ ", deviceNumner=" + deviceNumner + ", parameterNumner="
				+ parameterNumner + ", value=" + value + ", name=" + name + "]";
	}

	public void setTrackNumber(int trackNumber) {
		this.trackNumber = trackNumber;
	}

	public int getDeviceNumner() {
		return deviceNumner;
	}

	public void setDeviceNumner(int deviceNumner) {
		this.deviceNumner = deviceNumner;
	}

	public int getParameterNumner() {
		return parameterNumner;
	}

	public void setParameterNumner(int parameterNumner) {
		this.parameterNumner = parameterNumner;
	}

	public float getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

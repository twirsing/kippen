package at.bakery.kippen.common.data;

public class BatteryData {

	private boolean charging;
	private float level;
	
	public BatteryData(boolean charging, float level) {
		this.charging = charging;
		this.level = level;
	}

	public boolean isCharging() {
		return charging;
	}

	public float getLevel() {
		return level;
	}
}

package at.bakery.kippen.common.data;

public class BatteryData implements IData {

	private static final long serialVersionUID = -7605416261325422492L;

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

	@Override
	public String toString() {
		return "BATTERY " + (!charging ? "dis" : "") + "charging @ " + (level * 100.0) + "%";
	}
}

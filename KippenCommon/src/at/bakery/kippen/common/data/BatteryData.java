package at.bakery.kippen.common.data;

import at.bakery.kippen.common.AbstractData;

public class BatteryData extends AbstractData {

	public BatteryData(boolean charging, double level) {
		this(System.nanoTime(), charging, level);
	}
	
	public BatteryData(long ts, boolean charging, double level) {
		super(ts);
		
		setBoolean("charging", charging);
		setDouble("level", level);
	}

	public boolean isCharging() {
		return (Boolean)getValue("charging");
	}

	public double getLevel() {
		return (Double)getValue("level");
	}

	@Override
	public String toString() {
		return "SENSOR battery " + (!isCharging() ? "dis" : "") + "charging @ " + (getLevel() * 100.0) + "%";
	}
}

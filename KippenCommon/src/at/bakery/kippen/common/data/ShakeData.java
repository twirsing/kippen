package at.bakery.kippen.common.data;

import at.bakery.kippen.common.AbstractData;

public class ShakeData extends AbstractData {

	public ShakeData() {
		this(System.nanoTime());
	}
	
	public ShakeData(long timestamp) {
		super(timestamp);
		
		setBoolean("shaking", false);
	}
	
	public boolean isShaking() {
		return (Boolean)getValue("shaking");
	}
	
	public void setShaking(boolean shaking) {
		setBoolean("shaking", shaking);
	}

	@Override
	public String toString() {
		return "SENSOR shaking? " + (isShaking() ? "YEEEEES!!!!" : "no");
	}
}

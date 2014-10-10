package at.bakery.kippen.common.data;

import at.bakery.kippen.common.AbstractData;

public class ShakeData extends AbstractData {

	public ShakeData() {
		this(System.nanoTime());
	}
	
	public ShakeData(long timestamp) {
		super(timestamp);
		
		setShaking(false);
	}
	
	public boolean isShaking() {
		return (Boolean)getValue("shaking");
	}
	
	public void setShaking(boolean shaking) {
		setBoolean("shaking", shaking);
	}

	@Override
	public String toString() {
		return "SHAKE ? " + (isShaking() ? "YEEEEES!!!!" : "no");
	}
}

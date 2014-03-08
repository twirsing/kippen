package at.bakery.kippen.common.data;

import at.bakery.kippen.common.AbstractData;

public class ShakeData extends AbstractData {

	public ShakeData() {
		this(System.nanoTime());
	}
	
	public ShakeData(long timestamp) {
		super(timestamp);
	}

	@Override
	public String toString() {
		return "SENSOR shaking";
	}
}

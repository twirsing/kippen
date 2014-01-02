package at.bakery.kippen.common.data;

import java.text.DecimalFormat;

import at.bakery.kippen.common.IData;

public class SensorTripleData implements IData {
	
	private static final long serialVersionUID = -2311126585385429938L;

	public float x;
	public float y;
	public float z;
	
	public SensorTripleData(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public String toString() {
		DecimalFormat f = new DecimalFormat("###.##");
		return "SENSOR x = " + f.format(x) + ", y = " + f.format(y) + ", z = " + f.format(z);
	}
}
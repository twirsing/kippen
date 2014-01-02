package at.bakery.kippen.common.data;

import java.text.DecimalFormat;

import at.bakery.kippen.common.IData;

public class SensorSingleData implements IData {

	private static final long serialVersionUID = -1420878341062188979L;

	public float value;
	
	public SensorSingleData(float value) {
		this.value = value;
	}

	@Override
	public String toString() {
		DecimalFormat f = new DecimalFormat("###.##");
		return "SENSOR val = " + f.format(value);
	}
}

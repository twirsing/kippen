package at.bakery.kippen.common.data;

import java.text.DecimalFormat;

import at.bakery.kippen.common.AbstractData;

public class SensorSingleData extends AbstractData {

	public SensorSingleData(long ts, double value) {
		super(ts);
		
		setDouble("value", value);
	}
	
	public double getValue() {
		return (Double)getValue("value");
	}
	
	public void setValue(double value) {
		setTimestamp(System.nanoTime());
		setDouble("value", value);
	}

	@Override
	public String toString() {
		DecimalFormat f = new DecimalFormat("###.##");
		return "SENSOR val = " + f.format(getValue());
	}
}

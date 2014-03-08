package at.bakery.kippen.common.data;

public class AnalogOrientationData extends SensorTripleData {

	public AnalogOrientationData(double x, double y, double z) {
		this(System.nanoTime(), x, y, z);
	}
	
	public AnalogOrientationData(long ts, double x, double y, double z) {
		super(ts, x, y, z);
	}

}

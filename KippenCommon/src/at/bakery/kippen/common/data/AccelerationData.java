package at.bakery.kippen.common.data;

public class AccelerationData extends SensorTripleData {

	public AccelerationData(double x, double y, double z) {
		this(System.nanoTime(), x, y, z);
	}
	
	public AccelerationData(long ts, double x, double y, double z) {
		super(ts, x, y, z);
	}
}

package at.bakery.kippen.common.data;

public class AverageAccelerationData extends SensorTripleData {

	public AverageAccelerationData(double x, double y, double z) {
		this(System.nanoTime(), x, y, z);
	}
	
	public AverageAccelerationData(long ts, double x, double y, double z) {
		super(ts, x, y, z);
	}
}

package at.bakery.kippen.common.data;

public class BarrelOrientationData extends SensorSingleData {

	public BarrelOrientationData(double value) {
		this(System.nanoTime(), value);
	}
	
	public BarrelOrientationData(long ts, double value) {
		super(ts, value);
	}

	public double getOrientation() {
		return (double)getValue();
	}
	
	public void setOrientation(double orientation) {
		setValue(orientation);
	}
	
	@Override
	public String toString() {
		return "SENSOR orientation = " + getOrientation();
	}
}

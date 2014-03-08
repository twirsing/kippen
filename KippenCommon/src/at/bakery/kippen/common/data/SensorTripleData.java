package at.bakery.kippen.common.data;

import java.text.DecimalFormat;

import at.bakery.kippen.common.AbstractData;

public class SensorTripleData extends AbstractData {
	
	public SensorTripleData(double x, double y, double z) {
		this(System.nanoTime(), x, y, z);
	}
	
	public SensorTripleData(long ts, double x, double y, double z) {
		super(ts);
		
		setDouble("x", x);
		setDouble("y", y);
		setDouble("z", z);
	}
	
	public double getX() {
		return (Double)getValue("x");
	}
	
	public void setX(double x) {
		setDouble("x", x);
	}
	
	public double getY() {
		return (Double)getValue("y");
	}
	
	public void setY(double y) {
		setDouble("y", y);
	}
	
	public double getZ() {
		return (Double)getValue("z");
	}
	
	public void setZ(double z) {
		setDouble("z", z);
	}
	
	public void setXYZ(double x, double y, double z) {
		setDouble("x", x);
		setDouble("y", y);
		setDouble("z", z);
	}
	
	public void incrementXYZ(double dx, double dy, double dz) {
		setDouble("x", getX() + dx);
		setDouble("y", getY() + dy);
		setDouble("z", getZ() + dz);
	}

	@Override
	public String toString() {
		DecimalFormat f = new DecimalFormat("###.##");
		return "SENSOR x = " + f.format(getX()) + ", y = " + f.format(getY()) + ", z = " + f.format(getZ());
	}
}
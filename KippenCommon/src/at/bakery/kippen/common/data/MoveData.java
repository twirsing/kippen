package at.bakery.kippen.common.data;


public class MoveData extends SensorTripleData {

	public MoveData(double x, double y, double z) {
		this(System.nanoTime(), x, y, z);
	}
	
	public MoveData(long ts, double x, double y, double z) {
		super(ts, x, y, z);
	}
	
	@Override
	public String toString() {
		return "MOVE " + super.toString();
	}
}

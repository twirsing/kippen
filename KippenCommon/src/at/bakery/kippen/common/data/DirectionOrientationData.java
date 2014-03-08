package at.bakery.kippen.common.data;

public class DirectionOrientationData extends SensorSingleData {

//	public enum Orientation {
//		TOP(0),
//		BOTTOM(1),
//		LEFT(2),
//		RIGHT(3),
//		FRONT(4),
//		BACK(5);
//		
//		protected int value;
//		private static SparseArray<Orientation> lookup = new SparseArray<Orientation>();
//		
//		static {
//			for(Orientation o : Orientation.values()) {
//				lookup.put(o.value, o);
//			}
//		}
//		
//		public static Orientation getOrientationByValue(int value) {
//			return lookup.get(value);
//		}
//		
//		private Orientation(int value) {
//			this.value = value;
//		}
//	}
	
	public DirectionOrientationData(double value) {
		this(System.nanoTime(), value);
	}
	
	public DirectionOrientationData(long ts, double value) {
		super(ts, value);
	}

	public double getOrientation() {
		return (double)getValue();
	}
	
	@Override
	public String toString() {
		return "SENSOR orientation = " + getOrientation();
	}
}

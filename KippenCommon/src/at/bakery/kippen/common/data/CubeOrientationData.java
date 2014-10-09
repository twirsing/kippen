package at.bakery.kippen.common.data;

import at.bakery.kippen.common.AbstractData;

public class CubeOrientationData extends AbstractData {

	public enum Orientation {
		BOTTOM(0), TOP(1), LEFT(2), RIGHT(3), FRONT(4), BACK(5),
		UNKNOWN(-1);
		
		private int orientationValue;
		
		private Orientation(int orientationValue) {
			this.orientationValue = orientationValue;
		}
		
		public int getOrientationValue() {
			return orientationValue;
		}
	}
	
	private Orientation orientation;
	
	public CubeOrientationData(Orientation orientation) {
		this.orientation = orientation;
	}
	
	public Orientation getOrientation() {
		return orientation;
	}
	
	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}
	
	public String toString() {
		return "SENSOR cube = " + getOrientation();
	}
}

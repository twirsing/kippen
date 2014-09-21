package nerdproject;

public class ParameterRange {
	private float floatlow;
	private float floatHigh;

	public ParameterRange(float low, float high) {
		this.floatlow = low;
		this.floatHigh = high;
	}

	public float getLow() {
		return floatlow;
	}

	public void setLow(float low) {
		this.floatlow = low;
	}

	public float getHigh() {
		return floatHigh;
	}

	public void setHigh(float high) {
		this.floatHigh = high;
	}
}

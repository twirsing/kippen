package nerdproject;

public enum ClipState {
	NO_CLIP(0), HAS_CLIP(1), PLAYING(2), TRIGGERED(3);
	private final int value;

	private ClipState(int state) {
		this.value = state;
	}

	public int getValue() {
		return value;
	}

	static ClipState fromInt(int value) {
		switch (value) {
		case 0:
			return ClipState.NO_CLIP;
		case 1:
			return ClipState.HAS_CLIP;
		case 2:
			return ClipState.PLAYING;
		case 3:
			return ClipState.TRIGGERED;
		default:
			return null;
		}
		
	}
}
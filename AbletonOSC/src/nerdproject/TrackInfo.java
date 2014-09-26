package nerdproject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrackInfo {
	// /live/track/info (tracknumber, armed (clipnumber, state, length))
	// [state: 0 = no clip, 1 = has clip, 2 = playing, 3 = triggered]
	private int trackNumber;
	private boolean isArmed;

	private List<Clip> clips = new ArrayList<Clip>();

	public TrackInfo(Object[] message) {
		this.trackNumber = (Integer) message[0];
		this.isArmed = ((Integer) message[1]) == 0 ? false : true;

		Object[] clipArray = Arrays.copyOfRange(message, 2, message.length);

		int numClips = clipArray.length / 3;
		for (int i = 1; i <= numClips; i++) {
			Object[] clip = Arrays.copyOfRange(clipArray, 0, 3);
			clips.add(new Clip((Integer) clip[0], ClipState
					.fromInt((Integer) clip[1]), (float) clip[2]));
			clipArray = Arrays.copyOfRange(clipArray, 3, message.length);
		}
	}

	public int getTrackNumber() {
		return trackNumber;
	}

	public boolean isArmed() {
		return isArmed;
	}

	public List<Clip> getClips() {
		return clips;
	}

	public int getClipCount() {
		return clips.size();
	}

	class Clip {

		private int clipNumber;
		private ClipState clipState;
		private float length;

		public Clip(int clipNumber, ClipState clipState, float length) {
			super();
			this.clipNumber = clipNumber;
			this.clipState = clipState;
			this.length = length;
		}

		public int getClipNumber() {
			return clipNumber;
		}

		public void setClipNumber(int clipNumber) {
			this.clipNumber = clipNumber;
		}

		public ClipState getClipState() {
			return clipState;
		}

		public void setClipState(ClipState clipState) {
			this.clipState = clipState;
		}

		public float getLength() {
			return length;
		}

		public boolean hasClip() {
			return (this.clipState == ClipState.HAS_CLIP
					|| this.clipState == ClipState.PLAYING 
					|| this.clipState == ClipState.TRIGGERED);
		}

		public boolean isPlaying() {
			return this.clipState == ClipState.PLAYING;
		}

		public void setLength(float length) {
			this.length = length;
		}
	}
}
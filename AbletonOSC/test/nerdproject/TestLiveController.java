package nerdproject;

import junit.framework.TestCase;

import org.junit.Test;

public class TestLiveController extends TestCase {

	@Test
	public void testLiveController() {
		LiveController.getInstance().playClip(0,4 );
//	 	LiveController.getInstance().setTrackVolume(0, 0.1f);
	 	LiveController.getInstance().isClipPlaying(0,1);
	}
}

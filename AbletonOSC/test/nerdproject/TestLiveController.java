package nerdproject;

import junit.framework.TestCase;

import org.junit.Test;

public class TestLiveController extends TestCase {

	@Test
	public void testLiveController() throws Exception {
		LiveController.getInstance().playClip(0, 2);
		LiveController.getInstance().setMasterVolume(0.1f);
//		LiveController.getInstance().setTrackVolume(0, 1);
	}
}
	
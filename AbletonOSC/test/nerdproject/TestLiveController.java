package nerdproject;

import junit.framework.TestCase;

import org.junit.Test;

public class TestLiveController extends TestCase {

	@Test
	public void testLiveController() throws Exception {
//		LiveController.getInstance().playClip(1, 0);
//		LiveController.getInstance().toggleMute(0);
//		LiveController.getInstance().setMasterVolume(0.1f);
//		LiveController.getInstance().setTrackVolume(0, 1);
		
		LiveController.getInstance().setDeviceParameter(0, 0,  0, 10);
	}
}
	
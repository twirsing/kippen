package nerdproject;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class TestApi {
	LiveController c = LiveController.getInstance();

	@Test
	public void testPlayClip() throws InterruptedException {
		c.stopAll();
		c.stopTrack(0);
		Thread.sleep(500);
		Assert.assertFalse(c.isClipPlaying(0, 0));
		c.playAll();
		c.playClip(0, 0);
		Thread.sleep(500);
		Assert.assertTrue(c.isClipPlaying(0, 0));
	}

	@Test
	public void testMuteTrack() {
		c.muteTrack(0);
		Assert.assertTrue(c.isMuted(0));
		c.unMuteTrack(0);
		Assert.assertFalse(c.isMuted(0));

	}

	@Test
	public void testSetSend() {
		c.setSend(0, 0, 0.5f);
		Assert.assertTrue(c.getSend(0, 0) == 0.5f);
		c.setSend(0, 0, 0.0f);
	}

	@Test
	public void testDeviceList() throws AbletonCommunicationException {

	}

	@After
	public void after() throws AbletonCommunicationException {
		LiveController.getInstance().stopAll();
	}

}

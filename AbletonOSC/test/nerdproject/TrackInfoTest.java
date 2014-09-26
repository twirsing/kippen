package nerdproject;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class TrackInfoTest {

	@Test
	public void testTrackInfo() {
		Object[] message = new Object[] { 0, 0, 0, 2, 48.0f, 1, 1, 0.0f, 2, 0,
				0.0f, 3, 0, 0.0f, 4, 0, 0.0f, 5, 0, 0.0f, 6, 0, 0.0f, 7, 0, 0.0f };
		
		TrackInfo ti = new TrackInfo(message);
		
		Assert.assertTrue(ti.getTrackNumber() == 0);
		Assert.assertFalse(ti.isArmed());
		Assert.assertTrue(ti.getClipCount() == 8);
		
		Assert.assertTrue(ti.getClips().get(0).isPlaying());
		Assert.assertTrue(ti.getClips().get(1).hasClip());
		Assert.assertFalse(ti.getClips().get(3).hasClip());
	}
}
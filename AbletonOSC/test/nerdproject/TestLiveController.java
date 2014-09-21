package nerdproject;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

public class TestLiveController extends TestCase {

	@Test
	public void testLiveController() throws Exception {
		// LiveController.getInstance().playClip(1, 0);
		// LiveController.getInstance().toggleMute(0);
		// LiveController.getInstance().setMasterVolume(0.1f);
		// LiveController.getInstance().setTrackVolume(0, 1);

		// LiveController.getInstance().setDeviceParameter(0, 0, 0, 10);
	}

	@Test
	public void testGetDevices() {
		List<Device> devices = LiveController.getInstance().getDevices(0);
		Assert.assertTrue(devices.size() == 2);
		Assert.assertTrue(devices.get(0).getName().equals("Compressor"));
		Assert.assertTrue(devices.get(0).getTrackNumber() == 0);
		Assert.assertTrue(devices.get(0).getDeviceNumber() == 0);
	}

	@Test
	public void testGetDeviceParameters() {
		List<DeviceParameter> deviceParams = LiveController.getInstance().getDeviceParameters(0, 0);

		for (DeviceParameter p : deviceParams) {
			System.out.println(p);
		}

	}

	@Test
	public void testNormalizeParameterValue() {
		double value = LiveController.getInstance().normalizeParameterInputValue(0, new ParameterRange(0.0f, 1.0f));
		Assert.assertTrue(value == 0.0f);

		value = LiveController.getInstance().normalizeParameterInputValue(1, new ParameterRange(0.0f, 1.0f));
		Assert.assertTrue(value == 1.0f);

		value = LiveController.getInstance().normalizeParameterInputValue(0.5f, new ParameterRange(-0.5f, 0.5f));
		Assert.assertTrue(value == 0.0f);
	}

	@Test
	public void testSetNormalizedParameter() {
		LiveController.getInstance().setDeviceParameterNormalized(0, 0, 12, 0.5f);

		float deviceParameterValue = LiveController.getInstance().getDeviceParameterValue(0, 0, 12);
		System.out.println("val " +  deviceParameterValue);
		Assert.assertTrue(deviceParameterValue == 9f);

	}

	@Test
	public void testSetGetParameter() {
		LiveController.getInstance().setDeviceParameter(0, 0, 0, 1);

		float deviceParameterValue = LiveController.getInstance().getDeviceParameterValue(0, 0, 0);

		Assert.assertTrue(deviceParameterValue == 1f);
	}

	@Test
	public void testParameterRange() {
		ParameterRange range = LiveController.getInstance().getDeviceParameterRange(0, 0, 12);
		System.out.println("range  " + range.getLow() + " " + range.getHigh());
		Assert.assertTrue(0.0 == range.getLow());
		Assert.assertTrue(18.0 == range.getHigh());
	}

}

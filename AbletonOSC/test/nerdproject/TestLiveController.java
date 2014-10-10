package nerdproject;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestLiveController extends TestCase {
	LiveController lc = LiveController.getInstance();
	
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
		assertTrue(devices.size() == 3);
		assertTrue(devices.get(0).getName().equals("Flanger"));
		assertTrue(devices.get(0).getTrackNumber() == 0);
		assertTrue(devices.get(0).getDeviceNumber() == 0);
	}
	
	
	@Test
	public void testGetMasterDevices(){
		List<Device> masterDevices = lc.getMasterDevices();
		assertTrue(masterDevices.size() == 1);
		assertTrue(masterDevices.get(0).getName().equals("Klirr"));
	}
	
	@Test 
	public void testGetMasterDeviceParameters(){
		List<DeviceParameter> masterDeviceParameters = lc.getMasterDeviceParameters(0);
		for (DeviceParameter p : masterDeviceParameters) {
			System.out.println("master device param: " + p.getParameterNumner() + ": "  + p.getName());
		}
//		assertTrue(masterDeviceParameters.get(9).getName().equals("Global Drive"));
		
	}
	
	@Test
	public void testSetMasterDeviceParameter(){
		lc.setMasterDeviceParameter(0, 9, 1.0f);
	}
	
	
	@Test
	public void testGetMasterDeviceParameterRange(){
		ParameterRange range = lc.getMasterDeviceParameterRange(0, 9);
		assertTrue(range.getLow() == 0.0f);
		assertTrue(range.getHigh() == 1.0f);
	}

	@Test
	public void testGetDeviceParameters() {
		List<DeviceParameter> deviceParams = lc.getDeviceParameters(0, 0);

		for (DeviceParameter p : deviceParams) {
			System.out.println(p.getParameterNumner() + ": " + p.getName());
		}
//		assertTrue(deviceParams.get(1).getName().equals("Dry/Wet"));
	}

	@Test
	public void testNormalizeParameterValue() {
		double value = LiveController.getInstance().normalizeParameterInputValue(0, new ParameterRange(0.0f, 1.0f));
		assertTrue(value == 0.0f);

		value = LiveController.getInstance().normalizeParameterInputValue(1, new ParameterRange(0.0f, 1.0f));
		assertTrue(value == 1.0f);

		value = LiveController.getInstance().normalizeParameterInputValue(0.5f, new ParameterRange(-0.5f, 0.5f));
		assertTrue(value == 0.0f);
	}

	@Test
	public void testSetNormalizedParameter() {
		LiveController.getInstance().setDeviceParameterNormalized(0, 0, 1, 0.5f);

		float deviceParameterValue = LiveController.getInstance().getDeviceParameterValue(0, 0, 1);
		System.out.println("val " +  deviceParameterValue);
		assertTrue(deviceParameterValue == 0.5f);

	}

	@Test
	public void testSetGetParameter() {
		LiveController.getInstance().setDeviceParameter(0, 0, 0, 1);

		float deviceParameterValue = LiveController.getInstance().getDeviceParameterValue(0, 0, 0);

		assertTrue(deviceParameterValue == 1f);
	}

	@Test
	public void testParameterRange() {
		ParameterRange range = LiveController.getInstance().getDeviceParameterRange(0, 0, 12);
		System.out.println("range  " + range.getLow() + " " + range.getHigh());
		assertTrue(0.0 == range.getLow());
		assertTrue(1.0 == range.getHigh());
	}

}

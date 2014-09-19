package nerdproject;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

public class TestLiveController extends TestCase {

	@Test
	public void testLiveController() throws Exception {
//		LiveController.getInstance().playClip(1, 0);
//		LiveController.getInstance().toggleMute(0);
//		LiveController.getInstance().setMasterVolume(0.1f);
//		LiveController.getInstance().setTrackVolume(0, 1);
		
//		LiveController.getInstance().setDeviceParameter(0, 0,  0, 10);
	}
	
	
	@Test
	public void testGetDevices(){
		List<Device> devices = LiveController.getInstance().getDevices(0);
		Assert.assertTrue(devices.size() == 2);
		Assert.assertTrue(devices.get(0).getName().equals("Compressor"));
		Assert.assertTrue(devices.get(0).getTrackNumber() == 0);
		Assert.assertTrue(devices.get(0).getDeviceNumber() == 0);
	}
	
	
	@Test
	public void testGetDeviceParameters(){
		List<DeviceParameter> deviceParams = LiveController.getInstance().getDeviceParameters(0, 0);
		
		
		for(DeviceParameter p : deviceParams){
			System.out.println(p);
		}
		
	}
	
	@Test
	public void testSetParameter(){
		LiveController.getInstance().setDeviceParameter(0, 0,  0, 1);
	}
}
	
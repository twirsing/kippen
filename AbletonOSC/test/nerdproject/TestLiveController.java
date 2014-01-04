package nerdproject;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.junit.BeforeClass;
import org.junit.Test;

import junit.framework.TestCase;

public class TestLiveController extends TestCase {
	LiveController controller = null;
	@BeforeClass
	public void setUp(){
		try {
			controller = new LiveController();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	@Test
	public void testPlay(){
		try {
			controller.play();
		} catch (AbletonCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/

	/*
	
	public void testStop(){
		try {
			controller.stop();
		} catch (AbletonCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	@Test
	public void testLiveController(){
		try {
			controller.playClip(0, 0);
		} catch (AbletonCommunicationException e) {
			e.printStackTrace();
		}
	}
}

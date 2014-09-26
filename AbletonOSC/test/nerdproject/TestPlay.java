package nerdproject;

import junit.framework.TestCase;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;
import com.illposed.osc.TestOSCListener;

public class TestPlay extends TestCase {
	private OSCPortOut sender;
	private OSCPortIn receiver;

	@Override
	protected void setUp() throws Exception {
		sender = new OSCPortOut(9000);
		
	}
	public void testPlayTrack() throws Exception {
		OSCMessage mesg = new OSCMessage("/live/play/clip", new Object[]{0,0});
		OSCMessage stop = new OSCMessage("/live/stop");
		try {
			sender.send(mesg);
			Thread.sleep(2000);
			sender.send(stop);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			sender.close();
		}
	}
	public void testReceiving() throws Exception {
		OSCMessage mesg = new OSCMessage("/live/master/volume");
		 receiver = new OSCPortIn(9001);
		try {
			sender.send(mesg);
			
			TestOSCListener listener = new TestOSCListener();
			receiver.addListener("/live/master/volume", listener);
			receiver.startListening();
		//	sender.send(mesg);
			Thread.sleep(2000);
			receiver.stopListening();
			if (!listener.isMessageReceived()) {
				fail("Message was not received");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
//		OSCMessage mesg = new OSCMessage("/message/receiving");
//		TestOSCListener listener = new TestOSCListener();
//		receiver.addListener("/message/receiving", listener);
//		receiver.startListening();
//		sender.send(mesg);
//		Thread.sleep(100); // wait a bit
//		receiver.stopListening();
//		if (!listener.isMessageReceived()) {
//			fail("Message was not received");
//		}
	}

}
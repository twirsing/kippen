package nerdproject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

public class AbstractLiveController {
	private OSCPortIn receiver;
	private OSCPortOut sender;
	public AbstractLiveController(int liveOSCPort, int listeningPort) throws SocketException, UnknownHostException{
		receiver = new OSCPortIn(listeningPort);
		sender = new OSCPortOut(liveOSCPort);
	}
	public AbstractLiveController(InetAddress liveOSCAddress, int liveOSCPort, int listeningPort) throws SocketException, UnknownHostException{
		receiver = new OSCPortIn(listeningPort);
		sender = new OSCPortOut(liveOSCAddress, liveOSCPort);
	}
	
	
	public AbstractLiveController() throws SocketException, UnknownHostException{
		receiver = new OSCPortIn(9001);
		sender = new OSCPortOut(9999);
	}
	
	protected void sendMessage(String message, Object ... params) throws IOException{
		Collection<Object> arrayList = new ArrayList<>();
		arrayList.addAll(Arrays.asList(params));
		System.out.println(message);
		System.out.println(arrayList);
		OSCMessage oscMessage = new OSCMessage(message, arrayList);
		sender.send(oscMessage);
	}
	
	protected Object sendReceive(String message, Object ... params){
		OSCMessage oscMessage = new OSCMessage(message, params);
		return null;
	}
}

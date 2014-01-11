package nerdproject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

public class AbstractLiveController {
	private AbstractLiveController instance = null;
	private OSCPortIn receiver;
	private OSCPortOut sender;

	
	protected AbstractLiveController(){
		
	}
	private AbstractLiveController(InetAddress liveOSCAddress, int liveOSCPort,
			int listeningPort) {
		try {
			receiver = new OSCPortIn(listeningPort);
			sender = new OSCPortOut(liveOSCAddress, liveOSCPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	protected void sendMessage(String message, Object... params) {
		Collection<Object> arrayList = new ArrayList<>();
		arrayList.addAll(Arrays.asList(params));
		OSCMessage oscMessage = new OSCMessage(message, arrayList);
		try {
			sender.send(oscMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected Object sendReceive(String message, Object... params) {
		OSCMessage oscMessage = new OSCMessage(message, params);
		return null;
	}

	protected AbstractLiveController getInstance() {
		if (instance == null) {
			Properties properties = new Properties();
			BufferedInputStream stream;
			int incommingPort;
			int liveOSCPort;
			String hostIP;
			InetAddress hostAddress;
			try {
				 System.out.println("Working Directory = " +
			              System.getProperty("user.dir"));
				stream = new BufferedInputStream(new FileInputStream(
						"configuration.properties"));

				properties.load(stream);
				stream.close();
				incommingPort = Integer.valueOf(properties
						.getProperty("incommingPort"));
				liveOSCPort = Integer.valueOf(properties
						.getProperty("liveOSCPort"));
				hostIP = properties.getProperty("hostIP");

				if (hostIP == "localhost") {
					hostAddress = InetAddress.getLocalHost();
				} else {
					hostAddress = InetAddress.getByName(hostIP);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException("properties file cannot be read");
			}
			return new AbstractLiveController(hostAddress, liveOSCPort,
					incommingPort);
		}
		return instance;
	}
}

package nerdproject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
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

public class LiveController {
	private static LiveController instance = null;
	private OSCPortIn receiver;
	private OSCPortOut sender;

	private LiveController(InetAddress liveOSCAddress, int liveOSCPort,
			int listeningPort) {
		try {
			receiver = new OSCPortIn(listeningPort);
			sender = new OSCPortOut(liveOSCAddress, liveOSCPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void playAll() throws AbletonCommunicationException {
		this.sendMessage("/live/play");
	}

	public void stopAll() throws AbletonCommunicationException {
		this.sendMessage("/live/stop");
	}

	public void playClip(int trackNumber, int clipNumber) {
		this.sendMessage(
				"/live/play/clip",
				new Object[] { String.valueOf(trackNumber),
						String.valueOf(clipNumber) });
	}

	public void stopTrack(int trackNumber) {
		this.sendMessage("/live/stop/track",
				new Object[] { String.valueOf(trackNumber) });
	}

	public void setTrackVolume(int trackNum, float volume) {
		this.sendMessage(
				"/live/volume",
				new Object[] { String.valueOf(trackNum), String.valueOf(volume) });
	}

	// ##############################################################

	private void sendMessage(String message, Object... params) {
		Collection<Object> arrayList = new ArrayList<>();
		if (params == null) {
			params = new Object[] {};
		}
		arrayList.addAll(Arrays.asList(params));
		OSCMessage oscMessage = new OSCMessage(message, arrayList);

		System.out.println("Sending params: " + arrayList);
		try {
			sender.send(oscMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Object sendReceive(String message, Object... params) {
		OSCMessage oscMessage = new OSCMessage(message, params);
		return null;
	}

	public static LiveController getInstance() {
		if (instance == null) {
			System.out.println("Working Directory = "
					+ System.getProperty("user.dir"));
			Properties properties = new Properties();
			BufferedInputStream stream;
			int incommingPort;
			int liveOSCPort;
			String hostIP;
			InetAddress hostAddress;
			try {
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
			LiveController.instance = new LiveController(hostAddress,
					liveOSCPort, incommingPort);
			return LiveController.instance;
		}
		return instance;
	}
}

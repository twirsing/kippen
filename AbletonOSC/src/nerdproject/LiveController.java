package nerdproject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.ListSelectionEvent;

import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

import com.google.common.collect.Lists;
import com.illposed.osc.AbletonOSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

public class LiveController {
	private static LiveController instance = null;
	private OSCPortIn receiver;
	private OSCPortOut sender;

	static Logger log = Logger.getLogger(LiveController.class.getName());

	private LiveController(InetAddress liveOSCAddress, int liveOSCPort, int listeningPort) {
		try {
			log.log(Level.INFO, "Listening on port " + listeningPort);
			receiver = new OSCPortIn(listeningPort);
			sender = new OSCPortOut(liveOSCAddress, liveOSCPort);
			log.log(Level.INFO, "Sending to port " + liveOSCPort);
			log.log(Level.INFO, "Starting to listen...");
			receiver.startListening();

		} catch (Exception e) {
			log.log(Level.SEVERE, "Error starting Live API");
			e.printStackTrace();
		}
	}

	public void playAll() {
		this.sendMessage("/live/play");
	}

	public void stopAll() {
		this.sendMessage("/live/stop");
	}

	/**
	 * 
	 * @param trackNumber
	 * @param clipNumber
	 * @return
	 */
	public boolean isClipPlaying(int trackNumber, int clipNumber) {
		OSCMessage message = this.sendReceive("/live/track/info", null, trackNumber);

		TrackInfo trackInfo = new TrackInfo(message.getArguments());

		return trackInfo.getClips().get(clipNumber).isPlaying();
	}

	public List<Device> getDevices(int track) {
		OSCMessage devicesMessage = this.sendReceive("/live/devicelist", null, track);

		ArrayList<Device> devices = new ArrayList<Device>();

		Object[] arguments = devicesMessage.getArguments();

		// (int track, int device, str name, ...)
		List<List<Object>> pairs = Lists.partition(Arrays.asList(Arrays.copyOfRange(arguments, 1, arguments.length)), 2);
		for (List<Object> pair : pairs) {
			Device device = new Device((int) arguments[0], (int) pair.get(0), (String) pair.get(1));
			devices.add(device);
		}

		return devices;
	}

	public List<Device> getMasterDevices() {
		OSCMessage devicesMessage = this.sendReceive("/live/master/devicelist", null);

		ArrayList<Device> devices = new ArrayList<Device>();

		Object[] arguments = devicesMessage.getArguments();

		// (int track, int device, str name, ...)
		List<List<Object>> pairs = Lists.partition(Arrays.asList(Arrays.copyOfRange(arguments, 0, arguments.length)), 2);
		for (List<Object> pair : pairs) {
			Device device = new Device(-1, (int) pair.get(0), (String) pair.get(1));
			devices.add(device);
		}

		return devices;
	}

	public List<DeviceParameter> getMasterDeviceParameters(int deviceNumber) {
		// returns: /live/master/device (int device, int parameter int value,
		// str name, ...)
		OSCMessage message = this.sendReceive("/live/master/device", "/live/master/device", deviceNumber);

		ArrayList<DeviceParameter> deviceParameters = new ArrayList<DeviceParameter>();

		Object[] arguments = message.getArguments();

		List<List<Object>> triples = Lists.partition(Arrays.asList(Arrays.copyOfRange(arguments, 1, arguments.length)), 3);

		for (List<Object> triple : triples) {
			Object value = triple.get(1);
			DeviceParameter deviceParameter;
			if (value instanceof Integer) {
				deviceParameter = new DeviceParameter(-1, (int) arguments[1], (int) triple.get(0), (int) value,
						(String) triple.get(2));
			} else {
				deviceParameter = new DeviceParameter(-1, (int) arguments[1], (int) triple.get(0), (float) value,
						(String) triple.get(2));
			}

			deviceParameters.add(deviceParameter);
		}

		return deviceParameters;
	}

	public List<DeviceParameter> getDeviceParameters(int trackNumber, int deviceNumber) {

		// returns (int track, int device, int parameter int value, str name,
		// ...)
		OSCMessage message = this.sendReceive("/live/device", "/live/device/allparam", trackNumber, deviceNumber);

		Object[] arguments = message.getArguments();

		ArrayList<DeviceParameter> deviceParameters = new ArrayList<DeviceParameter>();

		// triples: (int track, int device, int parameter int value, str name,
		// ...)
		List<List<Object>> triples = Lists.partition(Arrays.asList(Arrays.copyOfRange(arguments, 2, arguments.length)), 3);

		for (List<Object> triple : triples) {
			Object value = triple.get(1);
			DeviceParameter deviceParameter;
			if (value instanceof Integer) {
				deviceParameter = new DeviceParameter((int) arguments[0], (int) arguments[1], (int) triple.get(0), (int) value,
						(String) triple.get(2));
			} else {
				deviceParameter = new DeviceParameter((int) arguments[0], (int) arguments[1], (int) triple.get(0), (float) value,
						(String) triple.get(2));
			}

			deviceParameters.add(deviceParameter);
		}

		return deviceParameters;
	}

	public void setMasterDeviceParameter(int deviceNumber, int parameterNumber, float value) {
		this.sendMessage("/live/master/device", new Object[] { deviceNumber, parameterNumber, value });
	}

	public void setDeviceParameter(int trackNumber, int deviceNumber, int parameterNumber, float value) {
		this.sendMessage("/live/device", new Object[] { trackNumber, deviceNumber, parameterNumber, value });
	}

	public void setDeviceParameterNormalized(int trackNumber, int deviceNumber, int parameterNumber, float value) {
		ParameterRange parameterRange = this.getDeviceParameterRange(trackNumber, deviceNumber, parameterNumber);

		double normalizedValue = normalizeParameterInputValue(value, parameterRange);
		this.setDeviceParameter(trackNumber, deviceNumber, parameterNumber, (float) normalizedValue);
	}
	
	public void setMasterDeviceParameterNormalized(int deviceNumber, int parameterNumber, float value) {
		ParameterRange parameterRange = this.getMasterDeviceParameterRange(deviceNumber, parameterNumber);

		double normalizedValue = normalizeParameterInputValue(value, parameterRange);
		this.setMasterDeviceParameter(deviceNumber, parameterNumber, (float) normalizedValue);
	}

	protected double normalizeParameterInputValue(float value, ParameterRange parameterRange) {
		NormalizedField normalizer = new NormalizedField(NormalizationAction.Normalize, null, 1.0, 0.0, parameterRange.getHigh(),
				parameterRange.getLow());
		double normalizedValue = normalizer.normalize(value);
		return normalizedValue;
	}

	public DeviceParameter getDeviceParameter(int trackNumber, int deviceNumber, int parameterNumber) {
		OSCMessage message = this.sendReceive("/live/device", "/live/device/param", new Object[] { trackNumber, deviceNumber,
				parameterNumber });

		Object[] m = message.getArguments();
		// (track, device, param, p.value, str(p.name)

		return new DeviceParameter((int) m[0], (int) m[1], (int) m[2], (float) m[3], (String) m[4]);
	}

	public float getDeviceParameterValue(int trackNumber, int deviceNumber, int parameterNumber) {
		return this.getDeviceParameter(trackNumber, deviceNumber, parameterNumber).getValue();
	}

	public ParameterRange getMasterDeviceParameterRange(int deviceNumber, int parameterNumber) {
		// return /live/master/device/range

		OSCMessage message = this.sendReceive("/live/master/device/range", null, new Object[] { deviceNumber, parameterNumber });

		// /live/master/device/range ( int device, int/float min, int/float
		// max)
		Object[] m = message.getArguments();

		return new ParameterRange((float) m[2], (float) m[3]);
	}

	public ParameterRange getDeviceParameterRange(int trackNumber, int deviceNumber, int parameterNumber) {
		OSCMessage message = this.sendReceive("/live/device/range", null, new Object[] { trackNumber, deviceNumber,
				parameterNumber });

		// /live/device/range (int track, int device, int/float min, int/float
		// max)
		Object[] m = message.getArguments();

		return new ParameterRange((float) m[3], (float) m[4]);
	}

	public void playClip(int trackNumber, int clipNumber) {
		this.sendMessage("/live/play/clip", new Object[] { trackNumber, clipNumber });
	}

	public boolean isMuted(int trackNumber) {
		OSCMessage message = this.sendReceive("/live/mute", null, trackNumber);
		Object[] arguments = message.getArguments();
		Integer muteStatus = (Integer) arguments[1];

		return muteStatus == 0 ? false : true;
	}

	public void muteTrack(int trackNumber) {
		this.sendMessage("/live/mute", new Object[] { trackNumber, 1 });
	}

	public void unMuteTrack(int trackNumber) {
		this.sendMessage("/live/mute", new Object[] { trackNumber, 0 });
	}

	public void toggleMute(int trackNumber) {
		boolean isMuted = this.isMuted(trackNumber);

		if (isMuted) {
			// unmute
			this.unMuteTrack(trackNumber);
		} else {
			// mute
			this.muteTrack(trackNumber);
		}

	}

	public void stopTrack(int trackNumber) {
		this.sendMessage("/live/stop/track", new Object[] { trackNumber });
	}

	public void setTrackVolume(int trackNum, float volume) {
		this.sendMessage("/live/volume", new Object[] { trackNum, volume });
	}

	public void setMasterVolume(float volume) {
		this.sendMessage("/live/master/volume", new Object[] { volume });
	}

	public void setSend(int track, int sendNum, float value) {
		this.sendMessage("/live/send", new Object[] { track, sendNum, value });
	}

	public float getSend(int track, int sendNum) {
		OSCMessage message = this.sendReceive("/live/send", null, track, sendNum);

		// [track, clip, send val]
		return (Float) message.getArguments()[2];

	}

	// ##############################################################

	private void sendMessage(String message, Object... params) {
		Collection<Object> arrayList = new ArrayList<>();
		if (params == null) {
			params = new Object[] {};
		}
		arrayList.addAll(Arrays.asList(params));
		OSCMessage oscMessage = new OSCMessage(message, arrayList);

		try {
			sender.send(oscMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a message and waits for a response to an incoming address. If the
	 * receiveAddress parameter is null the message is the receive address.
	 * 
	 * @param message
	 * @param receiveAddress
	 * @param params
	 * @return
	 */
	private OSCMessage sendReceive(String message, String receiveAddress, Object... params) {
		AbletonOSCListener listener = new AbletonOSCListener();
		Collection<Object> arrayList = new ArrayList<>();
		if (params == null) {
			params = new Object[] {};
		}
		arrayList.addAll(Arrays.asList(params));

		if (receiveAddress == null)
			receiver.addListener(message, listener);
		else
			receiver.addListener(receiveAddress, listener);
		sendMessage(message, params);
		while (!listener.isMessageReceived()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		OSCMessage response = listener.getMessage();
		receiver.removeListener(message);

		return response;
	}

	public synchronized static LiveController getInstance() {
		if (instance == null) {
			System.out.println("Working Directory = " + System.getProperty("user.dir"));
			Properties properties = new Properties();
			BufferedInputStream stream;
			int incommingPort;
			int liveOSCPort;
			String hostIP;
			InetAddress hostAddress;
			try {
				stream = new BufferedInputStream(new FileInputStream("configuration.properties"));

				properties.load(stream);
				stream.close();
				incommingPort = Integer.valueOf(properties.getProperty("incommingPort"));
				liveOSCPort = Integer.valueOf(properties.getProperty("liveOSCPort"));
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
			instance = new LiveController(hostAddress, liveOSCPort, incommingPort);
			return instance;
		}
		return instance;
	}

	private void printObjectArray(Object[] arr) {
		for (Object o : arr)
			System.out.println(o);
	}

}
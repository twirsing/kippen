package nerdproject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class LiveController extends AbstractLiveController {


	public LiveController()	throws SocketException, UnknownHostException {
		super();
	}
	
	public LiveController(int liveOSCPort, int listeningPort)
			throws SocketException, UnknownHostException {
		super(liveOSCPort, listeningPort);
	}

	public LiveController(InetAddress liveOSCAddress, int liveOSCPort,
			int listeningPort) throws SocketException, UnknownHostException {
		super(liveOSCAddress, liveOSCPort, listeningPort);
	}

	
	public void play()throws AbletonCommunicationException {
			try {
				this.sendMessage("/live/play", new Object[] {});
			} catch (IOException e) {
				throw new AbletonCommunicationException(e);
			}
	}
	
	public void stop()throws AbletonCommunicationException {
		try {
			this.sendMessage("/live/stop", new Object[] {});
		} catch (IOException e) {
			throw new AbletonCommunicationException(e);
		}
}

	
	/**
	 * Start clip of track starting from number 1.
	 * 
	 * @throws
	 */
	public void playClip(int trackNumber, int clipNumber)
			throws AbletonCommunicationException {
		try {
			this.sendMessage("/live/play/clip", new Object[] { String.valueOf(trackNumber),
					String.valueOf(clipNumber)});
		} catch (IOException e) {
			throw new AbletonCommunicationException(e);
		}
	}
}

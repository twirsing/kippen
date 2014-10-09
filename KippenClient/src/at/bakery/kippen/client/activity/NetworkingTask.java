package at.bakery.kippen.client.activity;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import android.os.Message;
import android.util.Log;
import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.json.JSONDataSerializer;

public class NetworkingTask extends Thread implements INetworking {

	private Socket socket;
	private OutputStream oos;
	
	private AbstractData txPackets;
	
	private Semaphore flush = new Semaphore(0);
	private Semaphore wait = new Semaphore(1);
	
	private String host;
	private int port;
	private String clientId;
	
	// singleton instance
	private static NetworkingTask instance;
	
	protected static void setup(String host, int port, String clientId) {
		instance = new NetworkingTask(host, port, clientId);
	}
	
	public static NetworkingTask getInstance() {
		if(instance == null) {
			Log.e("KIPPEN", "It is likely that you failed setup IP and port, using default localhost:8080");
			setup("127.0.0.1", 8080, "anonymousClient");
		}
		
		return instance;
	}
	
	private NetworkingTask(String host, int port, String clientId) {
		this.host = host;
		this.port = port;
		this.clientId = clientId;
	}
	
	@Override
	public void sendPacket(AbstractData packets) {
		try {
			wait.acquire();
		} catch (InterruptedException e) {}
		this.txPackets = packets;
		flush.release();
	}
	
	private void resetSocket() {
		try {
			socket.close();
		} catch(Exception ex) {
			Log.e("KIPPEN", "Failed to close socket, ignoring, but leaking");
		} finally {
			socket = null;
		}
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(10);
			} catch(Exception ex) {}
			
			try {
				flush.acquire();
			} catch (InterruptedException e1) {}
			
			if(socket == null) {
				try {
					socket = new Socket(InetAddress.getByName(host), port);
					oos = socket.getOutputStream();
				} catch (Exception ex) {
					Log.e("KIPPEN", "Failed to open socket", ex);
					resetSocket();
				}
			}
			
			// send packets and reset TX
			if(txPackets == null) continue;
			try {
				// set clientId
				txPackets.setClientId(clientId);
				
				// JSON serialize and send packet
				oos.write(JSONDataSerializer.serialize(txPackets));
			} catch(Exception ex) {
				Log.e("KIPPEN", "Failed to send packets", ex);
				resetSocket();
			}
			
			wait.release();
		}
	}
}

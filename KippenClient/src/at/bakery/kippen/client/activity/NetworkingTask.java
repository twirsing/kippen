package at.bakery.kippen.client.activity;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.json.JSONDataSerializer;

public class NetworkingTask extends Thread implements INetworking {

	private Socket socket;
	private OutputStream oos;
//	private ObjectInputStream ois;
	
	private AbstractData txPackets[] = new AbstractData[0];
	private AbstractData rxPacket;
	
	private boolean quit = false;
	
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
			System.err.println("It is likely that you failed setup IP and port, using default localhost:8080");
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
	public void sendPackets(AbstractData ... packets) {
		try {
			wait.acquire();
		} catch (InterruptedException e) {}
		this.txPackets = packets;
		flush.release();
	}
	
//	@Override
//	public DataWithTimestamp receivePacket() {
//		try {
//			wait.acquire();
//		} catch (InterruptedException e) {}
//		flush.release();
//		
//		// wait till finished
//		try {
//			wait.acquire();
//			
//			return rxPacket;
//		} catch (InterruptedException e) {}
//		finally {
//			wait.release();
//		}
//		
//		return null;
//	}
	
	public void quit() {
		quit = true;
	}
	
	@Override
	public void run() {
		while(!quit) {
			try {
				flush.acquire();
			} catch (InterruptedException e1) {}
			
			if(socket == null) {
				try {
					socket = new Socket(InetAddress.getByName(host), port);
					oos = socket.getOutputStream();
					// TODO establish input stream if RX needed
				} catch (Exception e) {
					return;
				}
			}
			
			// send packets and reset TX
			for(AbstractData packet : txPackets) {
				try {
					// set clientId
					packet.setClientId(clientId);
					
					// JSON serialize and send packet
					oos.write(JSONDataSerializer.serialize(packet));
				} catch(Exception ex) {
					ex.printStackTrace();
					System.err.println("Failed to send packets");
				}
			}
			
			// TODO try to RX a packet
			
			wait.release();
		}
	}
}

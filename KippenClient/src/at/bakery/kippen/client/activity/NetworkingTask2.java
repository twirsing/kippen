//package at.bakery.kippen.client.activity;
//
//import java.io.OutputStream;
//import java.net.InetAddress;
//import java.net.Socket;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.locks.Lock;
//import java.util.concurrent.locks.ReentrantLock;
//
//import android.util.Log;
//import at.bakery.kippen.common.AbstractData;
//import at.bakery.kippen.common.json.JSONDataSerializer;
//
//public class NetworkingTask2 implements INetworking {
//
//	private Socket socket = null;
//	private OutputStream oos;
//	
//	private String host;
//	private int port;
//	private String clientId;
//	
//	private ExecutorService executor;
//	private Lock sendLock = new ReentrantLock();
//	
//	// singleton instance
//	private static NetworkingTask2 instance;
//	
//	protected static void setup(String host, int port, String clientId) {
//		instance = new NetworkingTask2(host, port, clientId);
//	}
//	
//	public static NetworkingTask2 getInstance() {
//		if(instance == null) {
//			Log.e("KIPPEN", "It is likely that you failed setup IP and port, using default localhost:8080");
//			setup("127.0.0.1", 8080, "anonymousClient");
//		}
//		
//		return instance;
//	}
//	
//	private NetworkingTask2(String host, int port, String clientId) {
//		this.host = host;
//		this.port = port;
//		this.clientId = clientId;
//		
//		this.executor = Executors.newFixedThreadPool(2);  
//	}
//	
//	private void resetSocket() {
//		try {
//			socket.close();
//		} catch(Exception ex) {
//			Log.e("KIPPEN", "Failed to close socket, ignoring, but leaking");
//		} finally {
//			socket = null;
//		}
//	}
//	
//	@Override
//	public void sendPacket(final AbstractData packet) {
//		sendLock.lock();
//		
//		executor.execute(new Runnable() {
//			@Override
//			public void run() {
//				if(socket == null) {
//					try {
//						socket = new Socket(InetAddress.getByName(host), port);
//						oos = socket.getOutputStream();
//					} catch (Exception e) {
//						Log.e("KIPPEN", "Failed to establish connection", e);
//						resetSocket();
//					}
//				}
//					
//				// send packets and reset TX
//				try {
//					// set clientId
//					packet.setClientId(clientId);
//					
//					// JSON serialize and send packet
//					final byte[] data = JSONDataSerializer.serialize(packet);
//					if(data == null) {
//						Log.e("KIPPEN", "Packet data is null after serialization");
//						return;
//					}
//					
//					System.out.println("writing data: " + data);
//					oos.write(data);
//				} catch(Exception ex) {
//					ex.printStackTrace();
//					Log.e("KIPPEN", "Failed to send packets");
//					resetSocket();
//				} finally {
//					sendLock.unlock();
//				}
//			}
//		});
//	}
//}

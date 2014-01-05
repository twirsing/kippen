package at.bakery.kippen.client.activity;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import at.bakery.kippen.common.DataWithTimestampAndMac;

public class NetworkingTask extends Thread implements INetworking {

	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	private DataWithTimestampAndMac txPackets[] = new DataWithTimestampAndMac[0];
	private DataWithTimestampAndMac rxPacket;
	
	private boolean quit = false;
	
	private Semaphore flush = new Semaphore(0);
	private Semaphore wait = new Semaphore(1);
	
	private String host;
	private int port;
	
	public NetworkingTask(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	@Override
	public void sendPackets(DataWithTimestampAndMac ... packets) {
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
					oos = new ObjectOutputStream(socket.getOutputStream());
//					ois = new ObjectInputStream(socket.getInputStream());
				} catch (Exception e) {
					return;
				}
			}
			
			// send packets and reset TX
			for(DataWithTimestampAndMac packet : txPackets) {
				try {
					oos.writeObject(packet);
				} catch(Exception ex) {
					ex.printStackTrace();
					System.err.println("Failed to send packets");
				}
			}
//			txPackets = new DataWithTimestamp[0];
			
			// try to RX a packet
//			try {
//				rxPacket = (DataWithTimestamp)ois.readObject();
//			} catch(EOFException eofex) {
//				// ignore, there's simple nothing to be read
//			} catch (Exception e) {
//				System.err.println("Failed to receive packet");
//			}
			
			wait.release();
		}
	}
}

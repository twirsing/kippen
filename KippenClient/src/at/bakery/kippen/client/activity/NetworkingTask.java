package at.bakery.kippen.client.activity;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import at.bakery.kippen.common.DataWithTimestamp;

public class NetworkingTask extends Thread implements INetworking {

	private Socket socket;
	private ObjectOutputStream oos;
	private DataWithTimestamp packets[] = new DataWithTimestamp[0];
	
	private boolean quit = false;
	
	private Semaphore flush = new Semaphore(0);
	private Semaphore wait = new Semaphore(1);
	
	@Override
	public void sendPackets(DataWithTimestamp ... packets) {
		try {
			wait.acquire();
		} catch (InterruptedException e) {}
		this.packets = packets;
		flush.release();
	}
	
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
					socket = new Socket(InetAddress.getByName("192.168.1.12"), 10000);
					oos = new ObjectOutputStream(socket.getOutputStream());
				} catch (Exception e) {
					return;
				}
			}
			
			for(DataWithTimestamp packet : packets) {
				try {
					oos.writeObject(packet);
				} catch(Exception ex) {
					System.err.println("Failed to send packet");
				}
			}
			
			wait.release();
		}
	}
}

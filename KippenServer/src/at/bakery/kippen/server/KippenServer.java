package at.bakery.kippen.server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;

import at.bakery.kippen.common.DataWithTimestamp;

public class KippenServer extends JFrame {

	public static void main(String args[]) {
		try {
			new KippenServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ServerSocket serverSock;
	private boolean quit = false;

	public KippenServer() throws Exception {
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// TODO init GUI controller

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent winEvt) {
				quit = true;
			}
		});

		ServerSocket serverSock = new ServerSocket(10000);
		Socket client = serverSock.accept();
		ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
		while (!quit) {
			
			DataWithTimestamp data = (DataWithTimestamp) ois.readObject();
			System.out.println(data.getTimestamp() + ": " + data.getData());
		}
		
		serverSock.close();
	}

}

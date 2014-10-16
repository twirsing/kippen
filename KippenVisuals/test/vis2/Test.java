package vis2;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import processing.data.JSONObject;

public class Test {

	int port = 9876;
	
	@org.junit.Test
	public void setBarrelValue() throws UnknownHostException, IOException {
		JSONObject object = new JSONObject();
		object.setString("command", "barrelRoll");
		object.setString("trackNumber", "1");
		object.setString("value", "0.4");
		this.send(object);
	}


	@org.junit.Test
	public void setCubeValue() throws UnknownHostException, IOException {
		JSONObject object = new JSONObject();
		object.setString("command", "sideChange");
		object.setString("trackNumber", "1");
		object.setString("clipNumber", "3");
		this.send(object);
	}
	
	private void send(JSONObject o) throws UnknownHostException, IOException {
		Socket socket = new Socket("localhost",port);
		OutputStream oos = socket.getOutputStream();
		oos.write(o.toString().getBytes());
		oos.flush();
		oos.close();
	}
}

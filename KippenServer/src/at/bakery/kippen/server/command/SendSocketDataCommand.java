package at.bakery.kippen.server.command;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

import at.bakery.kippen.common.data.PingData;
import at.bakery.kippen.server.KippenServer;

import com.google.gson.Gson;

public class SendSocketDataCommand implements Command {

	private String command;
	private String destinationIP;
	private Integer destinationPort;
	private String data1, data2;

	static Logger log = Logger.getLogger(KippenServer.class.getName());

	public SendSocketDataCommand(String destinationIP, String destinationPort, String command, String data1, String data2) {
		this.command = command;
		this.destinationIP = destinationIP;
		this.destinationPort = Integer.valueOf(destinationPort);
		this.data1 = data1;
		this.data2 = data2;
	}

	@Override
	public void execute(Map<String, String> params) {
		System.out.println("execute: " + destinationIP + "::" + destinationPort + "::" + command + "::" + data1 + "::" + data2);

		OutputStream oos;
		BufferedReader ois;
		Socket socket;
		try {

			try {
				socket = new Socket(InetAddress.getByName(this.destinationIP), this.destinationPort);
				oos = socket.getOutputStream();
				ois = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
			} catch (Exception e) {
				return;
			}
			try {
				Object myobj = new PingData();
				String json = "{\"command\":\"" + this.command + "\", \"data1\":\"" + this.data1 + "\", \"data2\":\""
						+ this.data2 + "\"}";
				oos.write(json.getBytes("UTF8"));
				oos.flush();
				StringBuilder responseStrBuilder = new StringBuilder();
				String response = ois.readLine();
				String status = new Gson().fromJson(response, String.class);

			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("Failed to send packets");
			}
			oos.close();
			ois.close();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

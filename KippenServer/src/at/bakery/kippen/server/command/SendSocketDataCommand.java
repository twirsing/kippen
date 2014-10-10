package at.bakery.kippen.server.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import at.bakery.kippen.common.data.PingData;
import at.bakery.kippen.config.Param;
import at.bakery.kippen.server.KippenServer;

import com.google.gson.Gson;



/*
 * 
 	implemented commands on raspberry server:
 	<commands>
		<CommandConfig commandType="SENDSOCKETDATA">
			<param key="command" value="switchrelay" />
			<param key="destinationIP" value="192.168.20.15" />
			<param key="destinationPort" value="9999" />
			<param key="channel" value="1" />
			<param key="switchcommand" value="toggle" />  // toggle, high, low, flash
		</CommandConfig>
	</commands>
 */
public class SendSocketDataCommand implements Command{

	private List<Param> param;
	private String jsCommand;
	private String destinationIP;
	private int destinationPort;
	static Logger log = Logger.getLogger(KippenServer.class.getName());
	
	public SendSocketDataCommand(List<Param>  param){
		this.param = param;
		this.jsCommand = "";
		for (Param aParam : this.param) {
			if(aParam.getKey().equalsIgnoreCase("destinationIP")) this.destinationIP = aParam.getValue();
			else if(aParam.getKey().equalsIgnoreCase("destinationPort"))  this.destinationPort = Integer.parseInt(aParam.getValue());
			else this.jsCommand = this.jsCommand + "\""+aParam.getKey()+"\":\""+aParam.getValue()+"\", ";
		}
		
	}
	
	@Override
	public void execute(Map<String, String> params) {
		System.out.println("execute: "+this.jsCommand);

		OutputStream oos;
		BufferedReader ois;
		Socket socket;
		try {

			try {
				socket = new Socket(InetAddress.getByName(this.destinationIP), this.destinationPort);
				oos = socket.getOutputStream();
				ois = new BufferedReader(
						new InputStreamReader(socket.getInputStream(), "UTF8"));
			} catch (Exception e) {
				return;
			}
			try {
				Object myobj = new PingData();
				//String json = "{\"command\":\""+this.command+"\", \"data1\":\""+this.data1+"\", \"data2\":\""+this.data2+"\"}";
				oos.write(this.jsCommand.getBytes("UTF8"));
				oos.flush();
			    StringBuilder responseStrBuilder = new StringBuilder();
			    String response = ois.readLine();
			    String status = new Gson().fromJson(response, String.class);

			} catch(Exception ex) {
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

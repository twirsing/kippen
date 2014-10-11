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
	
	
	private static class MessageProcessor implements Runnable {

		private String jsCommand;
//		private Socket socket;
		private String destinationIP;
		private int destinationPort;
		
		public MessageProcessor(String destinationIP, int destinationPort, String jsCommand){
			this.destinationIP = destinationIP;
			this.destinationPort = destinationPort;
			this.jsCommand = jsCommand;
		}
		
		@Override
	    public void run() {
	    	
			OutputStream oos;
			BufferedReader ois;
			
	    	try {
	    		Socket socket = new Socket(InetAddress.getByName(this.destinationIP), this.destinationPort);
//	    		System.out.println("execute: "+this.socket);
//	    		System.out.println("jsCommand: "+this.jsCommand.getBytes("UTF8"));
	    		oos = socket.getOutputStream();
	    		oos.write(this.jsCommand.getBytes("UTF8"));
				oos.flush();
				oos.close();
//				ois = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
//				
//			    StringBuilder responseStrBuilder = new StringBuilder();
//			    String response = ois.readLine();
//			    String status = new Gson().fromJson(response, String.class);
//				
				socket.close();
	    	} catch (Exception ex) {
	    		ex.printStackTrace();
				System.err.println("Failed to send packets");
			}
//	    	
	    }
	}
	
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
	public void execute(Map<String, String> params) throws IOException {
		
		try {
			//socket = new Socket(InetAddress.getByName(this.destinationIP), this.destinationPort);
			MessageProcessor MessageProcessor = new MessageProcessor(this.destinationIP, this.destinationPort, this.jsCommand);
			Thread t = new Thread(MessageProcessor);
	        t.start();
	        
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		
		
	
	}
}

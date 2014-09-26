package nerdproject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.StringTokenizer;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

public class AbletonCommand {
	private OSCPortOut sender;

	public void init() throws UnknownHostException, SocketException {
		sender = new OSCPortOut(9000);
		System.out.print("Command> ");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String command = null;
		// read the username from the command-line; need to use try/catch with
		// the
		// readLine() method
		try {
			while(command != "exit"){
				command = br.readLine();
				
				StringTokenizer st = new StringTokenizer(command, " ");
				String token = st.nextToken();
				String msgString = "/live/" + token;
				
				Collection<Object> params = new ArrayList<Object>();
				while(st.hasMoreTokens()){
					token = st.nextToken();
					System.out.println("param: " + token);
					params.add(token);
				}
				System.out.println(params);
				OSCMessage msg = new OSCMessage(msgString,params);
				System.out.println("Sending message: " + msgString);
				sender.send(msg);
				System.out.print("Command> ");
			}
		} catch (IOException ioe) {
			System.out.println("IO error trying to read your name!");
			System.exit(1);
		}

	System.out.println("Bye bye!");

	}

	public static void main(String[] args) throws UnknownHostException,
			SocketException {
		AbletonCommand abletonCommand = new AbletonCommand();
		abletonCommand.init();

	}
}

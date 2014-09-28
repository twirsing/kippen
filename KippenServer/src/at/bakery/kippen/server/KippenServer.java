package at.bakery.kippen.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXB;

import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.json.JSONDataSerializer;
import at.bakery.kippen.config.CommandConfig;
import at.bakery.kippen.config.Configuration;
import at.bakery.kippen.config.EventConfig;
import at.bakery.kippen.config.ObjectConfig;
import at.bakery.kippen.config.Param;
import at.bakery.kippen.config.TypeEnum;
import at.bakery.kippen.server.command.AbletonPlayCommand;
import at.bakery.kippen.server.command.AbletonStopCommand;
import at.bakery.kippen.server.command.Command;
import at.bakery.kippen.server.command.MasterVolumeCommand;
import at.bakery.kippen.server.command.SendSocketDataCommand;
import at.bakery.kippen.server.command.ToggleMuteCommand;
import at.bakery.kippen.server.objects.AbstractKippenObject;
import at.bakery.kippen.server.objects.BarrelObject;
import at.bakery.kippen.server.objects.CubeObject;

public class KippenServer {
	static Logger log = Logger.getLogger(KippenServer.class.getName());
	public static Level LOG_LEVEL = Level.INFO;

	public static int OBJECT_TIMEOUT_MINUTES = 5;

	private HashMap<String, AbstractKippenObject> objectMap = new HashMap<String, AbstractKippenObject>();

	public static void main(String args[]) {
		try {
			final KippenServer server = new KippenServer();
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() throws Exception {
		initObjects();

		Executor workerExecutor = Executors.newCachedThreadPool();
		final ServerSocket serverSock = new ServerSocket(10001);

		log.info("Kippen Server is starting up ...");

		try {
			while (true) {
				final Socket client = serverSock.accept();
				final NetworkInterface ni = NetworkInterface.getByInetAddress(client.getInetAddress());
				final String clientId;
				if (ni != null && ni.getHardwareAddress() != null) {
					clientId = new String(ni.getHardwareAddress());
				} else {
					clientId = client.getInetAddress().getHostAddress();
				}

				log.info("Client " + clientId + " connected ...");

				workerExecutor.execute(new Runnable() {

					@Override
					public void run() {
						try {
							BufferedReader ois = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF8"));
							while (true) {
								// first line is canonical class name of event
								String dataType = ois.readLine();

								// second line is JSON data
								AbstractData data = JSONDataSerializer.deserialize(dataType, ois.readLine());

								// pick the client and process received data
								objectMap.get(data.getClientId()).processData(data);
							}
						} catch (Exception ex) {
							log.severe("Client " + clientId + " died ...");
						}
					}
				});
			}
		} finally {
			serverSock.close();
		}
	}

	private void initObjects() {
		Configuration config = JAXB.unmarshal(new File("config.xml"), Configuration.class);

		int objectTimeout = config.getTimeoutMinutes();

		log.info("Setting object timeout to: " + objectTimeout + " minute(s).");

		OBJECT_TIMEOUT_MINUTES = objectTimeout;

		// for each object set the commands and events
		for (ObjectConfig obj : config.getObjects().getObjectConfig()) {
			String mac = obj.getMac();

			if (obj.getType() == TypeEnum.CUBE) {
				log.info("Registering a CUBE with MAC " + mac);

				// make new kippen object
				CubeObject cubeKippObject = new CubeObject(mac);

				// add the new object to the server object map
				objectMap.put(obj.getMac(), cubeKippObject);

				// for all events the object reacts to
				for (EventConfig e : obj.getEvents().getEventConfig()) {
					List<Command> commands = makeCommands(e.getCommands().getCommandConfig());

					switch (e.getEventType()) {
					case EventTypes.SIDECHANGE:
						log.info("Registering side change event");
						cubeKippObject.setCommandsForEvents(EventTypes.SIDECHANGE, commands);
						break;
					case EventTypes.SHAKE:
						log.info("Registering shake event");
						cubeKippObject.setCommandsForEvents(EventTypes.SHAKE, commands);
						break;
					case EventTypes.TIMEOUT:
						log.info("Registering roll event");
						cubeKippObject.setCommandsForEvents(EventTypes.TIMEOUT, commands);
						break;
					// add other events here
					default:
						break;
					}
				}
			} else if (obj.getType() == TypeEnum.BARREL) {
				log.info("Registering a BARREL with MAC " + mac);

				// make new kippen object
				BarrelObject barrelKippObject = new BarrelObject(mac);

				// add the new object to the server object map
				objectMap.put(obj.getMac(), barrelKippObject);

				// for all events the object reacts to
				for (EventConfig e : obj.getEvents().getEventConfig()) {
					List<Command> commands = makeCommands(e.getCommands().getCommandConfig());

					switch (e.getEventType()) {
					case EventTypes.SHAKE:
						log.info("Registering shake event");
						barrelKippObject.setCommandsForEvents(EventTypes.SHAKE, commands);
						break;
					case EventTypes.ROLLCHANGE:
						log.info("Registering roll event");
						barrelKippObject.setCommandsForEvents(EventTypes.ROLLCHANGE, commands);
						break;
					case EventTypes.TIMEOUT:
						log.info("Registering roll event");
						barrelKippObject.setCommandsForEvents(EventTypes.TIMEOUT, commands);
						break;
					// add other events here
					default:
						break;
					}
				}
			}
		}
	}

	private List<Command> makeCommands(List<CommandConfig> configList) {
		ArrayList<Command> commandList = new ArrayList<Command>();
		for (CommandConfig c : configList) {
			switch (c.getCommandType()) {
			case "ABLETONPLAY":
				log.log(Level.INFO, "Registering ABLETONPLAY command");
				commandList.add(new AbletonPlayCommand(getCommandParamValue(
						"trackNumber", c.getParam())));
				break;
			case "STOPTRACK":
				log.log(Level.INFO, "Registering ABLETONSTOP command");
				commandList.add(new AbletonStopCommand(getCommandParamValue(
						"trackNumber", c.getParam())));
				break;
			case "TOGGLEMUTE":
				log.log(Level.INFO, "Registering TOGGLEMUTE command");
				commandList.add(new ToggleMuteCommand(Integer
						.valueOf(getCommandParamValue("trackNumber",
								c.getParam()))));
				break;
			case "MASTERVOLUME":
				log.log(Level.INFO, "Registering MASTERVOLUME command");
				commandList.add(new MasterVolumeCommand());
				break;
				
			case "SENDSOCKETDATA":
				log.log(Level.INFO, "Registering SENDSOCKETDATA command");
				commandList.add(new SendSocketDataCommand(c.getParam()));
				break;
				

			default:
				break;
			}
		}
		return commandList;
	}

	private String getCommandParamValue(String key, List<Param> commandParam) {
		for (Param param : commandParam) {
			if (param.getKey().equalsIgnoreCase(key)) {
				return param.getValue();
			}
		}
		return null;
	}
}

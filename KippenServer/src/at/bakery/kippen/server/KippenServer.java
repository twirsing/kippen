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
import at.bakery.kippen.common.data.ContainerData;
import at.bakery.kippen.common.json.JSONDataSerializer;
import at.bakery.kippen.config.CommandConfig;
import at.bakery.kippen.config.Configuration;
import at.bakery.kippen.config.EventConfig;
import at.bakery.kippen.config.ObjectConfig;
import at.bakery.kippen.config.Param;
import at.bakery.kippen.config.TypeEnum;
import at.bakery.kippen.server.command.AbletonDeviceCommand;
import at.bakery.kippen.server.command.AbletonMasterDeviceCommand;
import at.bakery.kippen.server.command.AbletonPlayCommand;
import at.bakery.kippen.server.command.AbletonStopCommand;
import at.bakery.kippen.server.command.Command;
import at.bakery.kippen.server.command.MasterVolumeCommand;
import at.bakery.kippen.server.command.SendSocketDataCommand;
import at.bakery.kippen.server.command.ToggleMuteCommand;
import at.bakery.kippen.server.objects.AbstractKippenObject;
import at.bakery.kippen.server.objects.BallObject;
import at.bakery.kippen.server.objects.BarrelObject;
import at.bakery.kippen.server.objects.CubeObject;

//TODO make server listen to changes in the xml  config file so changes can be applied at runtime.
public class KippenServer {
	static Logger log = Logger.getLogger(KippenServer.class.getName());
	public static Level LOG_LEVEL = Level.INFO;
	private Configuration _config;

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
								if (dataType == null) {
									break;
								}
								if (dataType.isEmpty()) {
									continue;
								}

								// second line is JSON data
								String dataLine = ois.readLine();
								if (dataLine == null) {
									break;
								}

								// set receive time stamp
								long receiveTime = System.currentTimeMillis();

								AbstractData data = JSONDataSerializer.deserialize(dataType, dataLine);
								if (data == null) {
									continue;
								}

								if (data instanceof ContainerData == false) {
									// TODO process battery, etc.
									continue;
								}

								// pick the client and process all received data
								AbstractKippenObject object = objectMap.get(data.getClientId());
								System.out.println(data.getClientId().substring(0,3));
								if (object == null) {
									log.warning("Client MAC address " + data.getClientId() + " is not registered");
									return;
								}

								// process each data packet
								ContainerData containerData = (ContainerData) data;

								// check lag and drop packet if necessary
								long lag = System.currentTimeMillis() - containerData.getTimestamp();
								if (lag > 5000) {
									// System.err.println("Dropping packet, lag is "
									// + lag + "ms");
									// continue;
								}

								// lag in bounds, process ...
								object.processData(containerData.accData);
								object.processData(containerData.avgAccData);
								object.processData(containerData.moveData);
								object.processData(containerData.shakeData);
								object.processData(containerData.cubeData);
								object.processData(containerData.barrelData);
								
								System.out.println(containerData.getClientId());
//								System.out.println(object);
//								System.out.println(containerData);
								

							}
						} catch (Exception ex) {
							log.severe("Client " + clientId + " died ...");
							ex.printStackTrace();
						}
					}
				});
			}
		} finally {
			serverSock.close();
		}
	}

	public Configuration getConfiguration() {
		return _config;
	}

	private void initObjects() {
		_config = JAXB.unmarshal(new File("config.xml"), Configuration.class);

		// for each object set the commands and events
		for (ObjectConfig objectConfig : _config.getObjects().getObjectConfig()) {
			String mac = objectConfig.getMac();

			if (objectConfig.getType() == TypeEnum.CUBE) {
				log.info("Registering a CUBE with MAC " + mac);

				// make new kippen object
				CubeObject cubeKippObject = new CubeObject(mac, _config.getTimeoutMinutes());

				// add the new object to the server object map
				objectMap.put(objectConfig.getMac(), cubeKippObject);

				// for all events the object reacts to
				for (EventConfig e : objectConfig.getEvents().getEventConfig()) {
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
						log.info("Registering timeout event");
						cubeKippObject.setCommandsForEvents(EventTypes.TIMEOUT, commands);
						break;
					case EventTypes.MOVE:
						log.info("Registering move event");
						cubeKippObject.setCommandsForEvents(EventTypes.MOVE, commands);
						break;
					// add other events here
					default:
						break;
					}
				}
			} else if (objectConfig.getType() == TypeEnum.BARREL) {
				log.info("Registering a BARREL with MAC " + mac);

				// make new kippen object
				BarrelObject barrelKippObject = new BarrelObject(mac, _config.getTimeoutMinutes());

				// add the new object to the server object map
				objectMap.put(objectConfig.getMac(), barrelKippObject);

				// for all events the object reacts to
				for (EventConfig e : objectConfig.getEvents().getEventConfig()) {
					List<Command> commands = makeCommands(e.getCommands().getCommandConfig());

					switch (e.getEventType()) {
					// case EventTypes.SHAKE:
					// log.info("Registering shake event");
					// barrelKippObject.setCommandsForEvents(EventTypes.SHAKE,
					// commands);
					// break;
					case EventTypes.ROLLCHANGE:
						log.info("Registering roll event");
						barrelKippObject.setCommandsForEvents(EventTypes.ROLLCHANGE, commands);
						break;
					// case EventTypes.TIMEOUT:
					// log.info("Registering roll event");
					// barrelKippObject.setCommandsForEvents(EventTypes.TIMEOUT,
					// commands);
					// break;
					// add other events here
					default:
						break;
					}
					
				//	objectMap.put(mac, barrelKippObject);
				}
			} else if (objectConfig.getType() == TypeEnum.BALL) {
				log.info("Registering a BALL with MAC " + mac);

				BallObject ballObject = new BallObject(mac, _config.getTimeoutMinutes());

				// add the new object to the server object map
				objectMap.put(mac, ballObject);

				for (EventConfig e : objectConfig.getEvents().getEventConfig()) {
					List<Command> commands = makeCommands(e.getCommands().getCommandConfig());
					switch (e.getEventType()) {
					case EventTypes.MOVE:
						log.info("Registering move event for ball");
						ballObject.setCommandsForEvents(EventTypes.MOVE, commands);
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
				commandList.add(new AbletonPlayCommand(getCommandParamValue("trackNumber", c.getParam())));
				break;
			case "STOPTRACK":
				log.log(Level.INFO, "Registering ABLETONSTOP command");
				commandList.add(new AbletonStopCommand(getCommandParamValue("trackNumber", c.getParam())));
				break;
			case "TOGGLEMUTE":
				log.log(Level.INFO, "Registering TOGGLEMUTE command");
				commandList.add(new ToggleMuteCommand(Integer.valueOf(getCommandParamValue("trackNumber", c.getParam()))));
				break;
			case "MASTERVOLUME":
				log.log(Level.INFO, "Registering MASTERVOLUME command");
				commandList.add(new MasterVolumeCommand());
				break;

			case "SENDSOCKETDATA":
				log.log(Level.INFO, "Registering SENDSOCKETDATA command");
				commandList.add(new SendSocketDataCommand(c.getParam()));
				break;

			case "ABLETONDEVICE":
				log.log(Level.INFO, "Registering ABLETONDEVICE command");
				int trackNumber = Integer.valueOf(getCommandParamValue("trackNumber", c.getParam()));
				int deviceNumber = Integer.valueOf(getCommandParamValue("deviceNumber", c.getParam()));
				int parameterNumber = Integer.valueOf(getCommandParamValue("parameterNumber", c.getParam()));
				commandList.add(new AbletonDeviceCommand(trackNumber, deviceNumber, parameterNumber));
				break;

			case "ABLETONMASTERDEVICE":
				log.log(Level.INFO, "Registering ABLETONMASTERDEVICE command");
				int masterDeviceNumber = Integer.valueOf(getCommandParamValue("deviceNumber", c.getParam()));
				int masterParameterNumber = Integer.valueOf(getCommandParamValue("parameterNumber", c.getParam()));
				commandList.add(new AbletonMasterDeviceCommand(masterDeviceNumber, masterParameterNumber));
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

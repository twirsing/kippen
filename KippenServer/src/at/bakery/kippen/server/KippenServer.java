package at.bakery.kippen.server;

import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXB;

import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.SensorTripleData;
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
import at.bakery.kippen.server.command.ToStringCommand;
import at.bakery.kippen.server.objects.AbstractKippObject;
import at.bakery.kippen.server.objects.CubeKippObject;
import at.bakery.kippen.server.outlets.JframeKippOutlet;

public class KippenServer extends JFrame {
	static Logger log =  Logger.getLogger(KippenServer.class.getName());
	
	private JLabel infoLabel = new JLabel("Info: ");
	private JLabel infoText = new JLabel("- - -");

	private JLabel wifiDistanceLabel = new JLabel("WIFI distance: ");
	private JLabel wifiDistanceText = new JLabel("- - -");

	private JLabel accIsMovingLabel = new JLabel("Is moving?: ");
	private JLabel accIsMovingText = new JLabel(Boolean.FALSE.toString());

	private JLabel orientLabel = new JLabel("Orientation vector: ");
	private JLabel orientText = new JLabel("- - -");

	private JLabel metricDistanceLabel = new JLabel("Metric distance: ");
	private JLabel metricDistanceText = new JLabel("- - -");

	private HashMap<String, AbstractKippObject> objectMap = new HashMap<String, AbstractKippObject>();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String args[]) {
		try {
			final KippenServer server = new KippenServer();

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					server.showGUI();
				}
			});

			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// FIXME no real exit condition
	private boolean quit = false;

	public void start() throws Exception {

		Executor workerExecutor = Executors.newCachedThreadPool();
		final ServerSocket serverSock = new ServerSocket(10000);
		log.info("server is runnning");
		try {
			while (true) {
				final Socket client = serverSock.accept();

				workerExecutor.execute(new Runnable() {

					@Override
					public void run() {
						try {
							BufferedReader ois = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF8"));
							// TODO if TX is needed
							// OutputStream oos = client.getOutputStream();
							while(true) {
								// first line is canonical class name of event
								String dataType = ois.readLine(); 
								
								// second line is JSON data
								AbstractData data = JSONDataSerializer.deserialize(dataType, ois.readLine());
							
								// FIXME debug
								if(data instanceof SensorTripleData) {
									System.out.println(data);
								}
								
								//objectMap.get(data.getClientId()).processData(data);

							}
						} catch (Exception ex) {
							System.out.println("Client dead ...");
							ex.printStackTrace();
						}
					}
				});
			}
		} finally {
			serverSock.close();
		}
	}

	public void init() {
		// read XML config file

		// set and register objects
//		CubeKippObject cube1 = new CubeKippObject("88:30:8A:38:53:05");

		HashMap<String, JLabel> jlabels = new HashMap<String, JLabel>();
		jlabels.put("info", infoText);
		jlabels.put("wifi", wifiDistanceText);
		jlabels.put("accaleration", accIsMovingText);
		jlabels.put("orientation", orientText);
		jlabels.put("distance", metricDistanceText);

		JframeKippOutlet Jfoutlet = new JframeKippOutlet(jlabels);
//		cube1.addOutlet(Jfoutlet);
//		this.objectMap.put(cube1.getId(), cube1);
	}

	private void initObjects() {
		Configuration config = JAXB.unmarshal(new File("config.xml"),
				Configuration.class);
		
		
		//for each object set the commands and events
		for (ObjectConfig obj : config.getObjects().getObjectConfig()) {
			//if its a cube
			if (obj.getType() == TypeEnum.CUBE) {
				String mac = obj.getMac();
				log.log(Level.INFO,"Found CUBE with MAC: " + mac);
				//make new kippen object
				CubeKippObject cubeKippObject = new CubeKippObject(mac);
				
				//add the new object to the server object map
				objectMap.put(obj.getMac(), cubeKippObject);
				
				//for all events the object reacts to
				for (EventConfig e : obj.getEvents().getEventConfig()) {
					List<Command> commands = makeCommands(e.getCommands().getCommandConfig());
					
					switch (e.getEventType()) {
					case EventTypes.SIDECHANGE:
						log.log(Level.INFO,"Found  side change event");
						cubeKippObject.setCommandsForEvents(EventTypes.SIDECHANGE, commands);
						break;
					case EventTypes.SHAKE:
						log.log(Level.INFO,"Found shake event");
						cubeKippObject.setCommandsForEvents(EventTypes.SHAKE, commands);
						break;
					//add other events here	
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
				log.log(Level.INFO,"Found ABLETONPLAY command");
				commandList.add(new AbletonPlayCommand(getCommandParamValue(
						"trackNumber", c.getParam())));
				break;
			case "ABLETONSTOP":
				log.log(Level.INFO,"Found ABLETONSTOP command");
				commandList.add(new AbletonStopCommand(getCommandParamValue(
						"trackNumber", c.getParam())));
				break;
			case "TOSTRING":
				log.log(Level.INFO,"Found TO STRING command");
				commandList.add(new ToStringCommand());
				break;

			default:
				break;
			}
		}
		return commandList;
	}

	private String getCommandParamValue(String key,
			List<Param> commandParam) {
		for (Param param : commandParam){
			if (param.getKey().equalsIgnoreCase(key))
				return param.getValue();
		}
		return null;
	}

	private void showGUI() {
		init();
		initObjects();
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent winEvt) {
				quit = true;
			}
		});

		JPanel contentPane = new JPanel(new GridLayout(5, 2));
		setContentPane(contentPane);
		setSize(800, 600);

		infoLabel.setVisible(true);
		infoText.setVisible(true);

		wifiDistanceLabel.setVisible(true);
		wifiDistanceText.setVisible(true);
		accIsMovingText.setVisible(true);
		accIsMovingLabel.setVisible(true);
		orientText.setVisible(true);
		orientLabel.setVisible(true);
		metricDistanceLabel.setVisible(true);
		metricDistanceText.setVisible(true);

		getContentPane().add(infoLabel);
		getContentPane().add(infoText);

		getContentPane().add(wifiDistanceLabel);
		getContentPane().add(wifiDistanceText);

		getContentPane().add(accIsMovingLabel);
		getContentPane().add(accIsMovingText);

		getContentPane().add(orientLabel);
		getContentPane().add(orientText);

		getContentPane().add(metricDistanceLabel);
		getContentPane().add(metricDistanceText);

		// display the window.
		// pack();
		setVisible(true);
	}
}

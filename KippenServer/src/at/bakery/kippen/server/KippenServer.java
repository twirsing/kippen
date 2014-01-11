package at.bakery.kippen.server;

import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import at.bakery.kippen.common.DataWithTimestampAndMac;
import at.bakery.kippen.common.IData;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.OrientationData;
import at.bakery.kippen.common.data.OrientationSimpleData;
import at.bakery.kippen.common.data.SensorSingleData;
import at.bakery.kippen.common.data.SensorTripleData;
import at.bakery.kippen.common.data.WifiLevelsData;
import at.bakery.kippen.server.objects.AbstractKippObject;
import at.bakery.kippen.server.objects.CubeKippObject;
import at.bakery.kippen.server.outlets.JframeKippOutlet;

public class KippenServer extends JFrame {
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

	private boolean quit = false;

	public void start() throws Exception {

		Executor workerExecutor = Executors.newCachedThreadPool();
		final ServerSocket serverSock = new ServerSocket(10000);

		try {
			while (true) {
				final Socket client = serverSock.accept();

				workerExecutor.execute(new Runnable() {

					@Override
					public void run() {
						try {
							ObjectInputStream ois = new ObjectInputStream(
									client.getInputStream());
							ObjectOutputStream oos = new ObjectOutputStream(
									client.getOutputStream());

							while (true) {
								DataWithTimestampAndMac data = (DataWithTimestampAndMac) ois
										.readObject();

								IData d = data.getData();
								String macAddress = data.getMacAddress();

								objectMap.get(macAddress).processData(d);

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
		CubeKippObject cube1 = new CubeKippObject(
				"88:30:8A:38:53:05");
		
		
		HashMap<String, JLabel> jlabels = new HashMap<String, JLabel>();
		jlabels.put("info", infoText);
		jlabels.put("wifi", wifiDistanceText);
		jlabels.put("accaleration", accIsMovingText);
		jlabels.put("orientation", orientText);
		jlabels.put("distance", metricDistanceText);
		
		JframeKippOutlet Jfoutlet = new JframeKippOutlet(
				jlabels);
		cube1.addOutlet(Jfoutlet);
		this.objectMap.put(cube1.getId(), cube1);
	}

	private void showGUI() {
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

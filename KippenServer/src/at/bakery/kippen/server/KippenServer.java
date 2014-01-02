package at.bakery.kippen.server;

import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import at.bakery.kippen.common.DataWithTimestamp;
import at.bakery.kippen.common.IData;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.OrientationData;
import at.bakery.kippen.common.data.OrientationSimpleData;
import at.bakery.kippen.common.data.SensorSingleData;
import at.bakery.kippen.common.data.SensorTripleData;
import at.bakery.kippen.common.data.WifiLevelsData;

public class KippenServer extends JFrame {

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
			while(true) {
				final Socket client = serverSock.accept();
	
				workerExecutor.execute(new Runnable() {
					
					@Override
					public void run() {
						try {
							ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
							ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
							
							// TODO move into own plugin classes
							Queue<WifiLevelsData> avgWifiLevel = new LinkedList<>();
							double lastAvgWifiLevel = -1;
							Boolean hasMoved = true;
							SensorTripleData lastAccData = new SensorTripleData(0, 0, 0);
							// TODO
							
							while(true) {
						
								DataWithTimestamp data = (DataWithTimestamp)ois.readObject();
								
								IData d = data.getData();
								
								// WIFI
								if(d instanceof WifiLevelsData) {
									WifiLevelsData wd = (WifiLevelsData)d;
									
									wifiDistanceText.setText(wd.toString());
									
									// add the current measurement to the list
									avgWifiLevel.offer(wd);
									
									// compute average
									if(avgWifiLevel.size() > 0) {
										double avgLevel = 0;
										
										// for each measurement item ...
										for(WifiLevelsData w : avgWifiLevel) {
											double innerAvgLevel = 0;
											
											// for each measured wifi in the item ...
											for(Integer val : w.getNetworks().values()) {
												innerAvgLevel += val;
											}
											
											// ... compute average
											avgLevel += (innerAvgLevel / w.getNetworks().values().size());
										}
										avgLevel /= avgWifiLevel.size();
										
										// RSSI to meters conversion
										double dist = Math.pow(10, ((27.55 - (67.6 + avgLevel)) / 20.0));
										metricDistanceText.setText("~ " + dist + "m (level: " + avgLevel + ")");
										
										// remember the last to measurements, remove others
										if(avgWifiLevel.size() > 10) {
											avgWifiLevel.poll();
										}
									}
									
								// ACCELERATION
								} else if(d instanceof AccelerationData) {
									SensorTripleData sd = (SensorTripleData)d;
									
									lastAccData = sd;
									
									double effAcc = Math.sqrt(sd.x * sd.x + sd.y * sd.y + sd.z + sd.z); 
									
									Boolean isMoving = effAcc > 0.1;
									accIsMovingText.setText(isMoving.toString());
									
									if(isMoving) hasMoved = true;
									
								// COMPLEX ORIENTATION
								} else if(d instanceof OrientationData) {
									SensorTripleData sd = (SensorTripleData)d;
									
									orientText.setText(sd.x + ", " + sd.y + ", " + sd.z);
									
								// SIMPLE (CUBE) ORIENTATION
								} else if(d instanceof OrientationSimpleData) {
									SensorSingleData sd = (SensorSingleData)d;
									int deg = (int)sd.value;
									
									// it is NOT flat on the ground
									if(deg != -1) {
										if(deg >= 315 || deg < 45) {
											orientText.setText("DOWN");
										} else if(deg >= 45 && deg < 135) {
											orientText.setText("RIGHT");
										} else if(deg >= 135 && deg < 225) {
											orientText.setText("UP");
										} else if(deg >= 225 && deg < 315) {
											orientText.setText("LEFT");
										}
									}
									// it IS flat on the ground
									else {
										if(lastAccData.z < 0) {
											orientText.setText("TOP");
										} else {
											orientText.setText("BOTTOM");
										}
									}
								}
							}
						} catch(Exception ex) {
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

	private JLabel wifiDistanceLabel = new JLabel("WIFI distance: ");
	private JLabel wifiDistanceText = new JLabel("- - -");
	
	private JLabel accIsMovingLabel = new JLabel("Is moving?: ");
	private JLabel accIsMovingText = new JLabel(Boolean.FALSE.toString());
	
	private JLabel orientLabel = new JLabel("Orientation vector: ");
	private JLabel orientText = new JLabel("- - -");
	
	private JLabel metricDistanceLabel = new JLabel("Metric distance: ");
	private JLabel metricDistanceText = new JLabel("- - -");
	
	private void showGUI() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent winEvt) {
				quit = true;
			}
		});
		
		JPanel contentPane = new JPanel(new GridLayout(4,2));
		setContentPane(contentPane);
		setSize(800, 600);
		
		wifiDistanceLabel.setVisible(true);
		wifiDistanceText.setVisible(true);
		accIsMovingText.setVisible(true);
		accIsMovingLabel.setVisible(true);
		orientText.setVisible(true);
		orientLabel.setVisible(true);
		metricDistanceLabel.setVisible(true);
		metricDistanceText.setVisible(true);
		
		getContentPane().add(wifiDistanceLabel);
		getContentPane().add(wifiDistanceText);
		
		getContentPane().add(accIsMovingLabel);
		getContentPane().add(accIsMovingText);
		
		getContentPane().add(orientLabel);
		getContentPane().add(orientText);
		
		getContentPane().add(metricDistanceLabel);
		getContentPane().add(metricDistanceText);
		
		// display the window.
        //pack();
        setVisible(true);
	}
}

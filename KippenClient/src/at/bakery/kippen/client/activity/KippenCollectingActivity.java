package at.bakery.kippen.client.activity;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.OrientationEventListener;
import android.widget.TableLayout;
import android.widget.TextView;
import at.bakery.kippen.client.R;
import at.bakery.kippen.client.sensor.AccSensingTextOutput;
import at.bakery.kippen.client.sensor.BatterySensingNoOutput;
import at.bakery.kippen.client.sensor.OrientationSensingNoOutput;
import at.bakery.kippen.client.sensor.OrientationSensingNoOutputSimple;
import at.bakery.kippen.client.sensor.WifiSensingTableOutput;
import at.bakery.kippen.client.sensor.ShakeDetector;
import at.bakery.kippen.common.DataWithTimestampAndMac;
import at.bakery.kippen.common.data.ClientConfigData;
import at.bakery.kippen.common.data.ClientConfigData.ConfigType;
import at.bakery.kippen.common.data.PingData;

public class KippenCollectingActivity extends Activity {
	
	// the WIFI to which the server is connected and the server IP 
	
	
	// setting tomw 01
//		private static final String WIFI_ESSID = "UPC014580"; //StockEINS
//		private static final String WIFI_PWD = "CBZZVGQI"; //IchBinEinLustigesPasswort
//		private static final String SERVER_IP = "192.168.0.11"; //server ip
	
	// setting tomw 02
	private static final String WIFI_ESSID = "WirsingRouter5"; //StockEINS
	private static final String WIFI_PWD = "wirsinggo"; //IchBinEinLustigesPasswort
	private static final String SERVER_IP = "192.168.20.90"; //server ip
	
	// the client config as sent by the server
	private ClientConfigData config;

	// used for accelerometer based measurements
	private SensorManager senseMan;
	
	private Sensor accSense;
	private SensorEventListener accSensorListener;
	
	// the orientation 3D
	private Sensor orientSense;
	private SensorEventListener orientSensorListener;
	
	// the simple orientation without flat phone detection
	private OrientationEventListener orientSensorListenerSimple;
	
	// used for audio measurements
//	private AudioManager audioMan;
	
	// used for Wifi based measurements and for server connection
	private WifiManager wifiMan;
	private WifiSensingTableOutput wifiReceiver;
	
	// used for battery based measurements
	private BatterySensingNoOutput batteryReceiver;
	
	// used for shake detection
	private ShakeDetector shakeDetectorListener;
	
	// helper for building alert messages for the front end
	private static AlertDialog.Builder alertBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kippen_collecting);
		
		alertBuilder = new AlertDialog.Builder(this);

		// the sensor manager, providing all sensor services of the device
		Object tmpMan = getSystemService(Context.SENSOR_SERVICE);
		if(tmpMan == null) {
			showErrorDialog("No sensor services detected on device. It does not make sense to start, I rather quit myself.");
			this.finish();
		}
		senseMan = (SensorManager)tmpMan;
		
		// wifi specific services
		tmpMan = getSystemService(Context.WIFI_SERVICE);
		if(tmpMan == null) {
			showErrorDialog("No WIFI capabilities detected on device. Would not know how to talk to my master any other way. Too sad ...");
			this.finish();
		}
		wifiMan = (WifiManager)tmpMan;
		
		// connect to host network
		WifiConfiguration wc = new WifiConfiguration(); 
	    wc.SSID = "\"" + WIFI_ESSID + "\"";
	    if(WIFI_PWD != null) {
		    wc.preSharedKey  = "\"" + WIFI_PWD + "\"";
		    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP); 
		    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP); 
		    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK); 
		    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP); 
		    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP); 
		    wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		    wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
	    }
	    wc.status = WifiConfiguration.Status.ENABLED;       

	    // add and enable might fail e.g., in case it exists already in the manager
	    wifiMan.enableNetwork(wifiMan.addNetwork(wc), true);         
	    if(!wifiMan.reconnect()) {
	    	showErrorDialog("The device was not able to connect to the host network. Please check your AP and client-side wifi configurations! Quit for now ...");
			this.finish();
	    }
		
	    // create the client socket for data transmission
	    NetworkingTask networkTask = new NetworkingTask(SERVER_IP, 10000);
	    networkTask.start();
	    
	    // the config which is send to each client, i.e. all wifi's to measure
	    ClientConfigData config = new ClientConfigData();
	    Set<String> essids = new HashSet<String>();
	    essids.add(WIFI_ESSID); // measure this wifi ...
	    essids.add("OpenWrt"); // ... measure that wifi ...
	    config.setConfig(ConfigType.MEASURE_AP_ESSID, essids);
	    
	    // send a simple ping to the server to notify about our presence
	    networkTask.sendPackets(new DataWithTimestampAndMac(new PingData(), wifiMan.getConnectionInfo().getMacAddress()));
	    
		// the battery status measurement
		batteryReceiver = new BatterySensingNoOutput(networkTask, wifiMan.getConnectionInfo().getMacAddress());
		
		// the accelerometer and its GUI component
		accSense = senseMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		accSensorListener = new AccSensingTextOutput(
				(TextView)findViewById(R.id.lblXAcc),
				(TextView)findViewById(R.id.lblYAcc),
				(TextView)findViewById(R.id.lblZAcc),
				networkTask,
				wifiMan.getConnectionInfo().getMacAddress());
		
		// magnetic field
		orientSense = senseMan.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		orientSensorListener = new OrientationSensingNoOutput(networkTask, wifiMan.getConnectionInfo().getMacAddress());
		
		// ... simple one
		orientSensorListenerSimple = new OrientationSensingNoOutputSimple(getApplicationContext(), networkTask, wifiMan.getConnectionInfo().getMacAddress());
		
		// the wifi and its GUI component
		wifiMan.setWifiEnabled(true);
		wifiReceiver = new WifiSensingTableOutput(wifiMan, (TableLayout)findViewById(R.id.tblWifi), config, networkTask);
		
		// TODO: switch on and test
		shakeDetectorListener = new ShakeDetector((TextView)findViewById(R.id.lblShake), networkTask, wifiMan.getConnectionInfo().getMacAddress());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		senseMan.registerListener(shakeDetectorListener, accSense, Sensor.TYPE_ACCELEROMETER);
		
		senseMan.registerListener(accSensorListener, accSense, Sensor.TYPE_ACCELEROMETER);
		orientSensorListenerSimple.enable();
//		senseMan.registerListener(orientSensorListener, orientSense, Sensor.TYPE_ROTATION_VECTOR);
		registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiMan.startScan();
		registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
	
	@Override
	protected void onPause() {
		// do nothing on pause
		super.onPause();
		/*senseMan.unregisterListener(accSensorListener);
		senseMan.unregisterListener(orientSensorListener);
		unregisterReceiver(wifiReceiver);
		unregisterReceiver(batteryReceiver);*/
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.kippen_collecting, menu);
		return true;
	}

	private static void showErrorDialog(String messageId) {
		alertBuilder.setMessage(messageId);
		alertBuilder.setCancelable(false);
		alertBuilder.setPositiveButton("Ok",
			new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}
		);
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}
}

package at.bakery.kippen.client.activity;

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
import android.widget.TableLayout;
import android.widget.TextView;
import at.bakery.kippen.client.R;
import at.bakery.kippen.client.sensor.AccSensingTextOutput;
import at.bakery.kippen.client.sensor.BatterySensingNoOutput;
import at.bakery.kippen.client.sensor.WifiSensingTableOutput;
import at.bakery.kippen.common.DataWithTimestamp;
import at.bakery.kippen.common.SensorConfig;
import at.bakery.kippen.common.SensorConfig.SensorConfigType;
import at.bakery.kippen.common.data.PingData;

public class KippenCollectingActivity extends Activity {
	
	// the client config as sent by the server
	private SensorConfig config;

	// used for accelerometer based measurements
	private SensorManager senseMan;
	private Sensor accSense;
	private SensorEventListener accSensorListener;
	
	// used for audio measurements
//	private AudioManager audioMan;
	
	// used for Wifi based measurements and for server connection
	private WifiManager wifiMan;
	private WifiSensingTableOutput wifiReceiver;
	
	// used for battery based measurements
	private BatterySensingNoOutput batteryReceiver;
	
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
	    wc.SSID = "\"StockEINS\""; 
	    wc.preSharedKey  = "\"IchBinEinLustigesPasswort\"";
	    wc.status = WifiConfiguration.Status.ENABLED;         
	    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP); 
	    wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP); 
	    wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK); 
	    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP); 
	    wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP); 
	    wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
	    wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

	    // add and enable might fail e.g., in case it exists already in the manager
	    wifiMan.enableNetwork(wifiMan.addNetwork(wc), true);         
	    if(!wifiMan.reconnect()) {
	    	showErrorDialog("The device was not able to connect to the host network. Please check your AP and client-side wifi configurations! Quit for now ...");
			this.finish();
	    }
		
	    NetworkingTask networkTask = new NetworkingTask();
	    networkTask.start();
	    
	    networkTask.sendPackets(new DataWithTimestamp(new PingData()));
	    
		// TODO protocol as follows: [action (from which side)]
		// TODO connect (c) -> receive UID/config (s) -> send data (c)
		
		/* FIXME test data */
		config = new SensorConfig();
		config.setConfig(SensorConfigType.MEASURE_AP_ESSID, "StockEINS");
		
		// the battery status measurement
		batteryReceiver = new BatterySensingNoOutput(networkTask);
		
		// the accelerometer and its GUI component
		accSense = senseMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		accSensorListener = new AccSensingTextOutput(
				(TextView)findViewById(R.id.lblXAcc),
				(TextView)findViewById(R.id.lblYAcc),
				(TextView)findViewById(R.id.lblZAcc),
				networkTask);
		
		// the wifi and its GUI component
		wifiMan.setWifiEnabled(true);
		wifiReceiver = new WifiSensingTableOutput(wifiMan, (TableLayout)findViewById(R.id.tblWifi), config, networkTask);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		senseMan.registerListener(accSensorListener, accSense, Sensor.TYPE_ACCELEROMETER);
		registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiMan.startScan();
		registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	@Override
	protected void onPause() {
		super.onPause();
		senseMan.unregisterListener(accSensorListener);
		unregisterReceiver(wifiReceiver);
		unregisterReceiver(batteryReceiver);
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

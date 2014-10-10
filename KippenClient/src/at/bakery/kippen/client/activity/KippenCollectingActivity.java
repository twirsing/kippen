package at.bakery.kippen.client.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import at.bakery.kippen.client.R;
import at.bakery.kippen.client.sensor.BatterySensing;
import at.bakery.kippen.client.sensor.MotionSensing;
import at.bakery.kippen.common.data.PingData;

/*
 * TODO all-in-one sensor listener
 * TODO deposit job for activity.recreate on any error (e.g. network task)
 */

public class KippenCollectingActivity extends Activity {

	// the WIFI to which the server is connected and the server IP

	// setting tomw
	/*
	 * private static final String WIFI_ESSID = "UPC014580"; //StockEINS private
	 * static final String WIFI_PWD = "CBZZVGQI"; //IchBinEinLustigesPasswort
	 * private static final String SERVER_IP = "192.168.0.11"; //server ip
	 */

	// setting matthias
//	private static final String WIFI_ESSID = "UPC015668";
//	private static final String WIFI_PWD = "IKSSEAAG"; 
//	private static final String SERVER_IP = "192.168.0.26"; // server ip

	// setting StockEINS
//	 private static final String WIFI_ESSID = "StockEINS"; //StockEINS
//	 private static final String WIFI_PWD = "IchBinEinLustigesPasswort";
//	 private static final String SERVER_IP = "192.168.0.109"; //server ip
	//
	// setting tomt
	 private static final String WIFI_ESSID = "JulesWinnfield";
	 private static final String WIFI_PWD = "wuzikrabuzi";
	 private static final String SERVER_IP = "192.168.1.141";

	// access to sensors
	private SensorManager senseMan;

	// used for wifi based measurements and for server connection
	private WifiManager wifiMan;
	// INACTIVE private WifiSensing wifiReceiver;

	// used for battery based measurements
	private BatterySensing batteryReceiver;

	// used for accelerometer based measurements
	private Sensor accSense;
	
	// move measurements
	private Sensor moveSenseMagnetic;
	
	private static MotionSensing sensorListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kippen_collecting);

		// the sensor manager, providing all sensor services of the device
		Object tmpMan = getSystemService(Context.SENSOR_SERVICE);
		if (tmpMan == null) {
			Log.e("KIPPEN", "No sensor services detected on device. It does not make sense to start, I rather quit myself.");
			this.finish();
		}
		senseMan = (SensorManager) tmpMan;

		// wifi specific services
		tmpMan = getSystemService(Context.WIFI_SERVICE);
		if (tmpMan == null) {
			Log.e("KIPPEN", "No WIFI capabilities detected on device. Would not know how to talk to my master any other way. Too sad ...");
			this.finish();
		}
		wifiMan = (WifiManager) tmpMan;

		// connect to host network
		WifiConfiguration wc = new WifiConfiguration();
		wc.SSID = "\"" + WIFI_ESSID + "\"";
		if (WIFI_PWD != null) {
			wc.preSharedKey = "\"" + WIFI_PWD + "\"";
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		}
		wc.status = WifiConfiguration.Status.ENABLED;

		// add and enable might fail e.g., in case it exists already in the
		// manager
		wifiMan.enableNetwork(wifiMan.addNetwork(wc), true);
		if (!wifiMan.reconnect()) {
			Log.e("KIPPEN", "The device was not able to connect to the host network. Please check your AP and client-side wifi configurations! Quit for now ...");
			this.finish();
		}

		// store the client id (MAC) for re-use
		String clientId = wifiMan.getConnectionInfo().getMacAddress();

		// create the client socket for data transmission
		NetworkingTask.setup(SERVER_IP, 10001, clientId);
		NetworkingTask networkTask = NetworkingTask.getInstance();
		networkTask.start();

		// FIXME remove or adjust, since hard-coded
		// the config which is send to each client, i.e. all wifi's to measure
		// INACTIVE Set<String> essids = new HashSet<String>();
		// INACTIVE essids.add(WIFI_ESSID); // measure this wifi ...
		// INACTIVE essids.add("WirsingRouter5"); // ... measure that wifi ...

		// INACTIVE ClientConfigData config = new ClientConfigData();
		// INACTIVE config.setConfig(ConfigType.MEASURE_AP_ESSID, essids);

		// send a simple ping to the server to notify about our presence
		networkTask.sendPacket(new PingData());

		// the battery status measurement
		batteryReceiver = new BatterySensing();

		// the accelerometer
		accSense = senseMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// move sensing via orientation and acceleration (TODO)
		moveSenseMagnetic = senseMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		// general sensor listener
		sensorListener = new MotionSensing();

		// the wifi measuring sensor
		// INACTIVE wifiMan.setWifiEnabled(true);
		// INACTIVE wifiReceiver = new WifiSensing(wifiMan, config);

		Log.i("KIPPEN", "Android client activity created.");
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		senseMan.registerListener(sensorListener, accSense, 100000);
		senseMan.registerListener(sensorListener, moveSenseMagnetic, 100000);
		
		registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		NetworkingTask.getInstance().quit();
		Log.d("KIPPEN", "Closing client");
	}
	
	
}

package at.bakery.kippen.client.activity;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import at.bakery.kippen.client.sensor.AccelerationSensing;
import at.bakery.kippen.client.sensor.BatterySensing;
import at.bakery.kippen.client.sensor.ISensorDataCache;
import at.bakery.kippen.client.sensor.MoveSensing;
import at.bakery.kippen.client.sensor.OrientationSensing;
import at.bakery.kippen.client.sensor.CubeOrientationSensing;
import at.bakery.kippen.client.sensor.ShakeSensing;
import at.bakery.kippen.client.sensor.WifiSensing;
import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.ClientConfigData;
import at.bakery.kippen.common.data.ClientConfigData.ConfigType;
import at.bakery.kippen.common.data.PingData;

public class KippenCollectingActivity extends Activity {

	// the WIFI to which the server is connected and the server IP

	
	//BACKSTUBE
//	 private static final String WIFI_ESSID = "WBackstube"; //StockEINS
//	 private static final String WIFI_PWD = "ibclibcl";
//	 //IchBinEinLustigesPasswort
//	 private static final String SERVER_IP = "192.168.1.20"; //server ip

	// setting tomw
	// private static final String WIFI_ESSID = "UPC014580"; //StockEINS
	// private static final String WIFI_PWD = "CBZZVGQI";
	// //IchBinEinLustigesPasswort
	// private static final String SERVER_IP = "192.168.0.11"; //server ip

	// setting bakery
	 private static final String WIFI_ESSID = "StockEINS"; // StockEINS
	 private static final String WIFI_PWD = "IchBinEinLustigesPasswort"; //
//	 IchBinEinLustigesPasswort
	 private static final String SERVER_IP = "192.168.1.20"; // server ip

	// setting bakery, reset router
	// private static final String WIFI_ESSID = "INTELLINET_AP";
	// private static final String WIFI_PWD = null;
	// private static final String SERVER_IP = "10.21.11.109";
	//

	// setting tomt
	// private static final String WIFI_ESSID = "JulesWinnfield";
	// private static final String WIFI_PWD = "wuzikrabuzi";
	// private static final String SERVER_IP = "192.168.1.141";

	// the client config as sent by the server
	// private ClientConfigData config;

	// used for accelerometer based measurements
	private SensorManager senseMan;

	private Sensor accSense;
	private static AccelerationSensing accSensorListener;

	// the orientation 3D
	private Sensor orientSense;
	private SensorEventListener orientSensorListener;

	// the simple orientation without flat phone detection
	private OrientationEventListener orientSensorListenerSimple;

	// used for audio measurements
	// private AudioManager audioMan;

	// used for Wifi based measurements and for server connection
	private WifiManager wifiMan;
	private WifiSensing wifiReceiver;

	// used for battery based measurements
	private BatterySensing batteryReceiver;

	// used for shake detection
	private Sensor shakeSense;
	private ShakeSensing shakeDetectorListener;

	// move measurements
	private Sensor moveSenseLinearAcc;
	private Sensor moveSenseMagnetic;
	private Sensor moveSenseGravity;
	private MoveSensing moveSensorListener;

	// helper for building alert messages for the front end
	private static AlertDialog.Builder alertBuilder;

	public static AbstractData getCachedSensorData(
			Class<? extends ISensorDataCache> cache) {
		if (cache == accSensorListener.getClass()) {
			return accSensorListener.getCacheData();
		}

		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kippen_collecting);

		alertBuilder = new AlertDialog.Builder(this);

		// the sensor manager, providing all sensor services of the device
		Object tmpMan = getSystemService(Context.SENSOR_SERVICE);
		if (tmpMan == null) {
			showErrorDialog("No sensor services detected on device. It does not make sense to start, I rather quit myself.");
			this.finish();
		}
		senseMan = (SensorManager) tmpMan;

		// wifi specific services
		tmpMan = getSystemService(Context.WIFI_SERVICE);
		if (tmpMan == null) {
			showErrorDialog("No WIFI capabilities detected on device. Would not know how to talk to my master any other way. Too sad ...");
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
			wc.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			wc.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		}
		wc.status = WifiConfiguration.Status.ENABLED;

		// add and enable might fail e.g., in case it exists already in the
		// manager
		wifiMan.enableNetwork(wifiMan.addNetwork(wc), true);
		if (!wifiMan.reconnect()) {
			showErrorDialog("The device was not able to connect to the host network. Please check your AP and client-side wifi configurations! Quit for now ...");
			this.finish();
		}

		// store the client id (MAC) for re-use
		String clientId = wifiMan.getConnectionInfo().getMacAddress();

		// create the client socket for data transmission
		NetworkingTask.setup(SERVER_IP, 10000, clientId);
		NetworkingTask networkTask = NetworkingTask.getInstance();
		networkTask.start();

		// FIXME remove or adjust, since hard-coded
		// the config which is send to each client, i.e. all wifi's to measure
		Set<String> essids = new HashSet<String>();
		essids.add(WIFI_ESSID); // measure this wifi ...
		essids.add("WirsingRouter5"); // ... measure that wifi ...

		ClientConfigData config = new ClientConfigData();
		config.setConfig(ConfigType.MEASURE_AP_ESSID, essids);

		// send a simple ping to the server to notify about our presence
		networkTask.sendPackets(new PingData());

		// the battery status measurement
		batteryReceiver = new BatterySensing();

		// the accelerometer and its GUI component
		accSense = senseMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		accSensorListener = new AccelerationSensing();

		// orientation field
		orientSense = senseMan.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		// orientSensorListener = new OrientationSensing();

		// move sensing via orientation and acceleration (TODO)
		moveSenseLinearAcc = senseMan
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		moveSenseMagnetic = senseMan
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		moveSenseGravity = senseMan.getDefaultSensor(Sensor.TYPE_GRAVITY);
		moveSensorListener = new MoveSensing();

		// ... simple one
		orientSensorListenerSimple = new CubeOrientationSensing(
				getApplicationContext());

		// the wifi and its GUI component
		wifiMan.setWifiEnabled(true);
		wifiReceiver = new WifiSensing(wifiMan, config);

		// the shake detector
		// TODO: switch on and test
		shakeSense = senseMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		shakeDetectorListener = new ShakeSensing();
	}

	@Override
	protected void onResume() {
		super.onResume();

		System.out.println(wifiMan.getConnectionInfo().getMacAddress());

		senseMan.registerListener(shakeDetectorListener, shakeSense,
				Sensor.TYPE_ACCELEROMETER);

		senseMan.registerListener(accSensorListener, accSense,
				Sensor.TYPE_ACCELEROMETER);

		orientSensorListenerSimple.enable();
		// senseMan.registerListener(orientSensorListener, orientSense,
		// Sensor.TYPE_ROTATION_VECTOR);

		registerReceiver(wifiReceiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiMan.startScan();

//		senseMan.registerListener(moveSensorListener, moveSenseLinearAcc,
//				Sensor.TYPE_LINEAR_ACCELERATION);
//		senseMan.registerListener(moveSensorListener, moveSenseMagnetic,
//				Sensor.TYPE_MAGNETIC_FIELD);
//		senseMan.registerListener(moveSensorListener, moveSenseGravity,
//				Sensor.TYPE_GRAVITY);

		// registerReceiver(batteryReceiver, new
		// IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	@Override
	protected void onPause() {
		// do nothing on pause
		super.onPause();
		/*
		 * senseMan.unregisterListener(accSensorListener);
		 * senseMan.unregisterListener(orientSensorListener);
		 * unregisterReceiver(wifiReceiver);
		 * unregisterReceiver(batteryReceiver);
		 */
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
		alertBuilder.setPositiveButton("Ok", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}
}

package at.bakery.kippen.client.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.OrientationEventListener;
import at.bakery.kippen.client.R;
import at.bakery.kippen.client.sensor.AccelerationSensing;
import at.bakery.kippen.client.sensor.BarrelOrientationSensing;
import at.bakery.kippen.client.sensor.CubeOrientationSensing;
import at.bakery.kippen.client.sensor.ISensorDataCache;
import at.bakery.kippen.client.sensor.MoveSensing;
import at.bakery.kippen.client.sensor.ShakeSensing;
import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.PingData;

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
	 private static final String WIFI_ESSID = "StockEINS"; //StockEINS
	 private static final String WIFI_PWD = "IchBinEinLustigesPasswort";
	 private static final String SERVER_IP = "192.168.0.100"; //server ip
	//
	// setting tomt
	// private static final String WIFI_ESSID = "JulesWinnfield";
	// private static final String WIFI_PWD = "wuzikrabuzi";
	// private static final String SERVER_IP = "192.168.1.141";

	// access to sensors
	private SensorManager senseMan;

	// used for accelerometer based measurements
	private Sensor accSense;
	private static AccelerationSensing accSensorListener;

	// the orientation 3D
	// INACTIVE private Sensor orientSense;
	// INACTIVE private SensorEventListener orientSensorListener;

	// the simple orientation without flat phone detection
	private OrientationEventListener cubeOrientSensorListener;

	// the barrel orientation
	private OrientationEventListener barrelOrientSensorListener;

	// used for wifi based measurements and for server connection
	private WifiManager wifiMan;
	// INACTIVE private WifiSensing wifiReceiver;

	// used for battery based measurements
	// INACTIVE private BatterySensing batteryReceiver;

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

	public static AbstractData getCachedSensorData(Class<? extends ISensorDataCache> cache) {
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
			showErrorDialog("The device was not able to connect to the host network. Please check your AP and client-side wifi configurations! Quit for now ...");
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
		networkTask.sendPackets(new PingData());

		// the battery status measurement
		// INACTIVE batteryReceiver = new BatterySensing();

		// the accelerometer
		accSense = senseMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		accSensorListener = new AccelerationSensing();

		// orientation field
		// INACTIVE orientSense =
		// senseMan.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		// INACTIVE orientSensorListener = new OrientationSensing();

		// move sensing via orientation and acceleration (TODO)
		moveSenseLinearAcc = senseMan.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		moveSenseMagnetic = senseMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		moveSenseGravity = senseMan.getDefaultSensor(Sensor.TYPE_GRAVITY);
		moveSensorListener = new MoveSensing();

		// ... simple cube side change listener
		cubeOrientSensorListener = new CubeOrientationSensing(getApplicationContext());

		// ... simple barrel roll degree change listener
		barrelOrientSensorListener = new BarrelOrientationSensing(getApplicationContext());

		// the wifi measuring sensor
		// INACTIVE wifiMan.setWifiEnabled(true);
		// INACTIVE wifiReceiver = new WifiSensing(wifiMan, config);

		// the shake detector
		shakeSense = senseMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		shakeDetectorListener = new ShakeSensing();
	}

	@Override
	protected void onResume() {
		super.onResume();

		senseMan.registerListener(moveSensorListener, moveSenseLinearAcc, SensorManager.SENSOR_DELAY_FASTEST);
		senseMan.registerListener(moveSensorListener, moveSenseMagnetic, SensorManager.SENSOR_DELAY_FASTEST);
		senseMan.registerListener(moveSensorListener, moveSenseGravity, SensorManager.SENSOR_DELAY_FASTEST);

		senseMan.registerListener(shakeDetectorListener, shakeSense, SensorManager.SENSOR_DELAY_FASTEST);

		senseMan.registerListener(accSensorListener, accSense, SensorManager.SENSOR_DELAY_FASTEST);

		cubeOrientSensorListener.enable();

		barrelOrientSensorListener.enable();

		// INACTIVE senseMan.registerListener(orientSensorListener, orientSense,
		// SensorManager.SENSOR_DELAY_FASTEST);

		// INACTIVE registerReceiver(wifiReceiver, new
		// IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		// INACTIVE wifiMan.startScan();

		// INACTIVE registerReceiver(batteryReceiver, new
		// IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	@Override
	protected void onPause() {
		// do nothing on pause
		super.onPause();
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

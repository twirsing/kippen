package at.bakery.kippen.client.sensor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.TableRow;
import android.widget.TextView;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.client.activity.NetworkingTask;
import at.bakery.kippen.common.data.ClientConfigData;
import at.bakery.kippen.common.data.ClientConfigData.ConfigType;
import at.bakery.kippen.common.data.WifiLevelsData;

public class WifiSensing extends BroadcastReceiver {

	// used for accessing Wifi scan results
	private WifiManager wifiMan;
	
	// the (optional) config object
	private ClientConfigData config;
	
	// the measurements, here its Wifi and its corresponding timestamp (i.e. scan time)
	private WifiLevelsData wifiLevels;
	
	private Lock updateLock = new ReentrantLock();
	
	private INetworking net = NetworkingTask.getInstance();

	
	public WifiSensing(WifiManager wifiMan) {
		this(wifiMan, new ClientConfigData());
	}
	
	public WifiSensing(WifiManager wifiMan, ClientConfigData config) {
		this.wifiMan = wifiMan;
		this.config = config;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// get the results
		List<ScanResult> results = wifiMan.getScanResults();
		
		updateLock.lock();
		
		wifiLevels = new WifiLevelsData();
		
		// for each scan results make an entry and store the signal level
		Object wconf = config.getConfig(ConfigType.MEASURE_AP_ESSID);
		for (ScanResult result : results) {
			if(!(result.SSID.equals(wconf) ||
					(wconf instanceof Collection<?> && ((Collection<?>)wconf).contains(result.SSID)))) {
				continue;
			}
			
			TableRow resRow = new TableRow(context);
			
			TextView essid = new TextView(context);
			essid.setText("" + result.SSID + " (" + result.BSSID + ")");
			
			TextView level = new TextView(context);
			level.setText("" + result.level);
			
			resRow.addView(essid);
			resRow.addView(level);
			
			// add the BSSID as SSID is not unique
			wifiLevels.setNetwork(result.BSSID, result.level);
		}
		
		if(wifiLevels.hasNetworks()) {
			net.sendPacket(wifiLevels);
		}
		
		updateLock.unlock();
		
		// initiate the next scan
		wifiMan.startScan();	 // FIXME do this periodically in main!!			
	}
	
}
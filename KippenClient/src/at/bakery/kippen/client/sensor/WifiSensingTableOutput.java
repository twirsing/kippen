package at.bakery.kippen.client.sensor;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import at.bakery.kippen.client.R;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.common.DataWithTimestamp;
import at.bakery.kippen.common.SensorConfig;
import at.bakery.kippen.common.SensorConfig.SensorConfigType;
import at.bakery.kippen.common.data.WifiLevelsData;

public class WifiSensingTableOutput extends BroadcastReceiver {

	// used for accessing Wifi scan results
	private WifiManager wifiMan;
	
	// the table for user interface output
	private TableLayout table;
	
	// the (optional) config object
	private SensorConfig config;
	
	// the measurements, here its Wifi and its corresponding timestamp (i.e. scan time)
	private WifiLevelsData wifiLevels;
	private long updateTime = -1;
	
	private Lock updateLock = new ReentrantLock();
	
	private INetworking net;
	
	public WifiSensingTableOutput(WifiManager wifiMan, TableLayout table, INetworking net) {
		this(wifiMan, table, new SensorConfig(), net);
	}
	
	public WifiSensingTableOutput(WifiManager wifiMan, TableLayout table, SensorConfig config, INetworking net) {
		this.wifiMan = wifiMan;
		this.table = table;
		this.config = config;
		this.net = net;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// get the results
		List<ScanResult> results = wifiMan.getScanResults();
		
		// remove all table contents (except the header row) and measurements
		table.removeViews(1, table.getChildCount()-1);
		wifiLevels = new WifiLevelsData();
		
		updateLock.lock();
		
		// for each scan results make an entry and store the signal level
		for (ScanResult result : results) {
			if(!result.SSID.equals(config.getConfig(SensorConfigType.MEASURE_AP_ESSID))) {
				continue;
			}
			
			TableRow resRow = new TableRow(context);
			
			TextView essid = new TextView(context);
			essid.setText("" + result.SSID);
			
			TextView level = new TextView(context);
			level.setText("" + result.level);
			
			resRow.addView(essid);
			resRow.addView(level);
			
			table.addView(resRow);
			
			// add the BSSID as SSID is not unique
			wifiLevels.put(result.BSSID, result.level);
		}
		updateTime = System.nanoTime();
		
		net.sendPackets(new DataWithTimestamp(wifiLevels, updateTime));
		
		updateLock.unlock();
			
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		
		// initiate the next scan
		wifiMan.startScan();	 // FIXME do this periodically in main!!			
	}
	
}
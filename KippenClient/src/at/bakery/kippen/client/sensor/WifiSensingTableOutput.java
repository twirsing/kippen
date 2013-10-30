package at.bakery.kippen.client.sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import at.bakery.kippen.common.ISensorData;
import at.bakery.kippen.common.SensorConfig;
import at.bakery.kippen.common.SensorConfig.SensorConfigType;

public class WifiSensingTableOutput extends BroadcastReceiver implements ISensorData {

	// used for accessing Wifi scan results
	private WifiManager wifiMan;
	
	// the table for user interface output
	private TableLayout table;
	
	// the (optional) config object
	private SensorConfig config;
	
	// the measurements, here its Wifi and its corresponding timestamp (i.e. scan time)
	private Map<String, Integer> wifiLevels = new HashMap<String, Integer>();
	private long updateTime = -1;
	
	private Lock updateLock = new ReentrantLock();
	
	public WifiSensingTableOutput(WifiManager wifiMan, TableLayout table) {
		this(wifiMan, table, new SensorConfig());
	}
	
	public WifiSensingTableOutput(WifiManager wifiMan, TableLayout table, SensorConfig config) {
		this.wifiMan = wifiMan;
		this.table = table;
		this.config = config;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// get the results
		List<ScanResult> results = wifiMan.getScanResults();
		
		// remove all table contents (except the header row) and measurements
		table.removeViews(1, table.getChildCount()-1);
		wifiLevels.clear();
		
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
			wifiLevels.put(result.SSID, result.level);
			updateTime = System.nanoTime();
		}
		
		updateLock.unlock();
						
		// initiate the next scan
		wifiMan.startScan();				
	}

	@Override
	public DataWithTimestamp getData() {
		updateLock.lock();
		
		DataWithTimestamp ret = new DataWithTimestamp(wifiLevels, updateTime);
		
		updateLock.unlock();
		
		return ret;
	}
}

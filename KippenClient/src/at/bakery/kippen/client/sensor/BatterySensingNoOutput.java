package at.bakery.kippen.client.sensor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import at.bakery.kippen.common.ISensorData;
import at.bakery.kippen.common.data.BatteryData;

public class BatterySensingNoOutput extends BroadcastReceiver implements ISensorData {

	private float capacity = -1;
	private boolean charging = false;
	
	private long updateTime = -1;
	
	private Lock updateLock = new ReentrantLock();
	
	@Override
	public DataWithTimestamp getData() {
		updateLock.lock();
		
		DataWithTimestamp ret = new DataWithTimestamp(
				new BatteryData(charging, capacity), 
				updateTime);
		
		updateLock.unlock();
		
		return ret;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		int lvl = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		float scl = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		updateLock.lock();
		
		capacity = lvl / scl;
		charging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) > 0;
		
		updateTime = System.nanoTime();
		
		updateLock.unlock();
	}

}

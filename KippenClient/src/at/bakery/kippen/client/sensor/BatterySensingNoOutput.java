package at.bakery.kippen.client.sensor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.common.DataWithTimestamp;
import at.bakery.kippen.common.data.BatteryData;

public class BatterySensingNoOutput extends BroadcastReceiver {

	private float capacity = -1;
	private boolean charging = false;
	
	private long updateTime = -1;
	
	private Lock updateLock = new ReentrantLock();
	
	private INetworking net;
	
	public BatterySensingNoOutput(INetworking net) {
		this.net = net;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		int lvl = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		float scl = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		updateLock.lock();
		
		capacity = lvl / scl;
		charging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) > 0;
		
		updateTime = System.nanoTime();
		
		net.sendPackets(new DataWithTimestamp(
				new BatteryData(charging, capacity), 
				updateTime));
		
		updateLock.unlock();
	}

}

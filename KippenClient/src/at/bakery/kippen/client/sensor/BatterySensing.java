package at.bakery.kippen.client.sensor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.client.activity.NetworkingTask;
import at.bakery.kippen.common.data.BatteryData;

public class BatterySensing extends BroadcastReceiver {

	private float capacity = -1;
	private boolean charging = false;
	
	private Lock updateLock = new ReentrantLock();
	
	private INetworking net = NetworkingTask.getInstance();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		int lvl = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		float scl = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		updateLock.lock();
		
		capacity = lvl / scl;
		charging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) > 0;
		
		net.sendPackets(new BatteryData(charging, capacity));
		
		updateLock.unlock();
	}

}

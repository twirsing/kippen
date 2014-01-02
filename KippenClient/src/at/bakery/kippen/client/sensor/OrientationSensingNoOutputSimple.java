package at.bakery.kippen.client.sensor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.view.OrientationEventListener;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.common.DataWithTimestamp;
import at.bakery.kippen.common.data.OrientationSimpleData;
import at.bakery.kippen.common.data.SensorSingleData;

public class OrientationSensingNoOutputSimple extends OrientationEventListener {

	private Lock updateLock = new ReentrantLock();
	
	private long updateTime = -1;
	
	private INetworking net;
	
	public OrientationSensingNoOutputSimple(Context context, INetworking net) {
		super(context);
		this.net = net;
	}

	@Override
	public void onOrientationChanged(int orientation) {
		SensorSingleData t = new OrientationSimpleData(orientation);
		
		updateLock.lock();
		
		updateTime = System.nanoTime();
			
		net.sendPackets(new DataWithTimestamp(t, updateTime));
		
		updateLock.unlock();
	}

}

package at.bakery.kippen.client.sensor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.view.OrientationEventListener;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.client.activity.NetworkingTask;
import at.bakery.kippen.common.data.DirectionOrientationData;
import at.bakery.kippen.common.data.SensorSingleData;

public class OrientationSensingNoOutputSimple extends OrientationEventListener {

	private Lock updateLock = new ReentrantLock();
	
	private INetworking net = NetworkingTask.getInstance();
	
	public OrientationSensingNoOutputSimple(Context context) {
		super(context);
	}

	@Override
	public void onOrientationChanged(int orientation) {
		SensorSingleData t = new DirectionOrientationData(orientation);
		
		updateLock.lock();
		
		net.sendPackets(t);
		
		updateLock.unlock();
	}

}

package at.bakery.kippen.client.sensor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.view.OrientationEventListener;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.client.activity.KippenCollectingActivity;
import at.bakery.kippen.client.activity.NetworkingTask;
import at.bakery.kippen.common.data.BarrelOrientationData;
import at.bakery.kippen.common.data.CubeOrientationData;
import at.bakery.kippen.common.data.CubeOrientationData.Orientation;
import at.bakery.kippen.common.data.SensorTripleData;

public class BarrelOrientationSensing extends OrientationEventListener {

	private Lock updateLock = new ReentrantLock();
	private INetworking net = NetworkingTask.getInstance();
	
	private static final int MAX_DEGREES = 10 * 360;
	private int degrees = 0;
	private int lastAbsDegrees = 0;
	
	public BarrelOrientationSensing(Context context) {
		super(context);
	}

	@Override
	public void onOrientationChanged(int deg) {
		if(deg < 0) return;
		
		// change from last change to now
		int deltaDeg = deg - lastAbsDegrees;
		
		// all in range, so add up or lower
		if(Math.abs(deltaDeg) < 180) {
			degrees += deltaDeg;
		} 
		// positive wrap bounds
		else if(deltaDeg > 0) {
			degrees += (360 - deg + lastAbsDegrees);
		}
		// negative wrap bounds
		else if(deltaDeg < 0) {
			degrees += (deg + 360 - lastAbsDegrees);
		}
		
		// reset
		lastAbsDegrees = deg;
		
		// bring into bounds
		if(degrees < 0) degrees = 0;
		else if(degrees > MAX_DEGREES) degrees = MAX_DEGREES;
		
		System.out.println("Barrel orientation " + degrees + "/" + MAX_DEGREES);
		
		BarrelOrientationData orientationData = new BarrelOrientationData((double)degrees / MAX_DEGREES); 
		
		updateLock.lock();
		
		net.sendPackets(orientationData);
		
		updateLock.unlock();
	}

}

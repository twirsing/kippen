package at.bakery.kippen.client.sensor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.util.Log;
import android.view.OrientationEventListener;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.client.activity.KippenCollectingActivity;
import at.bakery.kippen.client.activity.NetworkingTask;
import at.bakery.kippen.common.data.BarrelOrientationData;
import at.bakery.kippen.common.data.CubeOrientationData;
import at.bakery.kippen.common.data.CubeOrientationData.Orientation;
import at.bakery.kippen.common.data.SensorTripleData;

public class OrientationSensing extends OrientationEventListener {

	private Lock updateLock = new ReentrantLock();
	private INetworking net = NetworkingTask.getInstance();
	
	/* -------------------------------------------
	 * CUBE
	 * ------------------------------------------- */
	// doesn't need anything
	
	/* -------------------------------------------
	 * BARREL
	 * ------------------------------------------- */
	private static final int MAX_DEGREES = 10 * 360;
	private int degrees = 0;
	private int lastAbsDegrees = 0;
	
	public OrientationSensing(Context context) {
		super(context, 100000);
	}
	
	private void handleCube(int deg) {
		CubeOrientationData orientationData;
		
		if (deg != -1) {
			if (deg >= 315 || deg < 45) {
				orientationData = new CubeOrientationData(Orientation.BACK);
			} else if (deg >= 45 && deg < 135) {
				orientationData = new CubeOrientationData(Orientation.RIGHT);
			} else if (deg >= 135 && deg < 225) {
				orientationData = new CubeOrientationData(Orientation.FRONT);
			} else if (deg >= 225 && deg < 315) {
				orientationData = new CubeOrientationData(Orientation.LEFT);
			} else {
				orientationData = new CubeOrientationData(Orientation.UNKNOWN);
			}
		}
		// it IS flat on the ground
		else {
			SensorTripleData accData = KippenCollectingActivity.getCacheAccelerationData();
			if(accData == null) {
				orientationData = new CubeOrientationData(Orientation.UNKNOWN);
			} else if (accData.getZ() < 0) {
				orientationData = new CubeOrientationData(Orientation.BOTTOM);
			} else {
				orientationData = new CubeOrientationData(Orientation.TOP);
			}
		}
		
		updateLock.lock();
		net.sendPackets(orientationData);
		updateLock.unlock();
		
		Log.d("KIPPEN", "CUBE: " + orientationData);
	}
	
	private void handleBarrel(int deg) {
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
		
		BarrelOrientationData orientationData = new BarrelOrientationData((double)degrees / MAX_DEGREES); 
		
		updateLock.lock();
		net.sendPackets(orientationData);
		updateLock.unlock();
		
		Log.d("KIPPEN", "BARREL: " + degrees + "/" + MAX_DEGREES);
	}

	@Override
	public void onOrientationChanged(int deg) {
		Log.d("KIPPEN", "ORIENT changed");
		
		handleCube(deg);
		handleBarrel(deg);
	}

	@Override
	public boolean canDetectOrientation() {
		return true;
	}
}

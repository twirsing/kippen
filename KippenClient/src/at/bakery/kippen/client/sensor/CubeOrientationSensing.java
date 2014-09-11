package at.bakery.kippen.client.sensor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.view.OrientationEventListener;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.client.activity.KippenCollectingActivity;
import at.bakery.kippen.client.activity.NetworkingTask;
import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.CubeOrientationData;
import at.bakery.kippen.common.data.CubeOrientationData.Orientation;
import at.bakery.kippen.common.data.DirectionOrientationData;
import at.bakery.kippen.common.data.SensorSingleData;
import at.bakery.kippen.common.data.SensorTripleData;

public class CubeOrientationSensing extends OrientationEventListener {

	private Lock updateLock = new ReentrantLock();
	
	private INetworking net = NetworkingTask.getInstance();
	
	public CubeOrientationSensing(Context context) {
		super(context);
	}

	@Override
	public void onOrientationChanged(int deg) {
		System.out.println("Cube orientation " + deg);
		
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
			SensorTripleData accData = (SensorTripleData)KippenCollectingActivity.getCachedSensorData(AccelerationSensing.class);
			if(accData == null) {
				orientationData = new CubeOrientationData(Orientation.UNKNOWN);
			}
			
			if (accData.getZ() < 0) {
				orientationData = new CubeOrientationData(Orientation.BOTTOM);
			} else {
				orientationData = new CubeOrientationData(Orientation.TOP);
			}
		}
		
		updateLock.lock();
		
		net.sendPackets(orientationData);
		
		updateLock.unlock();
	}

}

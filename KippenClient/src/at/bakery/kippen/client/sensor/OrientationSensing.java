package at.bakery.kippen.client.sensor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.client.activity.NetworkingTask;
import at.bakery.kippen.common.data.AnalogOrientationData;
import at.bakery.kippen.common.data.SensorTripleData;

public class OrientationSensing implements SensorEventListener {

	private Lock updateLock = new ReentrantLock();
	
	private INetworking net = NetworkingTask.getInstance();
	
	@Override
	public void onSensorChanged(SensorEvent se) {
		if(se.sensor.getType() != Sensor.TYPE_ROTATION_VECTOR) {
			return;
		}
		
		if(se.values.length < 3) {
			return;
		}
		
		// the magnetic field
//		double magField = Math.sqrt(se.values[0]*se.values[0] + se.values[1]*se.values[1] + se.values[2]*se.values[2]);
		
		// the actual orientation
		float[] rotMat = new float[9];
//		float[] incMat = new float[9];
		float[] orientMat = new float[3];
		//SensorManager.getRotationMatrix(rotMat, incMat, new float[]{0, 0, SensorManager.GRAVITY_EARTH}, new float[]{0, (float)magField, 0});
		SensorManager.getRotationMatrixFromVector(rotMat, se.values);
		
		SensorManager.remapCoordinateSystem(rotMat, SensorManager.AXIS_X, SensorManager.AXIS_Z, rotMat);
		
		SensorManager.getOrientation(rotMat, orientMat);
		
		SensorTripleData t = new AnalogOrientationData(orientMat[0]/**180/(float)Math.PI*/, orientMat[1], orientMat[2]);
		
		updateLock.lock();
		
		net.sendPackets(t);
		
		updateLock.unlock();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}

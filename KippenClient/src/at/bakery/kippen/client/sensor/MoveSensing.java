package at.bakery.kippen.client.sensor;

import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.client.activity.NetworkingTask;
import at.bakery.kippen.common.data.MoveData;
import at.bakery.kippen.common.data.SensorTripleData;

public class MoveSensing implements SensorEventListener {

	private Lock updateLock = new ReentrantLock();
	
	private INetworking net = NetworkingTask.getInstance();
	
	private float[] accVector = new float[4];
	private float[] magVector;
	private float[] gravVector;
	
	// velocity and position
	private float[] velocity = new float[4];
	private float[] position = new float[2];
	
	// the result direction (acc) and position
	private float[] accMove = new float[4];
	//private float[] posMove = new float[4];
	
	private long lastTime = System.nanoTime();

	@Override
	public void onSensorChanged(SensorEvent se) {
		if(se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			processLinearAcc(se.values);
		} else if(se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			processMagnetic(se.values);
		} else if(se.sensor.getType() == Sensor.TYPE_GRAVITY) {
			processGravity(se.values);
		} else {
			return;
		}
		
		// we interested in accumulated position ...
		//computePosition();
		
		SensorTripleData t = new MoveData(accMove[0], accMove[1], accMove[2]);
		
		updateLock.lock();
		
		net.sendPackets(t);
		
		updateLock.unlock();
		
		lastTime = System.nanoTime();
	}
	
	private void processGravity(float[] values) {
		gravVector = Arrays.copyOf(values, 4);
	}

	private void processMagnetic(float[] values) {
		magVector = Arrays.copyOf(values, 4);
	}

	private void processLinearAcc(float[] values) {
		accVector = Arrays.copyOf(values, 4);
		
		if(gravVector == null || magVector == null) {
			return;
		}
		
		float[] rotMat = new float[16];
		float[] incMat = new float[16];
		SensorManager.getRotationMatrix(rotMat, incMat, gravVector, magVector);
		
		float[] invRotMat = new float[16];
		Matrix.invertM(invRotMat, 0, rotMat, 0);
		
		Matrix.multiplyMV(accMove, 0, invRotMat, 0, accVector, 0);
		
		// rounded for nicer output
		float[] tmpAcc = new float[accMove.length];
		for(int i = 0; i < accMove.length; i++) {
			tmpAcc[i] = (int)(accMove[i] * 100) / 100.0f;
		}
		
		// FIXME if more precise results are required, use original accWorld
		accMove = tmpAcc;
	}
	
	/*private void computePosition() {
		// only if acceleration then adjust position
		if(accMove[0]*accMove[0] + accMove[1]*accMove[1] + accMove[2]*accMove[2] < 1) {
			return;
		}
		
		// fetch delta time
		double timeDelta = (System.nanoTime() - lastTime) * 10E-9 * 10E-5;
		
		for(int i = 0; i < velocity.length; i++) {
			velocity[i] += timeDelta * (int)accMove[i];
		}
		
		for(int i = 0; i < position.length; i++) {
			position[i] += timeDelta * velocity[i];
		}
		
		// tmp pos rounded
		float[] roundedPos = new float[position.length];
		for(int i = 0; i < position.length; i++) {
			roundedPos[i] = (int)position[i];
		}
		
		posMove = roundedPos;
	}*/

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}

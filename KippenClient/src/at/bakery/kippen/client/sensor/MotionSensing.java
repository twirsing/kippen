package at.bakery.kippen.client.sensor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.util.Log;
import android.view.OrientationEventListener;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.client.activity.KippenCollectingActivity;
import at.bakery.kippen.client.activity.NetworkingTask;
import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.BarrelOrientationData;
import at.bakery.kippen.common.data.CubeOrientationData;
import at.bakery.kippen.common.data.MoveData;
import at.bakery.kippen.common.data.SensorTripleData;
import at.bakery.kippen.common.data.ShakeData;
import at.bakery.kippen.common.data.CubeOrientationData.Orientation;

public class MotionSensing implements SensorEventListener, ISensorDataCache {

	// network lock, networking and timing
	private Lock updateLock = new ReentrantLock();
	private INetworking net = NetworkingTask.getInstance();
	
	/* ------------------------------------------
	 * MOVE SENSING
	 * ------------------------------------------ */
	// current acc, magnetic field and gravity
	private float[] accVector = new float[4];
	private float[] magVector;
	private float[] gravVector;
	
	// the result direction (acc) and position
	private float[] accMove = new float[4];
	
	/* ------------------------------------------
	 * SHAKE SENSING
	 * ------------------------------------------ */
	// Minimum acceleration needed to count as a shake movement
    private static final int MIN_SHAKE_ACCELERATION = 5;
    
    // Minimum number of movements to register a shake
    private static final int MIN_MOVEMENTS = 8;
    
    // Maximum time (in milliseconds) for the whole shake to occur
    private static final int MAX_SHAKE_DURATION = 400;
	
    // Arrays to store gravity and linear acceleration values
	private float[] mGravity = { 0.0f, 0.0f, 0.0f };
	private float[] mLinearAcceleration = { 0.0f, 0.0f, 0.0f };
	
	// Indexes for x, y, and z values
	private static final int X = 0;
	private static final int Y = 1;
	private static final int Z = 2;
	
	// Start time for the shake detection
	long startTime = 0;
	
	// Counter for shake movements
	int moveCount = 0;
	
	/* ------------------------------------------
	 * AVERAGED ACCELERATION SENSING
	 * ------------------------------------------ */
	private static final int MEASURE_COUNT = 5;
	private static final int MEASURE_SEND_INTERVAL = 10;
	
	private Queue<SensorTripleData> values = new LinkedList<SensorTripleData>();
	private SensorTripleData avgValue = new SensorTripleData(0, 0, 0);
	private int interval = 0;
	
	private SensorTripleData measureStart = new SensorTripleData(0, 0, 0);
	private SensorTripleData measureEnd = new SensorTripleData(0, 0, 0);
	private SensorTripleData measureRef = measureStart;
	
	private float[] curAcc = new float[3];
	
	private AccelerationData cachedData;
	
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
	
	@Override
	public AccelerationData getCacheAccelerationData() {
		return cachedData;
	}
	
	private void handleAvgAcceleration(SensorEvent se) {
		if(se.sensor.getType() != Sensor.TYPE_ACCELEROMETER || se.values.length < 3) {
			return;
		}
		
		curAcc = Arrays.copyOf(se.values, 3);
		
		measureRef.setXYZ(se.values[0], se.values[1], se.values[2]);
		if(measureRef == measureStart) {
			measureRef = measureEnd;
			return;
		} else {
			measureRef = measureStart;
		}
				
		SensorTripleData t = new SensorTripleData(
				measureEnd.getX() - measureStart.getX(), 
				measureEnd.getY() - measureStart.getY(), 
				measureEnd.getZ() - measureStart.getZ());
		values.offer(t);
		
		updateLock.lock();
		
		avgValue.incrementXYZ(t.getX(), t.getY(), t.getZ());
		
		if(values.size() > MEASURE_COUNT) {
			SensorTripleData rem = values.poll();
			avgValue.incrementXYZ(-rem.getX(), -rem.getY(), -rem.getZ());
		}
		
		interval++;
		if(interval > MEASURE_SEND_INTERVAL) {
			AccelerationData accData = new AccelerationData(avgValue.getX() / values.size(), avgValue.getY() / values.size(), avgValue.getZ() / values.size());
			
			cachedData = accData;
			//net.sendPackets(accData);
			
			interval = 0;
			
			Log.d("KIPPEN", "ACCELERATION: " + accData);
		}
		
		updateLock.unlock();
	}
	
	private void handleMove(SensorEvent se) {
		if(se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			accVector = Arrays.copyOf(se.values, 4);
			
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
		} else if(se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magVector = Arrays.copyOf(se.values, 4);
		} else if(se.sensor.getType() == Sensor.TYPE_GRAVITY) {
			gravVector = Arrays.copyOf(se.values, 4);
		} else {
			return;
		}
		
		SensorTripleData t = new MoveData(accMove[0], accMove[1], accMove[2]);
		
		updateLock.lock();
		net.sendPackets(t);
		updateLock.unlock();
		
		Log.d("KIPPEN", "MOVE: " + t);
	}
	
	private void handleShake(SensorEvent se) {
		final float alpha = 0.8f;

        // gravity
        mGravity[X] = alpha * mGravity[X] + (1 - alpha) * se.values[X];
        mGravity[Y] = alpha * mGravity[Y] + (1 - alpha) * se.values[Y];
        mGravity[Z] = alpha * mGravity[Z] + (1 - alpha) * se.values[Z];

        // linear acceleration along the x, y, and z axes (gravity effects removed)
        mLinearAcceleration[X] = se.values[X] - mGravity[X];
        mLinearAcceleration[Y] = se.values[Y] - mGravity[Y];
        mLinearAcceleration[Z] = se.values[Z] - mGravity[Z];
         
        // max linear acceleration in any direction
        float maxLinearAcceleration = mLinearAcceleration[X];
        if(mLinearAcceleration[Y] > maxLinearAcceleration) {
        	maxLinearAcceleration = mLinearAcceleration[Y];
        }
        if(mLinearAcceleration[Z] > maxLinearAcceleration) {
        	maxLinearAcceleration = mLinearAcceleration[Z];
        }
        
        // check threshold
        if(maxLinearAcceleration > MIN_SHAKE_ACCELERATION) {
        	long now = System.currentTimeMillis();
        	if(startTime == 0) {
        		startTime = now;
        	}
        	
        	long elapsedTime = now - startTime;
        	if(elapsedTime > MAX_SHAKE_DURATION) {
        		// reset
        		startTime = 0;
            	moveCount = 0;
        	} else {
        		moveCount++;
        		if(moveCount > MIN_MOVEMENTS) {
        			updateLock.lock();
        			net.sendPackets(new ShakeData());
        			updateLock.unlock();
        			
        			Log.d("KIPPEN", "SHAKE: !");
        			
        			// reset
        			startTime = 0;
        	    	moveCount = 0;
        		}
        	}
        }
	}
	
	private void handleOrientation(SensorEvent se) {
		if(curAcc == null || magVector == null) {
			return;
		}
		
        int orientation = -1;
        float X = -curAcc[0];
        float Y = -curAcc[1];
        float Z = -curAcc[2];
        
        float magnitude = X*X + Y*Y;
        if(magnitude * 4 >= Z*Z) {
            float OneEightyOverPi = 57.29577957855f;
            float angle = (float)Math.atan2(-Y, X) * OneEightyOverPi;
            
            orientation = 90 - (int)Math.round(angle);
            while(orientation >= 360) {
                orientation -= 360;
            } 
            while(orientation < 0) {
                orientation += 360;
            }
        }
        
        onOrientationChanged(orientation);
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
	public void onSensorChanged(SensorEvent se) {
		handleMove(se);
		handleShake(se);
		handleAvgAcceleration(se);
		handleOrientation(se);
	}
	
	public void onOrientationChanged(int deg) {
		handleCube(deg);
		handleBarrel(deg);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}

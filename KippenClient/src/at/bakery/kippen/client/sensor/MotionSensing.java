package at.bakery.kippen.client.sensor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.util.Log;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.client.activity.NetworkingTask;
import at.bakery.kippen.common.data.BarrelOrientationData;
import at.bakery.kippen.common.data.ContainerData;
import at.bakery.kippen.common.data.CubeOrientationData;
import at.bakery.kippen.common.data.CubeOrientationData.Orientation;
import at.bakery.kippen.common.data.SensorTripleData;
import at.bakery.kippen.common.data.ShakeData;

public class MotionSensing implements SensorEventListener {

	// network lock, networking and timing
	private INetworking net = NetworkingTask.getInstance();
	
	/* ------------------------------------------
	 * SENSOR DATA CACHES - LATEST ONES
	 * ------------------------------------------ */
	private final SensorTripleData ACC_DATA = new SensorTripleData(0, 0, 0);
	private final SensorTripleData AVG_ACC_DATA = new SensorTripleData(0, 0, 0);
	private final ShakeData SHAKE_DATA = new ShakeData();
	private final SensorTripleData MOVE_DATA = new SensorTripleData(0, 0, 0);
	private final CubeOrientationData CUBE_DATA = new CubeOrientationData(Orientation.UNKNOWN);
	private final BarrelOrientationData BARREL_DATA = new BarrelOrientationData(0);
	
	// container data for sending (groups together all required data packages)
	private final ContainerData CONTAINER_DATA;
	
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
	
	public MotionSensing() {
		// tell the container data what to contain when sent
		CONTAINER_DATA = new ContainerData();
		CONTAINER_DATA.accData = ACC_DATA;
		CONTAINER_DATA.avgAccData = AVG_ACC_DATA;
		CONTAINER_DATA.moveData = MOVE_DATA;
		CONTAINER_DATA.shakeData = SHAKE_DATA;
		CONTAINER_DATA.cubeData = CUBE_DATA;
		CONTAINER_DATA.barrelData = BARREL_DATA;
	}
	
	private void handleAvgAcceleration(SensorEvent se) {
		if(se.sensor.getType() != Sensor.TYPE_ACCELEROMETER || se.values.length < 3) {
			return;
		}
		
		ACC_DATA.setXYZ(se.values[0], se.values[1], se.values[2]);
		
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
		
		avgValue.incrementXYZ(t.getX(), t.getY(), t.getZ());
		
		if(values.size() > MEASURE_COUNT) {
			SensorTripleData rem = values.poll();
			avgValue.incrementXYZ(-rem.getX(), -rem.getY(), -rem.getZ());
		}
		
		interval++;
		if(interval > MEASURE_SEND_INTERVAL) {
			AVG_ACC_DATA.setXYZ(avgValue.getX() / values.size(), avgValue.getY() / values.size(), avgValue.getZ() / values.size());
			
			interval = 0;
			
			Log.d("KIPPEN", "ACCELERATION: " + ACC_DATA);
		}
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
		
		MOVE_DATA.setXYZ(accMove[0], accMove[1], accMove[2]);
		
		Log.d("KIPPEN", "MOVE: " + MOVE_DATA);
	}
	
	private void handleShake(SensorEvent se) {
		// reset shake
		SHAKE_DATA.setShaking(false);
		
		final float alpha = 0.8f;

        // gravity
        mGravity[X] = alpha * mGravity[X] + (1 - alpha) * (float)ACC_DATA.getX();
        mGravity[Y] = alpha * mGravity[Y] + (1 - alpha) * (float)ACC_DATA.getY();
        mGravity[Z] = alpha * mGravity[Z] + (1 - alpha) * (float)ACC_DATA.getZ();

        // linear acceleration along the x, y, and z axes (gravity effects removed)
        mLinearAcceleration[X] = (float)ACC_DATA.getX() - mGravity[X];
        mLinearAcceleration[Y] = (float)ACC_DATA.getY() - mGravity[Y];
        mLinearAcceleration[Z] = (float)ACC_DATA.getZ() - mGravity[Z];
         
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
        			SHAKE_DATA.setShaking(true);
        			
        			Log.d("KIPPEN", "SHAKE: !");
        			
        			// reset
        			startTime = 0;
        	    	moveCount = 0;
        		}
        	}
        }
	}
	
	private void handleOrientation(SensorEvent se) {
        int orientation = -1;
        double X = -ACC_DATA.getX();
        double Y = -ACC_DATA.getY();
        double Z = -ACC_DATA.getZ();
        
        double magnitude = X*X + Y*Y;
        if(magnitude * 4 >= Z*Z) {
            double OneEightyOverPi = 57.29577957855f;
            double angle = Math.atan2(-Y, X) * OneEightyOverPi;
            
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
		CubeOrientationData orientationData = CUBE_DATA;
		
		if (deg != -1) {
			if (deg >= 315 || deg < 45) {
				orientationData.setOrientation(Orientation.BACK);
			} else if (deg >= 45 && deg < 135) {
				orientationData.setOrientation(Orientation.RIGHT);
			} else if (deg >= 135 && deg < 225) {
				orientationData.setOrientation(Orientation.FRONT);
			} else if (deg >= 225 && deg < 315) {
				orientationData.setOrientation(Orientation.LEFT);
			} else {
				orientationData.setOrientation(Orientation.UNKNOWN);
			}
		}
		// it IS flat on the ground
		else {
			SensorTripleData accData = ACC_DATA;
			if(accData == null) {
				orientationData.setOrientation(Orientation.UNKNOWN);
			} else if (accData.getZ() < 0) {
				orientationData.setOrientation(Orientation.BOTTOM);
			} else {
				orientationData.setOrientation(Orientation.TOP);
			}
		}
		
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
		
		BARREL_DATA.setOrientation((double)degrees / MAX_DEGREES);
		
		Log.d("KIPPEN", "BARREL: " + degrees + "/" + MAX_DEGREES);
	}
	
	@Override
	public void onSensorChanged(SensorEvent se) {
		handleAvgAcceleration(se);
		handleMove(se);
		handleShake(se);
		handleOrientation(se);
		
		// send all sensor data at once
		net.sendPacket(CONTAINER_DATA);
	}
	
	public void onOrientationChanged(int deg) {
		handleCube(deg);
		handleBarrel(deg);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	
	/*
	 * HELPERS
	 */
	
	/*public float[] computeLinearAcceleration() {
        // Get a local copy of the sensor values
		float[] acceleration = new float[] {(float)ACC_DATA.getX(), (float)ACC_DATA.getY(), (float)ACC_DATA.getZ()};
		float[] magnetic = new float[3];
 
        // Get a local copy of the sensor values
        System.arraycopy(magnetic, 0, this.magVector, 0, acceleration.length);
 
        // Get the rotation matrix to put our local device coordinates
        // into the world-coordinate system.
        float[] r = new float[9];
        if (SensorManager.getRotationMatrix(r, null, acceleration, magnetic)) {
            // values[0]: azimuth/yaw, rotation around the Z axis.
            // values[1]: pitch, rotation around the X axis.
            // values[2]: roll, rotation around the Y axis.
            float[] values = new float[3];
 
            // NOTE: the reference coordinate-system used is different
            // from the world coordinate-system defined for the rotation
            // matrix:
            // X is defined as the vector product Y.Z (It is tangential
            // to the ground at the device's current location and
            // roughly points West). Y is tangential to the ground at
            // the device's current location and points towards the
            // magnetic North Pole. Z points towards the center of the
            // Earth and is perpendicular to the ground.
            SensorManager.getOrientation(r, values);
 
            float magnitude = (float) (Math.sqrt(Math.pow(acceleration[0], 2)
                    + Math.pow(acceleration[1], 2)
                    + Math.pow(acceleration[2], 2)) / SensorManager.GRAVITY_EARTH);
 
            double var = varianceAccel.addSample(magnitude);
             
            // Attempt to estimate the gravity components when the device is
            // stable and not experiencing linear acceleration.
            if (var < 0.03)
            {
                //values[0]: azimuth, rotation around the Z axis.
                //values[1]: pitch, rotation around the X axis.
                //values[2]: roll, rotation around the Y axis.
                 
                // Find the gravity component of the X-axis
                // = g*-cos(pitch)*sin(roll);
                components[0] = (float) (SensorManager.GRAVITY_EARTH * -Math.cos(values[1]) * Math
                        .sin(values[2]));
                 
                // Find the gravity component of the Y-axis
                // = g*-sin(pitch);
                components[1] = (float) (SensorManager.GRAVITY_EARTH * -Math.sin(values[1]));
 
                // Find the gravity component of the Z-axis
                // = g*cos(pitch)*cos(roll);
                components[2] = (float) (SensorManager.GRAVITY_EARTH * Math.cos(values[1]) * Math
                        .cos(values[2]));
            }
 
            // Subtract the gravity component of the signal
            // from the input acceleration signal to get the
            // tilt compensated output.
            linearAcceleration[0] = (this.acceleration[0] - components[0])/SensorManager.GRAVITY_EARTH;
            linearAcceleration[1] = (this.acceleration[1] - components[1])/SensorManager.GRAVITY_EARTH;
            linearAcceleration[2] = (this.acceleration[2] - components[2])/SensorManager.GRAVITY_EARTH;
        }
 
        return linearAcceleration;
    }*/
}

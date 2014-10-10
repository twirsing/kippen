package at.bakery.kippen.client.sensor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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
	private float[] magVector = new float[4];
	private float[] gravVector = new float[4];
	
	// the result direction (acc) and position
	private float[] accMove = new float[4];
	
	// linear acceleration
	private float[] accLinVector = new float[4];
	
	/* ------------------------------------------
	 * SHAKE SENSING
	 * ------------------------------------------ */
	// Minimum acceleration needed to count as a shake movement
    private static final double MIN_SHAKE_ACCELERATION = 1.2;
    
    // Minimum number of movements to register a shake
    private static final int MIN_MOVEMENTS = 6;
    
    // Maximum time (in milliseconds) for the whole shake to occur
    private static final int MAX_SHAKE_DURATION = 2000;
	
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
	
	private void handleAvgAcceleration() {
		ACC_DATA.setXYZ(accVector[0], accVector[1], accVector[2]);
		
		measureRef.setXYZ(accVector[0], accVector[1], accVector[2]);
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
	
	private void handleMove() {
		float[] rotMat = new float[16];
		float[] incMat = new float[16];
		SensorManager.getRotationMatrix(rotMat, incMat, gravVector, magVector);
		
		float[] invRotMat = new float[16];
		Matrix.invertM(invRotMat, 0, rotMat, 0);
		
		Matrix.multiplyMV(accMove, 0, invRotMat, 0, accLinVector, 0);
		
		// rounded for nicer output
		float[] tmpAcc = new float[accMove.length];
		for(int i = 0; i < accMove.length; i++) {
			tmpAcc[i] = (int)(accMove[i] * 100) / 100.0f;
		}
		
		// FIXME if more precise results are required, use original accWorld
		accMove = tmpAcc;
		
		MOVE_DATA.setXYZ(accMove[0], accMove[1], accMove[2]);
		
		Log.d("KIPPEN", "MOVE: " + MOVE_DATA);
	}
	
	private void handleShake() {
		// reset shake
		SHAKE_DATA.setShaking(false);
		
		double linAccAmpl = Math.sqrt(accLinVector[0]*accLinVector[0] + accLinVector[1]*accLinVector[1] + accLinVector[2]*accLinVector[2]);
        if(linAccAmpl > MIN_SHAKE_ACCELERATION) {
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
        		if(moveCount >= MIN_MOVEMENTS) {
        			SHAKE_DATA.setShaking(true);
        			
        			Log.d("KIPPEN", "SHAKE: !");
        			
        			// reset
        			startTime = 0;
        	    	moveCount = 0;
        		}
        	}
        }
	}
	
	private void handleOrientation() {
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
		if(se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magVector = Arrays.copyOf(se.values, 4);
		} else if(se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			accVector = Arrays.copyOf(se.values, 4);
		}
		
		computeLinearAccelerationAndGravity();
		
		handleAvgAcceleration();
		handleMove();
		handleShake();
		handleOrientation();
		
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
	
	public void computeLinearAccelerationAndGravity() {
        // Get a local copy of the sensor values
		float[] acceleration = Arrays.copyOf(this.accVector,  4);
		float[] magnetic = Arrays.copyOf(this.magVector, 4);
 
        // Get the rotation matrix to put our local device coordinates
        // into the world-coordinate system.
        float[] r = new float[16];
        SensorManager.getRotationMatrix(r, null, acceleration, magnetic);
        float[] values = new float[3];
 
        SensorManager.getOrientation(r, values);
 
        float magnitude = (float) (Math.sqrt(Math.pow(acceleration[0], 2)
                + Math.pow(acceleration[1], 2)
                + Math.pow(acceleration[2], 2)) / SensorManager.GRAVITY_EARTH);
 
        double var = varianceAccel.addSample(magnitude);
        if (var < 0.03) {
         	this.gravVector[0] = (float)(0.8 * SensorManager.GRAVITY_EARTH * -Math.cos(values[1]) * Math.sin(values[2]));
           	this.gravVector[1] = (float)(0.8 * SensorManager.GRAVITY_EARTH * -Math.sin(values[1]));
           	this.gravVector[2] = (float)(0.8 * SensorManager.GRAVITY_EARTH * Math.cos(values[1]) * Math.cos(values[2]));
        }
 
        accLinVector[0] = (accVector[0] - gravVector[0])/SensorManager.GRAVITY_EARTH;
        accLinVector[1] = (accVector[1] - gravVector[1])/SensorManager.GRAVITY_EARTH;
        accLinVector[2] = (accVector[2] - gravVector[2])/SensorManager.GRAVITY_EARTH;
    }
	
	private StdDev varianceAccel = new StdDev();
	
	private class StdDev {
		private LinkedList<Double> list = new LinkedList<Double>();
		private double stdDev;
		private DescriptiveStatistics stats = new DescriptiveStatistics();

		public double addSample(double value) {
			list.addLast(value);
			enforceWindow();
			return calculateStdDev();
		}

		private void enforceWindow() {
			if (list.size() > 50) {
				list.removeFirst();
			}
		}

		private double calculateStdDev() {
			if (list.size() > 5) {
				stats.clear();
				for (int i = 0; i < list.size(); i++) {
					stats.addValue(list.get(i));
				}
				stdDev = stats.getStandardDeviation();
			}
			return stdDev;
		}
	}
}

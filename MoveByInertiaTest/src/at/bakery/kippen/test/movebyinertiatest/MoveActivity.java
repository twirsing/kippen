package at.bakery.kippen.test.movebyinertiatest;

import java.util.Arrays;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.Menu;

public class MoveActivity extends Activity {

	private SensorManager sensorMan;
	
	private Sensor accSense;
	private SensorEventListener accSensorListener;
	
	private Sensor magSense;
	private SensorEventListener magSensorListener;
	
	private Sensor gravSense;
	private SensorEventListener gravSensorListener;

	private float[] accVector = new float[4];
	private float[] magVector = new float[4];
	private float[] gravVector = new float[4];
	
	// velocity and position
	private float[] velocity = new float[4];
	private float[] position = new float[2];
	
	private long lastTime = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_move);
		
		Object tmpMan = getSystemService(Context.SENSOR_SERVICE);
		if(tmpMan == null) {
			System.err.println("No sensor services detected on device. It does not make sense to start, I rather quit myself.");
			this.finish();
		}
		sensorMan = (SensorManager)tmpMan;
		
		// fetch initial time
		lastTime = System.nanoTime();
		
		accSense = sensorMan.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		accSensorListener = new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent se) {
				accVector = Arrays.copyOf(se.values, 4);
				
				if(gravVector == null || magVector == null) {
					return;
				}
				
				float[] rotMat = new float[16];
				float[] incMat = new float[16];
				SensorManager.getRotationMatrix(rotMat, incMat, gravVector, magVector);
				
				float[] invRotMat = new float[16];
				Matrix.invertM(invRotMat, 0, rotMat, 0);
				
				float[] accWorld = new float[4];
				Matrix.multiplyMV(accWorld, 0, invRotMat, 0, accVector, 0);
				
				// rounded for nicer output
				float[] tmpAcc = new float[accWorld.length];
				for(int i = 0; i < accWorld.length; i++) {
					tmpAcc[i] = (int)accWorld[i];
				}
				System.out.println("ACC: " + Arrays.toString(tmpAcc));
				
				// computeAndPrintPosition(accWorld);
			}
			
			@Override
			public void onAccuracyChanged(Sensor arg0, int arg1) {}
		};
		
		magSense = sensorMan.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		magSensorListener = new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent se) {
				magVector = Arrays.copyOf(se.values, 4);
//				System.out.println("MAG: " + Arrays.toString(se.values));
			}
			
			@Override
			public void onAccuracyChanged(Sensor arg0, int arg1) {}
		};
		
		gravSense = sensorMan.getDefaultSensor(Sensor.TYPE_GRAVITY);
		gravSensorListener = new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent se) {
				gravVector = Arrays.copyOf(se.values, 4);
//				System.out.println("GRAV: " + Arrays.toString(se.values));
			}
			
			@Override
			public void onAccuracyChanged(Sensor arg0, int arg1) {}
		};
	}
	
	public void computeAndPrintPosition(float[] accWorld) {
		// only if acceleration then adjust position
		if(accWorld[0]*accWorld[0] + accWorld[1]*accWorld[1] + accWorld[2]*accWorld[2] < 1) {
			System.out.println("OLD: " + Arrays.toString(position));
			return;
		}
		
		// fetch delta time
		double timeDelta = (System.nanoTime() - lastTime) * 10E-9 * 10E-5;
		
		for(int i = 0; i < velocity.length; i++) {
			velocity[i] += timeDelta * (int)accWorld[i];
		}
		
		for(int i = 0; i < position.length; i++) {
			position[i] += timeDelta * velocity[i];
		}
		
		// tmp pos rounded
		float[] roundedPos = new float[position.length];
		for(int i = 0; i < position.length; i++) {
			roundedPos[i] = (int)position[i];
		}
		
		System.out.println("POS: " + Arrays.toString(roundedPos));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.move, menu);
		return true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		sensorMan.registerListener(accSensorListener, accSense, Sensor.TYPE_LINEAR_ACCELERATION);
		sensorMan.registerListener(magSensorListener, magSense, Sensor.TYPE_MAGNETIC_FIELD);
		sensorMan.registerListener(gravSensorListener, gravSense, Sensor.TYPE_GRAVITY);
	}

}

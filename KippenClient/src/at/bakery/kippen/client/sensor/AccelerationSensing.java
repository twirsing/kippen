package at.bakery.kippen.client.sensor;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.client.activity.NetworkingTask;
import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.SensorTripleData;

public class AccelerationSensing implements SensorEventListener, ISensorDataCache {

	private static final int MEASURE_COUNT = 5;
	private static final int MEASURE_SEND_INTERVAL = 10;
	
	private Queue<SensorTripleData> values = new LinkedList<SensorTripleData>();
	private SensorTripleData avgValue = new SensorTripleData(0, 0, 0);
	private int interval = 0;
	
	private SensorTripleData measureStart = new SensorTripleData(0, 0, 0);
	private SensorTripleData measureEnd = new SensorTripleData(0, 0, 0);
	private SensorTripleData measureRef = measureStart;
	
	private Lock updateLock = new ReentrantLock();
	
	private INetworking net = NetworkingTask.getInstance();
	
	@Override
	public void onAccuracyChanged(Sensor s, int a) {}

	@Override
	public void onSensorChanged(SensorEvent se) {
		if(se.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
			return;
		}
		
		if(se.values.length < 3) {
			return;
		}
		
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
		}
		
		updateLock.unlock();
	}

	private AccelerationData cachedData;
	
	@Override
	public AbstractData getCacheData() {
		return cachedData;
	}
	
}

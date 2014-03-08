package at.bakery.kippen.client.sensor;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;
import at.bakery.kippen.client.activity.INetworking;
import at.bakery.kippen.client.activity.NetworkingTask;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.SensorTripleData;

public class AccSensingTextOutput implements SensorEventListener {

	private static final int MEASURE_COUNT = 50;
	private static final int MEASURE_SEND_INTERVAL = 20;
	
	private TextView lblXAccId, lblYAccId, lblZAccId;
	
	private Queue<SensorTripleData> values = new LinkedList<SensorTripleData>();
	private SensorTripleData avgValue = new SensorTripleData(0, 0, 0);
	private int interval = 0;
	
	private SensorTripleData measureStart = new SensorTripleData(0, 0, 0);
	private SensorTripleData measureEnd = new SensorTripleData(0, 0, 0);
	private SensorTripleData measureRef = measureStart;
	
	private Lock updateLock = new ReentrantLock();
	
	private INetworking net = NetworkingTask.getInstance();
	
	public AccSensingTextOutput(TextView lblXAccId, TextView lblYAccId, TextView lblZAccId) {
		this.lblXAccId = lblXAccId;
		this.lblYAccId = lblYAccId;
		this.lblZAccId = lblZAccId;
	}
	
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
			lblXAccId.setText("" + avgValue.getX() / values.size());
			lblYAccId.setText("" + avgValue.getY() / values.size());
			lblZAccId.setText("" + avgValue.getZ() / values.size());
			
			net.sendPackets(new AccelerationData(avgValue.getX() / values.size(), avgValue.getY() / values.size(), avgValue.getZ() / values.size()));
			interval = 0;
		}
		
		updateLock.unlock();
	}
	
}

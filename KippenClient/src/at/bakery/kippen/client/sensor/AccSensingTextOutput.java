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
import at.bakery.kippen.common.DataWithTimestampAndMac;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.SensorTripleData;

public class AccSensingTextOutput implements SensorEventListener {

	private static final int MEASURE_COUNT = 50;
	private static final int MEASURE_SEND_INTERVAL = 20;
	
	private TextView lblXAccId, lblYAccId, lblZAccId;
	
	private Queue<SensorTripleData> values = new LinkedList<SensorTripleData>();
	private SensorTripleData avgValue = new SensorTripleData(0, 0, 0);
	private long updateTime = -1;
	private int interval = 0;
	
	private SensorTripleData measureStart = new SensorTripleData(0, 0, 0);
	private SensorTripleData measureEnd = new SensorTripleData(0, 0, 0);
	private SensorTripleData measureRef = measureStart;
	
	private Lock updateLock = new ReentrantLock();
	
	private INetworking net;
	private String macAddress;
	
	public AccSensingTextOutput(TextView lblXAccId, TextView lblYAccId, TextView lblZAccId, INetworking net, String macAddress) {
		this.lblXAccId = lblXAccId;
		this.lblYAccId = lblYAccId;
		this.lblZAccId = lblZAccId;
		
		this.net = net;
		
		this.macAddress = macAddress;
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
		
		measureRef.x = se.values[0];
		measureRef.y = se.values[1];
		measureRef.z = se.values[2];
		
		if(measureRef == measureStart) {
			measureRef = measureEnd;
			return;
		} else {
			measureRef = measureStart;
		}
				
		SensorTripleData t = new SensorTripleData(measureEnd.x - measureStart.x, measureEnd.y - measureStart.y, measureEnd.z - measureStart.z);
		values.offer(t);
		
		updateLock.lock();
		
		avgValue.x += t.x;
		avgValue.y += t.y;
		avgValue.z += t.z;
		
		if(values.size() > MEASURE_COUNT) {
			SensorTripleData rem = values.poll();
			
			avgValue.x -= rem.x;
			avgValue.y -= rem.y;
			avgValue.z -= rem.z;
		}
		
		updateTime = System.nanoTime();
		
		interval++;
		if(interval > MEASURE_SEND_INTERVAL) {
			lblXAccId.setText("" + avgValue.x / values.size());
			lblYAccId.setText("" + avgValue.y / values.size());
			lblZAccId.setText("" + avgValue.z / values.size());
			
			net.sendPackets(new DataWithTimestampAndMac(
					new AccelerationData(avgValue.x / values.size(), avgValue.x / values.size(), avgValue.x / values.size()), 
					updateTime, macAddress));
			interval = 0;
		}
		
		updateLock.unlock();
	}
	
}

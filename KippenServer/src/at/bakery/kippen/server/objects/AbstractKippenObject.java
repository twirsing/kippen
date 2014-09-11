/**
 * 
 */
package at.bakery.kippen.server.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.AccelerationData;
import at.bakery.kippen.common.data.BatteryData;
import at.bakery.kippen.common.data.BarrelOrientationData;
import at.bakery.kippen.common.data.MoveData;
import at.bakery.kippen.common.data.WifiLevelsData;
import at.bakery.kippen.server.command.Command;
import at.bakery.kippen.server.outlets.AbstractKippOutlet;

/**
 * @author thomasw
 *
 */
public abstract class AbstractKippenObject {

	protected String id;
	
	protected HashMap<String, List<Command>> eventMap = new HashMap<String, List<Command>>();
	protected HashSet<AbstractKippOutlet> outletObjects = new HashSet<AbstractKippOutlet>();
	protected HashMap<String, AbstractData> dataObjects = new HashMap<String, AbstractData>();
	
	private static final long IDLE_AFTER_SECONDS = 2 * 60;
	private long lastActivityTime = System.nanoTime();
	
	/**
	 * @param id usually mac address
	 */
	public AbstractKippenObject(String id) {
		this.id = id;
		
		// periodically checks whether the object is idle or not (defined by the constant)
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if(System.nanoTime() - lastActivityTime > IDLE_AFTER_SECONDS * 1e9) {
					stop();
				}
			}
		}, IDLE_AFTER_SECONDS, 10, TimeUnit.SECONDS);
	}
	
	public void addOutlet(AbstractKippOutlet aKippOutlet){
		outletObjects.add(aKippOutlet);
	}
	
	public void setCommandsForEvents(String eventID, List<Command> commandList) {
		this.eventMap.put(eventID, commandList);
	}
	
	protected abstract void output();
	protected abstract void stop();
	
	/**
	 * Default implementation for data processing. Does nothing except storing received data 
	 * and updating activity timer. Due to the latter this super method needs to be called by
	 * overriding clients.
	 * 
	 * @param data The received data object.
	 */
	public void processData(AbstractData data){
		dataObjects.put(data.getClass().toString(), data);
		lastActivityTime = System.nanoTime();
	}
	
	public String getId() {
		return id;
	}
}

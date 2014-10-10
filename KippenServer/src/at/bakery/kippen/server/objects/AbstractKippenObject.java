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
import java.util.logging.Logger;

import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.CubeOrientationData;
import at.bakery.kippen.common.data.MoveData;
import at.bakery.kippen.common.data.ShakeData;
import at.bakery.kippen.server.EventTypes;
import at.bakery.kippen.server.command.Command;
import at.bakery.kippen.server.outlets.AbstractKippOutlet;

/**
 * @author thomasw
 * 
 */
public abstract class AbstractKippenObject {
	static Logger log = Logger.getLogger(AbstractKippenObject.class.getName());
	protected String id;

	protected HashMap<String, List<Command>> eventsOfObject = new HashMap<String, List<Command>>();
	protected HashSet<AbstractKippOutlet> outletObjects = new HashSet<AbstractKippOutlet>();

	private static final long IDLE_AFTER_SECONDS =  20;
	private long lastActivityTime = System.nanoTime();
	
	protected double MOVE_DATA_THRESHHOLD = 0.3;
	protected static final long NEW_SHAKE_AFTER = (long) 1e9;

	/**
	 * @param id
	 *            usually mac address
	 */
	public AbstractKippenObject(String id) {
		this.id = id;

		// periodically checks whether the object is idle or not (defined by the
		// constant)
		ScheduledExecutorService scheduler = Executors
				.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (System.nanoTime() - lastActivityTime > IDLE_AFTER_SECONDS * 1e9) {
					timeout();
				}
			}
		}, IDLE_AFTER_SECONDS, 10, TimeUnit.SECONDS);
	}

	public void addOutlet(AbstractKippOutlet aKippOutlet) {
		outletObjects.add(aKippOutlet);
	}

	public void setCommandsForEvents(String eventID, List<Command> commandList) {
		this.eventsOfObject.put(eventID, commandList);
	}

	protected abstract void output();

	protected void timeout() {

		// FIXME implement ableton stop
		System.out.println("Calling TIMOUT on object with " + id);
		HashMap<String, String> paramMap = new HashMap<String, String>();
		for (Command c : eventsOfObject.get(EventTypes.TIMEOUT)) {
			try {
				c.execute(paramMap);
			} catch (Exception e) {
				log.warning("Failed to execute command "
						+ c.getClass().getSimpleName());
			}
		}
	}

	/**
	 * Default implementation for data processing. Does nothing except storing
	 * received data and updating activity timer. Due to the latter this super
	 * method needs to be called by overriding clients.
	 * 
	 * @param data
	 *            The received data object.
	 */
	public void processData(AbstractData data) {
		// only count as activity when the object did a significant action
		if(data instanceof MoveData){
			MoveData move = (MoveData)data;
			double moveAmpl = Math.sqrt(move.getX()*move.getX() + move.getY()*move.getY() + move.getZ()*move.getZ());
			if(moveAmpl > MOVE_DATA_THRESHHOLD) {
				lastActivityTime = System.nanoTime();
			}
		}
	}

	public String getId() {
		return id;
	}
}

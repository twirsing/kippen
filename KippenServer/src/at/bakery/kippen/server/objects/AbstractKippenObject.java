/**
 * 
 */
package at.bakery.kippen.server.objects;

import java.util.HashMap;
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

/**
 * @author thomasw
 * 
 */
public abstract class AbstractKippenObject {
	static Logger log = Logger.getLogger(AbstractKippenObject.class.getName());
	protected String id;

	protected HashMap<String, List<Command>> eventsOfObject = new HashMap<String, List<Command>>();

	private static  long IDLE_AFTER_SECONDS = 340;
	private long lastActivityTime = System.nanoTime();

	protected double MOVE_DATA_THRESHHOLD = 0.2;
	protected static final long NEW_SHAKE_AFTER = (long) 2e9;

	/**
	 * @param id
	 *            usually mac address
	 */
	public AbstractKippenObject(String id, int timeout) {
		this.id = id;
		IDLE_AFTER_SECONDS = timeout * 60;
		
		// periodically checks whether the object is idle or not (defined by the
		// constant)
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (System.nanoTime() - lastActivityTime > IDLE_AFTER_SECONDS * 1e9) {
					timeout();
				}
			}
		}, IDLE_AFTER_SECONDS, 10, TimeUnit.SECONDS);
	}

	public void setCommandsForEvents(String eventID, List<Command> commandList) {
		this.eventsOfObject.put(eventID, commandList);
	}

	protected void timeout() {
		System.out.println("Calling TIMOUT on object " + id);
		HashMap<String, String> paramMap = new HashMap<String, String>();
		for (Command c : eventsOfObject.get(EventTypes.TIMEOUT)) {
			try {
				c.execute(paramMap);
			} catch (Exception e) {
				log.warning("Failed to execute command " + c.getClass().getSimpleName());
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
		if (data instanceof MoveData) {
			MoveData move = (MoveData) data;
			double moveAmpl = Math.sqrt(move.getX() * move.getX() + move.getY() * move.getY() + move.getZ() * move.getZ());
			if (moveAmpl > MOVE_DATA_THRESHHOLD) {
				lastActivityTime = System.nanoTime();
			}
		}
	}

	/**
	 * Looks for the commands of this object that are registed for this specific
	 * event type.
	 */
	protected void executeCommands(HashMap<String, String> paramMap, String eventType) {
		List<Command> commands = eventsOfObject.get(eventType);
		if (commands != null) {
			for (Command c : commands) {
				try {
					c.execute(paramMap);
				} catch (Exception e) {
					e.printStackTrace();
					log.warning("Failed to execute command " + c.getClass().getSimpleName());
				} finally {

				}
			}
		}
	}

	public String getId() {
		return id;
	}
}

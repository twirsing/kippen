/**
 * 
 */
package at.bakery.kippen.server;

import java.util.HashSet;

/**
 * @author thomasw
 *
 */
public abstract class AbstractKippObject {

	String id;

	HashSet<AbstractKippOutlet> outetObjects = new HashSet<AbstractKippOutlet>();
	

	/**
	 * @param id usually mac address
	 */
	public AbstractKippObject(String id) {
		this.id = id;
	}
	
	public void addOutlet(AbstractKippOutlet aKippOutlet){
		outetObjects.add(aKippOutlet);
	}
	
	abstract public void output();
	
	public String getId() {
		return id;
	}

}

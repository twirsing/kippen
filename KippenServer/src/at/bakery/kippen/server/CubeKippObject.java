package at.bakery.kippen.server;


public class CubeKippObject extends AbstractKippObject {


	/**
	 * @param id usually mac address
	 */
	public CubeKippObject(String id) {
		super(id);
	}
	
	@Override
	public void addOutlet(AbstractKippOutlet aKippOutlet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void output() {
		// TODO Auto-generated method stub
		
		for (AbstractKippOutlet aOutlet : outetObjects) {
		    
			if(aOutlet instanceof CsvKippOutlet) {
				aOutlet.output();
			} 
		}

	}



}

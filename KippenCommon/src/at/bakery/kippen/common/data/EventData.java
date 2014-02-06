package at.bakery.kippen.common.data;

import at.bakery.kippen.common.IData;

public class EventData implements IData {

	private static final long serialVersionUID = -1520878341062188979L;

	public String value;
	
	public EventData(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		
		return "SENSOR val = " + value;
	}
}

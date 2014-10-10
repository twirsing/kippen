package at.bakery.kippen.common.data;

import at.bakery.kippen.common.AbstractData;

public class ContainerData extends AbstractData {

	/* all data to be send, need to do it explicitly
	 * (i.e. not using a generic list), otherwise 
	 * serialization would get pretty complex! */ 
	public SensorTripleData accData;
	public SensorTripleData avgAccData;
	public SensorTripleData moveData;
	public ShakeData shakeData;
	public CubeOrientationData cubeData;
	public BarrelOrientationData barrelData;
	
	@Override
	public String toString() {
		return "Sensor Container\n----------------\n" + 
				"ACC " + accData + "\n" + "AVG ACC " + avgAccData + "\n" + 
				"MOVE " + moveData + "\n" + shakeData + "\n" +
				cubeData + "\n" + barrelData + "\n";
	}
}

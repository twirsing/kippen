package at.bakery.kippen.client.activity;

import at.bakery.kippen.common.DataWithTimestampAndMac;

public interface INetworking {
	
	public void sendPackets(DataWithTimestampAndMac ... packets);
//	public DataWithTimestamp receivePacket();
}

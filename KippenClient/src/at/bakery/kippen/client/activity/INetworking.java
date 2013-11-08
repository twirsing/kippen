package at.bakery.kippen.client.activity;

import at.bakery.kippen.common.DataWithTimestamp;

public interface INetworking {
	
	public void sendPackets(DataWithTimestamp ... packets);
}

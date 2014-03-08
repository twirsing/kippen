package at.bakery.kippen.client.activity;

import at.bakery.kippen.common.AbstractData;

public interface INetworking {
	
	public void sendPackets(AbstractData ... packets);
}

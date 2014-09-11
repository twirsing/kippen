package at.bakery.kippen.server.command;

import java.util.Map;

import nerdproject.LiveController;

public class AbletonStopCommand implements Command {
	private int trackNumber;

	public AbletonStopCommand(int trackNumber) {
		this.trackNumber = trackNumber;
	}
	
	public AbletonStopCommand(String  trackNumber) {
		this.trackNumber = Integer.valueOf(trackNumber);
	}

	@Override
	public void execute(Map<String, String> params) throws Exception {
		LiveController.getInstance().stopTrack(trackNumber);
	}

}

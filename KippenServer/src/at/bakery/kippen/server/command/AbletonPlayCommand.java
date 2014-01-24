package at.bakery.kippen.server.command;

import java.util.Map;

import nerdproject.LiveController;

public class AbletonPlayCommand extends Command {
	private int trackNumber;

	AbletonPlayCommand(int trackNumber) {
		this.trackNumber = trackNumber;
	}

	@Override
	void execute(Map<String, Object> params) {
		LiveController.getInstance().playClip(trackNumber, (Integer) params.get("clipNumber"));
	}

}

package at.bakery.kippen.server.command;

import java.util.Map;

import nerdproject.LiveController;

public class AbletonPlayCommand extends Command {
	private int trackNumber;

	public AbletonPlayCommand(int trackNumber) {
		this.trackNumber = trackNumber;
	}

	public AbletonPlayCommand(String trackNumber) {
		this.trackNumber = Integer.valueOf(trackNumber);
	}

	@Override
	public void execute(Map<String, String> params) {
		LiveController.getInstance().playClip(trackNumber,
				Integer.valueOf(params.get("clipNumber")));
	}

}

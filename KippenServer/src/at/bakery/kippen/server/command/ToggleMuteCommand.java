package at.bakery.kippen.server.command;

import java.util.Map;

import nerdproject.LiveController;

public class ToggleMuteCommand implements Command{

	private int trackNumber;

	public ToggleMuteCommand(int trackNumber){
		this.trackNumber = trackNumber;
	}
	
	@Override
	public void execute(Map<String, String> params) {
		LiveController.getInstance().toggleMute(this.trackNumber);
	}

}

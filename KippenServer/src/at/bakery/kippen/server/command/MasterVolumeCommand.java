package at.bakery.kippen.server.command;

import java.util.Map;

import nerdproject.LiveController;

public class MasterVolumeCommand implements Command{

	@Override
	public void execute(Map<String, String> params) throws Exception {
		//should return value between 0.0 and 1.0
		float volume = Float.parseFloat(params.get("volume"));
		LiveController.getInstance().setMasterVolume(volume);
	}

}

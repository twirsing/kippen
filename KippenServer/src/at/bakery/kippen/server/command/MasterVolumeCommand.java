package at.bakery.kippen.server.command;

import java.util.Map;

import com.google.common.base.Preconditions;

import nerdproject.LiveController;

public class MasterVolumeCommand implements Command{

	@Override
	public void execute(Map<String, String> params) throws Exception {
		//should return value between 0.0 and 1.0
		float volume = Float.parseFloat(params.get("volume"));
		Preconditions.checkArgument(volume >= 0.0f && volume <= 1.0f);
		
		LiveController.getInstance().setMasterVolume(volume);
	}

}

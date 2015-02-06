package at.bakery.kippen.server.command;

import java.util.Map;

import nerdproject.LiveController;

public class AbletonDeviceCommand implements Command {
	int trackNumber;
	int deviceNumber;
	int parameterNumber;

	public AbletonDeviceCommand(int trackNumber, int deviceNumber, int parameterNumber) {
		this.trackNumber = trackNumber;
		this.deviceNumber = deviceNumber;
		this.parameterNumber = parameterNumber;
	}

	@Override
	public void execute(Map<String, String> params) throws Exception {
		System.out.println("sending barrel orientation");
		float value = Float.valueOf(params.get("value"));
		LiveController.getInstance().setDeviceParameterNormalized(trackNumber, deviceNumber, parameterNumber, value);
	}

}

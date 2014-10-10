package at.bakery.kippen.server.command;

import java.util.Map;

import nerdproject.LiveController;

public class AbletonMasterDeviceCommand implements Command{
	
	private int deviceNumber;
	private int parameterNumber;
	
	
	
	public AbletonMasterDeviceCommand(int deviceNumber, int parameterNumber) {
		this.deviceNumber = deviceNumber;
		this.parameterNumber = parameterNumber;
	}



	@Override
	public void execute(Map<String, String> params) throws Exception {
		float value = Float.valueOf(params.get("value"));
		LiveController.getInstance().setMasterDeviceParameter(deviceNumber, parameterNumber, value);
	}
	

}

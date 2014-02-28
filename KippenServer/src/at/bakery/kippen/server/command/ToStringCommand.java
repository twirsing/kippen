package at.bakery.kippen.server.command;

import java.util.Map;

public class ToStringCommand implements Command {

	
	
	@Override
	public void execute(Map<String, String> params) {
		
		System.out.println(params.get("string"));
	}

}

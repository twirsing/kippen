package at.bakery.kippen.server.command;

import java.util.Map;

public class ToStringCommand implements Command {
	
	@Override
	public void execute(Map<String, String> params) throws Exception {
		System.out.println("To String: " + params.get("string"));
	}
}

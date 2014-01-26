package at.bakery.kippen.server.command;

import java.util.Map;

public abstract class Command {
	public abstract void execute(Map<String, String> params);
}

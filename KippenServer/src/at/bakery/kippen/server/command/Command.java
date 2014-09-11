package at.bakery.kippen.server.command;

import java.util.Map;

public interface Command {
	public abstract void execute(Map<String, String> params) throws Exception;
}

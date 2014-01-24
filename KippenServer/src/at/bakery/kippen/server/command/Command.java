package at.bakery.kippen.server.command;

import java.util.Map;

public abstract class Command {
	abstract void execute(Map<String, Object> params);
}

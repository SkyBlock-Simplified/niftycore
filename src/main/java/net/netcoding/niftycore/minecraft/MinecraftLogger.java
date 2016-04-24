package net.netcoding.niftycore.minecraft;

import java.util.logging.Logger;

public abstract class MinecraftLogger extends ConsoleLogger {

	public MinecraftLogger(Logger logger) {
		super(logger);
	}

	public void broadcast(Object... args) {
		this.broadcast("", null, args);
	}

	public void broadcast(Throwable exception, Object... args) {
		this.broadcast("", exception, args);
	}

	public void broadcast(String message, Object... args) {
		this.broadcast(message, null, args);
	}

	public abstract void broadcast(String message, Throwable exception, Object... args);

}
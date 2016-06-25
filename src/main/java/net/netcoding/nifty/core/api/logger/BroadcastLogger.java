package net.netcoding.nifty.core.api.logger;

import net.netcoding.nifty.core.api.plugin.Plugin;

public abstract class BroadcastLogger extends ConsoleLogger {

	public BroadcastLogger(Plugin plugin) {
		super(plugin);
	}

	public final void broadcast(Throwable exception, Object... args) {
		this.broadcast("", exception, args);
	}

	public final void broadcast(String message, Object... args) {
		this.broadcast(message, null, args);
	}

	public abstract void broadcast(String message, Throwable exception, Object... args);

}
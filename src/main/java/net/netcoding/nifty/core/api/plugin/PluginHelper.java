package net.netcoding.nifty.core.api.plugin;

import net.netcoding.nifty.core.api.logger.BroadcasttLogger;

public interface PluginHelper<T extends BroadcasttLogger> {

	PluginDescription getPluginDescription();

	T getLog();

}
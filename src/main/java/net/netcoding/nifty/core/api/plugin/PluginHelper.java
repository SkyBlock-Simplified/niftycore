package net.netcoding.nifty.core.api.plugin;

import net.netcoding.nifty.core.api.logger.BroadcastLogger;

public interface PluginHelper<T extends BroadcastLogger> {

	PluginDescription getDesc();

	T getLog();

}
package net.netcoding.niftycore;

import java.util.logging.Logger;

import net.netcoding.niftycore.reflection.Reflection;

public class NiftyCore {

	private static final Logger LOGGER;
	private static final boolean IS_BUNGEE;
	private static final Object PLUGIN;

	static {
		boolean isBungee = false;
		Object pluginObj = null;
		Logger logger = Logger.getGlobal();

		try {
			Reflection proxy = new Reflection("ProxyServer", "net.md_5.bungee.api");
			Reflection manager = new Reflection("PluginManager", "net.md_5.bungee.api.plugin");
			Reflection plugin = new Reflection("Plugin", "net.md_5.bungee.api.plugin");
			proxy.getClazz();
			isBungee = true;
			Object proxyObj = proxy.invokeMethod("getInstance", null);
			Object managerObj = proxy.invokeMethod("getPluginManager", proxyObj);
			pluginObj = manager.invokeMethod("getPlugin", managerObj, "NiftyBungee");
			logger = (Logger)plugin.invokeMethod("getLogger", pluginObj);
		} catch (Exception ex) {
			try {
				Reflection bukkit = new Reflection("Bukkit", "org.bukkit");
				Reflection manager = new Reflection("PluginManager", "org.bukkit.plugin");
				Reflection plugin = new Reflection("Plugin", "org.bukkit.plugin");
				Object managerObj = bukkit.invokeMethod("getPluginManager", null);
				pluginObj = manager.invokeMethod("getPlugin", managerObj, "NiftyBukkit");
				logger = (Logger)plugin.invokeMethod("getLogger", pluginObj);
			} catch (Exception ex2) { }
		}

		PLUGIN = pluginObj;
		IS_BUNGEE = isBungee;
		LOGGER = logger;
	}

	public static final Logger getLogger() {
		return LOGGER;
	}

	public static final Object getPlugin() {
		return PLUGIN;
	}

	public static final boolean isBungee() {
		return IS_BUNGEE;
	}

	public static final boolean isBukkit() {
		return !IS_BUNGEE;
	}

}
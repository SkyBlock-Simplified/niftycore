package net.netcoding.nifty.core;

import net.netcoding.nifty.core.reflection.Reflection;

import java.util.Optional;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class NiftyCore {

	private static final Logger LOGGER;
	private static final Logger NIFTY_LOGGER;
	private static final boolean IS_BUNGEE;
	private static final boolean IS_BUKKIT;
	private static final boolean IS_SPONGE;
	private static final Object PLUGIN;

	static {
		boolean isBungee = false;
		boolean isBukkit = false;
		boolean isSponge = false;
		Object pluginObj;
		Logger logger = Logger.getGlobal();

		try {
			Reflection proxy = new Reflection("ProxyServer", "net.md_5.bungee.api");
			Reflection manager = new Reflection("PluginManager", "plugin", "net.md_5.bungee.api");
			Reflection plugin = new Reflection("Plugin", "plugin", "net.md_5.bungee.api");
			proxy.getClazz();
			Object proxyObj = proxy.invokeMethod("getInstance", null);
			Object managerObj = proxy.invokeMethod("getPluginManager", proxyObj);
			pluginObj = manager.invokeMethod("getPlugin", managerObj, "NiftyBungee");
			logger = (Logger)plugin.invokeMethod("getLogger", pluginObj);
			isBungee = true;
		} catch (Exception bungee) {
			try {
				Reflection bukkit = new Reflection("Bukkit", "org.bukkit");
				Reflection manager = new Reflection("PluginManager", "plugin", "org.bukkit");
				Reflection plugin = new Reflection("Plugin", "plugin", "org.bukkit");
				bukkit.getClazz();
				Object managerObj = bukkit.invokeMethod("getPluginManager", null);
				pluginObj = manager.invokeMethod("getPlugin", managerObj, "NiftyLibrary");
				logger = (Logger)plugin.invokeMethod("getLogger", pluginObj);
				isBukkit = true;
			} catch (Exception bukkit) {
				try {
					Reflection sponge = new Reflection("Sponge", "api", "org.spongepowered");
					Reflection manager = new Reflection("PluginManager", "plugin", "org.spongepowered.api");
					sponge.getClazz();
					Object managerObj = sponge.invokeMethod("getPluginManager", null);
					Optional<Object> opPlugin = (Optional<Object>)manager.invokeMethod("getPlugin", managerObj, "NiftyLibrary");
					pluginObj = opPlugin.orElse(null);
					isSponge = true;
				} catch (Exception ignore) {
					throw new IllegalStateException("Unable to determine bungee, bukkit or sponge status!");
				}
			}
		}

		PLUGIN = pluginObj;
		IS_BUNGEE = isBungee;
		IS_BUKKIT = isBukkit;
		IS_SPONGE = isSponge;
		LOGGER = Logger.getLogger(isBungee ? "BungeeCord" : "Minecraft");
		NIFTY_LOGGER = logger;
	}

	public static Logger getLogger() {
		return LOGGER;
	}

	public static Logger getNiftyLogger() {
		return NIFTY_LOGGER;
	}

	public static Object getPlugin() {
		return PLUGIN;
	}

	public static boolean isBungee() {
		return IS_BUNGEE;
	}

	public static boolean isBukkit() {
		return IS_BUKKIT;
	}

	public static boolean isSponge() {
		return IS_SPONGE;
	}

}
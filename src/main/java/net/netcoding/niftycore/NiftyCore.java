package net.netcoding.niftycore;

import java.util.logging.Logger;

import net.netcoding.niftycore.reflection.Reflection;
import net.netcoding.niftycore.util.StringUtil;

public class NiftyCore {

	private static final Logger LOGGER;
	private static final boolean IS_BUNGEE;

	static {
		boolean isBungee = false;
		Logger logger = Logger.getGlobal();

		try {
			Class.forName("net.md_5.bungee.api.ProxyServer");
			isBungee = true;
		} catch (Exception ex) { }

		try {
			String name = StringUtil.format("Nifty{0}", (isBungee() ? "Bungee" : "Bukkit"));
			Reflection main = new Reflection(name, StringUtil.format("net.netcoding.{0}", name.toLowerCase()));
			Object mainObj = main.invokeMethod("getPlugin", null);
			logger = (Logger)main.invokeMethod("getLogger", mainObj);
		} catch (Exception ex) { }

		IS_BUNGEE = isBungee;
		LOGGER = logger;
	}

	public static final boolean isBungee() {
		return IS_BUNGEE;
	}

	public static final boolean isBukkit() {
		return !IS_BUNGEE;
	}

	public static final Logger getNiftyLogger() {
		return LOGGER;
	}

}
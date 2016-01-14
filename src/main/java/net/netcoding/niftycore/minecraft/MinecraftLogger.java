package net.netcoding.niftycore.minecraft;

import net.netcoding.niftycore.util.RegexUtil;
import net.netcoding.niftycore.util.StringUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MinecraftLogger {

	private final transient Logger logger;

	public MinecraftLogger(Logger logger) {
		this.logger = logger;
	}

	public String getPrefix(String text) {
		return StringUtil.format("{0}[{1}{2}{0}]{3}", ChatColor.DARK_GRAY, ChatColor.RED, text, ChatColor.GRAY);
	}

	public void console(Object... args) {
		this.console("", null, args);
	}

	public void console(Throwable exception, Object... args) {
		this.console("", exception, args);
	}

	public void console(String message, Object... args) {
		this.console(message, null, args);
	}

	public void console(String message, Throwable exception, Object... args) {
		message = StringUtil.isEmpty(message) ? "null" : message;
		message = RegexUtil.strip(StringUtil.format(RegexUtil.replace(message, RegexUtil.LOG_PATTERN), args), RegexUtil.VANILLA_PATTERN);

		if (exception != null)
			this.logger.log(Level.SEVERE, message, exception);
		else
			this.logger.log(Level.INFO, message);
	}

	public String parse(String message, Object... args) {
		return ChatColor.GRAY + StringUtil.format(message, args);
	}

}
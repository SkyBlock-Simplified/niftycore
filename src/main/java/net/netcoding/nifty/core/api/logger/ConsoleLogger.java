package net.netcoding.nifty.core.api.logger;

import net.netcoding.nifty.core.api.color.ChatColor;
import net.netcoding.nifty.core.api.plugin.Plugin;
import net.netcoding.nifty.core.reflection.Reflection;
import net.netcoding.nifty.core.util.RegexUtil;
import net.netcoding.nifty.core.util.StringUtil;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public abstract class ConsoleLogger {

	private final JavaLogger logger;

	public ConsoleLogger(Plugin plugin) {
		this.logger = new JavaLogger(plugin);
	}

	public final void console(Throwable exception, Object... args) {
		this.console("", exception, args);
	}

	public final void console(String message, Object... args) {
		this.console(message, null, args);
	}

	public final void console(String message, Throwable exception, Object... args) {
		message = StringUtil.isEmpty(message) ? "null" : message;
		message = RegexUtil.strip(this.parse(RegexUtil.replace(message, RegexUtil.LOG_PATTERN), args), RegexUtil.VANILLA_PATTERN);

		if (exception != null)
			this.logger.log(Level.SEVERE, message, exception);
		else
			this.logger.log(Level.INFO, message);
	}

	public final void warn(String message, Object... args) {
		message = StringUtil.isEmpty(message) ? "null" : message;
		message = RegexUtil.strip(this.parse(RegexUtil.replace(message, RegexUtil.LOG_PATTERN), args), RegexUtil.VANILLA_PATTERN);
		this.logger.log(Level.WARNING, message);
	}

	public final String getPrefix(String text) {
		return StringUtil.format("{0}[{1}{2}{0}]{3} ", ChatColor.DARK_GRAY, ChatColor.RED, text, ChatColor.GRAY);
	}

	public final String parse(String message, Object... args) {
		return ChatColor.GRAY + StringUtil.format(message, args);
	}

	public final void setLevel(Level level) {
		this.logger.setLevel(level);
	}

	private final class JavaLogger extends Logger {

		private final String pluginName;

		public JavaLogger(Plugin plugin) {
			super(plugin.getName(), null);
			this.pluginName = StringUtil.format("[{0}] ", plugin.getName());
			this.setLevel(Level.ALL);

			try {
				new Reflection("ProxyServer", "net.md_5.bungee.api").getClazz();
				this.setParent(Logger.getLogger("BungeeCord"));
			} catch (Exception ex) {
				this.setParent(Logger.getLogger("Minecraft"));
			}
		}

		@Override
		public void log(LogRecord logRecord) {
			logRecord.setMessage(this.pluginName + logRecord.getMessage());
			super.log(logRecord);
		}

	}
}
package net.netcoding.nifty.core.api.logger;

import net.netcoding.nifty.core.api.color.ChatColor;
import net.netcoding.nifty.core.api.plugin.PluginDescription;
import net.netcoding.nifty.core.reflection.Reflection;
import net.netcoding.nifty.core.util.RegexUtil;
import net.netcoding.nifty.core.util.StringUtil;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public abstract class ConsoleLogger {

	private final JavaLogger logger;
	private ChatColor bracket = ChatColor.DARK_GRAY;
	private ChatColor color = ChatColor.GRAY;
	private ChatColor important = ChatColor.RED;

	public ConsoleLogger(PluginDescription desc) {
		this.logger = new JavaLogger(desc);
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

	public final ChatColor getBracket() {
		return this.bracket;
	}

	public final ChatColor getColor() {
		return this.color;
	}

	public final ChatColor getImportant() {
		return this.important;
	}

	public final Level getLevel() {
		return this.logger.getLevel();
	}

	public final String getPrefix(String text) {
		return StringUtil.format("{0}[{1}{2}{0}]{3} ", this.getBracket(), this.getImportant(), text, this.getColor());
	}

	public final String parse(String message, Object... args) {
		return this.getColor() + StringUtil.format(message, this.getColor(), this.getImportant(), args);
	}

	public final void setBracket(ChatColor color) {
		this.bracket = (color != null ? color : ChatColor.DARK_GRAY);
	}

	public final void setColor(ChatColor color) {
		this.color = (color != null ? color : ChatColor.GRAY);
	}

	public final void setImportant(ChatColor color) {
		this.important = (color != null ? color : ChatColor.RED);
	}

	public final void setLevel(Level level) {
		this.logger.setLevel(level != null ? level : Level.ALL);
	}

	public final void warn(String message, Object... args) {
		message = StringUtil.isEmpty(message) ? "null" : message;
		message = RegexUtil.strip(this.parse(RegexUtil.replace(message, RegexUtil.LOG_PATTERN), args), RegexUtil.VANILLA_PATTERN);
		this.logger.log(Level.WARNING, message);
	}

	private final class JavaLogger extends Logger {

		private final String pluginName;

		public JavaLogger(PluginDescription desc) {
			super(desc.getName(), null);
			this.pluginName = StringUtil.format("[{0}] ", desc.getName());
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
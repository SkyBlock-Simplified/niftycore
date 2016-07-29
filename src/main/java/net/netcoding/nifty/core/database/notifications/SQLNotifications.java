package net.netcoding.nifty.core.database.notifications;

import net.netcoding.nifty.core.NiftyCore;
import net.netcoding.nifty.core.api.scheduler.MinecraftScheduler;
import net.netcoding.nifty.core.database.pooling.SQLPooling;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.ConcurrentSet;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Adds support for notifications through an sql server instance.
 */
public abstract class SQLNotifications extends SQLPooling implements Runnable {

	static final String ACTIVITY_TABLE = "nifty_notifications";
	private static final int DEFAULT_DELAY = 10;
	private final transient ConcurrentSet<DatabaseNotification> listeners = Concurrent.newSet();
	private int taskId = -1;

	/**
	 * Create a new notification instance.
	 *
	 * @param url  Database connection url.
	 * @param user Username of the database connection.
	 * @param pass Password of the database connection.
	 */
	public SQLNotifications(String driver, String url, String user, String pass) throws SQLException {
		super(driver, url, user, pass);
	}

	/**
	 * Create a new notification instance.
	 *
	 * @param url        Database connection url.
	 * @param properties Properties of the database connection.
	 */
	public SQLNotifications(String driver, String url, Properties properties) throws SQLException {
		super(driver, url, properties);
	}

	public SQLNotifications(String driver, String url, File directory, String schema, Properties properties) throws SQLException {
		super(driver, url, directory, schema, properties);
	}

	/**
	 * Add a listener on the given table.
	 *
	 * @param table    Table name to listen to.
	 * @param notifier Listener to send notifications to.
	 */
	public void addListener(String table, DatabaseListener notifier) throws SQLException {
		this.addListener(table, notifier, DEFAULT_DELAY, false);
	}

	/**
	 * Add a listener on the given table.
	 *
	 * @param table     Table name to listen to.
	 * @param notifier  Listener to send notifications to.
	 * @param overwrite True to overwrite the triggers in the database, otherwise false.
	 */
	public void addListener(String table, DatabaseListener notifier, boolean overwrite) throws SQLException {
		this.addListener(table, notifier, DEFAULT_DELAY, overwrite);
	}

	/**
	 * Add a listener on the given table.
	 *
	 * @param table    Table name to listen to.
	 * @param notifier Listener to send notifications to.
	 * @param delay    How long in ticks to wait before checking.
	 */
	public void addListener(String table, DatabaseListener notifier, long delay) throws SQLException {
		this.addListener(table, notifier, delay, false);
	}

	/**
	 * Add a listener on the given table.
	 *
	 * @param table     Table name to listen to.
	 * @param notifier  Listener to send notifications to.
	 * @param delay     How long in ticks to wait before checking.
	 * @param overwrite True to overwrite the triggers in the database, otherwise false.
	 */
	public void addListener(String table, DatabaseListener notifier, long delay, boolean overwrite) throws SQLException {
		this.createLogTable();
		this.createPurgeEvent();
		this.listeners.add(new DatabaseNotification(this, table, notifier, overwrite));

		if (this.taskId == -1)
			this.taskId = MinecraftScheduler.getInstance().runAsync(this, 0, delay * (NiftyCore.isBungee() ? 50 : 1)).getId();
	}

	private void createLogTable() throws SQLException {
		this.createTable(ACTIVITY_TABLE, "id INT AUTO_INCREMENT PRIMARY KEY, schema_name VARCHAR(255) NOT NULL, table_name VARCHAR(255) NOT NULL, sql_action ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL, _submitted INT NOT NULL, primary_keys VARCHAR(255), old_data VARCHAR(255), new_data VARCHAR(255)");
	}

	private void createPurgeEvent() {
		this.updateAsync(StringUtil.format("CREATE EVENT IF NOT EXISTS purgeNiftyNotifications ON SCHEDULE EVERY 24 DAY_HOUR DO DELETE LOW_PRIORITY FROM {0}.{1} WHERE time < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 DAY));", this.getSchema(), ACTIVITY_TABLE));
	}

	public boolean isRunning() {
		return this.taskId != -1;
	}

	/**
	 * Remove all listeners.
	 */
	public void removeListeners() {
		this.removeListener(null);
	}

	/**
	 * Remove all listeners.
	 *
	 * @param dropTriggers True to drop triggers, otherwise false.
	 */
	public void removeListeners(boolean dropTriggers) {
		this.removeListener(null, dropTriggers);
	}

	/**
	 * Remove listener from the given table.
	 *
	 * @param table Table name to remove listeners from.
	 */
	public void removeListener(String table) {
		this.removeListener(table, false);
	}

	/**
	 * Remove listener from the given table.
	 *
	 * @param table        Table name to remove listeners from.
	 * @param dropTriggers True to drop triggers, otherwise false.
	 */
	public void removeListener(String table, boolean dropTriggers) {
		this.listeners.stream().filter(listener -> StringUtil.isEmpty(table) || listener.getTable().equals(table)).forEach(listener -> listener.stop(dropTriggers));

		if (this.listeners.isEmpty()) {
			if (this.taskId != -1) {
				MinecraftScheduler.getInstance().cancel(this.taskId);
				this.taskId = -1;
			}
		}
	}

	@Override
	public void run() {
		for (DatabaseNotification notification : this.listeners) {
			if (notification.isStopped()) {
				this.listeners.remove(notification);
				continue;
			}

			if (notification.pulse())
				notification.sendNotification();
		}
	}

}
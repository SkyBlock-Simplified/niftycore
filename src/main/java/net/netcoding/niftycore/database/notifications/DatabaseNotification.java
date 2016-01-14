package net.netcoding.niftycore.database.notifications;

import net.netcoding.niftycore.NiftyCore;
import net.netcoding.niftycore.database.factory.SQLFactory;
import net.netcoding.niftycore.database.factory.callbacks.ResultCallback;
import net.netcoding.niftycore.database.factory.callbacks.VoidResultCallback;
import net.netcoding.niftycore.minecraft.MinecraftLogger;
import net.netcoding.niftycore.util.StringUtil;
import net.netcoding.niftycore.util.concurrent.ConcurrentList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An sql listener used to check for updates to its associated table and notify plugins.
 */
public class DatabaseNotification extends MinecraftLogger {

	private TriggerEvent event;
	private final transient DatabaseListener listener;
	private final ConcurrentList<String> primaryColumnNames = new ConcurrentList<>();
	private int previousId = 0;
	private final transient SQLFactory sql;
	private volatile boolean stopped = false;
	private final String table;

	DatabaseNotification(SQLFactory sql, String table, DatabaseListener listener, boolean overwrite) throws SQLException {
		super(NiftyCore.getLogger());
		if (listener == null) throw new IllegalArgumentException("DatabaseListener cannot be null!");
		this.sql = sql;
		this.table = table;
		this.pulse();
		this.listener = listener;
		this.loadPrimaryKeys();

		if (!this.triggersExist() || overwrite) {
			for (TriggerEvent event : TriggerEvent.values()) {
				this.dropTrigger(event);
				this.createTrigger(event);
			}
		}
	}

	private void createTrigger(TriggerEvent event) throws SQLException {
		if (!this.primaryColumnNames.isEmpty()) {
			String primaryKeys = StringUtil.implode(",", this.primaryColumnNames);
			String quote = this.sql.getIdentifierQuoteString();
			String trigger = StringUtil.format("CREATE TRIGGER {1}.{2} AFTER {3} ON {0}{4}{0} FOR EACH ROW INSERT INTO {1}.{5} (schema_name, table_name, sql_action, primary_keys, _submitted, old_data, new_data) VALUES (?, ?, ?, ?, UNIX_TIMESTAMP(), ",
					quote, this.getSchema(), this.getName(event), event.toUppercase(), this.getTable(), SQLNotifications.ACTIVITY_TABLE);
			String _old = null;
			String _new = null;
			if (TriggerEvent.INSERT != event) _old = StringUtil.format("CONCAT(OLD.{0}{1}{0})", quote, StringUtil.implode(StringUtil.format("{0}, '','', OLD.{0}", quote), this.primaryColumnNames));
			if (TriggerEvent.DELETE != event) _new = StringUtil.format("CONCAT(NEW.{0}{1}{0})", quote, StringUtil.implode(StringUtil.format("{0}, '','', NEW.{0}", quote), this.primaryColumnNames));
			this.sql.updateAsync(String.format(trigger + "%s, %s);", _old, _new), this.getSchema(), this.getTable(), event.toUppercase(), primaryKeys);
		} else
			throw new SQLException(StringUtil.format("The table {0}.{1} has no primary key columns to keep track of!", this.getSchema(), this.getTable()));
	}

	private void dropTrigger(TriggerEvent event) {
		try {
			this.sql.update(StringUtil.format("DROP TRIGGER IF EXISTS {0};", this.getName(event)));
		} catch (Exception ignore) { }
	}

	/**
	 * Gets the primary keys and associated deleted data of the current notification.
	 *
	 * @return Map of primary keys and associated deleted data.
	 * @throws SQLException If you attempt to retrieve deleted data when inserting a record.
	 */
	public HashMap<String, Object> getDeletedData() throws SQLException {
		if (this.getEvent() == TriggerEvent.INSERT) throw new SQLException("Cannot retrieve an inserted record!");
		final HashMap<String, Object> deleted = new HashMap<>();

		this.sql.query(StringUtil.format("SELECT old_data FROM {0} WHERE schema_name = ? AND table_name = ? AND sql_action = ? AND id = ?;", SQLNotifications.ACTIVITY_TABLE), new VoidResultCallback() {
			@Override
			public void handle(ResultSet result) throws SQLException {
				if (result.next()) {
					String[] _old = result.getString("old_data").split(",");
					int keyCount = primaryColumnNames.size();
					for (int i = 0; i < keyCount; i++) deleted.put(primaryColumnNames.get(i), _old[i]);
				}
			}
		}, this.getSchema(), this.getTable(), this.getEvent().toUppercase(), this.previousId);

		return deleted;
	}

	/**
	 * Gets the event of the current notification.
	 *
	 * @return Event type of the current notification.
	 */
	public TriggerEvent getEvent() {
		return this.event;
	}

	private String getName(TriggerEvent event) {
		return StringUtil.format("on{0}{1}", this.getTable(), event.toUppercase());
	}

	/**
	 * Gets the schema of the current notification.
	 *
	 * @return Database name of the current notification.
	 */
	public String getSchema() {
		return this.sql.getSchema();
	}

	/**
	 * Gets the table of the current notification.
	 *
	 * @return Table name of the current notification.
	 */
	public String getTable() {
		return this.table;
	}

	/**
	 * Gets the updated data of the current notification.
	 *
	 * @param callback Callback class to handle retrieved data.
	 * @throws SQLException If you attempt to retrieve updated data when deleting a record.
	 */
	public void getUpdatedRow(final VoidResultCallback callback) throws SQLException {
		if (this.getEvent() == TriggerEvent.DELETE) throw new SQLException("Cannot retrieve a deleted record!");

		this.sql.query(StringUtil.format("SELECT new_data FROM {0} WHERE schema_name = ? AND table_name = ? AND sql_action = ? AND id = ?;", SQLNotifications.ACTIVITY_TABLE), new VoidResultCallback() {
			@Override
			public void handle(ResultSet result) throws SQLException {
				if (result.next()) {
					List<String> whereClause = new ArrayList<>();
					int keyCount = primaryColumnNames.size();
					String[] _new = result.getString("new_data").split(",");

					if (keyCount > 0) {
						for (int i = 0; i < keyCount; i++) whereClause.add(StringUtil.format("SUBSTRING_INDEX(SUBSTRING_INDEX({0}{1}{0}, '','', {2}), '','', -1) = ?", sql.getIdentifierQuoteString(), primaryColumnNames.get(i), (i + 1)));
						sql.query(StringUtil.format("SELECT * FROM {0} WHERE {1};", getTable(), StringUtil.implode(" AND ", whereClause)), callback, (Object[])_new);
					}
				}
			}
		}, this.getSchema(), this.getTable(), this.getEvent().toUppercase(), this.previousId);
	}

	/**
	 * Gets if the current notification has stopped.
	 *
	 * @return True if has stopped, otherwise false.
	 */
	public synchronized boolean isStopped() {
		return this.stopped;
	}

	private void loadPrimaryKeys() throws SQLException {
		this.primaryColumnNames.clear();
		this.primaryColumnNames.addAll(this.sql.query("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_KEY = ?;", new ResultCallback<ConcurrentList<String>>() {
			@Override
			public ConcurrentList<String> handle(ResultSet result) throws SQLException {
				ConcurrentList<String> keyNames = new ConcurrentList<>();
				while (result.next()) keyNames.add(result.getString("COLUMN_NAME"));
				return keyNames;
			}
		}, this.getSchema(), this.getTable(), "PRI"));
	}

	boolean pulse() {
		if (this.isStopped()) return false;

		try {
			return this.sql.query(StringUtil.format("SELECT id, sql_action FROM {0} WHERE table_name = ? AND id > ? AND sql_action IN (?, ?, ?) ORDER BY id {1}SC LIMIT 1;", SQLNotifications.ACTIVITY_TABLE, (this.previousId == 0 ? "DE" : "A")), new ResultCallback<Boolean>() {
				@Override
				public Boolean handle(ResultSet result) throws SQLException {
					if (result.next()) {
						int last = result.getInt("id");

						if (last > previousId) {
							previousId = last;
							event = TriggerEvent.fromString(result.getString("sql_action"));
							return true;
						}
					}

					return false;
				}
			}, this.getTable(), this.previousId, "INSERT", "UPDATE", "DELETE");
		} catch (SQLException ex) {
			this.console(ex);
			this.stop();
		}

		return false;
	}

	void sendNotification() {
		try {
			this.listener.onDatabaseNotification(this);
		} catch (SQLException ex) {
			this.console(ex);
		}
	}

	/**
	 * Stops this class from listening for further notifications.
	 */
	public synchronized void stop() {
		this.stop(false);
	}

	/**
	 * Stops this class from listening for further notifications, and optionally delete the trigger from the database.
	 *
	 * @param dropTriggers True to delete the triggers, otherwise false.
	 */
	public synchronized void stop(boolean dropTriggers) {
		this.stopped = true;

		if (dropTriggers) {
			for (TriggerEvent event : TriggerEvent.values())
				this.dropTrigger(event);
		}
	}

	private boolean triggersExist() {
		try {
			// SELECT * FROM INFORMATION_SCHEMA.TRIGGERS WHERE TRIGGER_SCHEMA LIKE '%nifty' AND TRIGGER_NAME LIKE '%onnifty';
			return this.sql.query("SELECT TRIGGER_NAME FROM INFORMATION_SCHEMA.TRIGGERS WHERE TRIGGER_SCHEMA = ? AND TRIGGER_NAME IN (?, ?, ?);", new ResultCallback<Boolean>() {
				@Override
				public Boolean handle(ResultSet result) throws SQLException {
					int count = 0;
					while (result.next()) count++;
					return count == 3;
				}
			}, this.getSchema(), this.getName(TriggerEvent.INSERT), this.getName(TriggerEvent.UPDATE), this.getName(TriggerEvent.DELETE));
		} catch (Exception ex) {
			this.console(ex);
		}

		return false;
	}

}
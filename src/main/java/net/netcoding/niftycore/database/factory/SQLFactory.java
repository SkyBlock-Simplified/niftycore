package net.netcoding.niftycore.database.factory;

import net.netcoding.niftycore.database.factory.callbacks.ResultCallback;
import net.netcoding.niftycore.database.factory.callbacks.VoidResultCallback;
import net.netcoding.niftycore.minecraft.scheduler.MinecraftScheduler;
import net.netcoding.niftycore.util.StringUtil;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Properties;
import java.util.UUID;

/**
 * Factory sql classes to be inherited from when creating a wrapper.
 */
public abstract class SQLFactory {

	private final String driver;
	private final boolean driverAvailable;
	private final String url;
	private final Properties properties;
	private String product;
	private String schema;
	private String quote = " ";

	/**
	 * Create a new factory instance.
	 *
	 * @param url  Database connection url.
	 * @param user Username of the database connection.
	 * @param pass Password of the database connection.
	 * @throws SQLException
	 */
	public SQLFactory(String driver, String url, final String user, final String pass) throws SQLException {
		this(driver, url, new Properties() {{ setProperty("user", user); setProperty("password", pass); }});
	}

	/**
	 * Create a new factory instance.
	 *
	 * @param url        Database connection url.
	 * @param properties Properties of the database connection.
	 * @throws SQLException
	 */
	public SQLFactory(String driver, String url, Properties properties) throws SQLException {
		try {
			Class.forName(driver);
			this.driverAvailable = true;
			this.driver = driver;
		} catch (ClassNotFoundException ex) {
			throw new SQLException(StringUtil.format("The specific driver {0} is not available!", driver));
		}

		this.url = url;
		this.properties = properties;
		this.load();
	}

	private static void assignArgs(PreparedStatement statement, Object... args) throws SQLException {
		for (int i = 0; i < args.length; i++) {
			int index = i + 1;
			Object arg = args[i];

			if (arg instanceof byte[])
				statement.setBytes(index, (byte[])arg);
			else if (arg instanceof Byte)
				statement.setByte(index, (byte)arg);
			else if (arg instanceof Boolean)
				statement.setBoolean(index, (boolean)arg);
			else if (arg instanceof Short)
				statement.setShort(index, (short)arg);
			else if (arg instanceof Integer)
				statement.setInt(index, (int)arg);
			else if (arg instanceof Long)
				statement.setLong(index, (long)arg);
			else if (arg instanceof Double)
				statement.setDouble(index, (double)arg);
			else if (arg instanceof Float)
				statement.setFloat(index, (float)arg);
			else if (arg instanceof Blob)
				statement.setBlob(index, (Blob)arg);
			else if (arg instanceof String)
				statement.setString(index, (String)arg);
			else if (arg instanceof UUID)
				statement.setString(index, arg.toString());
			else if (arg == null)
				statement.setNull(index, Types.NULL);
			else
				statement.setObject(index, arg);
		}
	}

	/**
	 * Gets if the given column name exists in the given table for the current DBMS.
	 *
	 * @param tableName  Table name to use.
	 * @param columnName Column name to check existence of.
	 * @return True if column exists, otherwise false.
	 */
	public boolean checkColumnExists(String tableName, String columnName) throws SQLException {
		return this.query("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND (COLUMN_NAME = ? || \"\" = ?);", new ResultCallback<Boolean>() {
			@Override
			public Boolean handle(ResultSet result) throws SQLException {
				return result.next();
			}
		}, this.getSchema(), tableName, columnName, columnName);
	}

	/**
	 * Gets if the given table name exists for the current DBMS.
	 *
	 * @param tableName Table name to check existence of.
	 * @return True if table exists, otherwise false.
	 */
	public boolean checkTableExists(String tableName) throws SQLException {
		return this.checkColumnExists(tableName, "");
	}

	/**
	 * Create a table if it does not exist.
	 *
	 * @param tableName Name of the table.
	 * @param sql       Fields and constrains of the table
	 * @return True if the table was created, otherwise false.
	 * @throws SQLException
	 */
	public boolean createTable(String tableName, String sql) throws SQLException {
		try (Connection connection = this.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				return statement.executeUpdate(StringUtil.format("CREATE TABLE IF NOT EXISTS {0}{1}{0}.{0}{2}{0} ({3}){4};", this.getIdentifierQuoteString(), this.getSchema(), tableName, sql, ("MySQL".equalsIgnoreCase(this.getProduct()) ? " ENGINE=InnoDB" : ""))) > 0;
			}
		}
	}

	/**
	 * Create a table if it does not exist asynchronously.
	 *
	 * @param tableName Name of the table.
	 * @param sql       Table fields and constraints.
	 */
	public void createTableAsync(final String tableName, final String sql) throws SQLException {
		try {
			MinecraftScheduler.runAsync(new Runnable() {
				@Override
				public void run() {
					try (Connection connection = getConnection()) {
						try (Statement statement = connection.createStatement()) {
							statement.executeUpdate(StringUtil.format("CREATE TABLE IF NOT EXISTS {0}{1}{0}.{0}{2}{0} ({3}){4};", getIdentifierQuoteString(), getSchema(), tableName, sql, ("MySQL".equals(getProduct()) ? " ENGINE=InnoDB" : "")));
						}
					} catch (SQLException ignore) { }
				}
			});
		} catch (Exception ex) {
			throw new SQLException(ex);
		}
	}

	/**
	 * Drop a table if it exists.
	 *
	 * @param tableName Name of the table.
	 */
	public void dropTable(String tableName) throws SQLException {
		this.update(StringUtil.format("DROP TABLE IF EXISTS ?;"), tableName);
	}

	/**
	 * Gets a connection to this DBMS.
	 *
	 * @return Connection to the database.
	 */
	protected Connection getConnection() throws SQLException {
		if (!this.isDriverAvailable()) throw new SQLException("The driver for this sql instance is unavailable!");
		return DriverManager.getConnection(this.getUrl(), this.getProperties());
	}

	/**
	 * Gets the registered driver class for this DBMS.
	 *
	 * @return Registered driver class.
	 */
	public final String getDriver() {
		return this.driver;
	}

	/**
	 * Gets the string used to quote identifiers.
	 *
	 * @return Identifier quote, a space " " if unsupported.
	 */
	public final String getIdentifierQuoteString() {
		return this.quote;
	}

	/**
	 * Gets the current DBMS product name.
	 *
	 * @return Name of the product for the current DBMS.
	 */
	public final String getProduct() {
		return this.product;
	}

	/**
	 * Gets the properties used to create a connection for this DBMS.
	 *
	 * @return Connection property details.
	 */
	protected Properties getProperties() {
		return this.properties;
	}

	/**
	 * Gets the schema for this DBMS.
	 *
	 * @return Database name currently being used by connections.
	 */
	public final String getSchema() {
		return this.schema;
	}

	/**
	 * Gets the url for this DBMS.
	 *
	 * @return Url for this DBMS.
	 */
	public String getUrl() {
		return StringUtil.format("{0}?autoReconnectForPools=true&characterEncoding=UTF-8", this.url);
	}

	/**
	 * Gets if the given jdbc driver is available.
	 *
	 * @return True if driver available, otherwise false.
	 */
	public boolean isDriverAvailable() {
		return this.driverAvailable;
	}

	private void load() throws SQLException {
		try (Connection connection = this.getConnection()) {
			this.product = connection.getMetaData().getDatabaseProductName();
			this.quote = connection.getMetaData().getIdentifierQuoteString();
			this.schema = connection.getCatalog();
		}

		if (StringUtil.isEmpty(this.product))
			throw new SQLException("Unable to determine product name!");

		if (StringUtil.isEmpty(this.schema))
			throw new SQLException("Unable to determine schema!");
	}

	/**
	 * Run SELECT query against the DBMS.
	 *
	 * @param sql      Query to run.
	 * @param callback Callback to process results with.
	 * @param args     Arguments to pass to the query.
	 * @return Whatever you decide to return in the callback.
	 * @throws SQLException
	 */
	public <T> T query(String sql, ResultCallback<T> callback, Object... args) throws SQLException {
		try (Connection connection = this.getConnection()) {
			return this.query(connection, sql, callback, args);
		}
	}

	/**
	 * Run SELECT query against the DBMS.
	 *
	 * @param sql      Query to run.
	 * @param callback Callback t process results with.
	 * @param args     Arguments to pass to the query.
	 * @throws SQLException
	 */
	public void query(String sql, VoidResultCallback callback, Object... args) throws SQLException {
		try (Connection connection = this.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				assignArgs(statement, args);
				statement.executeQuery();

				if (callback != null) {
					try (ResultSet result = statement.getResultSet()) {
						callback.handle(result);
					}
				}
			}
		}
	}

	/**
	 * Run SELECT query against the DBMS asynchronously.
	 *
	 * @param sql      Query to run.
	 * @param callback Callback to process results with.
	 * @param args     Arguments to pass to the query.
	 */
	public void queryAsync(final String sql, final VoidResultCallback callback, final Object... args) throws SQLException {
		try {
			MinecraftScheduler.runAsync(new Runnable() {
				@Override
				public void run() {
					try {
						query(sql, callback, args);
					} catch (SQLException ignore) { }
				}
			});
		} catch (Exception ex) {
			throw new SQLException(ex);
		}
	}

	protected final <T> T query(Connection connection, String sql, ResultCallback<T> callback, Object... args) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			assignArgs(statement, args);
			statement.executeQuery();

			if (callback != null) {
				try (ResultSet result = statement.getResultSet()) {
					return callback.handle(result);
				}
			}
		}

		return null;
	}

	/**
	 * Changes the schema currently in use.
	 *
	 * @param schema database name
	 * @return True if correctly set.
	 * @throws SQLException
	 */
	public boolean setSchema(String schema) throws SQLException {
		try (Connection connection = this.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement("USE ?;")) {
				assignArgs(statement, schema);
				boolean result = statement.executeUpdate() > 0;

				if (result)
					this.schema = schema;

				return result;
			}
		}
	}

	/**
	 * Run INSERT, UPDATE or DELETE query against this DBMS.
	 *
	 * @param sql  Query to run.
	 * @param args Arguments to pass to the query.
	 * @return True if query was successful, otherwise false.
	 * @throws SQLException
	 */
	public boolean update(String sql, Object... args) throws SQLException {
		try (Connection connection = this.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				assignArgs(statement, args);
				return statement.executeUpdate() > 0;
			}
		}
	}

	/**
	 * Run INSERT, UPDATE or DELETE query against this DBMS asynchronously.
	 *
	 * @param sql  Query to run.
	 * @param args Arguments to pass to the query.
	 */
	public void updateAsync(final String sql, final Object... args) throws SQLException {
		try {
			MinecraftScheduler.runAsync(new Runnable() {
				@Override
				public void run() {
					try (Connection connection = getConnection()) {
						try (PreparedStatement statement = connection.prepareStatement(sql)) {
							assignArgs(statement, args);
							statement.executeUpdate();
						}
					} catch (SQLException ignore) { }
				}
			});
		} catch (Exception ex) {
			throw new SQLException(ex);
		}
	}

}
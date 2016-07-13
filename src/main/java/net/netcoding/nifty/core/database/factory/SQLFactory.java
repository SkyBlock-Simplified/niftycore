package net.netcoding.nifty.core.database.factory;

import net.netcoding.nifty.core.database.factory.callbacks.ResultCallback;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.database.factory.callbacks.VoidResultCallback;
import net.netcoding.nifty.core.api.scheduler.MinecraftScheduler;

import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Factory sql classes to be inherited from when creating a wrapper.
 */
@SuppressWarnings("JDBCExecuteWithNonConstantString")
public abstract class SQLFactory {

	private final String driver;
	private final boolean driverAvailable;
	private final String url;
	private final Properties properties;
	private final boolean fileStorage;
	private String product;
	private String schema;
	private String quote = " ";

	/**
	 * Create a new factory instance.
	 *
	 * @param driver Connection driver.
	 * @param url Database connection url.
	 * @param user Username of the database connection.
	 * @param pass Password of the database connection.
	 */
	public SQLFactory(String driver, String url, final String user, final String pass) throws SQLException {
		this(driver, url, new Properties() {{ setProperty("user", user); setProperty("password", pass); }});
	}

	/**
	 * Create a new factory instance.
	 *
	 * @param driver Connection driver.
	 * @param url Database connection url.
	 * @param properties Properties of the database connection.
	 */
	public SQLFactory(String driver, String url, Properties properties) throws SQLException {
		try {
			Class.forName(driver);
			this.driverAvailable = true;
			this.driver = driver;
		} catch (ClassNotFoundException cnfex) {
			throw new SQLException(StringUtil.format("The specified driver {0} is not available!", driver), cnfex);
		}

		this.url = url;
		this.properties = properties;
		this.fileStorage = false;
		this.load();
	}

	/**
	 * Create a new factory instance.
	 *
	 * @param driver Connection driver.
	 * @param url Database connection url.
	 * @param directory Directory of local database file.
	 * @param schema Name of database.
	 * @param properties Properties of the database connection.
	 */
	public SQLFactory(String driver, String url, File directory, String schema, Properties properties) throws SQLException {
		try {
			Class.forName(driver);
			this.driverAvailable = true;
			this.driver = driver;
		} catch (ClassNotFoundException cnfex) {
			throw new SQLException(StringUtil.format("The specified driver {0} is not available!", driver), cnfex);
		}

		String schemaName = (schema.endsWith(".db") ? schema.substring(0, schema.length() - 3) : schema);
		String file = StringUtil.format("{0}.db", schemaName);
		File database = new File(directory.getAbsolutePath(), file);

		try {
			if (!database.exists()) {
				if (!directory.exists()) {
					if (!directory.mkdirs())
						throw new IOException(StringUtil.format("Unable to create directory {0}!", directory.getAbsolutePath()));
				}

				if (!database.createNewFile())
					throw new IOException(StringUtil.format("Unable to create file {0}!", file));
			}
		} catch (Exception ex) {
			throw new SQLException(StringUtil.format("Unable to create specified database file {0}!", database.getAbsolutePath()), ex);
		}

		this.url = StringUtil.format(url, directory.getAbsolutePath(), file);
		this.properties = properties;
		this.fileStorage = true;
		this.load(schemaName);
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
			else if (arg instanceof Date)
				statement.setDate(index, (Date)arg);
			else if (arg instanceof Timestamp)
				statement.setTimestamp(index, (Timestamp)arg);
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
		boolean exists;

		try (Connection connection = this.getConnection()) {
			try (ResultSet result = connection.getMetaData().getColumns((this.fileStorage ? null : this.schema), null, tableName, columnName)) {
				exists = result.next();
			}
		}

		return exists;
	}

	/**
	 * Gets if the given table name exists for the current DBMS.
	 *
	 * @param tableName Table name to check existence of.
	 * @return True if table exists, otherwise false.
	 */
	public boolean checkTableExists(String tableName) throws SQLException {
		boolean exists;

		try (Connection connection = this.getConnection()) {
			try (ResultSet result = connection.getMetaData().getTables((this.fileStorage ? null : this.schema), null, tableName, null)) {
				exists = result.next();
			}
		}

		return exists;
	}

	/**
	 * Create a table if it does not exist.
	 *
	 * @param tableName Name of the table.
	 * @param sql       Fields and constrains of the table
	 * @return True if the table was created, otherwise false.
	 */
	public boolean createTable(String tableName, String sql) throws SQLException {
		try (Connection connection = this.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				return statement.executeUpdate(StringUtil.format("CREATE TABLE IF NOT EXISTS {0}{1}{2}{1} ({3}){4};", (this.fileStorage ? "" : StringUtil.format("{0}{1}{0}.", this.getIdentifierQuoteString(), this.getSchema())), this.getIdentifierQuoteString(), tableName, sql, ("MySQL".equalsIgnoreCase(this.getProduct()) ? " ENGINE=InnoDB" : ""))) > 0;
			}
		}
	}

	/**
	 * Create a table if it does not exist asynchronously.
	 *
	 * @param tableName Name of the table.
	 * @param sql       Table fields and constraints.
	 */
	public void createTableAsync(final String tableName, final String sql) {
		MinecraftScheduler.getInstance().runAsync(() -> {
			try {
				createTable(tableName, sql);
			} catch (SQLException ignore) { }
		});
	}

	/**
	 * Drop a table if it exists.
	 *
	 * @param tableName Name of the table.
	 */
	public void dropTable(String tableName) throws SQLException {
		this.update("DROP TABLE IF EXISTS ?;", tableName);
	}

	/**
	 * Get all columns belonging to a table.
	 *
	 * @param tableName Table name to get columns from.
	 * @return All columns belonging to the passed table name.
	 */
	public final Set<String> getColumns(String tableName) throws SQLException {
		Set<String> columns = new HashSet<>();

		try (Connection connection = this.getConnection()) {
			try (ResultSet result = connection.getMetaData().getColumns((this.fileStorage ? null : this.schema), null, tableName, null)) {
				while (result.next())
					columns.add(result.getString("COLUMN_NAME"));
			}
		}

		return columns;
	}

	/**
	 * Gets a connection to this DBMS.
	 *
	 * @return Connection to the database.
	 */
	protected Connection getConnection() throws SQLException {
		if (!this.isDriverAvailable()) throw new SQLException(StringUtil.format("The sql driver {0} is unavailable!", this.getDriver()));
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
	 * Get all tables in the current schema.
	 *
	 * @return All tables names in the current schema.
	 */
	public final Set<String> getTables() throws SQLException {
		Set<String> tables = new HashSet<>();

		try (Connection connection = this.getConnection()) {
			try (ResultSet result = connection.getMetaData().getTables((this.fileStorage ? null : this.schema), null, "%", null)) {
				while (result.next())
					tables.add(result.getString("TABLE_NAME"));
			}
		}

		return tables;
	}

	/**
	 * Gets the url for this DBMS.
	 *
	 * @return Url for this DBMS.
	 */
	public final String getUrl() {
		return StringUtil.format("{0}{1}", this.url, (this.fileStorage ? "" : "?autoReconnectForPools=true&useUnicode=true&characterEncoding=UTF-8"));
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
		this.load(null);
	}

	private void load(String schema) throws SQLException {
		try (Connection connection = this.getConnection()) {
			this.product = connection.getMetaData().getDatabaseProductName();
			this.quote = connection.getMetaData().getIdentifierQuoteString();
			this.schema = connection.getCatalog();
		}

		if (StringUtil.isEmpty(this.schema) && StringUtil.notEmpty(schema))
			this.schema = schema;

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
	 */
	public void query(String sql, VoidResultCallback callback, Object... args) throws SQLException {
		try (Connection connection = this.getConnection()) {
			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				assignArgs(statement, args);

				try (ResultSet result = statement.executeQuery()) {
					if (callback != null)
						callback.handle(result);
				}
			}
		}
	}

	protected final <T> T query(Connection connection, String sql, ResultCallback<T> callback, Object... args) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			assignArgs(statement, args);

			try (ResultSet result = statement.executeQuery()) {
				if (callback != null)
					return callback.handle(result);
			}
		}

		return null;
	}

	/**
	 * Run SELECT query against the DBMS asynchronously.
	 *
	 * @param sql      Query to run.
	 * @param callback Callback to process results with.
	 * @param args     Arguments to pass to the query.
	 */
	public void queryAsync(final String sql, final VoidResultCallback callback, final Object... args) {
		MinecraftScheduler.getInstance().runAsync(() -> {
			try {
				query(sql, callback, args);
			} catch (SQLException ignore) { }
		});
	}

	/**
	 * Changes the schema currently in use.
	 *
	 * @param schema database name
	 * @return True if correctly set.
	 */
	public boolean setSchema(String schema) throws SQLException {
		if (this.fileStorage)
			throw new SQLException("Local databases cannot change schema!");

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
	public void updateAsync(final String sql, final Object... args) {
		MinecraftScheduler.getInstance().runAsync(() -> {
			try (Connection connection = getConnection()) {
				try (PreparedStatement statement = connection.prepareStatement(sql)) {
					assignArgs(statement, args);
					statement.executeUpdate();
				}
			} catch (SQLException ignore) { }
		});
	}

}
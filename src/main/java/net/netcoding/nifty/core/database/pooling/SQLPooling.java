package net.netcoding.nifty.core.database.pooling;

import net.netcoding.nifty.core.NiftyCore;
import net.netcoding.nifty.core.api.scheduler.MinecraftScheduler;
import net.netcoding.nifty.core.database.factory.SQLFactory;
import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.ConcurrentDeque;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Handles database connections with connection pooling functionality.
 */
public abstract class SQLPooling extends SQLFactory {

	private static final int DEFAULT_MIN_CONNECTIONS = 2;
	private static final int DEFAULT_MAX_CONNECTIONS = 10;
	private final transient ConcurrentDeque<Connection> availableConnections = Concurrent.newDeque();
	private final transient ConcurrentDeque<Connection> usedConnections = Concurrent.newDeque();
	private final Object lock = new Object();
	private String validationQuery = "SELECT 1;";
	private int minimumConnections = DEFAULT_MIN_CONNECTIONS;
	private int maximumConnections = DEFAULT_MAX_CONNECTIONS;
	private boolean testOnBorrow = true;
	private boolean firstConnect = true;

	/**
	 * Create a new pooling instance.
	 *
	 * @param url Database connection url.
	 * @param user Username of the database connection.
	 * @param pass Password of the database connection.
	 */
	public SQLPooling(String driver, String url, String user, String pass) throws SQLException {
		super(driver, url, user, pass);
		this.initializeTimer();
	}

	/**
	 * Create a new pooling instance.
	 *
	 * @param url Database connection url.
	 * @param properties Properties of the database connection.
	 */
	public SQLPooling(String driver, String url, Properties properties) throws SQLException {
		super(driver, url, properties);
		this.initializeTimer();
	}

	/**
	 * Create a new pooling instance.
	 *
	 * @param driver Database driver.
	 * @param url Database connection url.
	 * @param directory Directory of local database file.
	 * @param schema Name of database.
	 * @param properties Properties of the database connection.
	 */
	public SQLPooling(String driver, String url, File directory, String schema, Properties properties) throws SQLException {
		super(driver, url, directory, schema, properties);
		this.initializeTimer();
	}

	private void initializeTimer() {
		MinecraftScheduler.getInstance().runAsync(new ConnectionCleaner(), 0, 200 * (NiftyCore.isBungee() ? 50 : 1));
	}

	/**
	 * Gets a connection from connection pool.
	 *
	 * @return Connection to the database.
	 * @throws SQLException When connection is not available immediately.
	 */
	@Override
	protected final Connection getConnection() throws SQLException {
		return this.getConnection(WaitTime.IMMEDIATELY);
	}

	private void initializeConnections() throws SQLException {
		if (!this.firstConnect) return;
		this.firstConnect = false;

		for (int i = 0; i < this.getMinimumConnections(); i++)
			this.availableConnections.offer(this.getConnection());
	}

	/**
	 * Gets a connection from connection pool, waiting if necessary.
	 *
	 * @param waitTime     Time to wait for a connection.
	 * @return Connection to the database.
	 * @throws SQLException When connection is not available within given wait time.
	 */
	protected final Connection getConnection(WaitTime waitTime) throws SQLException {
		Connection connection;

		if (this.availableConnections != null) {
			this.initializeConnections();

			synchronized (this.lock) {
				if (this.availableConnections.isEmpty()) {
					if (this.usedConnections.size() < this.getMaximumConnections())
						this.usedConnections.offer(connection = new RecoverableConnection(super.getConnection(), this));
					else {
						if (waitTime == WaitTime.IMMEDIATELY)
							throw new SQLException("Failed to borrow connection from the available pool!");

						try {
							Thread.sleep(waitTime.getWaitTime());
						} catch (InterruptedException ignore) { }

						connection = this.getConnection();
					}
				} else {
					connection = this.availableConnections.remove();
					this.usedConnections.offer(connection);
				}
			}

			if (connection.isClosed())
				connection = new RecoverableConnection(super.getConnection(), this);
			else {
				if (this.isTestingOnBorrow()) {
					try {
						this.query(connection, this.getValidationQuery(), null);
					} catch (SQLException sqlex) {
						this.usedConnections.remove(connection);
						connection = this.getConnection(WaitTime.IMMEDIATELY);
					}
				}
			}
		} else
			connection = super.getConnection();

		return connection;
	}

	/**
	 * Gets the minimum number of concurrent connections.
	 *
	 * @return Minimum number of connections to be stored in the pool.
	 */
	public final int getMinimumConnections() {
		return this.minimumConnections;
	}

	/**
	 * Gets the maximum number of concurrent connections.
	 *
	 * @return Maximum number of connections to be stored in the pool.
	 */
	public final int getMaximumConnections() {
		return this.maximumConnections;
	}

	/**
	 * Gets the query used to test for valid connections
	 * before returning them from the pool.
	 *
	 * @return Query to run the test with.
	 */
	protected final String getValidationQuery() {
		return this.validationQuery;
	}

	/**
	 * Gets if the connection is being tested before
	 * being returned from the pool.
	 *
	 * @return True if tested, otherwise false.
	 */
	protected final boolean isTestingOnBorrow() {
		return this.testOnBorrow;
	}

	void recycle(Connection connection) {
		this.usedConnections.remove(connection);
		this.availableConnections.offer(connection);
	}

	/**
	 * Sets the minimum number of concurrent connections.
	 *
	 * @param count Minimum number of connections to have available.
	 */
	public final void setMinimumConnections(int count) {
		count = count < 0 ? DEFAULT_MIN_CONNECTIONS : count;
		count = count > this.getMaximumConnections() ? this.getMaximumConnections() : count;
		this.minimumConnections = count;
	}

	/**
	 * Sets the maximum number of concurrent connections.
	 *
	 * @param count Maximum number of connections to have available.
	 */
	public final void setMaximumConnections(int count) {
		count = count <= this.getMinimumConnections() ? this.getMinimumConnections() + 1 : count;
		this.maximumConnections = count;
	}

	/**
	 * Sets whether or not to test the connection when it
	 * is requested from the pool.
	 *
	 * @param value True to test, otherwise false.
	 */
	protected final void setTestOnBorrow(boolean value) {
		this.testOnBorrow = value;
	}

	/**
	 * Sets the query to be used to test for valid connections
	 * before returning them from the pool.
	 *
	 * @param query Query to run the test with.
	 */
	protected final void setValidationQuery(String query) {
		this.validationQuery = query;
	}

	private class ConnectionCleaner implements Runnable {

		@Override
		public void run() {
			while (SQLPooling.this.availableConnections.size() > SQLPooling.this.getMinimumConnections()) {
				Connection connection = SQLPooling.this.availableConnections.removeLast();

				try {
					if (!connection.isClosed())
						((RecoverableConnection)connection).closeOnly();
				} catch (SQLException ignore) { }
			}
		}

	}

}
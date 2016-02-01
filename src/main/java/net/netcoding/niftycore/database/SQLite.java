package net.netcoding.niftycore.database;

import net.netcoding.niftycore.database.factory.SQLWrapper;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

public class SQLite extends SQLWrapper {

	public SQLite(File folder, String schema) throws SQLException {
		super("org.sqlite.JDBC", "jdbc:sqlite:{0}/{1}", folder, schema, new Properties());
	}

	/**
	 * Checks if the SQLite jdbc driver is available.
	 *
	 * @return True if available, otherwise false.
	 */
	@Override
	public final boolean isDriverAvailable() {
		return super.isDriverAvailable();
	}

}
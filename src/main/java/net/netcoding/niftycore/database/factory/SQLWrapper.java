package net.netcoding.niftycore.database.factory;

import net.netcoding.niftycore.database.notifications.SQLNotifications;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

public abstract class SQLWrapper extends SQLNotifications {

	public static final int DEFAULT_PORT = 0;

	public SQLWrapper(String driver, String url, String user, String pass) throws SQLException {
		super(driver, url, user, pass);
	}

	public SQLWrapper(String driver, String url, Properties properties) throws SQLException {
		super(driver, url, properties);
	}

	public SQLWrapper(String driver, String url, File directory, String schema, Properties properties) throws SQLException {
		super(driver, url, directory, schema, properties);
	}

}
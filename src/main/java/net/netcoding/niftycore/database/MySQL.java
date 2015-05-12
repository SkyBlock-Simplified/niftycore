package net.netcoding.niftycore.database;

import java.sql.SQLException;

import net.netcoding.niftycore.database.factory.SQLWrapper;
import net.netcoding.niftycore.util.StringUtil;

public class MySQL extends SQLWrapper {

	public static final int DEFAULT_PORT = 3306;

	public MySQL(String host, String user, String pass, String schema) throws SQLException {
		this(host, DEFAULT_PORT, user, pass, schema);
	}

	public MySQL(String host, int port, String user, String pass, String schema) throws SQLException {
		super("com.mysql.jdbc.Driver", StringUtil.format("jdbc:mysql://{0}:{1,number,#}/{2}", host, port, schema), user, pass);
	}

	/**
	 * Checks if the MySQL jdbc driver is available.
	 * 
	 * @return True if available, otherwise false.
	 */
	@Override
	public boolean isDriverAvailable() {
		return super.isDriverAvailable();
	}

}
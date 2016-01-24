package net.netcoding.niftycore.database;

import net.netcoding.niftycore.database.factory.SQLWrapper;
import net.netcoding.niftycore.util.StringUtil;

import java.io.File;
import java.sql.SQLException;

public class SQLite extends SQLWrapper {

	public SQLite(File folder, String schema) throws SQLException {
		super("org.sqlite.JDBC", StringUtil.format("jdbc:sqlite://{0}/{1}.db", (folder.isDirectory() ? folder.getAbsolutePath() : folder.getParent()), schema), "", "");
	}

	/**
	 * Checks if the SQLite jdbc driver is available.
	 *
	 * @return True if available, otherwise false.
	 */
	@Override
	public boolean isDriverAvailable() {
		return super.isDriverAvailable();
	}

}
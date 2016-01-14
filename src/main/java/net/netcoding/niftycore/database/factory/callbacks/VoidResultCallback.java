package net.netcoding.niftycore.database.factory.callbacks;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface VoidResultCallback {

	void handle(ResultSet result) throws SQLException;

}
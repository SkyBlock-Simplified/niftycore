package net.netcoding.nifty.core.database.factory.callbacks;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultCallback<T> {

	T handle(ResultSet result) throws SQLException;

}
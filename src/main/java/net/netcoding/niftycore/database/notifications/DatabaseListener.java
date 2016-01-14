package net.netcoding.niftycore.database.notifications;

import java.sql.SQLException;

public interface DatabaseListener {

	void onDatabaseNotification(final DatabaseNotification databaseNotification) throws SQLException;

}
package rcms.utilities.daqaggregator.datasource;

import java.sql.DriverManager;
import java.sql.SQLException;
import rcms.common.db.AbstractDBConnector;
import rcms.common.db.DBConnectorException;

/**
 * class to access hardware database information in an SQLite database
 */
public class DBConnectorSqlite extends AbstractDBConnector {

	DBConnectorSqlite(String url) {
		this.url = url;
		super.startQueryWatcher();
	}

	@Override
	public void openConnection() throws DBConnectorException {

		if (!queryWatcher.isAlive()) {
			super.startQueryWatcher();
		}

		try {
			this.connection = DriverManager.getConnection(url);
		} catch (SQLException ex) {
			throw new DBConnectorException("failed to connect to database " + url, ex);
		}
	}
}

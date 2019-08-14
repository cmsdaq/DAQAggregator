package rcms.utilities.daqaggregator.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.commons.dbcp.DelegatingConnection;
import rcms.common.db.AbstractDBConnector;
import rcms.common.db.DBConnectorException;

/**
 * class wrapping around a SQLite connection object to rewrite certain SQL
 * queries
 *
 * @author holzner
 */
class RewritingConnection extends DelegatingConnection {

	public RewritingConnection(Connection conn) {
		super(conn);
	}

	// original query to catch
	private static final String refQuery1 = "WITH "
					+ "  ho AS (SELECT * FROM DAQ_EQCFG_HOST WHERE eqset_id = ?), "
					+ "  ha AS (SELECT * FROM DAQ_EQCFG_HOST_ATTRIBUTE WHERE eqset_id = ?), "
					+ "  hn AS (SELECT * FROM DAQ_EQCFG_HOST_NIC WHERE eqset_id = ?), "
					+ "  ni AS (SELECT * FROM DAQ_EQCFG_NIC WHERE eqset_id = ?), "
					+ "  dn AS (SELECT * FROM DAQ_EQCFG_DNSNAME WHERE eqset_id = ?) "
					+ "SELECT ho.host_id, "
					+ "       ho.ncores, "
					+ "       ha.attr_name, "
					+ "       ha.attr_value, "
					+ "       ni.nic_id, "
					+ "       ni.device_name, "
					+ "       dn.dnsname, "
					+ "       dn.network_name "
					+ "FROM ho, ha, hn, ni, dn WHERE  "
					+ "   ho.host_id = hn.host_id AND"
					+ "   ha.host_id(+) = ho.host_id AND"
					+ "   hn.nic_id = ni.nic_id AND"
					+ "   ni.nic_id = dn.nic_id ORDER BY ho.host_id";

	// substitution query
	private static final String subsQuery1 = "WITH "
					+ "  ho AS (SELECT * FROM DAQ_EQCFG_HOST WHERE eqset_id = ?), "
					+ "  ha AS (SELECT * FROM DAQ_EQCFG_HOST_ATTRIBUTE WHERE eqset_id = ?), "
					+ "  hn AS (SELECT * FROM DAQ_EQCFG_HOST_NIC WHERE eqset_id = ?), "
					+ "  ni AS (SELECT * FROM DAQ_EQCFG_NIC WHERE eqset_id = ?), "
					+ "  dn AS (SELECT * FROM DAQ_EQCFG_DNSNAME WHERE eqset_id = ?) "
					+ "SELECT ho.host_id, "
					+ "       ho.ncores, "
					+ "       ha.attr_name, "
					+ "       ha.attr_value, "
					+ "       ni.nic_id, "
					+ "       ni.device_name, "
					+ "       dn.dnsname, "
					+ "       dn.network_name "
					+ "FROM ho, ha, hn, ni, dn WHERE  "
					+ "   ho.host_id = hn.host_id AND"
					+ //	"   ha.host_id(+) = ho.host_id AND" +
					"   ha.host_id = ho.host_id AND"
					+ "   hn.nic_id = ni.nic_id AND"
					+ "   ni.nic_id = dn.nic_id ORDER BY ho.host_id";

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {

		// TODO: should ignore/collapse multiple consecutive whitespace
		//       during comparison for somewhat more robust comparison
		if (sql.equals(refQuery1)) {
			sql = subsQuery1;
		}

		return super.prepareStatement(sql);
	}
}

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
			this.connection = new RewritingConnection(DriverManager.getConnection(url));
		} catch (SQLException ex) {
			throw new DBConnectorException("failed to connect to database " + url, ex);
		}
	}
}

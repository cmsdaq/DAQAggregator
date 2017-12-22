package rcms.utilities.daqaggregator.datasource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import rcms.common.db.DBConnectorException;
import rcms.common.db.DBConnectorIF;
import rcms.common.db.preparedstatement.DBPreparedStatement;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;
import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.dp.DAQPartitionSet;

/**
 * given a eqset path, produces a sqlite database with the required entries
 * copied from the hardware database
 */
public class HwdbToSqlite {

	private final List<DAQPartition> daqPartitions = new ArrayList<DAQPartition>();

	/** maps from numeric SQL type to String */
	private static final Map<Integer, String> sqlTypesMap = makeSqlTypesMap();
	
	private static class IdTriple {

		private final long eqsetId;
		private final long fbsetId;
		private final long dpsetId;

		public IdTriple(long eqsetId, long fbsetId, long dpsetId) {
			this.eqsetId = eqsetId;
			this.fbsetId = fbsetId;
			this.dpsetId = dpsetId;
		}

		public String makeSqlFilterQuery(boolean useEqsetId, boolean useFbsetId, boolean useDpsetId) {

			List<String> filters = new ArrayList<>();

			if (useEqsetId) {
				filters.add("EQSET_ID = " + eqsetId);
			}

			if (useFbsetId) {
				filters.add("FBSET_ID = " + fbsetId);
			}

			if (useDpsetId) {
				filters.add("DPSET_ID = " + dpsetId);
			}

			if (filters.isEmpty()) {
				return null;
			} else {
				return StringUtils.join(filters, " AND ");
			}
		}

	}

	private static Map<Integer, String> makeSqlTypesMap() {

		Map<Integer, String> result = new HashMap<>();

		for (Field field : Types.class.getFields()) {
			try {
				result.put((Integer) field.get(null), field.getName());
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				Logger.getLogger(HwdbToSqlite.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		return result;

	}

	private static Connection connectSqlite(File dbfile) throws SQLException {
		String url = "jdbc:sqlite:" + dbfile.getAbsolutePath();
		return DriverManager.getConnection(url);
	}

	/**
	 * @return a list of column names in the given table
	 */
	List<String> getColumnNames(DBConnectorIF dbconnSrc, String tableName) throws DBConnectorException, SQLException {

		ResultSet res = dbconnSrc.DBExecuteQuery("select * from " + tableName + " where 1 = 0");
		ResultSetMetaData metaData = res.getMetaData();

		List<String> result = new ArrayList<String>();

		// note the one based indexing
		for (int col = 1; col <= metaData.getColumnCount(); ++col) {

			String colName = metaData.getColumnName(col);
			result.add(colName.toUpperCase());
		}

		return result;
	}

	/**
	 * given the source database connector and a table name, creates an (empty)
	 * table in the destination database. (Note: does not copy any other things
	 * like default values, constraints etc.)
	 */
	private void copyTableSchema(DBConnectorIF dbconnSrc,
					Connection dbconnDest, String tableName) throws DBConnectorException, SQLException {

		// get table description
		// setting the table name as a parameter does not work...
		DBPreparedStatement dbps = new DBPreparedStatement("select * from " + tableName + " where 1 = 0");

		ResultSet res = dbconnSrc.DBPreparedStatementExecuteQuery(dbps);

		ResultSetMetaData metaData = res.getMetaData();

		// note the one based indexing
		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE IF NOT EXISTS ");
		query.append(tableName);
		query.append(" (");

		for (int col = 1; col <= metaData.getColumnCount(); ++col) {

			String colName = metaData.getColumnName(col);
			String typeName = sqlTypesMap.get(metaData.getColumnType(col));

			if (col > 1) {
				query.append(", ");
			}

			query.append(colName);
			query.append(" ");
			query.append(typeName);
		}
		query.append(" )");

		// create the table
		Statement stmt = dbconnDest.createStatement();
		stmt.execute(query.toString());
	}

	/**
	 * copies table content from source to destination.
	 *
	 * @param condition text added after the where clause to filter rows to be
	 * copied. May be null.
	 */
	private void copyTableContent(DBConnectorIF dbconnSrc,
					Connection dbconnDest, String tableName, String condition) throws DBConnectorException, SQLException {

		String query = "select * from " + tableName;

		if (condition != null) {
			query += " where " + condition;
		}

		// fetch data from the source DB
		ResultSet res = dbconnSrc.DBExecuteQuery(query);

		// make the output prepared statement from the result set meta data
		StringBuilder destStmtCmd = new StringBuilder(), sbValues = new StringBuilder();
		destStmtCmd.append("INSERT INTO ");
		destStmtCmd.append(tableName);
		destStmtCmd.append(" (");

		sbValues.append(" VALUES (");

		ResultSetMetaData metaData = res.getMetaData();

		int numCols = metaData.getColumnCount();

		for (int col = 1; col <= numCols; ++col) {

			if (col > 1) {
				destStmtCmd.append(", ");
				sbValues.append(", ");
			}

			destStmtCmd.append(metaData.getColumnName(col));

			sbValues.append("?");

		}

		destStmtCmd.append(")");
		sbValues.append(")");

		destStmtCmd.append(sbValues);

		PreparedStatement stmtDest = dbconnDest.prepareStatement(destStmtCmd.toString());

		// copy the data
		while (res.next()) {

			for (int col = 1; col <= numCols; ++col) {

				stmtDest.setObject(col, res.getObject(col));

			} // loop over columns

			stmtDest.execute();
		} // loop over rows
	}

	/**
	 * This function is intended to be used with the dpset, fbset and eqset tables
	 * to get the minimal set of ids to be copied.
	 *
	 * @return a set of ids which are found on the path from the given leaves in
	 * idValues to the root ('top directory') of the hierarchy
	 */
	private Set<Long> findIdPath(DBConnectorIF dbconnSrc, String tableName, String idVar,
					Collection<Long> idValues) throws DBConnectorException, SQLException {

		Set<Long> result = new HashSet<>();

		for (Long idValue : idValues) {

			while (idValue != null) {

				if (result.contains(idValue)) {
					// we're on a part of a path now we already have followed, 
					// no need to continue
					break;
				}

				result.add(idValue);
				// find parent
				// TODO: should properly close this result set in case of failures
				ResultSet res = dbconnSrc.DBExecuteQuery("SELECT parent_id FROM " + tableName
								+ " WHERE " + idVar + " = " + idValue);

				boolean rowFetched = false;

				Long parentIdValue = 0l;

				while (res.next()) {

					rowFetched = true;
					parentIdValue = res.getLong(1);

					if (res.wasNull()) {
						parentIdValue = null;
					}
					break;
				}

				if (!rowFetched) {
					throw new Error("could not find parent of id " + idValue);
				}

				idValue = parentIdValue;

			} // while the top of the hierarchy was not reached

		} // loop over given id values

		return result;
	}

	/**
	 * generates a SQL query condition to only include the given ids in the copy
	 * of the table (used for fb, dp and eq set tables).
	 */
	private String makeIdPathCondition(DBConnectorIF dbconnSrc, String tableName,
					String idVar, Collection<Long> idValues) throws DBConnectorException, SQLException {

		// get the set of ids to select
		Set<Long> ids = findIdPath(dbconnSrc, tableName, idVar, idValues);

		// make the query
		List<String> filters = new ArrayList<String>();

		for (Long id : ids) {
			filters.add(idVar + " = " + id);
		}

		return StringUtils.join(filters, " OR ");

	}

	private Set<Long> getEqSetIds(List<IdTriple> ids) {
		Set<Long> result = new HashSet<>();

		for (IdTriple idt : ids) {
			result.add(idt.eqsetId);
		}

		return result;
	}

	private Set<Long> getDpSetIds(List<IdTriple> ids) {
		Set<Long> result = new HashSet<>();

		for (IdTriple idt : ids) {
			result.add(idt.dpsetId);
		}

		return result;
	}

	private Set<Long> getFbSetIds(List<IdTriple> ids) {
		Set<Long> result = new HashSet<>();

		for (IdTriple idt : ids) {
			result.add(idt.fbsetId);
		}

		return result;
	}

	private void copyTable(DBConnectorIF dbconnSrc,
					Connection dbconnDest, String tableName,
					List<IdTriple> ids)
					throws DBConnectorException, SQLException {

		tableName = tableName.toUpperCase();

		// copy the table schema
		copyTableSchema(dbconnSrc, dbconnDest, tableName);

		List<String> filters = new ArrayList<String>();

		List<String> colNames = this.getColumnNames(dbconnSrc, tableName);

		// for the tables which contain the list of eq, fb and dp sets
		// we copy only the necessary entries (requested ids and their parents)
		// so that when we run tests on a dpset path which was not copied
		// to the sqlite file, we get an exception about the missing dpset path
		// and not an error elsewhere because e.g. a dpset was returned
		// but it does not contain any FEDs etc.
		if (tableName.equals("DAQ_EQCFG_EQSET")) {

			filters.add(makeIdPathCondition(dbconnSrc, tableName, "EQSET_ID", this.getEqSetIds(ids)));

		} else if (tableName.equals("DAQ_DPCFG_DPSET")) {

			filters.add(makeIdPathCondition(dbconnSrc, tableName, "DPSET_ID", this.getDpSetIds(ids)));

		} else if (tableName.equals("DAQ_FBCFG_FBSET")) {

			filters.add(makeIdPathCondition(dbconnSrc, tableName, "FBSET_ID", this.getFbSetIds(ids)));

		} else {
			// other tables

			for (IdTriple id : ids) {
				String filterExpr = id.makeSqlFilterQuery(
								colNames.contains("EQSET_ID"),
								colNames.contains("FBSET_ID"),
								colNames.contains("DPSET_ID")
				);

				if (filterExpr != null) {
					filters.add("(" + filterExpr + ")");
				}
			}
		}

		String condition = null;

		if (!filters.isEmpty()) {
			condition = StringUtils.join(filters, " OR ");
		} // if filter not empty

		this.copyTableContent(dbconnSrc, dbconnDest, tableName, condition);
	}

	/**
	 * convenience function: copy one single path from the hardware database to an
	 * sqlite file
	 */
	void run(String hwcfgPath, File sqliteOutput) throws SQLException, DBConnectorException,
					HardwareConfigurationException, IOException, PathNotFoundException,
					InvalidNodeTypeException, IllegalArgumentException, IllegalAccessException {

		List<String> paths = new ArrayList<String>();
		paths.add(hwcfgPath);

		run(paths, sqliteOutput);
	}

	/**
	 * copies the tables from the source database to the sqlite database
	 */
	void run(List<String> hwcfgPaths, File sqliteOutput) throws SQLException,
					DBConnectorException, HardwareConfigurationException, IOException,
					PathNotFoundException, InvalidNodeTypeException, IllegalArgumentException,
					IllegalAccessException {

		// retrieve from the hardware database
		HardwareConnector hardwareConnector = new HardwareConnector();
		Properties prop = new Properties();
		prop.load(new FileInputStream("DAQAggregator.properties"));

		// connect to the hardware database
		hardwareConnector.initialize(prop);

		// the list of dpset / fbset and eqset ids to be copied
		List<IdTriple> ids = new ArrayList<>();

		// get the daq partition objects
		for (String hwcfgPath : hwcfgPaths) {

			DAQPartition daqPartition = hardwareConnector.getPartition(hwcfgPath);
			daqPartitions.add(daqPartition);

			DAQPartitionSet dpset = daqPartition.getDAQPartitionSet();

			ids.add(new IdTriple(
							dpset.getFEDBuilderSet().getEquipmentSet().getId(),
							dpset.getFEDBuilderSet().getId(),
							dpset.getId()
			));

		}

		// create the output file
		// access the hardware database ourselves
		DBConnectorIF dbconnSrc = HardwareConnector.getDbconn();
		Connection dbconnDest = connectSqlite(sqliteOutput);

		// tables we need to copy
		final String tables[] = {
			"DAQ_DPCFG_DAQPARTITION",
			"DAQ_DPCFG_DPGENERICHOST",
			"DAQ_DPCFG_DPGH_PARAMETER",
			"DAQ_DPCFG_DPPROPERTY",
			"DAQ_DPCFG_DPSET",
			"DAQ_DPCFG_RU",
			"DAQ_DPCFG_SOURCE_PORT",
			"DAQ_EQCFG_DBPS",
			"DAQ_EQCFG_DNSNAME",
			"DAQ_EQCFG_EQSET",
			"DAQ_EQCFG_FED",
			"DAQ_EQCFG_FEDDEPENDENCY",
			"DAQ_EQCFG_FMM",
			"DAQ_EQCFG_FMMCRATE",
			"DAQ_EQCFG_FMMFMM",
			"DAQ_EQCFG_FMMTRIGGER",
			"DAQ_EQCFG_FRL",
			"DAQ_EQCFG_FRLCRATE",
			"DAQ_EQCFG_FRL_NIC",
			"DAQ_EQCFG_HOST",
			"DAQ_EQCFG_HOST_ATTRIBUTE",
			"DAQ_EQCFG_HOST_NIC",
			"DAQ_EQCFG_HOST_ROLE",
			"DAQ_EQCFG_HOST_ROLE_MAP",
			"DAQ_EQCFG_LINECARD",
			"DAQ_EQCFG_NETWORK",
			"DAQ_EQCFG_NETWORK_NETWORK",
			"DAQ_EQCFG_NETWORK_ROLE",
			"DAQ_EQCFG_NIC",
			"DAQ_EQCFG_NIC_PORT",
			"DAQ_EQCFG_PORT",
			"DAQ_EQCFG_PORT_PORT",
			"DAQ_EQCFG_SUBSYSTEM",
			"DAQ_EQCFG_SWITCH",
			"DAQ_EQCFG_TCDS_ICI",
			"DAQ_EQCFG_TCDS_PM",
			"DAQ_EQCFG_TRIGGER",
			"DAQ_EQCFG_TTCPARTITION",
			"DAQ_FBCFG_FBI",
			"DAQ_FBCFG_FBSET",
			"DAQ_FBCFG_FEDBUILDER",
			"DAQ_HWCONF_VERSION"
		};

		for (String table : tables) {

			this.copyTable(dbconnSrc, dbconnDest, table,
							ids);
		}

		dbconnDest.close();
		dbconnSrc.closeConnection();

	}

	public List<DAQPartition> getDaqPartitions() {
		return daqPartitions;
	}

	/**
	 * entry point for the command line application to write an sqlite file with
	 * information taken from the configured database (typically the central
	 * hardware database).
	 */
	public static void main(String argv[]) throws DBConnectorException, IOException,
					HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException,
					SQLException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException {

		if (argv.length < 2) {
			System.err.println();
			System.err.println("Utility program to extract a DAQPartition from the");
			System.err.println("hardware database (as described in DAQAggregator.properties)");
			System.err.println("given a DPSet path and an sqlite output file.");
			System.err.println("usage:");
			System.err.println();
			System.err.println("   <prog> dpset-path2 [ dpset-path2 ... ] output.sqlite");
			System.err.println();
			System.err.println("WARNING: will overwrite the output file without");
			System.err.println("         prior notification if it exists already.");
			System.err.println("         Note also that if the output file is an");
			System.err.println("         existing sqlite file it may be appended to");
			System.err.println("         instead of overwriting it.");
			System.err.println();
			System.exit(1);
		}

		// ensure the sqlite driver is registered
		Class.forName("org.sqlite.JDBC");

		List<String> paths = new ArrayList<String>();

		for (int i = 0; i < argv.length - 1; ++i) {
			paths.add(argv[i]);
		}

		String outputFname = argv[argv.length - 1];

		File sqliteOutput = new File(outputFname);
		new HwdbToSqlite().run(paths, sqliteOutput);
	}
}

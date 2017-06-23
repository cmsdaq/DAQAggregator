package rcms.utilities.daqaggregator.datasource;

import java.util.Properties;
import rcms.common.db.DBConnectorException;
import rcms.common.db.DBConnectorIF;
import rcms.common.db.DBConnectorMySQL;
import rcms.common.db.DBConnectorOracle;
import rcms.utilities.daqaggregator.Settings;
import rcms.utilities.hwcfg.HWCfgConnector;
import rcms.utilities.hwcfg.HWCfgDescriptor;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;
import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.dp.DAQPartitionSet;

public class HardwareConnector {

	protected static DBConnectorIF _dbconn = null;
	protected static HWCfgConnector _hwconn = null;

	public DAQPartition getPartition(String _dpsetPath)
			throws HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {
		HWCfgDescriptor dp_node = _hwconn.getNode(_dpsetPath);
		DAQPartitionSet dpset = _hwconn.retrieveDPSet(dp_node);
		return dpset.getDPs().values().iterator().next();
	}

	public void initialize(String url, String host, String port, String sid, String user, String passwd)
			throws DBConnectorException, HardwareConfigurationException {
		String _dbType = "ORACLE";
		if (url == null || url.isEmpty()) {
			url = "jdbc:oracle:thin:@" + host + ":" + port + "/" + sid;
		}

		if (_dbType.equals("ORACLE"))
			_dbconn = new DBConnectorOracle(url, user, passwd);
		else
			_dbconn = new DBConnectorMySQL(url, user, passwd);

		_hwconn = new HWCfgConnector(_dbconn);
	}

	public void initialize(Properties prop) throws DBConnectorException,
					HardwareConfigurationException {

		String url     = prop.getProperty(Settings.HWCFGDB_DBURL.getKey());
		String host    = prop.getProperty(Settings.HWCFGDB_HOST.getKey());
		String port    = prop.getProperty(Settings.HWCFGDB_PORT.getKey());
		String sid     = prop.getProperty(Settings.HWCFGDB_SID.getKey());
		String user    = prop.getProperty(Settings.HWCFGDB_LOGIN.getKey());
		String passwd  = prop.getProperty(Settings.HWCFGDB_PWD.getKey());

		initialize(url, host, port, sid, user, passwd);
	}
}

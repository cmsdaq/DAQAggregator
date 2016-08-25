package rcms.utilities.daqaggregator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.common.db.DBConnectorIF;
import rcms.common.db.DBConnectorMySQL;
import rcms.common.db.DBConnectorOracle;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.mappers.FlashlistManager;
import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.mappers.PostProcessor;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.PersistorManager;
import rcms.utilities.hwcfg.HWCfgConnector;
import rcms.utilities.hwcfg.HWCfgDescriptor;
import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.dp.DAQPartitionSet;

public class DAQAggregator {

	protected static DBConnectorIF _dbconn = null;
	protected static HWCfgConnector _hwconn = null;

	protected static String _dpsetPath = null;
	protected static int _sid = -1;

	protected static boolean _dpsetPathChanged = false;
	protected static boolean _sidChanged = false;

	private static final Logger logger = Logger.getLogger(DAQAggregator.class);

	public static void main(String[] args) {
		String propertiesFile = "DAQAggregator.properties";
		if (args.length > 0)
			propertiesFile = args[0];
		logger.info("DAQAggregator started with properties file '" + propertiesFile + "'");

		Application.initialize(propertiesFile);
		work();

	}

	public static void work() {

		try {

			setUpDBConnection();
			setUpSOCKSProxy();

			logger.debug("Read the following LAS URLs:");
			String[] lasURLs = Application.get().getProp().getProperty(Application.PROPERTYNAME_MONITOR_URLS)
					.split(" +");
			for (String lasUrl : lasURLs)
				logger.debug("   '" + lasUrl + "'");

			DAQPartition dp = null;

			MappingManager mappingManager = null;
			DAQ daq = null;
			Set<String> flashlistUrls = new HashSet<String>(Arrays.asList(lasURLs));

			String snapshotPersistenceDir = Application.get().getProp()
					.getProperty(Application.PERSISTENCE_SNAPSHOT_DIR);
			String flashlistPersistenceDir = Application.get().getProp()
					.getProperty(Application.PERSISTENCE_FLASHLIST_DIR);

			/*
			 * Run mode from properties file
			 */
			String runModeValue = Application.get().getProp().getProperty(Application.PERSISTENCE_MODE);
			PersistMode runMode = PersistMode.decode(runModeValue);
			logger.info("Run mode:" + runMode);

			/*
			 * Format of snapshot from properties file
			 */
			PersistenceFormat flashlistFormat = PersistenceFormat
					.decode(Application.get().getProp().getProperty(Application.PERSISTENCE_FLASHLIST_FORMAT));
			PersistenceFormat snapshotFormat = PersistenceFormat
					.decode(Application.get().getProp().getProperty(Application.PERSISTENCE_SNAPSHOT_FORMAT));

			PersistorManager persistorManager = new PersistorManager(snapshotPersistenceDir, flashlistPersistenceDir,
					snapshotFormat, flashlistFormat);

			FlashlistManager flashlistManager = null;

			while (true) {

				try {
					_dpsetPathChanged = false;
					_sidChanged = false;
					autoDetectSession(
							Application.get().getProp().getProperty(Application.PROPERTYNAME_SESSION_LASURL_GE),
							Application.get().getProp().getProperty(Application.PROPERTYNAME_SESSION_L0FILTER1),
							Application.get().getProp().getProperty(Application.PROPERTYNAME_SESSION_L0FILTER2));

					if (_dpsetPathChanged || _sidChanged) {
						logger.info("Session has changed.");
						logger.info("Loading DPSet '" + _dpsetPath + "' ...");
						HWCfgDescriptor dp_node = _hwconn.getNode(_dpsetPath);
						DAQPartitionSet dpset = _hwconn.retrieveDPSet(dp_node);
						dp = dpset.getDPs().values().iterator().next();

						// map the structure to new DAQ
						mappingManager = new MappingManager(dp);
						daq = mappingManager.map();
						daq.setSessionId(_sid);
						daq.setDpsetPath(_dpsetPath);
						flashlistManager = new FlashlistManager(flashlistUrls, mappingManager, _sid);
						flashlistManager.retrieveAvailableFlashlists();

						logger.info("Done for session " + daq.getSessionId());
					}

					if (flashlistManager != null) {

						switch (runMode) {
						case SNAPSHOT:
							flashlistManager.downloadAndMapFlashlists();
							finalizeSnapshot(daq);
							persistorManager.persistSnapshot(daq);
							break;
						case FLASHLIST:
							flashlistManager.downloadFlashlists(true);
							persistorManager.persistFlashlists(flashlistManager);
							break;
						case ALL:
							flashlistManager.downloadFlashlists(true);
							flashlistManager.mapFlashlists();
							finalizeSnapshot(daq);
							persistorManager.persistSnapshot(daq);
							persistorManager.persistFlashlists(flashlistManager);
							break;
						case CONVERT:
							break;
						}
					} else {
						logger.warn("Flashlist manager not initialized, session id not available");
					}

					// FIXME: the timer should be used here as sleep time !=
					// period time
					logger.debug("sleeping for 2 seconds ....\n");
					Thread.sleep(2000);
				} catch (Exception e) {
					logger.error("Error in main loop:", e);
					logger.info("Going to sleep for 30 seconds before trying again...\n");
					Thread.sleep(30000);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void finalizeSnapshot(DAQ daq) {
		daq.setLastUpdate(System.currentTimeMillis());
		// postprocess daq (derived values, summary classes)
		PostProcessor postProcessor = new PostProcessor(daq);
		postProcessor.postProcess();
	}

	/**
	 * 
	 * @param lasBaseURLge
	 * @param l0_filter1
	 * @param l0_filter2
	 * @throws IOException
	 */
	protected static void autoDetectSession(String lasBaseURLge, String l0_filter1, String l0_filter2)
			throws IOException {

		logger.debug("Auto-detecting session ...");
		String php = "";
		if (lasBaseURLge.contains("escaped"))
			php = ".php";
		Level0DataRetriever l0r = new Level0DataRetriever(
				lasBaseURLge + "/retrieveCollection" + php + "?flash=urn:xdaq-flashlist:", l0_filter1, l0_filter2);
		final String newDpsetPath = l0r.getDPsetPath();
		if (newDpsetPath == null) {
			logger.info("  No active session found for " + l0_filter1 + " and " + l0_filter2);
			throw new RuntimeException("No active session found!");
		} else if (_dpsetPath == null || !_dpsetPath.equals(newDpsetPath)) {
			logger.info("  Detected new HWCFG_KEY: old: " + _dpsetPath + "; new: " + newDpsetPath);
			_dpsetPath = newDpsetPath;
			_sid = l0r.getSID();
			logger.info("  SID_STRING='" + _sid + "'");

			_dpsetPathChanged = true;
			_sidChanged = true;
		} else if (_sid != l0r.getSID()) {
			logger.info("  Detected new SID: old: " + _sid + "; new: " + l0r.getSID());
			_sid = l0r.getSID();
			_sidChanged = true;
		}

	}

	protected static Properties loadPropertiesFile(String propertiesFile) {
		InputStream propertiesInputStream = DAQAggregator.class.getResourceAsStream(propertiesFile);

		if (propertiesInputStream == null) {

			// No resource found, try local
			try {
				propertiesInputStream = new FileInputStream(propertiesFile);
			} catch (FileNotFoundException e) {
				logger.error("Can not load the connection properties file : " + propertiesFile);
				e.printStackTrace();
				System.exit(-1);
			}
		}

		Properties jdaqMonitorProperties = new Properties();
		try {
			jdaqMonitorProperties.load(propertiesInputStream);
		} catch (IOException e) {
			logger.error("Can not load the connection properties file : " + propertiesFile);
			e.printStackTrace();
			System.exit(-1);
		}

		return jdaqMonitorProperties;
	}

	/*
	 * FIXME: handling exceptions of type Exception may mask bugs, fix
	 */
	protected static void setUpDBConnection() throws Exception {

		String _dbType = "ORACLE";
		String _dbURL = Application.get().getProp().getProperty(Application.PROPERTYNAME_HWCFGDB_DBURL);
		if (_dbURL == null || _dbURL.isEmpty()) {
			_dbURL = "jdbc:oracle:thin:@"
					+ Application.get().getProp().getProperty(Application.PROPERTYNAME_HWCFGDB_HOST) + ":"
					+ Application.get().getProp().getProperty(Application.PROPERTYNAME_HWCFGDB_PORT) + "/"
					+ Application.get().getProp().getProperty(Application.PROPERTYNAME_HWCFGDB_SID);
		}
		String _dbUser = Application.get().getProp().getProperty(Application.PROPERTYNAME_HWCFGDB_LOGIN);
		String _dbPasswd = Application.get().getProp().getProperty(Application.PROPERTYNAME_HWCFGDB_PWD);

		if (_dbType.equals("ORACLE"))
			_dbconn = new DBConnectorOracle(_dbURL, _dbUser, _dbPasswd);
		else
			_dbconn = new DBConnectorMySQL(_dbURL, _dbUser, _dbPasswd);

		_hwconn = new HWCfgConnector(_dbconn);

	}

	/*
	 * FIXME: handling exceptions of type Exception may mask bugs, fix
	 */
	protected static void setUpSOCKSProxy() throws Exception {

		if (Application.get().getProp().containsKey(Application.PROPERTYNAME_PROXY_ENABLE) && Application.get()
				.getProp().getProperty(Application.PROPERTYNAME_PROXY_ENABLE).toString().toLowerCase().equals("true")) {
			logger.info("Setting up SOCKS proxy ...");

			Properties sysProperties = System.getProperties();

			// Specify proxy settings
			sysProperties.put("socksProxyHost",
					Application.get().getProp().getProperty(Application.PROPERTYNAME_PROXY_HOST));
			sysProperties.put("socksProxyPort",
					Application.get().getProp().getProperty(Application.PROPERTYNAME_PROXY_PORT));

			sysProperties.put("proxySet", "true");
		}
	}

	static void tearDown() throws Exception {
		_dbconn.closeConnection();
	}

}

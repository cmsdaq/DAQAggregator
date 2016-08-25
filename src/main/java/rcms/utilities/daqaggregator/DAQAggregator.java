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
import rcms.utilities.daqaggregator.persistence.PersistorManager;
import rcms.utilities.daqaggregator.persistence.SnapshotFormat;
import rcms.utilities.hwcfg.HWCfgConnector;
import rcms.utilities.hwcfg.HWCfgDescriptor;
import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.dp.DAQPartitionSet;

public class DAQAggregator {

	// settings concerning session definition
	protected static String PROPERTYNAME_SESSION_LASURL_GE = "session.lasURLgeneral";
	protected static String PROPERTYNAME_SESSION_L0FILTER1 = "session.l0filter1";
	protected static String PROPERTYNAME_SESSION_L0FILTER2 = "session.l0filter2";

	// settings for monitoring
	protected static String PROPERTYNAME_MONITOR_SETUPNAME = "monitor.setupName";
	protected static String PROPERTYNAME_MONITOR_URLS = "monitor.lasURLs";

	// settings concerning HWCFG DB
	protected static String PROPERTYNAME_HWCFGDB_DBURL = "hwcfgdb.dburl";
	protected static String PROPERTYNAME_HWCFGDB_HOST = "hwcfgdb.host";
	protected static String PROPERTYNAME_HWCFGDB_PORT = "hwcfgdb.port";
	protected static String PROPERTYNAME_HWCFGDB_SID = "hwcfgdb.sid";
	protected static String PROPERTYNAME_HWCFGDB_LOGIN = "hwcfgdb.login";
	protected static String PROPERTYNAME_HWCFGDB_PWD = "hwcfgdb.pwd";

	// settings concerning SOCKS proxy
	protected static String PROPERTYNAME_PROXY_ENABLE = "socksproxy.enableproxy"; // optional
	protected static String PROPERTYNAME_PROXY_HOST = "socksproy.host";
	protected static String PROPERTYNAME_PROXY_PORT = "socksproxy.port";

	protected static String PERSISTENCE_DIR = "persistence.dir";
	protected static String PERSISTENCE_FORMAT = "persistence.format";
	protected static String PERSISTENCE_MODE = "persistence.mode";

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
		Properties daqAggregatorProperties = loadPropertiesFile(propertiesFile);
		work(daqAggregatorProperties);

	}

	public static void work(Properties daqAggregatorProperties) {

		try {

			setUpDBConnection(daqAggregatorProperties);
			setUpSOCKSProxy(daqAggregatorProperties);

			logger.debug("Read the following LAS URLs:");
			String[] lasURLs = daqAggregatorProperties.getProperty(PROPERTYNAME_MONITOR_URLS).split(" +");
			for (String lasUrl : lasURLs)
				logger.debug("   '" + lasUrl + "'");

			DAQPartition dp = null;

			MappingManager mappingManager = null;
			DAQ daq = null;
			Set<String> flashlistUrls = new HashSet<String>(Arrays.asList(lasURLs));

			String persistenceDir = daqAggregatorProperties.getProperty(PERSISTENCE_DIR);
			if (persistenceDir == null) {
				persistenceDir = "/tmp/snapshots/";
			}

			
			boolean flashlistPersistenceMode = false;
			String modeProperty = daqAggregatorProperties.getProperty(PERSISTENCE_MODE);
			if (modeProperty != null && modeProperty.equals("flashlist")) {
				logger.info("Flashlists will be persisted");
				flashlistPersistenceMode = true;
			}
			logger.info("Persist flashlists? " + flashlistPersistenceMode);

			
			/**
			 * will output to SMILE by default, if no other valid format option
			 * is found in properties file
			 */
			String formatProperty = daqAggregatorProperties.getProperty(PERSISTENCE_FORMAT);
			SnapshotFormat format = SnapshotFormat.SMILE;
			if (SnapshotFormat.JSON.name().equalsIgnoreCase(formatProperty))
				format = SnapshotFormat.JSON;
			else if (SnapshotFormat.SMILE.name().equalsIgnoreCase(formatProperty))
				format = SnapshotFormat.SMILE;
			else if (SnapshotFormat.JSONREFPREFIXED.name().equalsIgnoreCase(formatProperty))
				format = SnapshotFormat.JSONREFPREFIXED;
			else if (SnapshotFormat.JSONUGLY.name().equalsIgnoreCase(formatProperty))
				format = SnapshotFormat.JSONUGLY;
			else if (SnapshotFormat.JSONREFPREFIXEDUGLY.name().equalsIgnoreCase(formatProperty))
				format = SnapshotFormat.JSONREFPREFIXEDUGLY;

			PersistorManager persistorManager = new PersistorManager(persistenceDir, format);

			FlashlistManager flashlistManager = null;

			while (true) {

				try {
					_dpsetPathChanged = false;
					_sidChanged = false;
					autoDetectSession(daqAggregatorProperties.getProperty(PROPERTYNAME_SESSION_LASURL_GE),
							daqAggregatorProperties.getProperty(PROPERTYNAME_SESSION_L0FILTER1),
							daqAggregatorProperties.getProperty(PROPERTYNAME_SESSION_L0FILTER2));

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

						/* MODE SNAPSHOTS: persisting snapshots only */
						if (!flashlistPersistenceMode)
							prepareAndPersistSnapshot(daq, flashlistManager, persistorManager);

						/* MODE FLASHLISTS: persisting flashlists only */
						else
							persistorManager.persistFlashlists(flashlistManager);
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

	private static void prepareDAQStructure() {

	}

	protected static void prepareAndPersistSnapshot(DAQ daq, FlashlistManager flashlistManager,
			PersistorManager persistorManager) {
		flashlistManager.readAndMapFlashlists();

		daq.setLastUpdate(System.currentTimeMillis());

		// postprocess daq (derived values, summary classes)
		PostProcessor postProcessor = new PostProcessor(daq);
		postProcessor.postProcess();

		// serialize snapshot
		persistorManager.persistSnapshot(daq);
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
	protected static void setUpDBConnection(Properties jdaqMonitorProperties) throws Exception {

		String _dbType = "ORACLE";
		String _dbURL = jdaqMonitorProperties.getProperty(PROPERTYNAME_HWCFGDB_DBURL);
		if (_dbURL == null || _dbURL.isEmpty()) {
			_dbURL = "jdbc:oracle:thin:@" + jdaqMonitorProperties.getProperty(PROPERTYNAME_HWCFGDB_HOST) + ":"
					+ jdaqMonitorProperties.getProperty(PROPERTYNAME_HWCFGDB_PORT) + "/"
					+ jdaqMonitorProperties.getProperty(PROPERTYNAME_HWCFGDB_SID);
		}
		String _dbUser = jdaqMonitorProperties.getProperty(PROPERTYNAME_HWCFGDB_LOGIN);
		String _dbPasswd = jdaqMonitorProperties.getProperty(PROPERTYNAME_HWCFGDB_PWD);

		if (_dbType.equals("ORACLE"))
			_dbconn = new DBConnectorOracle(_dbURL, _dbUser, _dbPasswd);
		else
			_dbconn = new DBConnectorMySQL(_dbURL, _dbUser, _dbPasswd);

		_hwconn = new HWCfgConnector(_dbconn);

	}

	/*
	 * FIXME: handling exceptions of type Exception may mask bugs, fix
	 */
	protected static void setUpSOCKSProxy(Properties jdaqMonitorProperties) throws Exception {

		if (jdaqMonitorProperties.containsKey(PROPERTYNAME_PROXY_ENABLE)
				&& jdaqMonitorProperties.get(PROPERTYNAME_PROXY_ENABLE).toString().toLowerCase().equals("true")) {
			logger.info("Setting up SOCKS proxy ...");

			Properties sysProperties = System.getProperties();

			// Specify proxy settings
			sysProperties.put("socksProxyHost", jdaqMonitorProperties.get(PROPERTYNAME_PROXY_HOST));
			sysProperties.put("socksProxyPort", jdaqMonitorProperties.get(PROPERTYNAME_PROXY_PORT));
			sysProperties.put("proxySet", "true");
		}
	}

	static void tearDown() throws Exception {
		_dbconn.closeConnection();
	}

	/**
	 * TODO: this is tmp method for running daqaggregator as server with raw and
	 * reason streams on timeline, this will be separate projects in future.
	 */
	public void run() {
		String propFileName = "DAQAggregator.properties";
		logger.info("DAQAggregator started with properties file '" + propFileName + "'");

		Properties prop = new Properties();
		InputStream inputStream = null;

		try {
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
		} catch (FileNotFoundException e) {

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		work(prop);
	}
}

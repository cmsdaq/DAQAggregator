package rcms.utilities.daqaggregator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.common.db.DBConnectorIF;
import rcms.common.db.DBConnectorMySQL;
import rcms.common.db.DBConnectorOracle;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.mappers.FileFlashlistManager;
import rcms.utilities.daqaggregator.mappers.Flashlist;
import rcms.utilities.daqaggregator.mappers.FlashlistManager;
import rcms.utilities.daqaggregator.mappers.FlashlistType;
import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.mappers.PostProcessor;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.PersistorManager;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.hwcfg.HWCfgConnector;
import rcms.utilities.hwcfg.HWCfgDescriptor;
import rcms.utilities.hwcfg.HardwareConfigurationException;
import rcms.utilities.hwcfg.InvalidNodeTypeException;
import rcms.utilities.hwcfg.PathNotFoundException;
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
		initializeAndRun();

	}

	public static void initializeAndRun() {

		try {

			setUpDBConnection();
			setUpSOCKSProxy();

			logger.debug("Read the following LAS URLs:");
			String[] lasURLs = Application.get().getProp().getProperty(Application.PROPERTYNAME_MONITOR_URLS)
					.split(" +");
			for (String lasUrl : lasURLs)
				logger.debug("   '" + lasUrl + "'");

			Set<String> flashlistUrls = new HashSet<String>(Arrays.asList(lasURLs));

			String snapshotPersistenceDir = Application.get().getProp()
					.getProperty(Application.PERSISTENCE_SNAPSHOT_DIR);
			String flashlistPersistenceDir = Application.get().getProp()
					.getProperty(Application.PERSISTENCE_FLASHLIST_DIR);

			/*
			 * Persist mode from properties file
			 */
			PersistMode persistMode = PersistMode
					.decode(Application.get().getProp().getProperty(Application.PERSISTENCE_MODE));
			logger.info("Persist mode:" + persistMode);

			/*
			 * Run mode from properties file
			 */
			RunMode runMode = RunMode.decode(Application.get().getProp().getProperty(Application.RUN_MODE));
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

			switch (runMode) {
			case RT:
				runRealTime(persistMode, persistorManager, flashlistUrls);
				break;
			case FILE:
				runFromFile(persistMode, persistorManager, flashlistUrls);
				break;
			}

			logger.info("DAQAggregator is going down");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void iteration(DAQPartition dp, MappingManager mappingManager, DAQ daq,
			FlashlistManager flashlistManager, PersistMode persistMode, Set<String> flashlistUrls,
			PersistorManager persistorManager, Long timestamp, RunMode runMode)
			throws IOException, HardwareConfigurationException, PathNotFoundException, InvalidNodeTypeException {

		_dpsetPathChanged = false;
		_sidChanged = false;
		autoDetectSession(Application.get().getProp().getProperty(Application.PROPERTYNAME_SESSION_LASURL_GE),
				Application.get().getProp().getProperty(Application.PROPERTYNAME_SESSION_L0FILTER1),
				Application.get().getProp().getProperty(Application.PROPERTYNAME_SESSION_L0FILTER2), flashlistManager,
				runMode);

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
			flashlistManager.getFlashlists().clear();
			flashlistManager.setMappingManager(mappingManager);
			flashlistManager.setSessionId(_sid);
			flashlistManager.retrieveAvailableFlashlists();

			logger.info("Done for session " + daq.getSessionId());
		}

		if (flashlistManager != null) {

			switch (persistMode) {
			case SNAPSHOT:
				flashlistManager.downloadAndMapFlashlists();
				finalizeSnapshot(daq, timestamp);
				persistorManager.persistSnapshot(daq);
				break;
			case FLASHLIST:
				flashlistManager.downloadFlashlists(true);
				persistorManager.persistFlashlists(flashlistManager);
				break;
			case ALL:
				flashlistManager.downloadFlashlists(true);
				flashlistManager.mapFlashlists();
				finalizeSnapshot(daq, timestamp);
				persistorManager.persistSnapshot(daq);
				persistorManager.persistFlashlists(flashlistManager);
				break;
			}
		} else {
			logger.warn("Flashlist manager not initialized, session id not available");
		}

	}

	private static void runFromFile(PersistMode persistMode, PersistorManager persistorManager,
			Set<String> flashlistUrls) throws InterruptedException, IOException, HardwareConfigurationException,
			PathNotFoundException, InvalidNodeTypeException {

		DAQPartition dp = null;
		FileFlashlistManager flashlistManager = new FileFlashlistManager(persistorManager.getFlashlistFormat());
		MappingManager mappingManager = null;
		DAQ daq = null;

		// 1 explore the flashlist persistence dir and for each run:

		int exploredFlashlistCount = 0;
		Map<FlashlistType, List<File>> exploredFlashlists = new HashMap<>();
		for (FlashlistType flashlistType : FlashlistType.values()) {
			Entry<Long, List<File>> explored = persistorManager.explore(0L, Long.MAX_VALUE,
					persistorManager.getFlashlistPersistenceDir() + flashlistType.name(), Integer.MAX_VALUE);
			exploredFlashlists.put(flashlistType, explored.getValue());
			logger.info("Explored " + explored.getValue().size() + " for flashlist " + flashlistType.name());
			exploredFlashlistCount = explored.getValue().size();

		}

		for (int i = 0; i < exploredFlashlistCount; i++) {

			long timestamp = 0;
			for (FlashlistType flashlistType : FlashlistType.values()) {

				File currentFlashlistSnapshot = exploredFlashlists.get(flashlistType).get(i);

				flashlistManager.getCurrentIterationData().put(flashlistType, currentFlashlistSnapshot);

				int idx = currentFlashlistSnapshot.getName().indexOf(".");
				long currentTimestamp = Long.parseLong(currentFlashlistSnapshot.getName().substring(0, idx));
				if (currentTimestamp > timestamp)
					timestamp = currentTimestamp;

			}

			iteration(dp, mappingManager, daq, flashlistManager, persistMode, flashlistUrls, persistorManager,
					timestamp, RunMode.FILE);

		}

	}

	private static void runRealTime(PersistMode persistMode, PersistorManager persistorManager,
			Set<String> flashlistUrls) throws InterruptedException {

		DAQPartition dp = null;
		FlashlistManager flashlistManager = new FlashlistManager(flashlistUrls);
		MappingManager mappingManager = null;
		DAQ daq = null;

		while (true) {
			try {
				long timestamp = System.currentTimeMillis();
				iteration(dp, mappingManager, daq, flashlistManager, persistMode, flashlistUrls, persistorManager,
						timestamp, RunMode.RT);

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
	}

	protected static void finalizeSnapshot(DAQ daq, Long timestamp) {
		daq.setLastUpdate(timestamp);
		// postprocess daq (derived values, summary classes)
		PostProcessor postProcessor = new PostProcessor(daq);
		postProcessor.postProcess();
	}

	/**
	 * 
	 * @param lasBaseURLge
	 * @param l0_filter1
	 * @param l0_filter2
	 * @param runMode
	 * @throws IOException
	 */
	protected static void autoDetectSession(String lasBaseURLge, String l0_filter1, String l0_filter2,
			FlashlistManager flashlistManager, RunMode runMode) throws IOException {

		logger.debug("Auto-detecting session ...");

		Entry<String, Integer> result = flashlistManager.detectSession(runMode, l0_filter1, l0_filter2);
		String newDpsetPath = null;
		Integer newSid = null;
		
		if (result != null) {
			newDpsetPath = result.getKey();
			newSid = result.getValue();
		}
		
		if (newDpsetPath == null) {
			logger.info("  No active session found for " + l0_filter1 + " and " + l0_filter2);
			throw new RuntimeException("No active session found!");
		} else if (_dpsetPath == null || !_dpsetPath.equals(newDpsetPath)) {
			logger.info("  Detected new HWCFG_KEY: old: " + _dpsetPath + "; new: " + newDpsetPath);
			_dpsetPath = newDpsetPath;
			_sid = newSid;
			logger.info("  SID_STRING='" + _sid + "'");

			_dpsetPathChanged = true;
			_sidChanged = true;
		} else if (_sid != newSid) {
			logger.info("  Detected new SID: old: " + _sid + "; new: " + newSid);
			_sid = newSid;
			_sidChanged = true;
		}

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

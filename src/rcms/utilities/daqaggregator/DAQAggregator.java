package rcms.utilities.daqaggregator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

import rcms.common.db.DBConnectorIF;
import rcms.common.db.DBConnectorMySQL;
import rcms.common.db.DBConnectorOracle;
import rcms.utilities.hwcfg.HWCfgConnector;
import rcms.utilities.hwcfg.HWCfgDescriptor;
import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.dp.DAQPartitionSet;


public class DAQAggregator {

	// settings concerning session definition
	private static String PROPERTYNAME_SESSION_LASURL_GE         = "session.lasURLgeneral";
	private static String PROPERTYNAME_SESSION_L0FILTER1         = "session.l0filter1";
	private static String PROPERTYNAME_SESSION_L0FILTER2         = "session.l0filter2";

	// settings for monitoring
	private static String PROPERTYNAME_MONITOR_SETUPNAME         = "monitor.setupName";
	private static String PROPERTYNAME_MONITOR_URLS              = "monitor.lasURLs";

	// settings concerning HWCFG DB
	private static String PROPERTYNAME_HWCFGDB_DBURL             = "hwcfgdb.dburl";
	private static String PROPERTYNAME_HWCFGDB_HOST              = "hwcfgdb.host";
	private static String PROPERTYNAME_HWCFGDB_PORT              = "hwcfgdb.port";
	private static String PROPERTYNAME_HWCFGDB_SID               = "hwcfgdb.sid";
	private static String PROPERTYNAME_HWCFGDB_LOGIN             = "hwcfgdb.login";
	private static String PROPERTYNAME_HWCFGDB_PWD               = "hwcfgdb.pwd";

	// settings concerning SOCKS proxy
	private static String PROPERTYNAME_PROXY_ENABLE              = "socksproxy.enableproxy"; // optional
	private static String PROPERTYNAME_PROXY_HOST                = "socksproy.host";
	private static String PROPERTYNAME_PROXY_PORT                = "socksproxy.port";

	private static DBConnectorIF _dbconn = null;
	private static HWCfgConnector _hwconn = null;

	private static String _dpsetPath = null;
	private static int _sid = -1;

	private static boolean _dpsetPathChanged = false;
	private static boolean _sidChanged = false;




	public static void main(String[] args) {

		try {
			String propertiesFile = "DAQAggregator.properties";
			if (args.length>0)
				propertiesFile = args[0];

			System.out.println("DAQAggregator started with properties file '" + propertiesFile + "'");

			Properties daqAggregatorProperties = loadPropertiesFile(propertiesFile);
			setUpDBConnection(daqAggregatorProperties);
			setUpSOCKSProxy(daqAggregatorProperties);

			System.out.println("Read the following LAS URLs:");
			String[] lasURLs = daqAggregatorProperties.getProperty(PROPERTYNAME_MONITOR_URLS).split(" +");
			for (String lasUrl : lasURLs)
				System.out.println("   '" + lasUrl + "'");

			Thread monitorThread = null;
			DAQPartition dp = null;

			while(true) {

				try {
					_dpsetPathChanged = false;
					_sidChanged = false;
					autoDetectSession(daqAggregatorProperties.getProperty(PROPERTYNAME_SESSION_LASURL_GE),
							daqAggregatorProperties.getProperty(PROPERTYNAME_SESSION_L0FILTER1),
							daqAggregatorProperties.getProperty(PROPERTYNAME_SESSION_L0FILTER2));

					if ( _dpsetPathChanged || _sidChanged ) {
						if (monitorThread != null) {
							System.out.println("Session has changed. Stopping monitor thread ...");
							monitorThread.interrupt();
							monitorThread = null;
							System.out.println("done.");
						}
					}

					//
					// load DPSet if it changed or was not yet loaded
					//
					if ( _dpsetPathChanged || _sidChanged ) {
						System.out.println("Loading DPSet '" + _dpsetPath + "' ...");
						HWCfgDescriptor dp_node = _hwconn.getNode( _dpsetPath );
						DAQPartitionSet dpset = _hwconn.retrieveDPSet( dp_node );
						dp = dpset.getDPs().values().iterator().next();
						
						StructureMapper structureMapper = new StructureMapper();
						structureMapper.map(dp);

						StructurePersistor structurePersistor = new StructurePersistor();
						structurePersistor.persist(structureMapper.getDaq());
						System.out.println("done.");
					}

					if ( dp != null && monitorThread == null ) {
						System.out.println("Starting monitor thread.");
						monitorThread =  new MonitorThread(Arrays.asList(lasURLs) , dp, _dpsetPath, _sid,
								daqAggregatorProperties.getProperty(PROPERTYNAME_MONITOR_SETUPNAME));
						monitorThread.start();
					}

					System.out.println("sleeping for 10 seconds ....\n");
					Thread.sleep(10000);
				} catch (Exception e) {
					System.out.println("Error in main loop:\n" + e);
					e.printStackTrace();
					System.out.println("Going to sleep for 10 seconds before trying again...\n");
					Thread.sleep(10000);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void autoDetectSession(String lasBaseURLge, String l0_filter1, String l0_filter2) throws IOException {

		System.out.println("Auto-detecting session ...");
		String php = "";
		if (lasBaseURLge.contains("escaped"))
			php = ".php";
		Level0DataRetriever l0r = new Level0DataRetriever(lasBaseURLge + "/retrieveCollection"+php+"?flash=urn:xdaq-flashlist:", l0_filter1, l0_filter2);
		final String newDpsetPath = l0r.getDPsetPath();
		if ( newDpsetPath == null ) {
			System.out.println("  No active session found for "+l0_filter1+" and "+l0_filter2);
		}
		else if ( _dpsetPath == null || !_dpsetPath.equals(newDpsetPath) ) {
			System.out.println("  Detected new HWCFG_KEY: old: " + _dpsetPath + "; new: " + newDpsetPath);
			_dpsetPath = newDpsetPath;
			_sid = l0r.getSID();
			System.out.println("  SID_STRING='" + _sid + "'");

			_dpsetPathChanged = true;
			_sidChanged = true;
		}
		else if ( _sid != l0r.getSID() ) {
			System.out.println("  Detected new SID: old: " + _sid + "; new: " + l0r.getSID());
			_sid = l0r.getSID();
			_sidChanged = true;
		}
	}


	private static Properties loadPropertiesFile(String propertiesFile) {
		InputStream propertiesInputStream = DAQAggregator.class.getResourceAsStream(propertiesFile);

		if (propertiesInputStream == null) {

			// No resource found, try local
			try {
				propertiesInputStream= new FileInputStream(propertiesFile);
			}
			catch (FileNotFoundException e) {
				System.err.println("Can not load the connection properties file : " + propertiesFile);
				e.printStackTrace();
				System.exit(-1);
			}
		}

		Properties jdaqMonitorProperties = new Properties();
		try {
			jdaqMonitorProperties.load(propertiesInputStream);
		} catch (IOException e) {
			System.err.println("Can not load the connection properties file : " + propertiesFile);
			e.printStackTrace();
			System.exit(-1);		}

		return jdaqMonitorProperties;
	}


	static void setUpDBConnection(Properties jdaqMonitorProperties) throws Exception {

		String _dbType = "ORACLE";
		String _dbURL = jdaqMonitorProperties.getProperty(PROPERTYNAME_HWCFGDB_DBURL);
		if (_dbURL == null || _dbURL.isEmpty()) {
			_dbURL = "jdbc:oracle:thin:@" + jdaqMonitorProperties.getProperty(PROPERTYNAME_HWCFGDB_HOST)
					+ ":" + jdaqMonitorProperties.getProperty(PROPERTYNAME_HWCFGDB_PORT)
					+ "/" + jdaqMonitorProperties.getProperty(PROPERTYNAME_HWCFGDB_SID);
		}
		String _dbUser = jdaqMonitorProperties.getProperty(PROPERTYNAME_HWCFGDB_LOGIN);
		String _dbPasswd = jdaqMonitorProperties.getProperty(PROPERTYNAME_HWCFGDB_PWD);

		// Default log4j appender , can be changed later.
		ConsoleAppender simple = new ConsoleAppender(new PatternLayout("%d %-5p: %m %n"));
		simple.setThreshold(Level.INFO);
		BasicConfigurator.configure(simple);

		if (_dbType.equals("ORACLE"))
			_dbconn = new DBConnectorOracle(_dbURL, _dbUser, _dbPasswd);
		else
			_dbconn = new DBConnectorMySQL(_dbURL, _dbUser, _dbPasswd);

		_hwconn = new HWCfgConnector( _dbconn );

	}

	static void setUpSOCKSProxy(Properties jdaqMonitorProperties) throws Exception {

		if (jdaqMonitorProperties.containsKey(PROPERTYNAME_PROXY_ENABLE) &&
				jdaqMonitorProperties.get(PROPERTYNAME_PROXY_ENABLE).toString().toLowerCase().equals("true")) {
			System.out.println("Setting up SOCKS proxy ...");

			Properties sysProperties = System.getProperties();

			// Specify proxy settings
			sysProperties.put("socksProxyHost", jdaqMonitorProperties.get(PROPERTYNAME_PROXY_HOST));
			sysProperties.put("socksProxyPort", jdaqMonitorProperties.get(PROPERTYNAME_PROXY_PORT));
			sysProperties.put("proxySet",  "true");
		}
	}

	static void tearDown() throws Exception {
		_dbconn.closeConnection();
	}
}

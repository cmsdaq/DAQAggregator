package rcms.utilities.daqaggregator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Application {

	// settings concerning session definition
	public static String PROPERTYNAME_SESSION_LASURL_GE = "session.lasURLgeneral";
	public static String PROPERTYNAME_SESSION_L0FILTER1 = "session.l0filter1";
	public static String PROPERTYNAME_SESSION_L0FILTER2 = "session.l0filter2";

	// settings for monitoring
	public static String PROPERTYNAME_MONITOR_SETUPNAME = "monitor.setupName";
	public static String PROPERTYNAME_MONITOR_URLS = "monitor.lasURLs";

	// settings concerning HWCFG DB
	public static String PROPERTYNAME_HWCFGDB_DBURL = "hwcfgdb.dburl";
	public static String PROPERTYNAME_HWCFGDB_HOST = "hwcfgdb.host";
	public static String PROPERTYNAME_HWCFGDB_PORT = "hwcfgdb.port";
	public static String PROPERTYNAME_HWCFGDB_SID = "hwcfgdb.sid";
	public static String PROPERTYNAME_HWCFGDB_LOGIN = "hwcfgdb.login";
	public static String PROPERTYNAME_HWCFGDB_PWD = "hwcfgdb.pwd";

	// settings concerning SOCKS proxy
	public static String PROPERTYNAME_PROXY_ENABLE = "socksproxy.enableproxy"; // optional
	public static String PROPERTYNAME_PROXY_HOST = "socksproy.host";
	public static String PROPERTYNAME_PROXY_PORT = "socksproxy.port";

	// settings concerning persistence
	public static String PERSISTENCE_FLASHLIST_DIR = "persistence.flashlist.dir";
	public static String PERSISTENCE_SNAPSHOT_DIR = "persistence.snapshot.dir";
	public static String PERSISTENCE_FLASHLIST_FORMAT = "persistence.flashlist.format";
	public static String PERSISTENCE_SNAPSHOT_FORMAT = "persistence.snapshot.format";
	public static String PERSISTENCE_MODE = "persistence.mode";

	public static String RUN_MODE = "run.mode";

	private final Properties prop;

	public static Application get() {
		if (instance == null) {
			throw new RuntimeException("Not initialized");
		}
		return instance;
	}

	public static void initialize(String propertiesFile) {
		String message = "Required property missing ";
		instance = new Application(propertiesFile);
		if (!instance.prop.containsKey(PROPERTYNAME_SESSION_LASURL_GE))
			throw new RuntimeException(message + PROPERTYNAME_SESSION_LASURL_GE);
		if (!instance.prop.containsKey(PROPERTYNAME_MONITOR_URLS))
			throw new RuntimeException(message + PROPERTYNAME_MONITOR_URLS);
		if (!instance.prop.containsKey(PERSISTENCE_MODE))
			throw new RuntimeException(message + PERSISTENCE_MODE);
		if (!instance.prop.containsKey(RUN_MODE))
			throw new RuntimeException(message + RUN_MODE);
	}

	private Application(String propertiesFile) {
		prop = load(propertiesFile);
	}

	private static Application instance;

	private Properties load(String propertiesFile) {

		try {
			FileInputStream propertiesInputStream = new FileInputStream(propertiesFile);
			Properties properties = new Properties();
			properties.load(propertiesInputStream);

			return properties;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot run application without configuration file");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot run application without configuration file");
		}
	}

	public Properties getProp() {
		return prop;
	}
}

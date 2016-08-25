package rcms.utilities.daqaggregator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Application {

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

	// settings concerning persistence
	protected static String PERSISTENCE_FLASHLIST_DIR = "persistence.flashlist.dir";
	protected static String PERSISTENCE_SNAPSHOT_DIR = "persistence.snapshot.dir";
	protected static String PERSISTENCE_FLASHLIST_FORMAT = "persistence.flashlist.format";
	protected static String PERSISTENCE_SNAPSHOT_FORMAT = "persistence.snapshot.format";
	protected static String PERSISTENCE_MODE = "persistence.mode";

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

		if (!instance.prop.containsKey(PERSISTENCE_MODE))
			throw new RuntimeException(message + PERSISTENCE_MODE);
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

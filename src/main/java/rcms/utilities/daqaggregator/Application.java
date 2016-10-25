package rcms.utilities.daqaggregator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Application {

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
		if (!instance.prop.containsKey(Settings.SESSION_LASURL_GE))
			throw new RuntimeException(message + Settings.SESSION_LASURL_GE);
		if (!instance.prop.containsKey(Settings.MONITOR_URLS))
			throw new RuntimeException(message + Settings.MONITOR_URLS);
		if (!instance.prop.containsKey(Settings.PERSISTENCE_MODE))
			throw new RuntimeException(message + Settings.PERSISTENCE_MODE);
		if (!instance.prop.containsKey(Settings.RUN_MODE))
			throw new RuntimeException(message + Settings.RUN_MODE);
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

	public String getProp(Settings setting) {
		return prop.get(setting.getKey()).toString();
	}
	
	public Properties getProp() {
		return prop;
	}
}

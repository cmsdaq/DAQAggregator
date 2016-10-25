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

		for (Settings setting : Settings.values()) {
			if (setting.isRequired()) {
				if (!instance.prop.containsKey(setting.getKey()))
					throw new DAQException(DAQExceptionCode.MissingProperty, ": " + message + setting);
			}
		}

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
		Object property = prop.get(setting.getKey());
		if (property != null) {
			return property.toString();
		} else
			return null;
	}

	public Properties getProp() {
		return prop;
	}
}

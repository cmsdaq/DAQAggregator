package rcms.utilities.daqaggregator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import rcms.utilities.daqaggregator.datasource.LiveAccessService;

public class Application {

	private final Properties prop;

	private static Application instance;

	private Application(String propertiesFile) {
		prop = load(propertiesFile);
	}

	public static Application get() {
		if (instance == null) {
			throw new RuntimeException("Not initialized");
		}
		return instance;
	}

	public static void initialize(String propertiesFile) {
		instance = new Application(propertiesFile);
		checkRequiredSettings();
		initializeLAS();
	}

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

	/**
	 * Check if all required settings are present in configuration file
	 */
	private static void checkRequiredSettings() {
		for (Settings setting : Settings.values()) {
			if (setting.isRequired()) {
				if (!instance.prop.containsKey(setting.getKey()))
					throw new DAQException(DAQExceptionCode.MissingProperty, ": Required property missing " + setting);
			}
		}
	}

	/**
	 * Initialize Liva Access Service urls from configuration file
	 */
	private static void initializeLAS() {
		LiveAccessService.PRIMARY.setUrl(Application.get().getProp(Settings.FLASHLIST_PRIMARY_URL));
		LiveAccessService.SECONDARY.setUrl(Application.get().getProp(Settings.FLASHLIST_SECONDARY_URL));
		LiveAccessService.ADDITIONAL.setUrl(Application.get().getProp(Settings.FLASHLIST_ADDITIONAL_URL));
		LiveAccessService.ADDITIONAL2.setUrl(Application.get().getProp(Settings.FLASHLIST_ADDITIONAL2_URL));
		LiveAccessService.ADDITIONAL3.setUrl(Application.get().getProp(Settings.FLASHLIST_ADDITIONAL3_URL));
		LiveAccessService.ADDITIONAL4.setUrl(Application.get().getProp(Settings.FLASHLIST_ADDITIONAL4_URL));
		LiveAccessService.ADDITIONAL5.setUrl(Application.get().getProp(Settings.FLASHLIST_ADDITIONAL5_URL));
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

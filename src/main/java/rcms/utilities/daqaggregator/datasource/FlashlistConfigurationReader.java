package rcms.utilities.daqaggregator.datasource;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.Settings;

public class FlashlistConfigurationReader {

	public static final Logger logger = Logger.getLogger(FlashlistConfigurationReader.class);

	public Set<FlashlistType> readFlashlistOptionalConfigurations(Properties properties) {
		Set<FlashlistType> result = new HashSet<>();

		Object a = properties.get(Settings.FLASHLIST_OPTIONAL.getKey());
		if (a != null) {

			String[] flashlistTypes = ((String) a).split(" +");

			for (String value : flashlistTypes) {
				String flashlistName = (String) value;
				for (FlashlistType flashlistType : FlashlistType.values()) {
					if (flashlistType.name().equalsIgnoreCase(flashlistName)
							|| flashlistType.getFlashlistName().equalsIgnoreCase(flashlistName)) {
						result.add(flashlistType);
					}
				}
			}
		}
		return result;
	}
}

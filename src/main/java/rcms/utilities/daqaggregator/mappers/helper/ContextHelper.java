package rcms.utilities.daqaggregator.mappers.helper;

import org.apache.log4j.Logger;

/**
 * This class strips urls to hostname e.g. http://bu-0000:9999 -> bu-0000.cms
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class ContextHelper {

	private static final Logger logger = Logger.getLogger(ContextHelper.class);

	public static Integer getPortFromContext(String context) {
		String hostname = new String(context);
		String portString;
		Integer result;

		hostname = stripFromProtocol(hostname);

		// get port
		if (hostname.contains(":")) {
			portString = hostname.substring(hostname.indexOf(":")+1);
			try {
				result = Integer.valueOf(portString);
				return result;
			} catch (NumberFormatException e) {
				logger.warn("Could not parse port number from this string: " + portString + ", problematic context: "
						+ context);
				return null;
			}
		} else {
			logger.warn("Could not find port number in this context: " + context);
			return null;
		}
	}

	public static String getHostnameFromContext(String context) {
		String hostname = new String(context);
		hostname = stripFromProtocol(hostname);

		// remove port
		if (hostname.contains(":")) {
			hostname = hostname.substring(0, hostname.indexOf(":"));
		}
		return hostname;
	}

	private static String stripFromProtocol(String context) {
		String result = new String(context);
		if (result.startsWith("http://")) {
			result = result.substring(7);
		}
		if (result.startsWith("https://")) {
			result = result.substring(8);
		}
		return result;
	}

	/**
	 * Useful in removing network or other suffix (e.g. '.cms') from a hostname.
	 * Returns hostname argument as is, if it does not end with the suffix
	 * passed.
	 */
	public static String removeSuffixFromHostname(String hostname, String suffix) {
		if (hostname.endsWith(suffix)) {
			return hostname.substring(0, hostname.length() - suffix.length());
		} else {
			return hostname;
		}

	}
}

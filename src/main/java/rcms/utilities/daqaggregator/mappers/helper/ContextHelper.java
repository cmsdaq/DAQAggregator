package rcms.utilities.daqaggregator.mappers.helper;

public class ContextHelper {

	public static String getHostnameFromContext(String context) {
		String hostname = new String(context);
		// remove protocol
		if (hostname.startsWith("http://")) {
			hostname = hostname.substring(7);
		}
		// remove port
		if (hostname.contains(":")) {
			hostname = hostname.substring(0, hostname.indexOf(":"));
		}
		if (!hostname.endsWith(".cms")) {
			hostname = hostname + ".cms";
		}
		return hostname;
	}
}

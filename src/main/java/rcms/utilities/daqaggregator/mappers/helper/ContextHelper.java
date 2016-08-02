package rcms.utilities.daqaggregator.mappers.helper;

/**
 * This class strips urls to hostname e.g. http://bu-0000:9999 -> bu-0000.cms
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
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
	
	/** Useful in removing network or other suffix (e.g. '.cms') from a hostname.
	 * Returns hostname argument as is, if it does not end with the suffix passed.*/
	public static String removeSuffixFromHostname(String hostname, String suffix){
		if (hostname.endsWith(suffix)){
			return hostname.substring(0, hostname.length()-suffix.length());
		}else{
			return hostname;
		}
	
	}
}

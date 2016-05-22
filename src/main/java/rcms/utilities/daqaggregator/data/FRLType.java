package rcms.utilities.daqaggregator.data;

/**
 * Front-end Readout Link Types
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */

public enum FRLType {
	SLINK, SLINKEXPRESS6G, SLINKEXPRESS10G, NOLINK;

	public static FRLType getByName(String type) {
		if (SLINK.toString().equalsIgnoreCase(type)) {
			return SLINK;
		} else if (SLINKEXPRESS6G.toString().equalsIgnoreCase(type)) {
			return SLINKEXPRESS6G;
		} else if (SLINKEXPRESS10G.toString().equalsIgnoreCase(type)) {
			return SLINKEXPRESS10G;
		} else if (NOLINK.toString().equalsIgnoreCase(type)) {
			return NOLINK;
		} else {
			return null;
		}

	}
}

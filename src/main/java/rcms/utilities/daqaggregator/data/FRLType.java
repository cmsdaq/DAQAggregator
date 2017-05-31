package rcms.utilities.daqaggregator.data;

import org.apache.log4j.Logger;

/**
 * Front-end Readout Link Types
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */

public enum FRLType {
	SLINK, SLINKEXPRESS6G, SLINKEXPRESS10G, FEROL40_10G, FEROL40_6G, NOLINK;

	private static final Logger logger = Logger.getLogger(FRLType.class);

	public static FRLType getByName(String type) {
		if (SLINK.toString().equalsIgnoreCase(type)) {
			return SLINK;
		} else if (SLINKEXPRESS6G.toString().equalsIgnoreCase(type)) {
			return SLINKEXPRESS6G;
		} else if (SLINKEXPRESS10G.toString().equalsIgnoreCase(type)) {
			return SLINKEXPRESS10G;
		} else if (NOLINK.toString().equalsIgnoreCase(type)) {
			return NOLINK;
		} else if (FEROL40_10G.toString().equalsIgnoreCase(type)) {
			return FEROL40_10G;
		} else if (FEROL40_6G.toString().equalsIgnoreCase(type)) {
			return FEROL40_6G;
		} else {
			logger.warn("Frl type could not be mapped, type to map " + type + ", known types: " + FRLType.values());
			return null;
		}

	}
}

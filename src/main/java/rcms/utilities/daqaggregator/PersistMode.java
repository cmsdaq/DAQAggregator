package rcms.utilities.daqaggregator;

import org.apache.log4j.Logger;

public enum PersistMode {

	SNAPSHOT, FLASHLIST, ALL, CONVERT;

	public static PersistMode decode(String mode) {
		if (mode.equalsIgnoreCase(SNAPSHOT.name())) {
			return PersistMode.SNAPSHOT;
		} else if (mode.equalsIgnoreCase(FLASHLIST.name())) {
			return PersistMode.FLASHLIST;
		} else if (mode.equalsIgnoreCase(ALL.name())) {
			return PersistMode.ALL;
		} else if (mode.equalsIgnoreCase(CONVERT.name())) {
			return PersistMode.CONVERT;
		} else {
			Logger.getLogger(PersistMode.class).warn("Run mode could not be decoded, default is " + SNAPSHOT);
			return PersistMode.SNAPSHOT;
		}
	}

}

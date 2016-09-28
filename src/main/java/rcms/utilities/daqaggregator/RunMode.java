package rcms.utilities.daqaggregator;

import org.apache.log4j.Logger;

public enum RunMode {

	RT, FILE, SPECIAL;

	public static RunMode decode(String mode) {
		if (mode.equalsIgnoreCase(RT.name())) {
			return RunMode.RT;
		} else if (mode.equalsIgnoreCase(FILE.name())) {
			return RunMode.FILE;
		} else if (mode.equalsIgnoreCase(SPECIAL.name())) {
			return RunMode.SPECIAL;
		} else {
			Logger.getLogger(RunMode.class).warn("Run mode could not be decoded, default is " + RT);
			return RunMode.RT;
		}
	}

}

package rcms.utilities.daqaggregator.reasoning;

import java.util.Date;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.Comparator;
import rcms.utilities.daqaggregator.reasoning.base.Level;

public class LHCBeamModeComparator extends Comparator {

	private static Logger logger = Logger.getLogger(LHCBeamModeComparator.class);

	private String beamMode;

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		if (!current.getLhcBeamMode().equals(previous.getLhcBeamMode())) {
			logger.debug("New LHC Beam mode " + new Date(current.getLastUpdate()));
			beamMode = "Beam mode: " + current.getLhcBeamMode();
			result = true;
		}
		return result;
	}

	@Override
	public String getText() {
		return beamMode;
	}

	@Override
	public Level getLevel() {
		return Level.LHC;
	}

}

package rcms.utilities.daqaggregator.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.Comparator;
import rcms.utilities.daqaggregator.reasoning.base.Level;

public class RunComparator extends Comparator {

	private static Logger logger = Logger.getLogger(RunComparator.class);

	@Override
	public Level getLevel() {
		return Level.Info;
	}

	public boolean compare(DAQ previous, DAQ current) {
		boolean result = false;

		logger.debug("Comparing runs " + current.getSessionId() + " previous " + previous.getSessionId());
		if (current.getSessionId() != previous.getSessionId()) {
			logger.info("New run identified");
			runNumber = "Run: " + current.getSessionId();
			result = true;
		}
		return result;
	}

	private String runNumber;

	@Override
	public String getText() {
		return runNumber;
	}

}

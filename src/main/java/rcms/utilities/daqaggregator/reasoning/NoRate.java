package rcms.utilities.daqaggregator.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.Level;
import rcms.utilities.daqaggregator.reasoning.base.SimpleProblem;

public class NoRate implements SimpleProblem {
	private final static Logger logger = Logger.getLogger(NoRate.class);

	@Override
	public Boolean isProblem(DAQ daq) {
		float a = daq.getFedBuilderSummary().getRate();
		boolean result = false;
		if (a == 0)
			result = true;

		if (!result) {
			logger.debug("Check ok, rate non zero");
		} else {
			logger.debug("Check failed, range 0");
		}
		return result;
	}

	@Override
	public Level getLevel() {
		return Level.Warning;
	}

	@Override
	public String getText() {
		return NoRate.class.getSimpleName();
	}

}

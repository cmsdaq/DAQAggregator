package rcms.utilities.daqaggregator.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.reasoning.base.Level;
import rcms.utilities.daqaggregator.reasoning.base.SimpleProblem;

public class NotEqualFEDEvents implements SimpleProblem {
	private final static Logger logger = Logger.getLogger(NotEqualFEDEvents.class);

	@Override
	public Boolean isProblem(DAQ daq) {
		boolean result = false;

		long fedEvent = -1;
		for (FEDBuilder fedBuilder : daq.getFedBuilders()) {
			if (!fedBuilder.getRu().isEVM()) {
				if (fedEvent == -1)
					fedEvent = fedBuilder.getRu().getRequests();
				if (fedBuilder.getRu().getEventsInRU() != fedEvent) {
					logger.info("Events are differend " + fedEvent + "!=" + fedBuilder.getRu().getRequests());
					result = true;
				}
			}
		}

		return result;
	}

	@Override
	public Level getLevel() {
		return Level.Error;
	}
	

	@Override
	public String getText() {
		return NoRate.class.getSimpleName();
	}

}

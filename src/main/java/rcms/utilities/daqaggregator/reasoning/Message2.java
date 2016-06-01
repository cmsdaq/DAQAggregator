package rcms.utilities.daqaggregator.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.Condition;
import rcms.utilities.daqaggregator.reasoning.base.Level;
import rcms.utilities.daqaggregator.servlets.Entry;

public class Message2 implements Condition {

	private static Logger logger = Logger.getLogger(Message2.class);
	private final String ERROR_STATE = "ERROR";

	@Override
	public Boolean satisfied(DAQ daq) {
		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();
		if (ERROR_STATE.equalsIgnoreCase(l0state) && ERROR_STATE.equalsIgnoreCase(daqstate)) {

			logger.debug("Message 2  DAQ and level 0 in error state");
			return true;
		}
		return false;
	}

	@Override
	public Level getLevel() {
		return Level.Message;
	}

	@Override
	public String getText() {
		return "Message2: DAQ and level 0 in error state";
	}

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {

	}

}

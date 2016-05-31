package rcms.utilities.daqaggregator.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.reasoning.base.Condition;
import rcms.utilities.daqaggregator.reasoning.base.Level;

public class Message1 implements Condition {

	private static Logger logger = Logger.getLogger(Message1.class);
	private final String RUNBLOCKED_STATE = "RUNBLOCKED";

	private String message;

	@Override
	public Boolean satisfied(DAQ daq) {
		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();
		if (RUNBLOCKED_STATE.equalsIgnoreCase(l0state) && RUNBLOCKED_STATE.equalsIgnoreCase(daqstate)) {

			StringBuilder sb = new StringBuilder();
			for (FEDBuilder fb : daq.getFedBuilders()) {
				RU ru = fb.getRu();
				if (ru.getStatus().equalsIgnoreCase("SyncLoss")) {
					sb.append(ru.getHostname() + ", ");
				}
			}

			message = "Message1: DAQ and L0 in RUNBLOCKED, found rus in SYNCLOSS: " + sb.toString();
			logger.debug(message);
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
		return message;
	}

}

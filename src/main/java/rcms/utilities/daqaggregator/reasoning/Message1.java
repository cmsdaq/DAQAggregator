package rcms.utilities.daqaggregator.reasoning;

import java.util.HashSet;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.reasoning.base.Condition;
import rcms.utilities.daqaggregator.reasoning.base.Level;
import rcms.utilities.daqaggregator.servlets.Entry;
import rcms.utilities.hwcfg.gui.fb.design.FedBuilderDesignPanel;

public class Message1 implements Condition {

	private static Logger logger = Logger.getLogger(Message1.class);
	private final String RUNBLOCKED_STATE = "RUNBLOCKED";

	private String message;

	private RU problemRu;

	@Override
	public Boolean satisfied(DAQ daq) {
		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();
		if (RUNBLOCKED_STATE.equalsIgnoreCase(l0state) && RUNBLOCKED_STATE.equalsIgnoreCase(daqstate)) {

			for (FEDBuilder fb : daq.getFedBuilders()) {
				RU ru = fb.getRu();
				if (ru.getStatus().equalsIgnoreCase("SyncLoss")) {
					problemRu = ru;
				}

			}

			message = "Message1: DAQ and L0 in RUNBLOCKED";
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

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {

		if (problemRu != null) {
			if (!entry.getAdditional().containsKey("problemRus")) {
				entry.getAdditional().put("problemRus", new HashSet<Object>());
			}
			if (!entry.getAdditional().containsKey("fedsOutOfSync")) {
				entry.getAdditional().put("fedsOutOfSync", new HashSet<Object>());
			}
			((HashSet<Object>) entry.getAdditional().get("problemRus")).add(problemRu.getHostname());

			for (FED fed : daq.getAllFeds()) {
				if (fed.getRuFedOutOfSync() > 0) {
					String fedString = "FED id: " + fed.getId() + ", FED expected id: " + fed.getSrcIdExpected();
					((HashSet<Object>) entry.getAdditional().get("fedsOutOfSync")).add(fedString);
				}

			}
		}

	}

}

package rcms.utilities.daqaggregator.reasoning;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.reasoning.base.Condition;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;
import rcms.utilities.daqaggregator.reasoning.base.Level;
import rcms.utilities.daqaggregator.servlets.Entry;

public class Message2 implements Condition {

	private static Logger logger = Logger.getLogger(Message2.class);
	private final String ERROR_STATE = "ERROR";

	@Override
	public Boolean satisfied(DAQ daq) {
		String l0state = daq.getLevelZeroState();
		String daqstate = daq.getDaqState();

		if (!"Stable Beams".equalsIgnoreCase(daq.getLhcBeamMode()))
			return false;

		if (ERROR_STATE.equalsIgnoreCase(l0state) && ERROR_STATE.equalsIgnoreCase(daqstate)) {

			for (FEDBuilder fb : daq.getFedBuilders()) {
				RU ru = fb.getRu();
				if (ru.getStatus().equalsIgnoreCase("Failed")) {

					logger.debug("M2 DAQ and level 0 in error state, exists RU in failed state");
					return true;
				}
			}

			return false;
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

		Set<RU> problemRus = new HashSet<>();
		for (FEDBuilder fb : daq.getFedBuilders()) {
			RU ru = fb.getRu();
			if (ru.getStatus().equalsIgnoreCase("Failed")) {
				problemRus.add(ru);
			}
		}

		for (RU ru : problemRus) {
			if (!entry.getAdditional().containsKey("problemRus")) {
				entry.getAdditional().put("problemRus", new HashSet<Object>());
			}
			if (!entry.getAdditional().containsKey("fedsCorruptedData")) {
				entry.getAdditional().put("fedsCorruptedData", new HashSet<Object>());
			}
			((HashSet<Object>) entry.getAdditional().get("problemRus")).add(ru.getHostname());

			for (FED fed : daq.getAllFeds()) {
				if (fed.getRuFedDataCorruption() > 0) {
					String fedString = "FED id: " + fed.getId() + ", FED expected id: " + fed.getSrcIdExpected()
							+ ", FED corrupted: " + fed.getRuFedDataCorruption();
					((HashSet<Object>) entry.getAdditional().get("fedsCorruptedData")).add(fedString);
				}

			}
		}

	}
	
	@Override
	public EventClass getClassName() {
		return EventClass.critical;
	}

}

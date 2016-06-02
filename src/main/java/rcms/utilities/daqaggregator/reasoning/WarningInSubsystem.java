package rcms.utilities.daqaggregator.reasoning;

import java.util.Date;
import java.util.HashSet;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.reasoning.base.Level;
import rcms.utilities.daqaggregator.servlets.Entry;
import rcms.utilities.daqaggregator.reasoning.base.Condition;
import rcms.utilities.daqaggregator.reasoning.base.EventClass;

public class WarningInSubsystem implements Condition {
	private final static Logger logger = Logger.getLogger(WarningInSubsystem.class);

	private String text = "";

	@Override
	public Boolean satisfied(DAQ daq) {

		boolean result = false;

		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				if (ttcp.getPercentWarning() != 0F) {
					result = true;
					text = "TTCP in warning";
				}
			}
		}

		return result;
	}

	@Override
	public Level getLevel() {
		return Level.Warning;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void gatherInfo(DAQ daq, Entry entry) {
		for (SubSystem subSystem : daq.getSubSystems()) {

			for (TTCPartition ttcp : subSystem.getTtcPartitions()) {

				if (ttcp.getPercentWarning() != 0F) {

					if (!entry.getAdditional().containsKey("ttcpInWarning")) {
						entry.getAdditional().put("ttcpInWarning", new HashSet<Object>());
					}

					String ttcpString = "TTCP: " + ttcp.getName() + ", TTCP warning: " + ttcp.getPercentWarning()
							+ "%, subsystem: " + subSystem.getName();
					((HashSet<Object>) entry.getAdditional().get("ttcpInWarning")).add(ttcpString);
				}
			}
		}
	}

	@Override
	public EventClass getClassName() {
		return EventClass.defaultt;
	}
}

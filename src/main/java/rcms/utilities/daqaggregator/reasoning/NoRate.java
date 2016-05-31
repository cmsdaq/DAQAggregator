package rcms.utilities.daqaggregator.reasoning;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.Level;
import rcms.utilities.daqaggregator.reasoning.base.Condition;

public class NoRate implements Condition {

	@Override
	public Boolean satisfied(DAQ daq) {
		float rate = daq.getFedBuilderSummary().getRate();
		if (rate == 0)
			return true;
		return false;
	}

	@Override
	public Level getLevel() {
		return Level.Warning;
	}

	@Override
	public String getText() {
		return "No rate";
	}

}
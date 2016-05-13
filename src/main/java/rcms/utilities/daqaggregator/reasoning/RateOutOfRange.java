package rcms.utilities.daqaggregator.reasoning;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.base.Level;
import rcms.utilities.daqaggregator.reasoning.base.SimpleProblem;

public class RateOutOfRange implements SimpleProblem {
	private final static Logger logger = Logger.getLogger(RateOutOfRange.class);

	@Override
	public Boolean isProblem(DAQ daq) {
		float a = daq.getFedBuilderSummary().getRate();
		
		boolean result = false;
		if (50000 > a)
			result = true;

		return result;
	}

	@Override
	public Level getLevel(){
		return Level.Info;
	}
	
	@Override
	public String getText() {
		return RateOutOfRange.class.getSimpleName();
	}

}

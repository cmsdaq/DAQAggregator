package rcms.utilities.daqaggregator.reasoning.base;

import rcms.utilities.daqaggregator.data.DAQ;

public interface SimpleProblem extends Classificable {

	/**
	 * Returns true if there is a problem
	 * @param daq
	 * @return
	 */
	public Boolean isProblem(DAQ daq);
	
}

package rcms.utilities.daqaggregator.reasoning.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.reasoning.NoRate;
import rcms.utilities.daqaggregator.reasoning.NotEqualFEDEvents;
import rcms.utilities.daqaggregator.reasoning.RateOutOfRange;
import rcms.utilities.daqaggregator.reasoning.RunComparator;

/**
 * Manager of checking process
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class CheckManager {

	private final static Logger logger = Logger.getLogger(CheckManager.class);
	private final List<SimpleProblem> checkers = new ArrayList<>();

	private final List<Comparator> comparators = new ArrayList<>();

	/**
	 * Constructor
	 * 
	 * @param daq
	 *            daq object to analyze
	 */
	public CheckManager() {
		checkers.add(new RateOutOfRange());
		checkers.add(new NoRate());
		//checkers.add(new NotEqualFEDEvents());
		comparators.add(new RunComparator());
	}

	/**
	 * Run all checkers
	 */
	public void runCheckers(DAQ daq) {

		logger.debug("Running analysis modules for run " + daq.getSessionId());
		Date curr = null;
		for (SimpleProblem checker : checkers) {
			boolean result = checker.isProblem(daq);
			curr = new Date(daq.getLastUpdate());
			EventProducer.get().produce(checker, result, curr);
		}
		for (Comparator comparator : comparators) {
			Date last = null;
			if(comparator.getLast() != null)
				last = new Date(comparator.getLast().getLastUpdate());
			
			/* add artificial event starting point */
			if(last == null ){
				DAQ fake = new DAQ();
				last = new Date(daq.getLastUpdate());
				fake.setLastUpdate(daq.getLastUpdate());
				comparator.setLast(fake);
			}
			
			boolean result = comparator.compare(daq);
			Date current = new Date(comparator.getLast().getLastUpdate());
			
			
			
			EventProducer.get().produce(comparator, result, last, current);
		}
	}
}

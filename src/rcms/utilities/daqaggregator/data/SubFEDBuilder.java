package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing one line in DAQView
 */
public class SubFEDBuilder {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	/** the 'parent' FEDBuilder */
	private FEDBuilder fedBuilder;
  
	private TTCPartition ttcPartition;
  
	/** can be null */
	private FRLPc frlPc; 
	
	private List<FRL> frls = new ArrayList<FRL>();
	  
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------

	private long minTrig, maxTrig;
	
}

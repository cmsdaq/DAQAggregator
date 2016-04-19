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
	private final FEDBuilder fedBuilder;
  
	private final TTCPartition ttcPartition;
  
	/** can be null */
	private final FRLPc frlPc; 
	
	private final List<FRL> frls = new ArrayList<FRL>();
	  
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------

	private long minTrig, maxTrig;

	//----------------------------------------------------------------------

	public SubFEDBuilder(FEDBuilder fedBuilder, TTCPartition ttcPartition,
			FRLPc frlPc) {
		this.fedBuilder = fedBuilder;
		this.ttcPartition = ttcPartition;
		this.frlPc = frlPc;

		// TODO: fill frls
	}

	//----------------------------------------------------------------------

	public long getMinTrig() {
		return minTrig;
	}

	public void setMinTrig(long minTrig) {
		this.minTrig = minTrig;
	}

	public long getMaxTrig() {
		return maxTrig;
	}

	public void setMaxTrig(long maxTrig) {
		this.maxTrig = maxTrig;
	}

	public FEDBuilder getFedBuilder() {
		return fedBuilder;
	}

	public TTCPartition getTtcPartition() {
		return ttcPartition;
	}

	public FRLPc getFrlPc() {
		return frlPc;
	}

	public List<FRL> getFrls() {
		return frls;
	}
	
	//----------------------------------------------------------------------

}

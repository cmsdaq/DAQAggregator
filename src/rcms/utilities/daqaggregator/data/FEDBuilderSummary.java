package rcms.utilities.daqaggregator.data;

/** summary statistics of FED builders */
public class FEDBuilderSummary {

	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	/** parent */
	private final DAQ daq;
  
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------

	/** event building rate in kHz */
	private float rate;
  
	/** throughput in MByte/s ? */
	private float throughput;
  
	/** mean superfragment size in kByte ? */
	private float superFragmentSizeMean;
  
	/** spread of superfragment size in kByte ? */
	private float superFragmentSizeStddev;
  
	/** difference of number of events in RU between highest and lowest
	 *  fedbuilder ???
	 */
	private int deltaEvents;
  
	private int sumFragmentsInRU;
  
	private int sumEventsInRU;
  
	/** requests from BUs ? */
	private int sumRequests;
	
	//----------------------------------------------------------------------

	public FEDBuilderSummary(DAQ daq) {
		this.daq = daq;
	}

	//----------------------------------------------------------------------

	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		this.rate = rate;
	}

	public float getThroughput() {
		return throughput;
	}

	public void setThroughput(float throughput) {
		this.throughput = throughput;
	}

	public float getSuperFragmentSizeMean() {
		return superFragmentSizeMean;
	}

	public void setSuperFragmentSizeMean(float superFragmentSizeMean) {
		this.superFragmentSizeMean = superFragmentSizeMean;
	}

	public float getSuperFragmentSizeStddev() {
		return superFragmentSizeStddev;
	}

	public void setSuperFragmentSizeStddev(float superFragmentSizeStddev) {
		this.superFragmentSizeStddev = superFragmentSizeStddev;
	}

	public int getDeltaEvents() {
		return deltaEvents;
	}

	public void setDeltaEvents(int deltaEvents) {
		this.deltaEvents = deltaEvents;
	}

	public int getSumFragmentsInRU() {
		return sumFragmentsInRU;
	}

	public void setSumFragmentsInRU(int sumFragmentsInRU) {
		this.sumFragmentsInRU = sumFragmentsInRU;
	}

	public int getSumEventsInRU() {
		return sumEventsInRU;
	}

	public void setSumEventsInRU(int sumEventsInRU) {
		this.sumEventsInRU = sumEventsInRU;
	}

	public int getSumRequests() {
		return sumRequests;
	}

	public void setSumRequests(int sumRequests) {
		this.sumRequests = sumRequests;
	}

	public DAQ getDaq() {
		return daq;
	}
	
	//----------------------------------------------------------------------

}

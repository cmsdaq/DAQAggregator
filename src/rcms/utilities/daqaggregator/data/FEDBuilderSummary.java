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

}

package rcms.utilities.daqaggregator.data;

public class TTCPartition {

	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------
	
	private String name;
	
	private boolean masked;

	/** can be null */
	private FMM fmm;
		  
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------

	private String ttsState;
  
	private float percentWarning;
  
	private float percentBusy;

}

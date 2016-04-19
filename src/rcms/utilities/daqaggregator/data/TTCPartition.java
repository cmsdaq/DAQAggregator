package rcms.utilities.daqaggregator.data;

public class TTCPartition {

	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------
	
	private final String name;
	
	private final boolean masked;

	/** can be null */
	private final FMM fmm;
		  
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------

	private String ttsState;
  
	private float percentWarning;
  
	private float percentBusy;

	//----------------------------------------------------------------------

	public TTCPartition(String name, boolean masked, FMM fmm) {
		this.name = name;
		this.masked = masked;
		this.fmm = fmm;
	}

	//----------------------------------------------------------------------
	
}

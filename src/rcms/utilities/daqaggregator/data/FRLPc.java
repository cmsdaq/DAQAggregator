package rcms.utilities.daqaggregator.data;

public class FRLPc {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	private String hostname;
  
	/** xdaq application url */
	private String url; 
  
	private boolean masked;
	
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------
	private boolean crashed;
	
}

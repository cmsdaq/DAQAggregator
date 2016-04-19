package rcms.utilities.daqaggregator.data;

public class FRLPc {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	private final String hostname;
  
	/** xdaq application url */
	private final String url; 
  
	private final boolean masked;
	
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------
	private boolean crashed;

	//----------------------------------------------------------------------

	public FRLPc(String hostname, String url, boolean masked) {
		this.hostname = hostname;
		this.url = url;
		this.masked = masked;
	}

	//----------------------------------------------------------------------

}

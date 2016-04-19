package rcms.utilities.daqaggregator.data;

public class RU {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	/** the FEDbuilder this RU corresponds to */
	private final FEDBuilder fedBuilder;
  
	private final String hostname;
  
	private final boolean isEVM;
  
	private final boolean masked;
	
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------

	private String errorMsg;
  
	private String warnMsg;
  
	private String infoMsg;
  
	/** events rate in kHz ? */
	private float rate;
  
	/** MByte per second ? */
	private float throughput;
  
	/** mean superfragment size in kByte ? */
	private float superFragmentSizeMean;
  
	/** spread of superfragment size in kByte ? */
	private float superFragmentSizeStddev;
  
	private int fragmentsInRU;
  
	private int eventsInRU;
  
	/** requests from BUs ? */
	private int requests;

	//----------------------------------------------------------------------

	public RU(FEDBuilder fedBuilder, String hostname, boolean isEVM,
			boolean masked) {
		this.fedBuilder = fedBuilder;
		this.hostname = hostname;
		this.isEVM = isEVM;
		this.masked = masked;
	}

	//----------------------------------------------------------------------

}

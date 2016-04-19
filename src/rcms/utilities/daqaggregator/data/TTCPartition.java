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

	public String getTtsState() {
		return ttsState;
	}

	public void setTtsState(String ttsState) {
		this.ttsState = ttsState;
	}

	public float getPercentWarning() {
		return percentWarning;
	}

	public void setPercentWarning(float percentWarning) {
		this.percentWarning = percentWarning;
	}

	public float getPercentBusy() {
		return percentBusy;
	}

	public void setPercentBusy(float percentBusy) {
		this.percentBusy = percentBusy;
	}

	public String getName() {
		return name;
	}

	public boolean isMasked() {
		return masked;
	}

	public FMM getFmm() {
		return fmm;
	}

	//----------------------------------------------------------------------
	
}

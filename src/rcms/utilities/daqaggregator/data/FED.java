package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

public class FED {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	/** the parent FRL */
	private final FRL frl;
  
	/** which FRL input: 0 or 1 */ 
	private final int frlIO; 
  
	/** can be null */
	private final FMM fmm;
          
	private final int fmmIO;
  
	private final int srcIdExpected;

	/** important for pseudofeds */
	private final List<FED> mainFeds = new ArrayList<FED>();
	  
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------

	private int srcIdReceived;
  
	private float percentBackpressure;
	
	private float percentWarning;
  
	private float percentBusy;
  
	private String ttsState;
  
	private long numSCRCerrors;
  
	private long numFRCerrors;
  
	private long numTriggers;

	//----------------------------------------------------------------------

	public FED(FRL frl, int frlIO, FMM fmm, int fmmIO, int srcIdExpected) {
		this.frl = frl;
		this.frlIO = frlIO;
		this.fmm = fmm;
		this.fmmIO = fmmIO;
		this.srcIdExpected = srcIdExpected;

		// TODO: fill mainFeds
	}
	
	//----------------------------------------------------------------------

  
}

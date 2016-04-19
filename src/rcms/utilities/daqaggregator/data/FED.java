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

	public int getSrcIdReceived() {
		return srcIdReceived;
	}

	public void setSrcIdReceived(int srcIdReceived) {
		this.srcIdReceived = srcIdReceived;
	}

	public float getPercentBackpressure() {
		return percentBackpressure;
	}

	public void setPercentBackpressure(float percentBackpressure) {
		this.percentBackpressure = percentBackpressure;
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

	public String getTtsState() {
		return ttsState;
	}

	public void setTtsState(String ttsState) {
		this.ttsState = ttsState;
	}

	public long getNumSCRCerrors() {
		return numSCRCerrors;
	}

	public void setNumSCRCerrors(long numSCRCerrors) {
		this.numSCRCerrors = numSCRCerrors;
	}

	public long getNumFRCerrors() {
		return numFRCerrors;
	}

	public void setNumFRCerrors(long numFRCerrors) {
		this.numFRCerrors = numFRCerrors;
	}

	public long getNumTriggers() {
		return numTriggers;
	}

	public void setNumTriggers(long numTriggers) {
		this.numTriggers = numTriggers;
	}

	public FRL getFrl() {
		return frl;
	}

	public int getFrlIO() {
		return frlIO;
	}

	public FMM getFmm() {
		return fmm;
	}

	public int getFmmIO() {
		return fmmIO;
	}

	public int getSrcIdExpected() {
		return srcIdExpected;
	}

	public List<FED> getMainFeds() {
		return mainFeds;
	}
	
	//----------------------------------------------------------------------

  
}

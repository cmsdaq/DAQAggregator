package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class FED {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	/** the parent FRL */
	private FRL frl;
  
	/** which FRL input: 0 or 1 */ 
	private int frlIO; 
  
	/** can be null */
	private FMM fmm;
          
	private int fmmIO;
  
	private int srcIdExpected;

	/** important for pseudofeds */
	private List<FED> mainFeds = new ArrayList<FED>();
	  
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

	public void setFrl(FRL frl) {
		this.frl = frl;
	}

	public void setFrlIO(int frlIO) {
		this.frlIO = frlIO;
	}

	public void setFmm(FMM fmm) {
		this.fmm = fmm;
	}

	public void setFmmIO(int fmmIO) {
		this.fmmIO = fmmIO;
	}

	public void setSrcIdExpected(int srcIdExpected) {
		this.srcIdExpected = srcIdExpected;
	}

	public void setMainFeds(List<FED> mainFeds) {
		this.mainFeds = mainFeds;
	}
	
	//----------------------------------------------------------------------

  
}

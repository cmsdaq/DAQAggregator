package rcms.utilities.daqaggregator.data;

/**
 * Wrapper for information for a partition which comes from TCDS side
 * 
 * 
 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
 */
public class TCDSPartitionInfo {
	
	private String triggerName;
	
	private int PMNr;
	
	private int ICINr;
	
	private String piContext;
	
	//stores special values when trigger cannot be resolved
	private String nullCause;

	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	public int getPMNr() {
		return PMNr;
	}

	public void setPMNr(int pMNr) {
		PMNr = pMNr;
	}

	public int getICINr() {
		return ICINr;
	}

	public void setICINr(int iCINr) {
		ICINr = iCINr;
	}

	public String getNullCause() {
		return nullCause;
	}

	public void setNullCause(String nullCause) {
		this.nullCause = nullCause;
	}

	public String getPiContext() {
		return piContext;
	}

	public void setPiContext(String piContext) {
		this.piContext = piContext;
	} 
	
}

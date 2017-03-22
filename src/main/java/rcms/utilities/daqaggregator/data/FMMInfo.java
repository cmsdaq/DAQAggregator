package rcms.utilities.daqaggregator.data;

/**
 * Wrapper for information for the topFMM of TTCPartition
 * 
 * 
 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
 */
public class FMMInfo {
	
	private String ab;
	
	private String nullCause;

	public String getAb() {
		return ab;
	}

	public void setAb(String ab) {
		this.ab = ab;
	}

	public String getNullCause() {
		return nullCause;
	}

	public void setNullCause(String nullCause) {
		this.nullCause = nullCause;
	} 
	
}

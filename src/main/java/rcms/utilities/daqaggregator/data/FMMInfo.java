package rcms.utilities.daqaggregator.data;

/**
 * Wrapper for information at the topFMM of TTCPartition
 * 
 * Fields of this class are used to query the TCDS_PM_TTS_CHANNEL flashlist (or local maps with its tuples indexed)
 * 
 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
 */
public class FMMInfo {
	
	private String ab;
	
	private int PMNr;
	
	private int ICINr;
	
	//to further add icitype and other parameters from ici in future development (now hardcoded wherever used)
	
	private String nullCause; //stores "-", "noTRG", "noICI", "noPI" in those cases that lead to no topFMM being returned for a TTCP
	

	public String getAb() {
		return ab;
	}

	public void setAb(String ab) {
		this.ab = ab;
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

}

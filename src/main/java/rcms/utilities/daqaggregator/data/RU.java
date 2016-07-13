package rcms.utilities.daqaggregator.data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;
import rcms.utilities.daqaggregator.mappers.Derivable;
import rcms.utilities.daqaggregator.mappers.FlashlistType;

/**
 * Readout Unit
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class RU implements Serializable, FlashlistUpdatable, Derivable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** the FEDbuilder this RU corresponds to */
	private FEDBuilder fedBuilder;

	private String hostname;

	private boolean isEVM;

	private boolean masked;

	private int instance;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	private String stateName;

	private String errorMsg;

	private String warnMsg;

	private String infoMsg;

	/** events rate in kHz ? */
	private float rate;

	/** MByte per second ? */
	private float throughput;

	/**
	 * TODO: mean superfragment size in kByte?, TODO: mean over what period ?
	 */
	private float superFragmentSizeMean;

	/** spread of superfragment size in kByte ? */
	private float superFragmentSizeStddev;

	private int fragmentsInRU;

	private int eventsInRU;

	/** requests from BUs ? */
	private int requests;

	private int incompleteSuperFragmentCount;
	
	private String status;
	// ----------------------------------------------------------------------

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getWarnMsg() {
		return warnMsg;
	}

	public void setWarnMsg(String warnMsg) {
		this.warnMsg = warnMsg;
	}

	public String getInfoMsg() {
		return infoMsg;
	}

	public void setInfoMsg(String infoMsg) {
		this.infoMsg = infoMsg;
	}

	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		this.rate = rate;
	}

	public float getThroughput() {
		return throughput;
	}

	public void setThroughput(float throughput) {
		this.throughput = throughput;
	}

	public float getSuperFragmentSizeMean() {
		return superFragmentSizeMean;
	}

	public void setSuperFragmentSizeMean(float superFragmentSizeMean) {
		this.superFragmentSizeMean = superFragmentSizeMean;
	}

	public float getSuperFragmentSizeStddev() {
		return superFragmentSizeStddev;
	}

	public void setSuperFragmentSizeStddev(float superFragmentSizeStddev) {
		this.superFragmentSizeStddev = superFragmentSizeStddev;
	}

	public int getFragmentsInRU() {
		return fragmentsInRU;
	}

	public void setFragmentsInRU(int fragmentsInRU) {
		this.fragmentsInRU = fragmentsInRU;
	}

	public int getEventsInRU() {
		return eventsInRU;
	}

	public void setEventsInRU(int eventsInRU) {
		this.eventsInRU = eventsInRU;
	}

	public int getRequests() {
		return requests;
	}

	public void setRequests(int requests) {
		this.requests = requests;
	}

	public FEDBuilder getFedBuilder() {
		return fedBuilder;
	}

	public String getHostname() {
		return hostname;
	}

	public boolean isEVM() {
		return isEVM;
	}

	public boolean isMasked() {
		return masked;
	}

	public void setFedBuilder(FEDBuilder fedBuilder) {
		this.fedBuilder = fedBuilder;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setEVM(boolean isEVM) {
		this.isEVM = isEVM;
	}

	public void setMasked(boolean masked) {
		this.masked = masked;
	}

	public int getInstance() {
		return instance;
	}

	public void setInstance(int instance) {
		this.instance = instance;
	}

	@Override
	public String toString() {
		return "RU [rate=" + rate + ", throughput=" + throughput + ", superFragmentSizeMean=" + superFragmentSizeMean
				+ ", superFragmentSizeStddev=" + superFragmentSizeStddev + ", fragmentsInRU=" + fragmentsInRU
				+ ", eventsInRU=" + eventsInRU + "]";
	}


	public int getIncompleteSuperFragmentCount() {
		return incompleteSuperFragmentCount;
	}

	public void setIncompleteSuperFragmentCount(int incompleteSuperFragmentCount) {
		this.incompleteSuperFragmentCount = incompleteSuperFragmentCount;
	}

	@Override
	public void calculateDerivedValues() {

		masked = false;
		int maskedFeds = 0;
		int allFeds = 0;

		for (SubFEDBuilder subFedBuilder : fedBuilder.getSubFedbuilders()) {
			for (FRL frl : subFedBuilder.getFrls()) {
				for (FED fed : frl.getFeds().values()) {
					allFeds++;
					if (fed.isFrlMasked()) {
						maskedFeds++;
					}
				}
			}
		}

		/* Ru is mask if all of FEDs are masked */
		if (maskedFeds == allFeds) {
			masked = true;
		}

	}

	/**
	 * Update object based on given flashlist fragment
	 * 
	 * @param flashlistRow
	 *            JsonNode representing one row from flashlist
	 */
	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {

		/* ignore data from RU flashlist for EVM */
		if (isEVM && flashlistType == FlashlistType.RU)
			return;
		
		/**
		 * For dispatching Flashlist RU to RU objects and flashist EVM to EVM
		 * objects see {@link FlashlistDispatcher}
		 */
		if (flashlistType == FlashlistType.RU || flashlistType == FlashlistType.EVM) {
			// direct values

			this.setStateName(flashlistRow.get("stateName").asText());
			this.setErrorMsg(flashlistRow.get("errorMsg").asText());
			this.requests = flashlistRow.get("eventCount").asInt();
			this.rate = flashlistRow.get("eventRate").asInt();
			this.eventsInRU = flashlistRow.get("eventsInRU").asInt();
			this.fragmentsInRU = flashlistRow.get("fragmentCount").asInt();
			this.superFragmentSizeMean = flashlistRow.get("superFragmentSize").asInt();
			this.superFragmentSizeStddev = flashlistRow.get("superFragmentSizeStdDev").asInt();
			this.incompleteSuperFragmentCount = flashlistRow.get("incompleteSuperFragmentCount").asInt();

			// derived values
			this.throughput = rate * superFragmentSizeMean;

		}
	}
	
	

	@Override
	public void clean() {
		// nothing to do
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((errorMsg == null) ? 0 : errorMsg.hashCode());
		result = prime * result + eventsInRU;
		result = prime * result + fragmentsInRU;
		result = prime * result + ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + incompleteSuperFragmentCount;
		result = prime * result + ((infoMsg == null) ? 0 : infoMsg.hashCode());
		result = prime * result + instance;
		result = prime * result + (isEVM ? 1231 : 1237);
		result = prime * result + (masked ? 1231 : 1237);
		result = prime * result + Float.floatToIntBits(rate);
		result = prime * result + requests;
		result = prime * result + ((stateName == null) ? 0 : stateName.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + Float.floatToIntBits(superFragmentSizeMean);
		result = prime * result + Float.floatToIntBits(superFragmentSizeStddev);
		result = prime * result + Float.floatToIntBits(throughput);
		result = prime * result + ((warnMsg == null) ? 0 : warnMsg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RU other = (RU) obj;
		if (errorMsg == null) {
			if (other.errorMsg != null)
				return false;
		} else if (!errorMsg.equals(other.errorMsg))
			return false;
		if (eventsInRU != other.eventsInRU)
			return false;
		if (fragmentsInRU != other.fragmentsInRU)
			return false;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (incompleteSuperFragmentCount != other.incompleteSuperFragmentCount)
			return false;
		if (infoMsg == null) {
			if (other.infoMsg != null)
				return false;
		} else if (!infoMsg.equals(other.infoMsg))
			return false;
		if (instance != other.instance)
			return false;
		if (isEVM != other.isEVM)
			return false;
		if (masked != other.masked)
			return false;
		if (Float.floatToIntBits(rate) != Float.floatToIntBits(other.rate))
			return false;
		if (requests != other.requests)
			return false;
		if (stateName == null) {
			if (other.stateName != null)
				return false;
		} else if (!stateName.equals(other.stateName))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (Float.floatToIntBits(superFragmentSizeMean) != Float.floatToIntBits(other.superFragmentSizeMean))
			return false;
		if (Float.floatToIntBits(superFragmentSizeStddev) != Float.floatToIntBits(other.superFragmentSizeStddev))
			return false;
		if (Float.floatToIntBits(throughput) != Float.floatToIntBits(other.throughput))
			return false;
		if (warnMsg == null) {
			if (other.warnMsg != null)
				return false;
		} else if (!warnMsg.equals(other.warnMsg))
			return false;
		return true;
	}



}

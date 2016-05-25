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

	private String status;

	// ----------------------------------------------------------------------

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

		if (flashlistType == FlashlistType.RU) {
			// direct values
			this.requests = flashlistRow.get("eventCount").asInt();
			this.rate = flashlistRow.get("eventRate").asInt();
			this.eventsInRU = flashlistRow.get("eventsInRU").asInt();
			this.fragmentsInRU = flashlistRow.get("fragmentCount").asInt();
			this.superFragmentSizeMean = flashlistRow.get("superFragmentSize").asInt();
			this.superFragmentSizeStddev = flashlistRow.get("superFragmentSizeStdDev").asInt();
			this.status = flashlistRow.get("stateName").asText();

			// derived values
			this.throughput = rate * superFragmentSizeMean;

		}
	}

	// ----------------------------------------------------------------------

}

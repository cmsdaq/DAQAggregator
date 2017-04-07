package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.helper.BackpressureConverter;
import rcms.utilities.daqaggregator.datasource.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Front End Driver
 *
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
public class FED implements FlashlistUpdatable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** id of the fed */
	private int id;

	/** the parent FRL */
	private FRL frl;

	/** can be null */
	private FMM fmm;

	private TTCPartition ttcp;

	/** which FRL input: 0 or 1 */
	private int frlIO;

	private int fmmIO;

	private int srcIdExpected;

	/** pseudofeds */
	private List<FED> dependentFeds = new ArrayList<FED>();

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	private Integer srcIdReceived;

	private Float percentBackpressure;

	private Double frl_AccBIFIBackpressureSeconds;

	private Float percentWarning;

	private Float percentBusy;

	private String ttsState;

	private Long numSCRCerrors;

	private Long numFCRCerrors;

	private Long numTriggers;

	private Long eventCounter;

	private Boolean fmmMasked;

	private Boolean frlMasked;

	private boolean hasSLINK;

	private boolean hasTTS;

	private Boolean ruFedInError;
	private Integer ruFedBXError;
	private Integer ruFedCRCError;
	private Integer ruFedDataCorruption;
	private Integer ruFedOutOfSync;

	private Boolean ruFedWithoutFragments;

	private Double frl_AccSlinkFullSec;

	private Double frl_AccLatchedFerol40ClockSeconds;

	@JsonIgnore
	private BackpressureConverter converter = new BackpressureConverter();

	/**
	 * Available columns in flashlist FMM_INPUT:
	 * 
	 * <pre>
	 * {@code[
	[{"key":"class","type":"string"},
	{"key":"context","type":"string"},
	{"key":"fractionBusy","type":"double"},
	{"key":"fractionError","type":"double"},
	{"key":"fractionOOS","type":"double"},
	{"key":"fractionReady","type":"double"},
	{"key":"fractionWarning","type":"double"},
	{"key":"geoslot","type":"unsigned short"},
	{"key":"hostname","type":"string"},
	{"key":"inputState","type":"string"},
	{"key":"instance","type":"string"},
	{"key":"integralTimeBusy","type":"unsigned int 64"},
	{"key":"integralTimeError","type":"unsigned int 64"},
	{"key":"integralTimeOOS","type":"unsigned int 64"},
	{"key":"integralTimeReady","type":"unsigned int 64"},
	{"key":"integralTimeWarning","type":"unsigned int 64"},
	{"key":"io","type":"unsigned short"},
	{"key":"isActive","type":"bool"},
	{"key":"lid","type":"string"},
	{"key":"readTimestamp","type":"time"},
	{"key":"runNumber","type":"unsigned int 32"},
	{"key":"sessionid","type":"string"},
	{"key":"timestamp","type":"time"},
	{"key":"timeTag","type":"unsigned int 64"}] 

	 * }
	 * </pre>
	 * 
	 * Available columns in flashlist FEROL_INPUT_STREAM:
	 * 
	 * <pre>
	 * {@code[
	[{"key":"AccBackpressureSecond","type":"double"},
	{"key":"BackpressureCounter","type":"unsigned int 64"},
	{"key":"BX","type":"unsigned int 32"},
	{"key":"context","type":"string"},
	{"key":"CurrentFragSizeReceived","type":"unsigned int 32"},
	{"key":"EventCounter","type":"unsigned int 64"},
	{"key":"expectedFedId","type":"unsigned int 32"},
	{"key":"ExpectedTriggerNumber","type":"unsigned int 32"},
	{"key":"FEDCRCError","type":"unsigned int 64"},
	{"key":"FEDFrequency","type":"unsigned int 32"},
	{"key":"instance","type":"string"},
	{"key":"lid","type":"string"},
	{"key":"LinkCRCError","type":"unsigned int 64"},
	{"key":"MaxFragSizeReceived","type":"unsigned int 32"},
	{"key":"NoOfFragmentsCut","type":"unsigned int 32"},
	{"key":"ReceivedTriggerNumber","type":"unsigned int 32"},
	{"key":"SenderFwVersion","type":"unsigned int 32"},
	{"key":"sessionid","type":"string"},
	{"key":"slotNumber","type":"unsigned int 32"},
	{"key":"streamNumber","type":"unsigned int 32"},
	{"key":"SyncLostDraining","type":"unsigned int 32"},
	{"key":"timestamp","type":"time"},
	{"key":"TriggerNumber","type":"unsigned int 32"},
	{"key":"WrongFEDId","type":"unsigned int 32"},
	{"key":"WrongFEDIdDetected","type":"unsigned int 32"}] 

	 * }
	 * </pre>
	 * 
	 */
	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {

		if (flashlistType == FlashlistType.FMM_INPUT) {

			this.percentWarning = (float) (flashlistRow.get("fractionWarning").asDouble() * 100);
			this.percentBusy = (float) (flashlistRow.get("fractionBusy").asDouble() * 100);
			this.ttsState = flashlistRow.get("inputState").asText();
			this.fmmMasked = !flashlistRow.get("isActive").asBoolean();

		} else if (flashlistType == FlashlistType.FEROL_INPUT_STREAM) {

			if (flashlistRow.get("WrongFEDIdDetected").asInt() == 0) {
				// srcIdExpected already filled at mapping from corresponding
				// hwfed
				this.srcIdReceived = this.srcIdExpected;
			} else {
				this.srcIdReceived = flashlistRow.get("WrongFEDId").asInt();
			}

			this.numSCRCerrors = flashlistRow.get("LinkCRCError").asLong();
			this.numFCRCerrors = flashlistRow.get("FEDCRCError").asLong();
			this.numTriggers = flashlistRow.get("TriggerNumber").asLong();
			this.eventCounter = flashlistRow.get("EventCounter").asLong();

			/*
			 * converting accumulated backpressure from flashlist
			 */
			this.percentBackpressure = converter.calculatePercent(flashlistRow.get("AccBackpressureSecond").asDouble(),
					flashlistRow.get("timestamp").asText());

		} else if (flashlistType == FlashlistType.FEROL_CONFIGURATION) {

			if (this.frlIO == 0){
				this.frlMasked = !flashlistRow.get("enableStream0").asBoolean();
			}else if (this.frlIO == 1){
				this.frlMasked = !flashlistRow.get("enableStream1").asBoolean();
			}

		} else if (flashlistType == FlashlistType.FEROL40_STREAM_CONFIGURATION) {

			this.frlMasked = !flashlistRow.get("enable").asBoolean();

		} else if (flashlistType == FlashlistType.FRL_MONITORING) {

			if (this.frlIO == 0)
				this.frl_AccSlinkFullSec = flashlistRow.get("AccSlinkFullSec_L0").asDouble();

			else if (this.frlIO == 1)
				this.frl_AccSlinkFullSec = flashlistRow.get("AccSlinkFullSec_L1").asDouble();

		} else if (flashlistType == FlashlistType.RU) {

			int myPositionInErrorArray = -1;
			int currentPosition = 0;
			for (JsonNode fedIdWithError : flashlistRow.get("fedIdsWithErrors")) {
				if (srcIdExpected == fedIdWithError.asInt())
					myPositionInErrorArray = currentPosition;
				currentPosition++;
			}
			ruFedInError = false;
			if (myPositionInErrorArray >= 0) {
				ruFedInError = true;
				ruFedBXError = flashlistRow.get("fedBXerrors").get(myPositionInErrorArray).asInt();
				ruFedCRCError = flashlistRow.get("fedCRCerrors").get(myPositionInErrorArray).asInt();
				ruFedDataCorruption = flashlistRow.get("fedDataCorruption").get(myPositionInErrorArray).asInt();
				ruFedOutOfSync = flashlistRow.get("fedOutOfSync").get(myPositionInErrorArray).asInt();
			}

			ruFedWithoutFragments = false;
			for (JsonNode fedIdWithError : flashlistRow.get("fedIdsWithoutFragments")) {
				if (srcIdExpected == fedIdWithError.asInt()) {
					ruFedWithoutFragments = true;
					break;
				}
			}
		} else if (flashlistType == FlashlistType.FEROL40_INPUT_STREAM) {

			if (flashlistRow.get("WrongFEDIdDetected").asInt() == 0) {
				// srcIdExpected already filled at mapping from corresponding
				// hwfed
				this.srcIdReceived = this.srcIdExpected;
			} else {
				this.srcIdReceived = flashlistRow.get("WrongFEDId").asInt();
			}

			this.numSCRCerrors = flashlistRow.get("LinkCRCError").asLong();
			this.numFCRCerrors = flashlistRow.get("FEDCRCError").asLong();
			this.numTriggers = flashlistRow.get("TriggerNumber").asLong();
			this.eventCounter = flashlistRow.get("EventCounter").asLong();

			/*
			 * should be used to convert accumulated backpressure from flashlist
			 */
			this.frl_AccLatchedFerol40ClockSeconds  = flashlistRow.get("LatchedFerol40ClockSeconds").asDouble();


			this.percentBackpressure = converter.calculatePercent(flashlistRow.get("AccBackpressureSeconds").asDouble(),
					flashlistRow.get("timestamp").asText()); //to be replaced with latchedSeconds (unit is seconds)

			this.frl_AccSlinkFullSec = flashlistRow.get("AccSlinkFullSeconds").asDouble();

			this.frl_AccBIFIBackpressureSeconds = flashlistRow.get("AccBIFIBackpressureSeconds").asDouble();

		}

	}

	@Override
	public void clean() {
		ruFedBXError = null;
		ruFedCRCError = null;
		ruFedDataCorruption = null;
		ruFedOutOfSync = null;
		ruFedInError = null;
		ruFedWithoutFragments = null;
		percentWarning = null;
		percentBusy = null;
		ttsState = null;
		fmmMasked = null;
		srcIdReceived = null;
		numSCRCerrors = null;
		numFCRCerrors = null;
		numTriggers = null;
		eventCounter = null;
		percentBackpressure = null;
		frlMasked = null;
		frl_AccSlinkFullSec = null;
		frl_AccLatchedFerol40ClockSeconds  = null;
		frl_AccBIFIBackpressureSeconds = null;
	}


	public String getTtsState() {
		return ttsState;
	}

	public void setTtsState(String ttsState) {
		this.ttsState = ttsState;
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

	public List<FED> getDependentFeds() {
		return dependentFeds;
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

	public void setDependentFeds(List<FED> dependentFeds) {
		this.dependentFeds = dependentFeds;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TTCPartition getTtcp() {
		return ttcp;
	}

	public void setTtcp(TTCPartition ttcp) {
		this.ttcp = ttcp;
	}
	

	public Integer getSrcIdReceived() {
		return srcIdReceived;
	}

	public void setSrcIdReceived(Integer srcIdReceived) {
		this.srcIdReceived = srcIdReceived;
	}

	public Float getPercentBackpressure() {
		return percentBackpressure;
	}

	public void setPercentBackpressure(Float percentBackpressure) {
		this.percentBackpressure = percentBackpressure;
	}

	public Double getFrl_AccBIFIBackpressureSeconds() {
		return frl_AccBIFIBackpressureSeconds;
	}

	public void setFrl_AccBIFIBackpressureSeconds(Double frl_AccBIFIBackpressureSeconds) {
		this.frl_AccBIFIBackpressureSeconds = frl_AccBIFIBackpressureSeconds;
	}

	public Float getPercentWarning() {
		return percentWarning;
	}

	public void setPercentWarning(Float percentWarning) {
		this.percentWarning = percentWarning;
	}

	public Float getPercentBusy() {
		return percentBusy;
	}

	public void setPercentBusy(Float percentBusy) {
		this.percentBusy = percentBusy;
	}

	public Long getNumSCRCerrors() {
		return numSCRCerrors;
	}

	public void setNumSCRCerrors(Long numSCRCerrors) {
		this.numSCRCerrors = numSCRCerrors;
	}

	public Long getNumFCRCerrors() {
		return numFCRCerrors;
	}

	public void setNumFCRCerrors(Long numFCRCerrors) {
		this.numFCRCerrors = numFCRCerrors;
	}

	public Long getNumTriggers() {
		return numTriggers;
	}

	public void setNumTriggers(Long numTriggers) {
		this.numTriggers = numTriggers;
	}

	public Long getEventCounter() {
		return eventCounter;
	}

	public void setEventCounter(Long eventCounter) {
		this.eventCounter = eventCounter;
	}

	public Boolean isFmmMasked() {
		return fmmMasked;
	}

	public void setFmmMasked(Boolean fmmMasked) {
		this.fmmMasked = fmmMasked;
	}

	public Boolean isFrlMasked() {
		return frlMasked;
	}

	public void setFrlMasked(Boolean frlMasked) {
		this.frlMasked = frlMasked;
	}

	public boolean isHasSLINK() {
		return hasSLINK;
	}

	public void setHasSLINK(boolean hasSLINK) {
		this.hasSLINK = hasSLINK;
	}

	public boolean isHasTTS() {
		return hasTTS;
	}

	public void setHasTTS(boolean hasTTS) {
		this.hasTTS = hasTTS;
	}

	public Boolean isRuFedInError() {
		return ruFedInError;
	}

	public void setRuFedInError(Boolean ruFedInError) {
		this.ruFedInError = ruFedInError;
	}

	public Integer getRuFedBXError() {
		return ruFedBXError;
	}

	public void setRuFedBXError(Integer ruFedBXError) {
		this.ruFedBXError = ruFedBXError;
	}

	public Integer getRuFedCRCError() {
		return ruFedCRCError;
	}

	public void setRuFedCRCError(Integer ruFedCRCError) {
		this.ruFedCRCError = ruFedCRCError;
	}

	public Integer getRuFedDataCorruption() {
		return ruFedDataCorruption;
	}

	public void setRuFedDataCorruption(Integer ruFedDataCorruption) {
		this.ruFedDataCorruption = ruFedDataCorruption;
	}

	public Integer getRuFedOutOfSync() {
		return ruFedOutOfSync;
	}

	public void setRuFedOutOfSync(Integer ruFedOutOfSync) {
		this.ruFedOutOfSync = ruFedOutOfSync;
	}

	public Boolean isRuFedWithoutFragments() {
		return ruFedWithoutFragments;
	}

	public void setRuFedWithoutFragments(Boolean ruFedWithoutFragments) {
		this.ruFedWithoutFragments = ruFedWithoutFragments;
	}

	public Double getFrl_AccSlinkFullSec() {
		return frl_AccSlinkFullSec;
	}

	public void setFrl_AccSlinkFullSec(Double frl_AccSlinkFullSec) {
		this.frl_AccSlinkFullSec = frl_AccSlinkFullSec;
	}

	public Double getFrl_AccLatchedFerol40ClockSeconds() {
		return frl_AccLatchedFerol40ClockSeconds;
	}

	public void setFrl_AccLatchedFerol40ClockSeconds(Double frl_AccLatchedFerol40ClockSeconds) {
		this.frl_AccLatchedFerol40ClockSeconds = frl_AccLatchedFerol40ClockSeconds;
	}

	@Override
	public String toString() {
		return "FED [id=" + id + ", ttsState=" + ttsState + ", frlMasked=" + frlMasked + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eventCounter == null) ? 0 : eventCounter.hashCode());
		result = prime * result + fmmIO;
		result = prime * result + ((fmmMasked == null) ? 0 : fmmMasked.hashCode());
		result = prime * result + frlIO;
		result = prime * result + ((frlMasked == null) ? 0 : frlMasked.hashCode());
		result = prime * result
				+ ((frl_AccBIFIBackpressureSeconds == null) ? 0 : frl_AccBIFIBackpressureSeconds.hashCode());
		result = prime * result
				+ ((frl_AccLatchedFerol40ClockSeconds == null) ? 0 : frl_AccLatchedFerol40ClockSeconds.hashCode());
		result = prime * result + ((frl_AccSlinkFullSec == null) ? 0 : frl_AccSlinkFullSec.hashCode());
		result = prime * result + (hasSLINK ? 1231 : 1237);
		result = prime * result + (hasTTS ? 1231 : 1237);
		result = prime * result + id;
		result = prime * result + ((numFCRCerrors == null) ? 0 : numFCRCerrors.hashCode());
		result = prime * result + ((numSCRCerrors == null) ? 0 : numSCRCerrors.hashCode());
		result = prime * result + ((numTriggers == null) ? 0 : numTriggers.hashCode());
		result = prime * result + ((percentBackpressure == null) ? 0 : percentBackpressure.hashCode());
		result = prime * result + ((percentBusy == null) ? 0 : percentBusy.hashCode());
		result = prime * result + ((percentWarning == null) ? 0 : percentWarning.hashCode());
		result = prime * result + ((ruFedBXError == null) ? 0 : ruFedBXError.hashCode());
		result = prime * result + ((ruFedCRCError == null) ? 0 : ruFedCRCError.hashCode());
		result = prime * result + ((ruFedDataCorruption == null) ? 0 : ruFedDataCorruption.hashCode());
		result = prime * result + ((ruFedInError == null) ? 0 : ruFedInError.hashCode());
		result = prime * result + ((ruFedOutOfSync == null) ? 0 : ruFedOutOfSync.hashCode());
		result = prime * result + ((ruFedWithoutFragments == null) ? 0 : ruFedWithoutFragments.hashCode());
		result = prime * result + srcIdExpected;
		result = prime * result + ((srcIdReceived == null) ? 0 : srcIdReceived.hashCode());
		result = prime * result + ((ttsState == null) ? 0 : ttsState.hashCode());
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
		FED other = (FED) obj;
		if (eventCounter == null) {
			if (other.eventCounter != null)
				return false;
		} else if (!eventCounter.equals(other.eventCounter))
			return false;
		if (fmmIO != other.fmmIO)
			return false;
		if (fmmMasked == null) {
			if (other.fmmMasked != null)
				return false;
		} else if (!fmmMasked.equals(other.fmmMasked))
			return false;
		if (frlIO != other.frlIO)
			return false;
		if (frlMasked == null) {
			if (other.frlMasked != null)
				return false;
		} else if (!frlMasked.equals(other.frlMasked))
			return false;
		if (frl_AccBIFIBackpressureSeconds == null) {
			if (other.frl_AccBIFIBackpressureSeconds != null)
				return false;
		} else if (!frl_AccBIFIBackpressureSeconds.equals(other.frl_AccBIFIBackpressureSeconds))
			return false;
		if (frl_AccLatchedFerol40ClockSeconds == null) {
			if (other.frl_AccLatchedFerol40ClockSeconds != null)
				return false;
		} else if (!frl_AccLatchedFerol40ClockSeconds.equals(other.frl_AccLatchedFerol40ClockSeconds))
			return false;
		if (frl_AccSlinkFullSec == null) {
			if (other.frl_AccSlinkFullSec != null)
				return false;
		} else if (!frl_AccSlinkFullSec.equals(other.frl_AccSlinkFullSec))
			return false;
		if (hasSLINK != other.hasSLINK)
			return false;
		if (hasTTS != other.hasTTS)
			return false;
		if (id != other.id)
			return false;
		if (numFCRCerrors == null) {
			if (other.numFCRCerrors != null)
				return false;
		} else if (!numFCRCerrors.equals(other.numFCRCerrors))
			return false;
		if (numSCRCerrors == null) {
			if (other.numSCRCerrors != null)
				return false;
		} else if (!numSCRCerrors.equals(other.numSCRCerrors))
			return false;
		if (numTriggers == null) {
			if (other.numTriggers != null)
				return false;
		} else if (!numTriggers.equals(other.numTriggers))
			return false;
		if (percentBackpressure == null) {
			if (other.percentBackpressure != null)
				return false;
		} else if (!percentBackpressure.equals(other.percentBackpressure))
			return false;
		if (percentBusy == null) {
			if (other.percentBusy != null)
				return false;
		} else if (!percentBusy.equals(other.percentBusy))
			return false;
		if (percentWarning == null) {
			if (other.percentWarning != null)
				return false;
		} else if (!percentWarning.equals(other.percentWarning))
			return false;
		if (ruFedBXError == null) {
			if (other.ruFedBXError != null)
				return false;
		} else if (!ruFedBXError.equals(other.ruFedBXError))
			return false;
		if (ruFedCRCError == null) {
			if (other.ruFedCRCError != null)
				return false;
		} else if (!ruFedCRCError.equals(other.ruFedCRCError))
			return false;
		if (ruFedDataCorruption == null) {
			if (other.ruFedDataCorruption != null)
				return false;
		} else if (!ruFedDataCorruption.equals(other.ruFedDataCorruption))
			return false;
		if (ruFedInError == null) {
			if (other.ruFedInError != null)
				return false;
		} else if (!ruFedInError.equals(other.ruFedInError))
			return false;
		if (ruFedOutOfSync == null) {
			if (other.ruFedOutOfSync != null)
				return false;
		} else if (!ruFedOutOfSync.equals(other.ruFedOutOfSync))
			return false;
		if (ruFedWithoutFragments == null) {
			if (other.ruFedWithoutFragments != null)
				return false;
		} else if (!ruFedWithoutFragments.equals(other.ruFedWithoutFragments))
			return false;
		if (srcIdExpected != other.srcIdExpected)
			return false;
		if (srcIdReceived == null) {
			if (other.srcIdReceived != null)
				return false;
		} else if (!srcIdReceived.equals(other.srcIdReceived))
			return false;
		if (ttsState == null) {
			if (other.ttsState != null)
				return false;
		} else if (!ttsState.equals(other.ttsState))
			return false;
		return true;
	}
	
	
	

}

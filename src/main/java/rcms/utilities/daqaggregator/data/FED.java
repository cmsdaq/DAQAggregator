package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.helper.BackpressureConverter;
import rcms.utilities.daqaggregator.data.helper.FEDHelper;
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

    private String fedGroup;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	private int srcIdReceived;

	private float percentBackpressure;

	private double frl_AccBIFIBackpressureSeconds;

	private float percentWarning;

	private float percentBusy;

	private String ttsState;

	private long numSCRCerrors;

	private long numFCRCerrors;

	private long numTriggers;

	private long eventCounter;

	private boolean fmmMasked;

	private boolean frlMasked;

	private boolean hasSLINK;

	private boolean hasTTS;

	private boolean ruFedInError;
	private int ruFedBXError;
	private int ruFedCRCError;
	private int ruFedDataCorruption;
	private int ruFedOutOfSync;

	private boolean ruFedWithoutFragments;

	private double frl_AccSlinkFullSec;

	private double frl_AccLatchedFerol40ClockSeconds;

	/* flag indicating if data source id generated */
	@JsonIgnore
	private boolean generatorDataSource;

	@JsonIgnore
	private BackpressureConverter converter = new BackpressureConverter();

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

			/*
			 * this field was renamed - to prevent DAQAgg from crashing when old flashlists are used to produce snapshot
			 * below there is a condition. Note that this does NOT mean backward compatibility with old flashlists -
			 * some values will not be mapped
			 */
			if (flashlistRow.has("SLinkCRCError")) {
				this.numSCRCerrors = flashlistRow.get("SLinkCRCError").asInt();
			}
			this.numFCRCerrors = flashlistRow.get("FEDCRCError").asInt();
			this.numTriggers = flashlistRow.get("TriggerNumber").asInt();
			this.eventCounter = flashlistRow.get("EventCounter").asLong();

			/*
			 * some fields were introduced later - to prevent DAQAgg from crashing when old flashlists are used to
			 * produce snapshot below there is a condition. Note that this does NOT mean backward compatibility with old
			 * flashlists - some values will not be mapped
			 */
			if (FEDHelper.isFlashlistFerolInputStreamRowAfterFerol40Backporting(flashlistRow)) {

				this.frl_AccSlinkFullSec = flashlistRow.get("AccSlinkFullSeconds").asDouble();

				/* if data source is generated this value will come from other flashlist */
				if (!generatorDataSource) {
					double latchedTime = flashlistRow.get("LatchedTimeFrontendSeconds").asDouble();
					double backpressureTime = flashlistRow.get("AccBackpressureSeconds").asDouble();

					this.percentBackpressure = converter.calculatePercent(backpressureTime, latchedTime, true);

					this.frl_AccLatchedFerol40ClockSeconds = latchedTime;
				}

			}

		} else if (flashlistType == FlashlistType.FEROL_CONFIGURATION) {

			if (this.frlIO == 0) {
				this.frlMasked = !flashlistRow.get("enableStream0").asBoolean();
			} else if (this.frlIO == 1) {
				this.frlMasked = !flashlistRow.get("enableStream1").asBoolean();
			}

			if (flashlistRow.has("DataSource")
					&& "GENERATOR_SOURCE".equalsIgnoreCase(flashlistRow.get("DataSource").asText())) {
				this.generatorDataSource = true;
			}

		} else if (flashlistType == FlashlistType.FEROL40_STREAM_CONFIGURATION) {

			this.frlMasked = !flashlistRow.get("enable").asBoolean();

		} else if (flashlistType == FlashlistType.RU) {

			int myPositionInErrorArray = -1;
			int currentPosition = 0;
			for (JsonNode fedIdWithError : flashlistRow.get("fedIdsWithErrors")) {
				if (srcIdExpected == fedIdWithError.asInt())
					myPositionInErrorArray = currentPosition;
				currentPosition++;
			}
			if (myPositionInErrorArray >= 0) {
				ruFedInError = true;
				ruFedBXError = flashlistRow.get("fedBXerrors").get(myPositionInErrorArray).asInt();
				ruFedCRCError = flashlistRow.get("fedCRCerrors").get(myPositionInErrorArray).asInt();
				ruFedDataCorruption = flashlistRow.get("fedDataCorruption").get(myPositionInErrorArray).asInt();
				ruFedOutOfSync = flashlistRow.get("fedOutOfSync").get(myPositionInErrorArray).asInt();
			}

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

			// changes - backward compatibility
			if (flashlistRow.has("SLinkCRCError")){
				this.numSCRCerrors = flashlistRow.get("SLinkCRCError").asInt();
			}
			this.numFCRCerrors = flashlistRow.get("FEDCRCError").asInt();
			this.numTriggers = flashlistRow.get("TriggerNumber").asInt();
			this.eventCounter = flashlistRow.get("EventCounter").asLong();

			// if: ferol40StreamConfiguration->DataSource = GENERATOR_SOURCE do not use AccBackpressureSeconds, instead
			// use AccBIFIBackpressureSeconds
			String backpressureColumn = "AccBackpressureSeconds";
			/*
			 * if (this.generatorDataSource) { backpressureColumn = "AccBIFIBackpressureSeconds"; }
			 */
			/*
			 * some fields were introduced later - to prevent DAQAgg from crashing when old flashlists are used to
			 * produce snapshot below there is a condition. Note that this does NOT mean backward compatibility with old
			 * flashlists - some values will not be mapped
			 */
			if (FEDHelper.isFlashlistFerolInputStreamRowAfterFerol40Backporting(flashlistRow)) {
				this.frl_AccLatchedFerol40ClockSeconds = flashlistRow.get("LatchedTimeFrontendSeconds").asDouble();
				this.percentBackpressure = converter.calculatePercent(flashlistRow.get(backpressureColumn).asDouble(),
						this.frl_AccLatchedFerol40ClockSeconds, true); // calculate with latchedSeconds (unit is
																		// seconds)
				this.frl_AccSlinkFullSec = flashlistRow.get("AccSlinkFullSeconds").asDouble();
			}

		} else if (flashlistType == FlashlistType.TCDS_PI_TTS_SUMMARY) {

			String ttsState = FEDHelper.getTTSState(this.fmmIO, flashlistRow);
			this.ttsState = ttsState;

			// unmask if other than 152 = 0x98: ignored (i.e., forced ready)
			if (ttsState != null && !"-".equals(ttsState)) {
				this.fmmMasked = false;
			}
		} else if (flashlistType == FlashlistType.FEROL_TCP_STREAM) {

			if (generatorDataSource) {
				if (flashlistRow.has("AccBIFIBackpressureSeconds")) {
					double latchedTime = flashlistRow.get("LatchedTimeBackendSeconds").asDouble();
					double backpressureTime = flashlistRow.get("AccBIFIBackpressureSeconds").asDouble();

					this.percentBackpressure = converter.calculatePercent(backpressureTime, latchedTime, true);
					this.frl_AccBIFIBackpressureSeconds = backpressureTime;
				}

			}
		}

	}

	@Override
	public void clean() {
		ruFedBXError = 0;
		ruFedCRCError = 0;
		ruFedDataCorruption = 0;
		ruFedOutOfSync = 0;
		ruFedInError = false;
		ruFedWithoutFragments = false;
		percentWarning = 0;
		percentBusy = 0;
		ttsState = null;
		fmmMasked = true;
		srcIdReceived = 0;
		numSCRCerrors = 0;
		numFCRCerrors = 0;
		numTriggers = 0;
		eventCounter = 0;
		percentBackpressure = 0;
		frlMasked = true;
		frl_AccSlinkFullSec = 0;
		frl_AccLatchedFerol40ClockSeconds = 0;
		frl_AccBIFIBackpressureSeconds = 0;
		generatorDataSource = false;
	}

	/**
	 * @return the RU to which this FED is associated or null if no RU is associated to it
	 */
	public RU getRu() {
		try {
			return getFrl().getSubFedbuilder().getFedBuilder().getRu();
		} catch (NullPointerException ex) {
			// happens e.g. if the FED is not associated to an FRL etc.
			return null;
		}
	}

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

	public double getFrl_AccBIFIBackpressureSeconds() {
		return frl_AccBIFIBackpressureSeconds;
	}

	public void setFrl_AccBIFIBackpressureSeconds(double frl_AccBIFIBackpressureSeconds) {
		this.frl_AccBIFIBackpressureSeconds = frl_AccBIFIBackpressureSeconds;
	}

	public double getFrl_AccLatchedFerol40ClockSeconds() {
		return frl_AccLatchedFerol40ClockSeconds;
	}

	public void setFrl_AccLatchedFerol40ClockSeconds(double frl_AccLatchedFerol40ClockSeconds) {
		this.frl_AccLatchedFerol40ClockSeconds = frl_AccLatchedFerol40ClockSeconds;
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

	public long getNumFCRCerrors() {
		return numFCRCerrors;
	}

	public void setNumFCRCerrors(long numFCRCerrors) {
		this.numFCRCerrors = numFCRCerrors;
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

	public long getEventCounter() {
		return eventCounter;
	}

	public void setEventCounter(long eventCounter) {
		this.eventCounter = eventCounter;
	}

	public boolean isFmmMasked() {
		return fmmMasked;
	}

	public void setFmmMasked(boolean fmmMasked) {
		this.fmmMasked = fmmMasked;
	}

	public boolean isFrlMasked() {
		return frlMasked;
	}

	public void setFrlMasked(boolean frlMasked) {
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

	public boolean isRuFedInError() {
		return ruFedInError;
	}

	public void setRuFedInError(boolean ruFedInError) {
		this.ruFedInError = ruFedInError;
	}

	public int getRuFedBXError() {
		return ruFedBXError;
	}

	public void setRuFedBXError(int ruFedBXError) {
		this.ruFedBXError = ruFedBXError;
	}

	public int getRuFedCRCError() {
		return ruFedCRCError;
	}

	public void setRuFedCRCError(int ruFedCRCError) {
		this.ruFedCRCError = ruFedCRCError;
	}

	public int getRuFedDataCorruption() {
		return ruFedDataCorruption;
	}

	public void setRuFedDataCorruption(int ruFedDataCorruption) {
		this.ruFedDataCorruption = ruFedDataCorruption;
	}

	public int getRuFedOutOfSync() {
		return ruFedOutOfSync;
	}

	public void setRuFedOutOfSync(int ruFedOutOfSync) {
		this.ruFedOutOfSync = ruFedOutOfSync;
	}

	public boolean isRuFedWithoutFragments() {
		return ruFedWithoutFragments;
	}

	public void setRuFedWithoutFragments(boolean ruFedWithoutFragments) {
		this.ruFedWithoutFragments = ruFedWithoutFragments;
	}

	public double getFrl_AccSlinkFullSec() {
		return frl_AccSlinkFullSec;
	}

	public void setFrl_AccSlinkFullSec(double frl_AccSlinkFullSec) {
		this.frl_AccSlinkFullSec = frl_AccSlinkFullSec;
	}

	public TTCPartition getTtcp() {
		return ttcp;
	}

	public void setTtcp(TTCPartition ttcp) {
		this.ttcp = ttcp;
	}

	public String getFedGroup() {
		return fedGroup;
	}

	public void setFedGroup(String fedGroup) {
		this.fedGroup = fedGroup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (eventCounter ^ (eventCounter >>> 32));
		result = prime * result + fmmIO;
		result = prime * result + (fmmMasked ? 1231 : 1237);
		result = prime * result + frlIO;
		result = prime * result + (frlMasked ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(frl_AccSlinkFullSec);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (hasSLINK ? 1231 : 1237);
		result = prime * result + (hasTTS ? 1231 : 1237);
		result = prime * result + id;
		result = prime * result + (int) (numFCRCerrors ^ (numFCRCerrors >>> 32));
		result = prime * result + (int) (numSCRCerrors ^ (numSCRCerrors >>> 32));
		result = prime * result + (int) (numTriggers ^ (numTriggers >>> 32));
		result = prime * result + Float.floatToIntBits(percentBackpressure);
		result = prime * result + Float.floatToIntBits(percentBusy);
		result = prime * result + Float.floatToIntBits(percentWarning);
		result = prime * result + ruFedBXError;
		result = prime * result + ruFedCRCError;
		result = prime * result + ruFedDataCorruption;
		result = prime * result + (ruFedInError ? 1231 : 1237);
		result = prime * result + ruFedOutOfSync;
		result = prime * result + (ruFedWithoutFragments ? 1231 : 1237);
		result = prime * result + srcIdExpected;
		result = prime * result + srcIdReceived;
		result = prime * result + ((ttsState == null) ? 0 : ttsState.hashCode());
		result = prime * result + ((fedGroup == null) ? 0 : fedGroup.hashCode());
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
		if (eventCounter != other.eventCounter)
			return false;
		if (fmmIO != other.fmmIO)
			return false;
		if (fmmMasked != other.fmmMasked)
			return false;
		if (frlIO != other.frlIO)
			return false;
		if (frlMasked != other.frlMasked)
			return false;
		if (Double.doubleToLongBits(frl_AccSlinkFullSec) != Double.doubleToLongBits(other.frl_AccSlinkFullSec))
			return false;
		if (hasSLINK != other.hasSLINK)
			return false;
		if (hasTTS != other.hasTTS)
			return false;
		if (id != other.id)
			return false;
		if (numFCRCerrors != other.numFCRCerrors)
			return false;
		if (numSCRCerrors != other.numSCRCerrors)
			return false;
		if (numTriggers != other.numTriggers)
			return false;
		if (Float.floatToIntBits(percentBackpressure) != Float.floatToIntBits(other.percentBackpressure))
			return false;
		if (Float.floatToIntBits(percentBusy) != Float.floatToIntBits(other.percentBusy))
			return false;
		if (Float.floatToIntBits(percentWarning) != Float.floatToIntBits(other.percentWarning))
			return false;
		if (ruFedBXError != other.ruFedBXError)
			return false;
		if (ruFedCRCError != other.ruFedCRCError)
			return false;
		if (ruFedDataCorruption != other.ruFedDataCorruption)
			return false;
		if (ruFedInError != other.ruFedInError)
			return false;
		if (ruFedOutOfSync != other.ruFedOutOfSync)
			return false;
		if (ruFedWithoutFragments != other.ruFedWithoutFragments)
			return false;
		if (srcIdExpected != other.srcIdExpected)
			return false;
		if (srcIdReceived != other.srcIdReceived)
			return false;
		if (ttsState == null) {
			if (other.ttsState != null)
				return false;
		} else if (!ttsState.equals(other.ttsState))
			return false;
		if (fedGroup == null) {
			if (other.fedGroup != null)
				return false;
		} else if (!fedGroup.equals(other.fedGroup))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FED [id=" + id + ", esrcIdExpected=" + srcIdExpected + ", ttsState=" + ttsState + ", frlMasked="
				+ frlMasked + "]";
	}

}

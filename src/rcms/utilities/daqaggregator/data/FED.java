package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;
import rcms.utilities.daqaggregator.mappers.FlashlistType;

/**
 * Front End Driver
 *
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class FED implements java.io.Serializable, FlashlistUpdatable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	/** id of the fed */
	private int id;

	/** the parent FRL */
	private FRL frl;

	/** can be null */
	private FMM fmm;

	/** which FRL input: 0 or 1 */
	private int frlIO;

	private int fmmIO;

	private int srcIdExpected;

	/** important for pseudofeds */
	private List<FED> mainFeds = new ArrayList<FED>();

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	private int srcIdReceived;

	private float percentBackpressure;

	private float percentWarning;

	private float percentBusy;

	private String ttsState;

	private long numSCRCerrors;

	private long numFRCerrors;

	private long numTriggers;
	
	private long eventCounter;

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

		} else if (flashlistType == FlashlistType.FEROL_INPUT_STREAM) {
			// TODO or WrongFEDIdDetected
			this.srcIdReceived = flashlistRow.get("WrongFEDId").asInt();
			this.percentBackpressure = (float) flashlistRow.get("AccBackpressureSecond").asDouble();
			this.numSCRCerrors = flashlistRow.get("LinkCRCError").asInt();
			this.numFRCerrors = flashlistRow.get("FEDCRCError").asInt();
			this.numTriggers = flashlistRow.get("TriggerNumber").asInt();
			this.eventCounter = flashlistRow.get("EventCounter").asInt();
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "FED [id=" + id + ", frlIO=" + frlIO + ", fmmIO=" + fmmIO + ", srcIdExpected=" + srcIdExpected + "]";
	}

	public long getEventCounter() {
		return eventCounter;
	}

	public void setEventCounter(long eventCounter) {
		this.eventCounter = eventCounter;
	}

	// ----------------------------------------------------------------------

}

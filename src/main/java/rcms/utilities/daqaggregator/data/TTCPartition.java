package rcms.utilities.daqaggregator.data;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.Derivable;
import rcms.utilities.daqaggregator.mappers.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Timing Trigger and Control Partition
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class TTCPartition implements java.io.Serializable, FlashlistUpdatable, Derivable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	private String name;

	private boolean masked;

	/** can be null */
	private FMM fmm;
	
	private SubSystem subsystem;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	private String ttsState;

	private float percentWarning;

	private float percentBusy;

	public String getTtsState() {
		return ttsState;
	}

	public void setTtsState(String ttsState) {
		this.ttsState = ttsState;
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

	public String getName() {
		return name;
	}

	public boolean isMasked() {
		return masked;
	}

	public FMM getFmm() {
		return fmm;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMasked(boolean masked) {
		this.masked = masked;
	}

	public void setFmm(FMM fmm) {
		this.fmm = fmm;
	}

	@Override
	public void calculateDerivedValues() {
		masked = false;
		int maskedFeds = 0;
		for (FED fed : fmm.getFeds()) {
			if (fed.isFmmMasked()) {
				maskedFeds++;
			}
		}

		/* TTCPartition is mask if any of FED is masked */
		if (maskedFeds > 0) {
			masked = true;
		}

	}

	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {

		if (flashlistType == FlashlistType.FMM_STATUS) {

			String busyKey = "outputFractionBusy";
			String warningKey = "outputFractionWarning";
			String ttsStateKey = "outputState";
			String output = "A";
			if (fmm.takeB)
				output = "B";

			this.percentBusy = (float) flashlistRow.get(busyKey + output).asDouble() * 100;
			this.percentWarning = (float) flashlistRow.get(warningKey + output).asDouble() * 100;
			this.ttsState = flashlistRow.get(ttsStateKey + output).asText();
		}

	}

	public SubSystem getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(SubSystem subsystem) {
		this.subsystem = subsystem;
	}

}

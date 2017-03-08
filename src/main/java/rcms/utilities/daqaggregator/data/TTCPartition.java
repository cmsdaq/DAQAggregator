package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.FlashlistType;
import rcms.utilities.daqaggregator.mappers.Derivable;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Timing Trigger and Control Partition
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */

public class TTCPartition implements FlashlistUpdatable, Derivable {

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	private String name;

	private boolean masked;

	/** can be null */
	/** this is only a link to the TTCPartition's top level FMM and there might be more FMMs */
	private FMM fmm;
	
	/** Info for the topFMM, or meta-info in case topFMM is null*/
	private FMMInfo topFMMInfo;
	
	private String tcds_pm_ttsState;
	
	private String tcds_apv_pm_ttsState;

	private SubSystem subsystem;
	
	private int ttcpNr;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	// @ top level FMM
	private String ttsState;
	
	//TODO: ttsState @ trigger (now)
	
	//TODO: ttsState @ PI (in future)

	// @ top level FMM
	private float percentWarning;

	// @ top level FMM
	private float percentBusy;

	private List<FED> feds = new ArrayList<>();
	

	public String getTcds_apv_pm_ttsState() {
		return tcds_apv_pm_ttsState;
	}

	public void setTcds_apv_pm_ttsState(String tcds_apv_pm_ttsState) {
		this.tcds_apv_pm_ttsState = tcds_apv_pm_ttsState;
	}

	public String getTcds_pm_ttsState() {
		return tcds_pm_ttsState;
	}

	public void setTcds_pm_ttsState(String tcds_pm_ttsState) {
		this.tcds_pm_ttsState = tcds_pm_ttsState;
	}

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

	public FMMInfo getTopFMMInfo() {
		return topFMMInfo;
	}

	public void setTopFMMInfo(FMMInfo topFMMInfo) {
		this.topFMMInfo = topFMMInfo;
	}

	@Override
	public void calculateDerivedValues() {
		int maskedFeds = 0;
		int all = 0;
		for (FED fed : feds) {
			all++;
			if (fed.isFmmMasked() || !fed.isHasTTS()) {
				maskedFeds++;
			}
		}

		/* TTCPartition is mask if all FEDs are masked */
		if (maskedFeds == all) {
			masked = true;
		}

	}

	@Override
	public void clean() {
		masked = false;
	}

	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {

		if (flashlistType == FlashlistType.FMM_STATUS) {

			String busyKey = "outputFractionBusy";
			String warningKey = "outputFractionWarning";
			String ttsStateKey = "outputState";
			String output = "A";
			if (fmm.isTakeB())
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

	public int getTtcpNr() {
		return ttcpNr;
	}

	public void setTtcpNr(int ttcpNr) {
		this.ttcpNr = ttcpNr;
	}

	public List<FED> getFeds() {
		return feds;
	}

	public void setFeds(List<FED> feds) {
		this.feds = feds;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((feds == null) ? 0 : feds.hashCode());
		result = prime * result + (masked ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + Float.floatToIntBits(percentBusy);
		result = prime * result + Float.floatToIntBits(percentWarning);
		result = prime * result + ttcpNr;
		result = prime * result + ((ttsState == null) ? 0 : ttsState.hashCode());
		result = prime * result + ((tcds_pm_ttsState == null) ? 0 : tcds_pm_ttsState.hashCode());
		result = prime * result + ((tcds_apv_pm_ttsState == null) ? 0 : tcds_apv_pm_ttsState.hashCode());
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
		TTCPartition other = (TTCPartition) obj;
		if (feds == null) {
			if (other.feds != null)
				return false;
		} else if (!feds.equals(other.feds))
			return false;
		if (masked != other.masked)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Float.floatToIntBits(percentBusy) != Float.floatToIntBits(other.percentBusy))
			return false;
		if (Float.floatToIntBits(percentWarning) != Float.floatToIntBits(other.percentWarning))
			return false;
		if (ttcpNr != other.ttcpNr)
			return false;
		if (ttsState == null) {
			if (other.ttsState != null)
				return false;
		} else if (!ttsState.equals(other.ttsState))
			return false;
		if (tcds_pm_ttsState == null) {
			if (other.tcds_pm_ttsState != null)
				return false;
		} else if (!tcds_pm_ttsState.equals(other.tcds_pm_ttsState))
			return false;
		if (tcds_apv_pm_ttsState == null) {
			if (other.tcds_apv_pm_ttsState != null)
				return false;
		} else if (!tcds_apv_pm_ttsState.equals(other.tcds_apv_pm_ttsState))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TTCPartition [name=" + name + ", masked=" + masked + ", ttsState=" + ttsState + ", percentWarning="
				+ percentWarning + ", percentBusy=" + percentBusy + ", feds=" + feds + "]";
	}

}

package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.mappers.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Root class of DAQ structure
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 * 
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
@JsonPropertyOrder({ "allFeds", "frlPcs", "subSystems", "fedBuilders", "fmmApplications" })
public class DAQ implements FlashlistUpdatable {


	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------
	private Set<SubSystem> subSystems;

	private List<FRLPc> frlPcs;

	private List<BU> bus;

	private List<FMMApplication> fmmApplications;

	private int sessionId;

	private String dpsetPath;

	private FEDBuilderSummary fedBuilderSummary;

	private BUSummary buSummary;

	private Set<FED> allFeds;

	// TODO: fed builders not in the structure, but addes tmprlly?
	private final List<FEDBuilder> fedBuilders = new ArrayList<>();

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	public List<FEDBuilder> getFedBuilders() {
		return fedBuilders;
	}

	private int runNumber;

	/** timestamp */
	private long lastUpdate;

	private String daqState;

	private String levelZeroState;
	private String lhcMachineMode;
	private String lhcBeamMode;

	public int getRunNumber() {
		return runNumber;
	}

	public void setRunNumber(int runNumber) {
		this.runNumber = runNumber;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getDaqState() {
		return daqState;
	}

	public void setDaqState(String daqState) {
		this.daqState = daqState;
	}

	public List<FRLPc> getFrlPcs() {
		return frlPcs;
	}

	public List<BU> getBus() {
		return bus;
	}

	public List<FMMApplication> getFmmApplications() {
		return fmmApplications;
	}

	public int getSessionId() {
		return sessionId;
	}

	public String getDpsetPath() {
		return dpsetPath;
	}

	public FEDBuilderSummary getFedBuilderSummary() {
		return fedBuilderSummary;
	}

	public BUSummary getBuSummary() {
		return buSummary;
	}

	public void setFrlPcs(List<FRLPc> frlPcs) {
		this.frlPcs = frlPcs;
	}

	public void setBus(List<BU> bus) {
		this.bus = bus;
	}

	public void setFmmApplications(List<FMMApplication> fmmApplications) {
		this.fmmApplications = fmmApplications;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public void setDpsetPath(String dpsetPath) {
		this.dpsetPath = dpsetPath;
	}

	public void setFedBuilderSummary(FEDBuilderSummary fedBuilderSummary) {
		this.fedBuilderSummary = fedBuilderSummary;
	}

	public void setBuSummary(BUSummary buSummary) {
		this.buSummary = buSummary;
	}

	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {
		if (flashlistType == FlashlistType.LEVEL_ZERO_FM_SUBSYS) {
			this.daqState = flashlistRow.get("STATE").asText();
		} else if (flashlistType == FlashlistType.LEVEL_ZERO_FM_DYNAMIC) {
			this.levelZeroState = flashlistRow.get("STATE").asText();
			this.lhcBeamMode = flashlistRow.get("LHC_BEAM_MODE").asText();
			this.lhcMachineMode = flashlistRow.get("LHC_MACHINE_MODE").asText();
		}

	}

	public String getLevelZeroState() {
		return levelZeroState;
	}

	public void setLevelZeroState(String levelZeroState) {
		this.levelZeroState = levelZeroState;
	}

	public String getLhcMachineMode() {
		return lhcMachineMode;
	}

	public void setLhcMachineMode(String lhcMachineMode) {
		this.lhcMachineMode = lhcMachineMode;
	}

	public String getLhcBeamMode() {
		return lhcBeamMode;
	}

	public void setLhcBeamMode(String lhcBeamMode) {
		this.lhcBeamMode = lhcBeamMode;
	}

	public Set<SubSystem> getSubSystems() {
		return subSystems;
	}

	public void setSubSystems(Set<SubSystem> subSystems) {
		this.subSystems = subSystems;
	}

	@Override
	public void clean() {
		// nothing to do
	}

	public Set<FED> getAllFeds() {
		return allFeds;
	}

	public void setAllFeds(Set<FED> allFeds) {
		this.allFeds = allFeds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allFeds == null) ? 0 : allFeds.hashCode());
		result = prime * result + ((buSummary == null) ? 0 : buSummary.hashCode());
		result = prime * result + ((bus == null) ? 0 : bus.hashCode());
		result = prime * result + ((daqState == null) ? 0 : daqState.hashCode());
		result = prime * result + ((dpsetPath == null) ? 0 : dpsetPath.hashCode());
		result = prime * result + ((fedBuilderSummary == null) ? 0 : fedBuilderSummary.hashCode());
		result = prime * result + ((fedBuilders == null) ? 0 : fedBuilders.hashCode());
		result = prime * result + ((fmmApplications == null) ? 0 : fmmApplications.hashCode());
		result = prime * result + ((frlPcs == null) ? 0 : frlPcs.hashCode());
		result = prime * result + (int) (lastUpdate ^ (lastUpdate >>> 32));
		result = prime * result + ((levelZeroState == null) ? 0 : levelZeroState.hashCode());
		result = prime * result + ((lhcBeamMode == null) ? 0 : lhcBeamMode.hashCode());
		result = prime * result + ((lhcMachineMode == null) ? 0 : lhcMachineMode.hashCode());
		result = prime * result + runNumber;
		result = prime * result + sessionId;
		result = prime * result + ((subSystems == null) ? 0 : subSystems.hashCode());
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
		DAQ other = (DAQ) obj;
		if (allFeds == null) {
			if (other.allFeds != null)
				return false;
		} else if (!allFeds.equals(other.allFeds))
			return false;
		if (buSummary == null) {
			if (other.buSummary != null)
				return false;
		} else if (!buSummary.equals(other.buSummary))
			return false;
		if (bus == null) {
			if (other.bus != null)
				return false;
		} else if (!bus.equals(other.bus))
			return false;
		if (daqState == null) {
			if (other.daqState != null)
				return false;
		} else if (!daqState.equals(other.daqState))
			return false;
		if (dpsetPath == null) {
			if (other.dpsetPath != null)
				return false;
		} else if (!dpsetPath.equals(other.dpsetPath))
			return false;
		if (fedBuilderSummary == null) {
			if (other.fedBuilderSummary != null)
				return false;
		} else if (!fedBuilderSummary.equals(other.fedBuilderSummary))
			return false;
		if (fedBuilders == null) {
			if (other.fedBuilders != null)
				return false;
		} else if (!fedBuilders.equals(other.fedBuilders))
			return false;
		if (fmmApplications == null) {
			if (other.fmmApplications != null)
				return false;
		} else if (!fmmApplications.equals(other.fmmApplications))
			return false;
		if (frlPcs == null) {
			if (other.frlPcs != null)
				return false;
		} else if (!frlPcs.equals(other.frlPcs))
			return false;
		if (lastUpdate != other.lastUpdate)
			return false;
		if (levelZeroState == null) {
			if (other.levelZeroState != null)
				return false;
		} else if (!levelZeroState.equals(other.levelZeroState))
			return false;
		if (lhcBeamMode == null) {
			if (other.lhcBeamMode != null)
				return false;
		} else if (!lhcBeamMode.equals(other.lhcBeamMode))
			return false;
		if (lhcMachineMode == null) {
			if (other.lhcMachineMode != null)
				return false;
		} else if (!lhcMachineMode.equals(other.lhcMachineMode))
			return false;
		if (runNumber != other.runNumber)
			return false;
		if (sessionId != other.sessionId)
			return false;
		if (subSystems == null) {
			if (other.subSystems != null)
				return false;
		} else if (!subSystems.equals(other.subSystems))
			return false;
		return true;
	}

}

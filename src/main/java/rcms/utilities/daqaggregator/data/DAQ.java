package rcms.utilities.daqaggregator.data;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.datasource.DateParser;
import rcms.utilities.daqaggregator.datasource.FlashlistType;
import rcms.utilities.daqaggregator.mappers.FlashlistUpdatable;

/**
 * Root class of DAQ structure
 * 
 * @author Andre Georg Holzner (andre.georg.holzner@cern.ch)
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
 */
public class DAQ implements FlashlistUpdatable {
	
	private String daqAggregatorProducer;

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------

	private int sessionId;

	private String dpsetPath;

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	private Integer runNumber;
	private Long runStart;
	private Long runDurationInMillis;

	/** timestamp */
	private Long lastUpdate;

	private String daqState;

	private String levelZeroState;
	private Long levelZeroStateEntry;
	private String lhcMachineMode;
	private String lhcBeamMode;

	private BUSummary buSummary;
	private FEDBuilderSummary fedBuilderSummary;
	private List<SubSystem> subSystems;
	private List<TTCPartition> ttcPartitions;
	private List<BU> bus;
	private List<RU> rus;
	private List<FEDBuilder> fedBuilders;
	private List<FMMApplication> fmmApplications;
	private List<FMM> fmms;
	private List<FRLPc> frlPcs;
	private List<FRL> frls;
	private List<SubFEDBuilder> subFEDBuilders;
	private Collection<FED> feds;
	
	private TCDSGlobalInfo tcdsGlobalInfo;
	
	/**
	 * HLT rate in Hz
	 */
	private Double hltRate;
	private String hltKey;
	private String hltKeyDescription;
	
	public BUSummary getBuSummary() {
		return buSummary;
	}

	public void setBuSummary(BUSummary buSummary) {
		this.buSummary = buSummary;
	}

	public FEDBuilderSummary getFedBuilderSummary() {
		return fedBuilderSummary;
	}

	public void setFedBuilderSummary(FEDBuilderSummary fedBuilderSummary) {
		this.fedBuilderSummary = fedBuilderSummary;
	}

	public List<SubSystem> getSubSystems() {
		return subSystems;
	}

	public void setSubSystems(List<SubSystem> subSystems) {
		this.subSystems = subSystems;
	}

	public List<TTCPartition> getTtcPartitions() {
		return ttcPartitions;
	}

	public void setTtcPartitions(List<TTCPartition> ttcPartitions) {
		this.ttcPartitions = ttcPartitions;
	}

	public List<BU> getBus() {
		return bus;
	}

	public void setBus(List<BU> bus) {
		this.bus = bus;
	}

	public List<RU> getRus() {
		return rus;
	}

	public void setRus(List<RU> rus) {
		this.rus = rus;
	}

	public List<FEDBuilder> getFedBuilders() {
		return fedBuilders;
	}

	public void setFedBuilders(List<FEDBuilder> fedBuilders) {
		this.fedBuilders = fedBuilders;
	}

	public List<FMMApplication> getFmmApplications() {
		return fmmApplications;
	}

	public void setFmmApplications(List<FMMApplication> fmmApplications) {
		this.fmmApplications = fmmApplications;
	}

	public List<FMM> getFmms() {
		return fmms;
	}

	public void setFmms(List<FMM> fmms) {
		this.fmms = fmms;
	}

	public List<FRLPc> getFrlPcs() {
		return frlPcs;
	}

	public void setFrlPcs(List<FRLPc> frlPcs) {
		this.frlPcs = frlPcs;
	}

	public List<FRL> getFrls() {
		return frls;
	}

	public void setFrls(List<FRL> frls) {
		this.frls = frls;
	}

	public List<SubFEDBuilder> getSubFEDBuilders() {
		return subFEDBuilders;
	}

	public void setSubFEDBuilders(List<SubFEDBuilder> subFEDBuilders) {
		this.subFEDBuilders = subFEDBuilders;
	}

	public Collection<FED> getFeds() {
		return feds;
	}

	public void setFeds(Collection<FED> feds) {
		this.feds = feds;
	}

	public Integer getRunNumber() {
		return runNumber;
	}

	public void setRunNumber(Integer runNumber) {
		this.runNumber = runNumber;
	}

	public Long getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getDaqState() {
		return daqState;
	}

	public void setDaqState(String daqState) {
		this.daqState = daqState;
	}

	public int getSessionId() {
		return sessionId;
	}

	public String getDpsetPath() {
		return dpsetPath;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public void setDpsetPath(String dpsetPath) {
		this.dpsetPath = dpsetPath;
	}

	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {
		if (flashlistType == FlashlistType.LEVEL_ZERO_FM_SUBSYS) {
			this.daqState = flashlistRow.get("STATE").asText();
		} else if (flashlistType == FlashlistType.LEVEL_ZERO_FM_DYNAMIC) {
			this.levelZeroState = flashlistRow.get("STATE").asText();
			this.lhcBeamMode = flashlistRow.get("LHC_BEAM_MODE").asText();
			this.lhcMachineMode = flashlistRow.get("LHC_MACHINE_MODE").asText();
			this.hltKey = flashlistRow.get("HLT_KEY").asText();
			this.hltKeyDescription = flashlistRow.get("HLT_KEY_DESCRIPTION").asText();
			this.runNumber = flashlistRow.get("RUN_NUMBER").asInt();
			
			String runStart = flashlistRow.get("RUN_START_TIME").asText();
			String stateEntry = flashlistRow.get("STATE_ENTRY_TIME").asText();
			
			Date date = DateParser.parseDateTransparently(runStart);
			if (date != null) {
				this.runStart = date.getTime();
				this.runDurationInMillis = (new Date()).getTime() - date.getTime();
			}
			
			date = DateParser.parseDateTransparently(stateEntry);
			if (date != null) {
				this.levelZeroStateEntry = date.getTime();
			}
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
	
	public TCDSGlobalInfo getTcdsGlobalInfo() {
		return tcdsGlobalInfo;
	}

	public void setTcdsGlobalInfo(TCDSGlobalInfo tcdsGlobalInfo) {
		this.tcdsGlobalInfo = tcdsGlobalInfo;
	}

	/** @return the corresponding FED object (there should be at most one)
	 *  corresponding to the given a numeric fed source ID or null if 
	 *  no such source id was found.
	 *  @param fedId the source id of the FED requested
	 */
	public FED getFEDbySrcId(int fedId) {
		
		for (FED fed : getFeds()) {
			
			if (fed.getSrcIdExpected() == fedId) {
				return fed;
			}

		} // loop over FEDs

		// fedId not found
		return null;
		
	}
	
	@Override
	public void clean() {
		this.daqState = "Unknown";
	}


	public Double getHltRate() {
		return hltRate;
	}

	public void setHltRate(Double hltRate) {
		this.hltRate = hltRate;
	}

	public Long getRunStart() {
		return runStart;
	}

	public void setRunStart(Long runStart) {
		this.runStart = runStart;
	}

	public Long getLevelZeroStateEntry() {
		return levelZeroStateEntry;
	}

	public void setLevelZeroStateEntry(Long levelZeroStateEntry) {
		this.levelZeroStateEntry = levelZeroStateEntry;
	}

	public Long getRunDurationInMillis() {
		return runDurationInMillis;
	}

	public void setRunDurationInMillis(Long runDurationInMillis) {
		this.runDurationInMillis = runDurationInMillis;
	}

	public String getHltKey() {
		return hltKey;
	}

	public void setHltKey(String hltKey) {
		this.hltKey = hltKey;
	}

	public String getHltKeyDescription() {
		return hltKeyDescription;
	}

	public void setHltKeyDescription(String hltKeyDescription) {
		this.hltKeyDescription = hltKeyDescription;
	}

	public String getDaqAggregatorProducer() {
		return daqAggregatorProducer;
	}

	public void setDaqAggregatorProducer(String daqAggregatorProducer) {
		this.daqAggregatorProducer = daqAggregatorProducer;
	}

	@Override
	public String toString() {
		return "DAQ [sessionId=" + sessionId + ", dpsetPath=" + dpsetPath + ", runNumber=" + runNumber + ", runStart="
				+ runStart + ", runDurationInMillis=" + runDurationInMillis + ", lastUpdate=" + lastUpdate
				+ ", daqState=" + daqState + ", levelZeroState=" + levelZeroState + ", levelZeroStateEntry="
				+ levelZeroStateEntry + ", lhcMachineMode=" + lhcMachineMode + ", lhcBeamMode=" + lhcBeamMode
				+ ", buSummary=" + buSummary + ", fedBuilderSummary=" + fedBuilderSummary + ", subSystems=" + subSystems
				+ ", ttcPartitions=" + ttcPartitions + ", bus=" + bus + ", rus=" + rus + ", fedBuilders=" + fedBuilders
				+ ", fmmApplications=" + fmmApplications + ", fmms=" + fmms + ", frlPcs=" + frlPcs + ", frls=" + frls
				+ ", subFEDBuilders=" + subFEDBuilders + ", feds=" + feds + ", tcdsGlobalInfo=" + tcdsGlobalInfo
				+ ", hltRate=" + hltRate + ", hltKey=" + hltKey + ", hltKeyDescription=" + hltKeyDescription + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bus == null) ? 0 : bus.hashCode());
		result = prime * result + ((daqState == null) ? 0 : daqState.hashCode());
		result = prime * result + ((dpsetPath == null) ? 0 : dpsetPath.hashCode());
		result = prime * result + ((fedBuilders == null) ? 0 : fedBuilders.hashCode());
		result = prime * result + ((fmmApplications == null) ? 0 : fmmApplications.hashCode());
		result = prime * result + ((frlPcs == null) ? 0 : frlPcs.hashCode());
		result = prime * result + ((lastUpdate == null) ? 0 : lastUpdate.hashCode());
		result = prime * result + ((levelZeroState == null) ? 0 : levelZeroState.hashCode());
		result = prime * result + ((lhcBeamMode == null) ? 0 : lhcBeamMode.hashCode());
		result = prime * result + ((lhcMachineMode == null) ? 0 : lhcMachineMode.hashCode());
		result = prime * result + ((runNumber == null) ? 0 : runNumber.hashCode());
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
		if (lastUpdate == null) {
			if (other.lastUpdate != null)
				return false;
		} else if (!lastUpdate.equals(other.lastUpdate))
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
		if (runNumber == null) {
			if (other.runNumber != null)
				return false;
		} else if (!runNumber.equals(other.runNumber))
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

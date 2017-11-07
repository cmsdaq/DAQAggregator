package rcms.utilities.daqaggregator.data;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;

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

	private int runNumber;
	private long runStart;
	private long runDurationInMillis;

	/** timestamp */
	private long lastUpdate;

	private String daqState;

	private String levelZeroState;
	private long levelZeroStateEntry;
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

	private HltInfo hltInfo;

	/**
	 * HLT stream physics output rate in Hz (events per second)
	 */
	private Double hltRate;
	
	/**
	 * HLT output bandwidth (bytes per second)
	 */
	private Double hltBandwidth;
	
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

	/**
	 * TODO: some columns were added later to the flashlists - accessing them in old flashlists should be fail-safe
	 */
	@Override
	public void updateFromFlashlist(FlashlistType flashlistType, JsonNode flashlistRow) {
		if (flashlistType == FlashlistType.LEVEL_ZERO_FM_SUBSYS) {
			this.daqState = flashlistRow.get("STATE").asText();
		} else if (flashlistType == FlashlistType.LEVEL_ZERO_FM_DYNAMIC) {
			this.levelZeroState = flashlistRow.get("STATE").asText();
			this.lhcBeamMode = flashlistRow.get("LHC_BEAM_MODE").asText();
			this.lhcMachineMode = flashlistRow.get("LHC_MACHINE_MODE").asText();
			try {this.hltKey = flashlistRow.get("HLT_KEY").asText();} catch (NullPointerException e) {}
			try {this.hltKeyDescription = flashlistRow.get("HLT_KEY_DESCRIPTION").asText();;} catch (NullPointerException e) {}
			this.runNumber = flashlistRow.get("RUN_NUMBER").asInt();

			try {
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

			} catch (NullPointerException e) {}
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

	/**
	 * @return the corresponding FED object (there should be at most one)
	 *         corresponding to the given a numeric fed source ID or null if no
	 *         such source id was found.
	 * @param fedId
	 *            the source id of the FED requested
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

	/** @return the RU which is the EVM or null if none is found
	 *  (returns the first one found in case there are multiple EVMs
	 *  but this should never happen)
	 */
	public RU getEVM() {

		for (RU ru : getRus()) {
			if (ru.isEVM())
				return ru;
		} // loop over RUs

		// not found
		return null;
	}

	/** @return a list of RUs matching the given state
	 *
	 *  @param state the requested state (must not be null)
	 */
	public List<RU> getRusInState(String state) {

		List<RU> result = new ArrayList<>();

		if (getRus() != null) {

			for (RU ru : getRus()) {

				if (ru.isMasked())
					continue;

				if (state.equalsIgnoreCase(ru.getStateName())) {
					result.add(ru);
				}

			} // loop over RUs
		}

		return result;

	}

	/** @return a list of BUs matching the given state
	 *
	 *  @param state the requested state (must not be null)
	 */
	public List<BU> getBusInState(String state) {

		List<BU> result = new ArrayList<>();

		if (getBus() != null) {

			for (BU bu : getBus()) {

				if (state.equalsIgnoreCase(bu.getStateName())) {
					result.add(bu);
				}

			} // loop over BUs
		}

		return result;

	}

	@Override
	public void clean() {
		this.daqState = "Unknown";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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

	public Double getHltRate() {
		return hltRate;
	}

	public void setHltRate(Double hltRate) {
		this.hltRate = hltRate;
	}

	/** @return the HLT output bandwidth in bytes per second
	 *  for the Physics stream
	 */
	public Double getHltBandwidth()	{
		return hltBandwidth;
	}

	public void setHltBandwidth(Double hltBandwidth) {
		this.hltBandwidth = hltBandwidth;
	}

	public long getRunStart() {
		return runStart;
	}

	public void setRunStart(long runStart) {
		this.runStart = runStart;
	}

	public long getLevelZeroStateEntry() {
		return levelZeroStateEntry;
	}

	public void setLevelZeroStateEntry(long levelZeroStateEntry) {
		this.levelZeroStateEntry = levelZeroStateEntry;
	}

	public void setRunDurationInMillis(long runDurationInMillis) {
		this.runDurationInMillis = runDurationInMillis;
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

	public HltInfo getHltInfo() {
		return hltInfo;
	}

	public void setHltInfo(HltInfo hltInfo) {
		this.hltInfo = hltInfo;
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

}

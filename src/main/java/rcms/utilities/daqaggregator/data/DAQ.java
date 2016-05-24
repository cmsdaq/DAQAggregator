package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
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
public class DAQ implements java.io.Serializable , FlashlistUpdatable{

	public String toString() {
		return "BUs number in DAQ: " + bus.toString();
	}

	// ----------------------------------------
	// fields set at beginning of session
	// ----------------------------------------
	private List<TTCPartition> ttcPartitions;

	private List<FRLPc> frlPcs;

	private List<BU> bus;

	private List<FMMApplication> fmmApplications;

	private int sessionId;

	private String dpsetPath;

	private FEDBuilderSummary fedBuilderSummary;

	private BUSummary buSummary;

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

	public List<TTCPartition> getTtcPartitions() {
		return ttcPartitions;
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

	public void setTtcPartitions(List<TTCPartition> ttcPartitions) {
		this.ttcPartitions = ttcPartitions;
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
		if (flashlistType == FlashlistType.LEVEL_ZERO_FM_SUBSYS){
			this.daqState = flashlistRow.get("STATE").asText();
		}
		else if (flashlistType == FlashlistType.LEVEL_ZERO_FM_DYNAMIC){
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

}

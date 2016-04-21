package rcms.utilities.daqaggregator.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class DAQ {

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

	// ----------------------------------------
	// fields updated periodically
	// ----------------------------------------

	private int runNumber;

	/** timestamp */
	private long lastUpdate;

	private String daqState;

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

}

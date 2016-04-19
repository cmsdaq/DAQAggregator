package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rcms.utilities.hwcfg.dp.DAQPartition;
import rcms.utilities.hwcfg.dp.DPGenericHost;

public class DAQ {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------
	private final List<TTCPartition> ttcPartitions = new ArrayList<>();
  
	private final List<FRLPc> frlPcs = new ArrayList<>();
  
	private final List<BU> bus = new ArrayList<>();

	private final List<FMMApplication> fmmApplications = new ArrayList<>();

	private final int sessionId;
	
	private final String dpsetPath;

	private final FEDBuilderSummary fedBuilderSummary;
	  
	private final BUSummary buSummary;
	  
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------

	private int runNumber;
    
	/** timestamp */
	private long lastUpdate;
  
	private String daqState;

	//----------------------------------------------------------------------

	public DAQ(DAQPartition dp, String dpsetPath, int sessionId) {
		this.sessionId = sessionId;
		this.dpsetPath = dpsetPath;
		
		this.fedBuilderSummary = new FEDBuilderSummary(this);
		this.buSummary = new BUSummary(this);
	
		// TODO: initialize fields ttcPartitions, frlPcs, fmmApplications
		//       from information in dp
		
		//----------
		// initialize the list of BUs
		//----------
		for (DPGenericHost host : dp.getGenericHosts()) {
        	if ( host.getRole().equals("BU") ) {
        		bus.add(new BU(this, host.getHostName()));
        	}
        }
		
		// sort BUs by name
        Collections.sort(bus, new BU.HostNameComparator());

		//----------

	}

	//----------------------------------------------------------------------

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
		
	//----------------------------------------------------------------------

  
}

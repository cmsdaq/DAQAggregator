package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import rcms.utilities.hwcfg.dp.DAQPartition;

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
	
		// TODO: initialize fields ttcPartitions, frlPcs, bus, fmmApplications
		//       from information in dp
	}
		
	//----------------------------------------------------------------------

  
}

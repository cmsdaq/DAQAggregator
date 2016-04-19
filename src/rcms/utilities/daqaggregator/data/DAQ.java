package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

public class DAQ {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------
	private List<TTCPartition> ttcPartitions = new ArrayList<>();
  
	private List<FRLPc> frlPc = new ArrayList<>();
  
	private List<BU> bus = new ArrayList<>();

	private List<FMMApplication> fmmApplications = new ArrayList<>();

	private int sessionId;
	
	private String dpsetPath;

	private FEDBuilderSummary fedBuilderSummary;
	  
	private BUSummary BUSummary;
	  
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------

	private int runNumber;
    
	/** timestamp */
	private long lastUpdate;
  
	private String daqState;
  
}

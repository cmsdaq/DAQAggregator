package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

public class FMM {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	/** parent TTCPartition */
	private TTCPartition ttcPartition;
  
	private FMMApplication fmmApplication;
  
	private int geoslot;
  
	private String url;
  
	private List<FED> feds = new ArrayList<FED>();
  
}

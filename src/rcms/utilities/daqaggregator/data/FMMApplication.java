package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

public class FMMApplication {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	private DAQ daq;
  
	private String hostname;
  
	private String url;

	private List<FMM> fmms = new ArrayList<FMM>();

	//----------------------------------------
	// fields updated periodically
	//----------------------------------------
	
	private boolean crashed;
    
}

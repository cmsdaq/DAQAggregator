package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

public class FMM {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	/** parent TTCPartition */
	private final TTCPartition ttcPartition;
  
	private final FMMApplication fmmApplication;
  
	private final int geoslot;
  
	private final String url;
  
	private final List<FED> feds = new ArrayList<FED>();

	//----------------------------------------------------------------------

	public FMM(TTCPartition ttcPartition, FMMApplication fmmApplication,
			int geoslot, String url) {
		this.ttcPartition = ttcPartition;
		this.fmmApplication = fmmApplication;
		this.geoslot = geoslot;
		this.url = url;

		// TODO: fill feds
	}

	//----------------------------------------------------------------------

	public TTCPartition getTtcPartition() {
		return ttcPartition;
	}

	public FMMApplication getFmmApplication() {
		return fmmApplication;
	}

	public int getGeoslot() {
		return geoslot;
	}

	public String getUrl() {
		return url;
	}

	public List<FED> getFeds() {
		return feds;
	}
	
	//----------------------------------------------------------------------
	
}

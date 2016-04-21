package rcms.utilities.daqaggregator.data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class FMMApplication {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	private final DAQ daq;
  
	private final String hostname;
  
	private final String url;

	private final List<FMM> fmms = new ArrayList<FMM>();

	//----------------------------------------
	// fields updated periodically
	//----------------------------------------
	
	private boolean crashed;

	//----------------------------------------------------------------------

	public FMMApplication(DAQ daq, String hostname, String url) {
		this.daq = daq;
		this.hostname = hostname;
		this.url = url;

		// TODO: fill fmms
	}

	//----------------------------------------------------------------------

	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(boolean crashed) {
		this.crashed = crashed;
	}

	public DAQ getDaq() {
		return daq;
	}

	public String getHostname() {
		return hostname;
	}

	public String getUrl() {
		return url;
	}

	public List<FMM> getFmms() {
		return fmms;
	}
    
	//----------------------------------------------------------------------

}

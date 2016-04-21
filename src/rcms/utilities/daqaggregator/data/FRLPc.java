package rcms.utilities.daqaggregator.data;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class FRLPc {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	private final String hostname;
  
	/** xdaq application url */
	private final String url; 
  
	private final boolean masked;
	
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------
	private boolean crashed;

	//----------------------------------------------------------------------

	public FRLPc(String hostname, String url, boolean masked) {
		this.hostname = hostname;
		this.url = url;
		this.masked = masked;
	}

	//----------------------------------------------------------------------

	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(boolean crashed) {
		this.crashed = crashed;
	}

	public String getHostname() {
		return hostname;
	}

	public String getUrl() {
		return url;
	}

	public boolean isMasked() {
		return masked;
	}

	//----------------------------------------------------------------------

}

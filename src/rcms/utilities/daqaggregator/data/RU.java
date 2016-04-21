package rcms.utilities.daqaggregator.data;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
public class RU {
	
	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------

	/** the FEDbuilder this RU corresponds to */
	private final FEDBuilder fedBuilder;
  
	private final String hostname;
  
	private final boolean isEVM;
  
	private final boolean masked;
	
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------

	private String errorMsg;
  
	private String warnMsg;
  
	private String infoMsg;
  
	/** events rate in kHz ? */
	private float rate;
  
	/** MByte per second ? */
	private float throughput;
  
	/** mean superfragment size in kByte ? */
	private float superFragmentSizeMean;
  
	/** spread of superfragment size in kByte ? */
	private float superFragmentSizeStddev;
  
	private int fragmentsInRU;
  
	private int eventsInRU;
  
	/** requests from BUs ? */
	private int requests;

	//----------------------------------------------------------------------

	public RU(FEDBuilder fedBuilder, String hostname, boolean isEVM,
			boolean masked) {
		this.fedBuilder = fedBuilder;
		this.hostname = hostname;
		this.isEVM = isEVM;
		this.masked = masked;
	}

	//----------------------------------------------------------------------

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getWarnMsg() {
		return warnMsg;
	}

	public void setWarnMsg(String warnMsg) {
		this.warnMsg = warnMsg;
	}

	public String getInfoMsg() {
		return infoMsg;
	}

	public void setInfoMsg(String infoMsg) {
		this.infoMsg = infoMsg;
	}

	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		this.rate = rate;
	}

	public float getThroughput() {
		return throughput;
	}

	public void setThroughput(float throughput) {
		this.throughput = throughput;
	}

	public float getSuperFragmentSizeMean() {
		return superFragmentSizeMean;
	}

	public void setSuperFragmentSizeMean(float superFragmentSizeMean) {
		this.superFragmentSizeMean = superFragmentSizeMean;
	}

	public float getSuperFragmentSizeStddev() {
		return superFragmentSizeStddev;
	}

	public void setSuperFragmentSizeStddev(float superFragmentSizeStddev) {
		this.superFragmentSizeStddev = superFragmentSizeStddev;
	}

	public int getFragmentsInRU() {
		return fragmentsInRU;
	}

	public void setFragmentsInRU(int fragmentsInRU) {
		this.fragmentsInRU = fragmentsInRU;
	}

	public int getEventsInRU() {
		return eventsInRU;
	}

	public void setEventsInRU(int eventsInRU) {
		this.eventsInRU = eventsInRU;
	}

	public int getRequests() {
		return requests;
	}

	public void setRequests(int requests) {
		this.requests = requests;
	}

	public FEDBuilder getFedBuilder() {
		return fedBuilder;
	}

	public String getHostname() {
		return hostname;
	}

	public boolean isEVM() {
		return isEVM;
	}

	public boolean isMasked() {
		return masked;
	}

	//----------------------------------------------------------------------

}

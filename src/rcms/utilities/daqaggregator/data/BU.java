package rcms.utilities.daqaggregator.data;

public class BU {

	//----------------------------------------
	// fields set at beginning of session
	//----------------------------------------
	
	/** parent */
	private final DAQ daq;
  
	private final String hostname;  
  
	//----------------------------------------
	// fields updated periodically
	//----------------------------------------

	/** event rate in kHz ? */
	private float rate;
  
	/** throughput in MByte/s ? */
	private float throughput;
  
	/** in MByte/s ? */
	private float eventSizeMean;
	private float eventSizeStddev;

	/** is this processed or requested ? */
	private int numEvents;
  
	private int numEventsInBU;
  
	private int priority;
  
	private int numRequestsSent;
  
	private int numRequestsUsed;
  
	private int numRequestsBlocked;
  
	private int numFUsHlt;
  
	private int numFUsCrashed;
  
	private int numFUsStale;
  
	private int numFUsCloud;
  
	/** in percent ? */
	private float ramDiskUsage;
  
	/** total amount of ramdisk */
	private float ramDiskTotal;
  
	/** processed ? to be processed ? on ramdisk ? */
	private int numFiles;
  
	private int numLumisectionsWithFiles;
  
	private int currentLumisection;
  
	private int numLumisectionsForHLT;
  
	private int numLumisectionsOutHLT;

	//----------------------------------------------------------------------
	
	public BU(DAQ daq, String hostname) {
		this.daq = daq;
		this.hostname = hostname;
	}	
	
	//----------------------------------------------------------------------
	
}

package rcms.utilities.daqaggregator.datasource;

/**
 * Definition of flashlist set that will be downloaded and mapped in each
 * iteration of DAQAggregator.
 * 
 * Note that the order is meaningful. Flashlists will be delivered to dispatcher
 * in this order. It means that if more than one flashlist updates the field of
 * the object the last one from this list will override the previous.
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public enum FlashlistType {

	BU("BU", "sessionid"),
	EVM("EVM", "sessionid"),
	FMM_INPUT("FMMInput", "sessionid"),
	FMM_INPUT_DETAIL("FMMInputDetail", "sessionid"),
	FMM_STATUS("FMMStatus", "sessionid"),
	RU("RU", "sessionid"),
	FEROL_CONFIGURATION("ferolConfiguration", "sessionid"),
	FEROL_INPUT_STREAM("ferolInputStream", "sessionid"),
	FEROL_MONITORING("ferolMonitoring", "sessionid"),
	FEROL_STATUS("ferolStatus", "sessionid"),
	FEROL_TCP_STREAM("ferolTcpStream", "sessionid"),
	// FRL_MONITORING("frlMonitoring", "sessionid"),
	HOST_INFO("hostInfo", "sessionid"),

	JOB_CONTROL("jobcontrol"),
	DISK_INFO("diskInfo", "sessionid"),
	// FMM_PARTITION_DEAD_TIME("FMMPartitionDeadTime"),
	// FMM_FED_DEAD_TIME("FMMFEDDeadTime"),

	TCDS_CPM_COUNTS("tcds_cpm_counts"),
	TCDS_CPM_DEADTIMES("tcds_cpm_deadtimes"),
	TCDS_CPM_RATES("tcds_cpm_rates"),
	TCDS_PM_ACTION_COUNTS("tcds_pm_action_counts"),
	TCDS_PM_TTS_CHANNEL("tcds_pm_tts_channel"),
	TCDS_PI_TTS_SUMMARY("tcds_pi_tts_summary"),
	

	LEVEL_ZERO_FM_DYNAMIC("levelZeroFM_dynamic", "SID"),
	LEVEL_ZERO_FM_STATIC("levelZeroFM_static", "SID"),
	LEVEL_ZERO_FM_SUBSYS("levelZeroFM_subsys", "SID"),

	FEROL40_CONFIGURATION("ferol40Configuration"),
	FEROL40_INPUT_STREAM("ferol40InputStream"),
	FEROL40_STATUS("ferol40Status"),
	FEROL40_STREAM_CONFIGURATION("ferol40StreamConfiguration"),
	FEROL40_TCP_STREAM("ferol40TcpStream"),

	TCDSFM("tcdsFM");

	private final String flashlistName;

	/**
	 * Retrieval of flashlist is in session context. Note that not every
	 * flashlist needs session id to retrieve.
	 */
	private final boolean sessionContext;

	/**
	 * Name of the column holding session id data, if flashlist is not subject
	 * to session context than this field is null. Note that this field is
	 * related to flag sessionContext
	 */
	private final String sessionIdColumnName;

	/**
	 * This field is autodiscovered since the configuration of LAS may change
	 * (flashlist may be hosted by differed LAS)
	 */
	private String url;

	/**
	 * Is the flashlist optional. If it's optional DAQAggregator will produce
	 * the snapshots event if flashlist cannot be retrieved
	 */
	private boolean optional;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	private FlashlistType(String name, String sessionIdColumnName) {
		this(name, true, sessionIdColumnName);
	}

	private FlashlistType(String name, boolean sessionContext, String sessionIdColumnName) {
		this.flashlistName = name;
		this.sessionContext = sessionContext;
		this.sessionIdColumnName = sessionIdColumnName;
	}

	/**
	 * 
	 * @param name
	 *            name of the flashlist used for auto discovery
	 */
	private FlashlistType(String name) {
		this(name, false, null);
	}

	public boolean isSessionContext() {
		return sessionContext;
	}

	public String getSessionIdColumnName() {
		return sessionIdColumnName;
	}

	public String getFlashlistName() {
		return flashlistName;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

}

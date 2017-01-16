package rcms.utilities.daqaggregator.datasource;

import org.apache.log4j.Logger;

public enum FlashlistType {

	// fsname, sessionContext?, download?, sessionIdColName
	BU(LiveAccessService.PRIMARY, "BU", "sessionid"),
	EVM(LiveAccessService.PRIMARY, "EVM", "sessionid"),
	FMM_INPUT(LiveAccessService.PRIMARY, "FMMInput", "sessionid"),
	FMM_INPUT_DETAIL(LiveAccessService.PRIMARY, "FMMInputDetail", "sessionid"),
	FMM_STATUS(LiveAccessService.PRIMARY, "FMMStatus", "sessionid"),
	RU(LiveAccessService.PRIMARY, "RU", "sessionid"),
	FEROL_CONFIGURATION(LiveAccessService.PRIMARY, "ferolConfiguration", "sessionid"),
	FEROL_INPUT_STREAM(LiveAccessService.PRIMARY, "ferolInputStream", "sessionid"),
	FEROL_MONITORING(LiveAccessService.PRIMARY, "ferolMonitoring", "sessionid"),
	FEROL_STATUS(LiveAccessService.PRIMARY, "ferolStatus", "sessionid"),
	FEROL_TCP_STREAM(LiveAccessService.PRIMARY, "ferolTcpStream", "sessionid"),
	FRL_MONITORING(LiveAccessService.PRIMARY, "frlMonitoring", "sessionid"),
	HOST_INFO(LiveAccessService.PRIMARY, "hostInfo", "sessionid"),
	LEVEL_ZERO_FM_DYNAMIC(LiveAccessService.PRIMARY, "levelZeroFM_dynamic"),
	LEVEL_ZERO_FM_STATIC(LiveAccessService.PRIMARY, "levelZeroFM_static"),
	LEVEL_ZERO_FM_SUBSYS(LiveAccessService.PRIMARY, "levelZeroFM_subsys"),

	JOB_CONTROL(LiveAccessService.SECONDARY, "jobcontrol"),
	DISK_INFO(LiveAccessService.SECONDARY, "diskInfo", "sessionid"),
	FMM_PARTITION_DEAD_TIME(LiveAccessService.SECONDARY, "FMMPartitionDeadTime"),
	FMM_FED_DEAD_TIME(LiveAccessService.SECONDARY, "FMMFEDDeadTime"),
	
	TCDS_CPM_COUNTS(LiveAccessService.ADDITIONAL, "tcds_cpm_counts"),
	TCDS_CPM_DEADTIMES(LiveAccessService.ADDITIONAL, "tcds_cpm_deadtimes"),
	TCDS_CPM_RATES(LiveAccessService.ADDITIONAL, "tcds_cpm_rates"),
	TCDS_PM_ACTION_COUNTS(LiveAccessService.ADDITIONAL, "tcds_pm_action_counts"),
	TCDS_PM_TTS_CHANNEL(LiveAccessService.ADDITIONAL, "tcds_pm_tts_channel"),
	
	FEROL40_CONFIGURATION(LiveAccessService.PRIMARY, "ferol40Configuration"),
	FEROL40_INPUT_STREAM(LiveAccessService.PRIMARY, "ferol40InputStream"),
	FEROL40_STATUS(LiveAccessService.PRIMARY, "ferol40Status"),
	FEROL40_STREAM_CONFIGURATION(LiveAccessService.PRIMARY, "ferol40StreamConfiguration"),
	FEROL40_TCP_STREAM(LiveAccessService.PRIMARY, "ferol40TcpStream");

	private static Logger logger = Logger.getLogger(FlashlistType.class);

	private final String flashlistName;

	/**
	 * Retrieval of flashlist is in session context. Note that not every
	 * flashlist needs session id to retrieve.
	 */
	private final boolean sessionContext;

	private final String sessionIdColumnName;

	private final LiveAccessService las;

	private FlashlistType(LiveAccessService las, String name, String sessionIdColumnName) {
		this(las, name, true, sessionIdColumnName);
	}

	private FlashlistType(LiveAccessService las, String name, boolean sessionContext, String sessionIdColumnName) {
		this.las = las;
		this.flashlistName = name;
		this.sessionContext = sessionContext;
		this.sessionIdColumnName = sessionIdColumnName;
	}

	private FlashlistType(LiveAccessService las, String name) {
		this(las, name, false, null);
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

	public LiveAccessService getLas() {
		return las;
	}

}

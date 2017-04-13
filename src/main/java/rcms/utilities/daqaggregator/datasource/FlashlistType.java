package rcms.utilities.daqaggregator.datasource;

import org.apache.log4j.Logger;

public enum FlashlistType {

	// fsname, sessionContext?, download?, sessionIdColName
	BU(LiveAccessService.ADDITIONAL4, "BU", "sessionid"),
	EVM(LiveAccessService.ADDITIONAL4, "EVM", "sessionid"),
	FMM_INPUT(LiveAccessService.ADDITIONAL2, "FMMInput", "sessionid"),
	FMM_INPUT_DETAIL(LiveAccessService.ADDITIONAL2, "FMMInputDetail", "sessionid"),
	FMM_STATUS(LiveAccessService.ADDITIONAL2, "FMMStatus", "sessionid"),
	RU(LiveAccessService.ADDITIONAL4, "RU", "sessionid"),
	FEROL_CONFIGURATION(LiveAccessService.SECONDARY, "ferolConfiguration", "sessionid"),
	FEROL_INPUT_STREAM(LiveAccessService.SECONDARY, "ferolInputStream", "sessionid"),
	FEROL_MONITORING(LiveAccessService.SECONDARY, "ferolMonitoring", "sessionid"),
	FEROL_STATUS(LiveAccessService.SECONDARY, "ferolStatus", "sessionid"),
	FEROL_TCP_STREAM(LiveAccessService.SECONDARY, "ferolTcpStream", "sessionid"),
	FRL_MONITORING(LiveAccessService.SECONDARY, "frlMonitoring", "sessionid"),
	HOST_INFO(LiveAccessService.ADDITIONAL3, "hostInfo", "sessionid"),
	LEVEL_ZERO_FM_DYNAMIC(LiveAccessService.PRIMARY, "levelZeroFM_dynamic"),
	LEVEL_ZERO_FM_STATIC(LiveAccessService.PRIMARY, "levelZeroFM_static"),
	LEVEL_ZERO_FM_SUBSYS(LiveAccessService.PRIMARY, "levelZeroFM_subsys"),

	JOB_CONTROL(LiveAccessService.ADDITIONAL3, "jobcontrol"),
	DISK_INFO(LiveAccessService.ADDITIONAL3, "diskInfo", "sessionid"),
	FMM_PARTITION_DEAD_TIME(LiveAccessService.ADDITIONAL, "FMMPartitionDeadTime"),
	FMM_FED_DEAD_TIME(LiveAccessService.ADDITIONAL, "FMMFEDDeadTime"),

	TCDS_CPM_COUNTS(LiveAccessService.ADDITIONAL5, "tcds_cpm_counts"),
	TCDS_CPM_DEADTIMES(LiveAccessService.ADDITIONAL5, "tcds_cpm_deadtimes"),
	TCDS_CPM_RATES(LiveAccessService.ADDITIONAL5, "tcds_cpm_rates"),
	TCDS_PM_ACTION_COUNTS(LiveAccessService.ADDITIONAL5, "tcds_pm_action_counts"),
	TCDS_PM_TTS_CHANNEL(LiveAccessService.ADDITIONAL5, "tcds_pm_tts_channel"),

	FEROL40_CONFIGURATION(LiveAccessService.SECONDARY, "ferol40Configuration"),
	FEROL40_INPUT_STREAM(LiveAccessService.SECONDARY, "ferol40InputStream"),
	FEROL40_STATUS(LiveAccessService.SECONDARY, "ferol40Status"),
	FEROL40_STREAM_CONFIGURATION(LiveAccessService.SECONDARY, "ferol40StreamConfiguration"),
	FEROL40_TCP_STREAM(LiveAccessService.SECONDARY, "ferol40TcpStream"),

	TCDSFM(LiveAccessService.PRIMARY, "tcdsFM");


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

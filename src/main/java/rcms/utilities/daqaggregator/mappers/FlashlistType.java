package rcms.utilities.daqaggregator.mappers;

import org.apache.log4j.Logger;

public enum FlashlistType {

	BU("BU", true, true, "sessionid"),
	EVM("EVM", true, true, "sessionid"),
	FMM_INPUT("FMMInput", true, true, "sessionid"),
	FMM_INPUT_DETAIL("FMMInputDetail", true, false, "sessionid"),
	FMM_STATUS("FMMStatus", true, true, "sessionid"),
	RU("RU", true, true, "sessionid"),
	FEROL_CONFIGURATION("ferolConfiguration", true, true, "sessionid"),
	FEROL_INPUT_STREAM("ferolInputStream", true, true, "sessionid"),
	FEROL_MONITORING("ferolMonitoring", true, false, "sessionid"),
	FEROL_STATUS("ferolStatus", true, true, "sessionid"),
	FEROL_TCP_STREAM("ferolTcpStream", true, false, "sessionid"),
	FRL_MONITORING("frlMonitoring", true, true, "sessionid"),
	HOST_INFO("hostInfo", true, false, "sessionid"),
	LEVEL_ZERO_FM_DYNAMIC("levelZeroFM_dynamic", false, true, "SID"),
	LEVEL_ZERO_FM_STATIC("levelZeroFM_static", true, true, "SID"),
	LEVEL_ZERO_FM_SUBSYS("levelZeroFM_subsys", false, true, "SID"),
	JOB_CONTROL("jobcontrol", false, true, "sessionid"),
	DISK_INFO("diskInfo", true, false, "sessionid"),
	FMM_PARTITION_DEAD_TIME("FMMPartitionDeadTime", true, false, ""),
	FMM_FED_DEAD_TIME("FMMFEDDeadTime", true, false, ""),
	TCDS_PM_TTS_CHANNEL("tcds_pm_tts_channel", false, true, "");
	
	private static Logger logger = Logger.getLogger(FlashlistType.class);

	private final String flashlistName;

	/**
	 * Retrieval of flashlist is in session context. Note that not every
	 * flashlist needs session id to retrieve.
	 */
	private final boolean sessionContext;

	private final boolean download;

	private final String sessionIdColumnName;

	private FlashlistType(String name, boolean sessionContext, boolean download, String sessionIdColumnName) {
		this.flashlistName = name;
		this.sessionContext = sessionContext;
		this.download = download;
		this.sessionIdColumnName = sessionIdColumnName;
	}

	private static String message = " flashlist type infered from name ";

	public static FlashlistType inferTypeByName(String name) {
		if (name.toLowerCase().contains(BU.flashlistName.toLowerCase())) {
			logger.debug(BU.flashlistName + message + name);
			return BU;
		} else if (name.toLowerCase().contains(RU.flashlistName.toLowerCase())) {
			logger.debug(RU.flashlistName + message + name);
			return RU;

		} else if (name.toLowerCase().contains(FMM_INPUT_DETAIL.flashlistName.toLowerCase())) {
			logger.debug(FMM_INPUT_DETAIL.flashlistName + message + name);
			return FMM_INPUT_DETAIL;

		} else if (name.toLowerCase().contains(FMM_INPUT.flashlistName.toLowerCase())) {
			logger.debug(FMM_INPUT.flashlistName + message + name);
			return FMM_INPUT;

		} else if (name.toLowerCase().contains(EVM.flashlistName.toLowerCase())) {
			logger.debug(EVM.flashlistName + message + name);
			return EVM;

		} else if (name.toLowerCase().contains(FMM_STATUS.flashlistName.toLowerCase())) {
			logger.debug(FMM_STATUS.flashlistName + message + name);
			return FMM_STATUS;

		} else if (name.toLowerCase().contains(FEROL_CONFIGURATION.flashlistName.toLowerCase())) {
			logger.debug(FEROL_CONFIGURATION.flashlistName + message + name);
			return FEROL_CONFIGURATION;

		} else if (name.toLowerCase().contains(FEROL_INPUT_STREAM.flashlistName.toLowerCase())) {
			logger.debug(FEROL_INPUT_STREAM.flashlistName + message + name);
			return FEROL_INPUT_STREAM;

		} else if (name.toLowerCase().contains(FEROL_MONITORING.flashlistName.toLowerCase())) {
			logger.debug(FEROL_MONITORING.flashlistName + message + name);
			return FEROL_MONITORING;

		} else if (name.toLowerCase().contains(FEROL_STATUS.flashlistName.toLowerCase())) {
			logger.debug(FEROL_STATUS.flashlistName + message + name);
			return FEROL_STATUS;

		} else if (name.toLowerCase().contains(FEROL_TCP_STREAM.flashlistName.toLowerCase())) {
			logger.debug(FEROL_TCP_STREAM.flashlistName + message + name);
			return FEROL_TCP_STREAM;

		} else if (name.toLowerCase().contains(FRL_MONITORING.flashlistName.toLowerCase())) {
			logger.debug(FRL_MONITORING.flashlistName + message + name);
			return FRL_MONITORING;

		} else if (name.toLowerCase().contains(HOST_INFO.flashlistName.toLowerCase())) {
			logger.debug(HOST_INFO.flashlistName + message + name);
			return HOST_INFO;

		} else if (name.toLowerCase().contains(LEVEL_ZERO_FM_DYNAMIC.flashlistName.toLowerCase())) {
			logger.debug(LEVEL_ZERO_FM_DYNAMIC.flashlistName + message + name);
			return LEVEL_ZERO_FM_DYNAMIC;

		} else if (name.toLowerCase().contains(LEVEL_ZERO_FM_STATIC.flashlistName.toLowerCase())) {
			logger.debug(LEVEL_ZERO_FM_STATIC.flashlistName + message + name);
			return LEVEL_ZERO_FM_STATIC;

		} else if (name.toLowerCase().contains(LEVEL_ZERO_FM_SUBSYS.flashlistName.toLowerCase())) {
			logger.debug(LEVEL_ZERO_FM_SUBSYS.flashlistName + message + name);
			return LEVEL_ZERO_FM_SUBSYS;

		} else if (name.toLowerCase().contains(JOB_CONTROL.flashlistName.toLowerCase())) {
			logger.debug(JOB_CONTROL.flashlistName + message + name);
			return JOB_CONTROL;

		} else if (name.toLowerCase().contains(DISK_INFO.flashlistName.toLowerCase())) {
			logger.debug(DISK_INFO.flashlistName + message + name);
			return DISK_INFO;

		} else if (name.toLowerCase().contains(FMM_PARTITION_DEAD_TIME.flashlistName.toLowerCase())) {
			logger.debug(FMM_PARTITION_DEAD_TIME.flashlistName + message + name);
			return FMM_PARTITION_DEAD_TIME;

		} else if (name.toLowerCase().contains(FMM_FED_DEAD_TIME.flashlistName.toLowerCase())) {
			logger.debug(FMM_FED_DEAD_TIME.flashlistName + message + name);
			return FMM_FED_DEAD_TIME;

		} else if (name.toLowerCase().contains(TCDS_PM_TTS_CHANNEL.flashlistName.toLowerCase())) {
			logger.debug(TCDS_PM_TTS_CHANNEL.flashlistName + message + name);
			return TCDS_PM_TTS_CHANNEL;

		}
		else {

			logger.warn("Cannot infer flashlist type from name " + name);
			return null;
		}

	}

	public boolean isSessionContext() {
		return sessionContext;
	}

	public boolean isDownload() {
		return download;
	}

	public String getSessionIdColumnName(){
		return sessionIdColumnName;
	}

	public String getFlashlistName() {
		return flashlistName;
	}

}

package rcms.utilities.daqaggregator.mappers;

import org.apache.log4j.Logger;

public enum FlashlistType {

	BU("BU"),
	EVM("EVM"),
	FMM_INPUT("FMMInput"),
	FMM_INPUT_DETAIL("FMMInputDetail"),
	FMM_STATUS("FMMStatus"),
	RU("RU"),
	FEROL_CONFIGURATION("ferolConfiguration"),
	FEROL_INPUT_STREAM("ferolInputStream"),
	FEROL_MONITORING("ferolMonitoring"),
	FEROL_STATUS("ferolStatus"),
	FEROL_TCP_STREAM("ferolTcpStream"),
	FRL_MONITORING("frlMonitoring"),
	HOST_INFO("hostInfo"),
	LEVEL_ZERO_FM_DYNAMIC("levelZeroFM_dynamic"),
	LEVEL_ZERO_FM_STATIC("levelZeroFM_static"),
	LEVEL_ZERO_FM_SUBSYS("levelZeroFM_subsys"),
	A(""),
	B(""),
	C(""),
	D("");

	private static Logger logger = Logger.getLogger(FlashlistType.class);

	private final String name;

	private FlashlistType(String name) {
		this.name = name;
	}

	private static String message = " flashlist type infered from name ";

	public static FlashlistType inferTypeByName(String name) {
		if (name.toLowerCase().contains(BU.name.toLowerCase())) {
			logger.debug(BU.name + message + name);
			return BU;
		} else if (name.toLowerCase().contains(RU.name.toLowerCase())) {
			logger.debug(RU.name + message + name);
			return RU;

		} else if (name.toLowerCase().contains(FMM_INPUT_DETAIL.name.toLowerCase())) {
			logger.debug(FMM_INPUT_DETAIL.name + message + name);
			return FMM_INPUT_DETAIL;

		} else if (name.toLowerCase().contains(FMM_INPUT.name.toLowerCase())) {
			logger.debug(FMM_INPUT.name + message + name);
			return FMM_INPUT;

		} else if (name.toLowerCase().contains(EVM.name.toLowerCase())) {
			logger.debug(EVM.name + message + name);
			return EVM;

		} else if (name.toLowerCase().contains(FMM_STATUS.name.toLowerCase())) {
			logger.debug(FMM_STATUS.name + message + name);
			return FMM_STATUS;

		} else if (name.toLowerCase().contains(FEROL_CONFIGURATION.name.toLowerCase())) {
			logger.debug(FEROL_CONFIGURATION.name + message + name);
			return FEROL_CONFIGURATION;

		} else if (name.toLowerCase().contains(FEROL_INPUT_STREAM.name.toLowerCase())) {
			logger.debug(FEROL_INPUT_STREAM.name + message + name);
			return FEROL_INPUT_STREAM;

		} else if (name.toLowerCase().contains(FEROL_MONITORING.name.toLowerCase())) {
			logger.debug(FEROL_MONITORING.name + message + name);
			return FEROL_MONITORING;

		} else if (name.toLowerCase().contains(FEROL_STATUS.name.toLowerCase())) {
			logger.debug(FEROL_STATUS.name + message + name);
			return FEROL_STATUS;

		} else if (name.toLowerCase().contains(FEROL_TCP_STREAM.name.toLowerCase())) {
			logger.debug(FEROL_TCP_STREAM.name + message + name);
			return FEROL_TCP_STREAM;

		} else if (name.toLowerCase().contains(FRL_MONITORING.name.toLowerCase())) {
			logger.debug(FRL_MONITORING.name + message + name);
			return FRL_MONITORING;

		} else if (name.toLowerCase().contains(HOST_INFO.name.toLowerCase())) {
			logger.debug(HOST_INFO.name + message + name);
			return HOST_INFO;

		} else if (name.toLowerCase().contains(LEVEL_ZERO_FM_DYNAMIC.name.toLowerCase())) {
			logger.debug(LEVEL_ZERO_FM_DYNAMIC.name + message + name);
			return LEVEL_ZERO_FM_DYNAMIC;

		} else if (name.toLowerCase().contains(LEVEL_ZERO_FM_STATIC.name.toLowerCase())) {
			logger.debug(LEVEL_ZERO_FM_STATIC.name + message + name);
			return LEVEL_ZERO_FM_STATIC;

		} else if (name.toLowerCase().contains(LEVEL_ZERO_FM_SUBSYS.name.toLowerCase())) {
			logger.debug(LEVEL_ZERO_FM_SUBSYS.name + message + name);
			return LEVEL_ZERO_FM_SUBSYS;

		} else if (name.toLowerCase().contains(A.name.toLowerCase())) {
			logger.debug(A.name + message + name);
			return A;

		} else if (name.toLowerCase().contains(B.name.toLowerCase())) {
			logger.debug(B.name + message + name);
			return B;

		} else if (name.toLowerCase().contains(C.name.toLowerCase())) {
			logger.debug(C.name + message + name);
			return C;

		} else if (name.toLowerCase().contains(D.name.toLowerCase())) {
			logger.debug(D.name + message + name);
			return D;

		} else {

			logger.warn("Cannot infer flashlist type from name " + name);
			return null;
		}

	}

}

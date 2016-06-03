package rcms.utilities.daqaggregator.reasoning.base;

public enum Level {
	LHC("lhc"), Info("info"), Warning("warning"), Error("error"), Run("run"), FLOWCHART("flowchart"), DAQ("daq");

	private final String code;

	private Level(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}

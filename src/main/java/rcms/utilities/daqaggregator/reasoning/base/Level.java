package rcms.utilities.daqaggregator.reasoning.base;

public enum Level {
	Info("info"), Warning("warning"), Error("error"), Run("run");

	private final String code;

	private Level(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}

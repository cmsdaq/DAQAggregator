package rcms.utilities.daqaggregator.reasoning.base;

public enum EventClass {
	
	defaultt("default"), critical("critical");
	
	
	private EventClass(String code){
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}

	private final String code;

}

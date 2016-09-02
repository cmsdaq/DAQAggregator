package rcms.utilities.daqaggregator;

public class DAQAggregatorException extends RuntimeException {

	private final DAQAggregatorExceptionCode code;

	public DAQAggregatorException(DAQAggregatorExceptionCode code, String message) {
		super(message);
		this.code = code;
	}

	public DAQAggregatorExceptionCode getCode() {
		return code;
	}

}

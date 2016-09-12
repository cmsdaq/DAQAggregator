package rcms.utilities.daqaggregator;

public class DAQException extends RuntimeException {

	private final DAQExceptionCode code;

	public DAQException(DAQExceptionCode code, String message) {
		super(message);
		this.code = code;
	}

	public DAQExceptionCode getCode() {
		return code;
	}

}

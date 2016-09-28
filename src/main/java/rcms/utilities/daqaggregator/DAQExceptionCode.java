package rcms.utilities.daqaggregator;

public enum DAQExceptionCode {

	/* General errors */
	SessionCannotBeRetrieved(101, "Cannot retrieve session"),

	/* LAS access errors */

	/* File access errors */
	NoMoreFlashlistSourceFiles(301, "No more flashlist source files"),
	ProblemExploringFiles(302, "Problem exploring files");

	/* Persistence errors */

	DAQExceptionCode(int code, String name) {
		this.code = code;
		this.name = name;
	}

	private final int code;
	private final String name;

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}

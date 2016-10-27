package rcms.utilities.daqaggregator;

public enum DAQExceptionCode {

	/* General errors */
	MissingProperty(102, "Missing property in configuration file"),
	ProblemParsingDate(103, "Problem parsing date from flashlist"),

	/* LAS access errors */
	ProblemRetrievingFlashlists(201, "Problem retrieving flashlist(s)"),
	MissingSessionIdRetrievingFlashlists(202, "Missing session id retrieving flashlists"),

	/* File access errors */
	NoMoreFlashlistSourceFiles(301, "No more flashlist source files"),
	ProblemExploringFiles(302, "Problem exploring files"),

	/* Session retrieval errors */
	ProblemDetectingSession(400, "Problem detecting the session"),
	MissingRowDetectingSession(
			401,
			"Could not find the appropriate row in flashist LEVEL_ZERO_STATIC to determine session"),
	EmptyFlashlistDetectingSession(402, "Could not detect session based on empty flashlist"),
	FlashlistNull(403, "Could not detect session based on null flashlist"),
	WrongFlaslhist(404, "Wrong flashlist type supplied, expected level zero static");

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

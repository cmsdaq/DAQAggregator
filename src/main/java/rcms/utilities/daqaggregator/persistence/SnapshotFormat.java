package rcms.utilities.daqaggregator.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

public enum SnapshotFormat {

	SMILE(".smile", false, new ObjectMapper(new SmileFactory())),
	JSON(".json", true, new ObjectMapper()),
	JSONUGLY(".json", false, new ObjectMapper()),
	JSONREFPREFIXED(".ref.json", true, new ObjectMapper()),
	JSONREFPREFIXEDUGLY(".ref.json",false, new ObjectMapper());

	private final String extension;
	private final boolean prettyPrint;
	private final ObjectMapper mapper;

	private SnapshotFormat(String extension, boolean prettyPrint, ObjectMapper mapper) {
		this.extension = extension;
		this.prettyPrint = prettyPrint;
		this.mapper = mapper;
	}

	public String getExtension() {
		return extension;
	}

	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

}

package rcms.utilities.daqaggregator.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;

public enum PersistenceFormat {

	
	JSON(".json", true, new ObjectMapper()),
	
	ZIPPED(".json.gz", true, new ObjectMapper()),

	@Deprecated
	SMILE(".smile", false, new ObjectMapper(new SmileFactory())),
	
	@Deprecated
	JSONUGLY(".json", false, new ObjectMapper()),

	@Deprecated
	JSONREFPREFIXED(".ref.json", true, new ObjectMapper()),

	@Deprecated
	JSONREFPREFIXEDUGLY(".ref.json",false, new ObjectMapper());

	private final String extension;
	private final boolean prettyPrint;
	private final ObjectMapper mapper;

	private PersistenceFormat(String extension, boolean prettyPrint, ObjectMapper mapper) {
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
	
	public static PersistenceFormat decode(String formatProperty){
		if (PersistenceFormat.JSON.name().equalsIgnoreCase(formatProperty))
			return PersistenceFormat.JSON;
		else if (PersistenceFormat.ZIPPED.name().equalsIgnoreCase(formatProperty))
			return PersistenceFormat.ZIPPED;
		else if (PersistenceFormat.SMILE.name().equalsIgnoreCase(formatProperty))
			return PersistenceFormat.SMILE;
		else if (PersistenceFormat.JSONREFPREFIXED.name().equalsIgnoreCase(formatProperty))
			return PersistenceFormat.JSONREFPREFIXED;
		else if (PersistenceFormat.JSONUGLY.name().equalsIgnoreCase(formatProperty))
			return PersistenceFormat.JSONUGLY;
		else if (PersistenceFormat.JSONREFPREFIXEDUGLY.name().equalsIgnoreCase(formatProperty))
			return PersistenceFormat.JSONREFPREFIXEDUGLY;
		else {
			return PersistenceFormat.SMILE;
		}
	}

}

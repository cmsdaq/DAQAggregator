package rcms.utilities.daqaggregator.datasource;

public enum LiveAccessService {

	PRIMARY, SECONDARY, ADDITIONAL;

	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}

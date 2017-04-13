package rcms.utilities.daqaggregator.datasource;

public enum LiveAccessService {

	PRIMARY, SECONDARY, ADDITIONAL, ADDITIONAL2, ADDITIONAL3, ADDITIONAL4, ADDITIONAL5;

	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}

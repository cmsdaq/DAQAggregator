package rcms.utilities.daqaggregator.mappers.helper;

public interface HostnameGeoslotFinder<E> {
	public String getHostname(E e);

	public Integer getGeoslot(E e);
	
	public String getFlashlistHostnameKey();
	
	public String getFlashlistGeoslotKey();
}

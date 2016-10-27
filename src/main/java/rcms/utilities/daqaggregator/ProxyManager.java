package rcms.utilities.daqaggregator;

import java.util.Properties;

import org.apache.log4j.Logger;

public class ProxyManager {

	private static final Logger logger = Logger.getLogger(ProxyManager.class);

	private static ProxyManager instance;

	private ProxyManager() {

	}

	public static ProxyManager get() {
		if (instance == null)
			instance = new ProxyManager();

		return instance;
	}

	public void startProxy() {
		if (Application.get().getProp().containsKey(Settings.PROXY_ENABLE.getKey())
				&& Application.get().getProp().getProperty(Settings.PROXY_ENABLE.getKey()).toString()
						.toLowerCase().equals("true")) {
			logger.debug("Setting up SOCKS proxy ...");

			Properties sysProperties = System.getProperties();

			// Specify proxy settings
			sysProperties.put("socksProxyHost", Application.get().getProp(Settings.PROXY_HOST));
			sysProperties.put("socksProxyPort", Application.get().getProp(Settings.PROXY_PORT));

			sysProperties.put("proxySet", "true");
		}
	}

}

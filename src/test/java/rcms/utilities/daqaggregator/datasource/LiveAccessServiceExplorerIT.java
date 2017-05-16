package rcms.utilities.daqaggregator.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.Test;

public class LiveAccessServiceExplorerIT {

	@Test
	public void test() {
		Properties sysProperties = System.getProperties();

		// Specify proxy settings
		sysProperties.put("socksProxyHost", "localhost");
		sysProperties.put("socksProxyPort", "1080");

		sysProperties.put("proxySet", "true");
		List<String> a = new ArrayList<>();
		a.add("http://ucsrv-c2e41-10-01.cms:9946/urn:xdaq-application:service=xmaslas2g");
		a.add("http://ucsrv-c2e41-09-01.cms:9945/urn:xdaq-application:service=xmaslas2g");
		a.add("http://ucsrv-c2e41-08-01.cms:9944/urn:xdaq-application:service=xmaslas2g");
		a.add("http://ucsrv-c2e41-07-01.cms:9943/urn:xdaq-application:service=xmaslas2g");
		a.add("http://ucsrv-c2e41-14-01.cms:9942/urn:xdaq-application:service=xmaslas2g");
		a.add("http://ucsrv-c2e41-13-01.cms:9941/urn:xdaq-application:service=xmaslas2g");
		a.add("http://kvm-s3562-1-ip151-95.cms:9945/urn:xdaq-application:service=xmaslas2g");

		LiveAccessServiceExplorer explorer = new LiveAccessServiceExplorer(a);
		explorer.exploreLiveAccessServices();

		System.out.println("Result of LAS exploring: ");
		for (Entry<String, String> result : explorer.getFlashlistToUrl().entrySet()) {
			System.out.println(result.getValue() + " : " + result.getKey());
		}

		for (FlashlistType flashlistType : FlashlistType.values()) {
			String exploredUrl = explorer.getFlashlistUrl(flashlistType.getFlashlistName());
			System.out.println(String.format("%1$-26s", flashlistType.getFlashlistName()) + " " + exploredUrl);

		}
	}

}

package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.ProxyManager;

/**
 * This test will not work when there is data available in LAS flashlist
 * 
 * @TODO: make it independent on flashlist availability
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class LASFlashlistRetrieverTest {

	private static String filter1;
	private static String filter2;

	private static LASFlashlistRetriever retriever;
	private static Integer sessionId;

	@BeforeClass
	public static void prepare() {
		Application.initialize("DAQAggregator.properties");

		ProxyManager.get().startProxy();
		String mainUrl = Application.get().getProp().get(Application.PROPERTYNAME_SESSION_LASURL_GE).toString();
		filter1 = Application.get().getProp().get(Application.PROPERTYNAME_SESSION_L0FILTER1).toString();
		filter2 = Application.get().getProp().get(Application.PROPERTYNAME_SESSION_L0FILTER2).toString();
		String[] urls = Application.get().getProp().get(Application.PROPERTYNAME_MONITOR_URLS).toString().split(" +");
		List<String> urlList = Arrays.asList(urls);

		retriever = new LASFlashlistRetriever(mainUrl, urlList);

		Flashlist flashlist = retriever.retrieveFlashlist(FlashlistType.LEVEL_ZERO_FM_STATIC);

		SessionRetriever sessionRetriever = new SessionRetriever(filter1, filter2);
		Triple<String, Integer, Long> result = sessionRetriever.retrieveSession(flashlist);

		sessionId = result.getMiddle();

	}

	@Test
	public void retrieveSingleFlashlistTest() throws IOException {

		Flashlist flashlist = retriever.retrieveFlashlist(FlashlistType.LEVEL_ZERO_FM_STATIC);
		Assert.assertNotNull(flashlist);
	}

	@Test
	public void retrieveMultipleFlashlistsTest() throws IOException {

		retriever.retrieveAvailableFlashlists(sessionId);
		Assert.assertEquals(21, retriever.retrieveAllFlashlists().size());
	}

}

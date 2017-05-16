package rcms.utilities.daqaggregator.datasource;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import rcms.utilities.daqaggregator.Application;
import rcms.utilities.daqaggregator.ProxyManager;
import rcms.utilities.daqaggregator.Settings;

/**
 * This test will not work when there is data available in LAS flashlist
 * 
 * @TODO: make it independent on flashlist availability
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
@Ignore
public class LASFlashlistRetrieverTest {

	private static String filter1;
	private static String filter2;

	private static LASFlashlistRetriever retriever;
	private static Integer sessionId;

	@BeforeClass
	public static void prepare() {
		Application.initialize("DAQAggregator.properties");

		ProxyManager.get().startProxy();
		filter1 = Application.get().getProp(Settings.SESSION_L0FILTER1).toString();
		filter2 = Application.get().getProp(Settings.SESSION_L0FILTER2).toString();

		retriever = new LASFlashlistRetriever(false);

		Pair<Flashlist, String> flashlistDownloadResult = retriever
				.retrieveFlashlist(FlashlistType.LEVEL_ZERO_FM_STATIC);

		SessionRetriever sessionRetriever = new SessionRetriever(filter1, filter2);
		Triple<String, Integer, Long> result = sessionRetriever.retrieveSession(flashlistDownloadResult.getLeft());

		sessionId = result.getMiddle();
		Assert.assertNotNull(flashlistDownloadResult);
		Assert.assertNotNull(flashlistDownloadResult.getLeft());

	}

	@Test
	public void retrieveMultipleFlashlistsTest() throws IOException {

		Map<FlashlistType, Flashlist> flashlistRetrieveResult = retriever.retrieveAllFlashlists(sessionId);
		Assert.assertEquals(25, flashlistRetrieveResult.size());
	}

}

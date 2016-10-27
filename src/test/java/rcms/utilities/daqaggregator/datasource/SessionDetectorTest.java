package rcms.utilities.daqaggregator.datasource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SessionDetectorTest {

	private static final String filter1 = "filter1";
	private static final String filter2 = "filter2";

	@Test
	public void sessionDoesntChangeTest() {
		SessionRetriever sessionRetriever = new SessionRetriever(filter1, filter2);

		List<Flashlist> flashists = new ArrayList<>();
		flashists.add(createFlashlist("abc1", 1));
		flashists.add(createFlashlist("abc1", 1));
		final Iterator<Flashlist> iterator = flashists.iterator();

		FlashlistRetriever flashlistRetriever = new FlashlistRetrieverStub(iterator);
		SessionDetector sessionDetector = new SessionDetector(sessionRetriever, flashlistRetriever);

		Assert.assertTrue(sessionDetector.detectNewSession());
		Assert.assertFalse(sessionDetector.detectNewSession());
	}

	@Test
	public void sessionChangeTest() {
		SessionRetriever sessionRetriever = new SessionRetriever(filter1, filter2);

		List<Flashlist> flashists = new ArrayList<>();
		flashists.add(createFlashlist("abc1", 1));
		flashists.add(createFlashlist("abc1", 2));
		final Iterator<Flashlist> iterator = flashists.iterator();

		FlashlistRetriever flashlistRetriever = new FlashlistRetrieverStub(iterator);
		SessionDetector sessionDetector = new SessionDetector(sessionRetriever, flashlistRetriever);

		Assert.assertTrue(sessionDetector.detectNewSession());
		Assert.assertTrue(sessionDetector.detectNewSession());
	}

	@Test
	public void hwcfgChangeTest() {
		SessionRetriever sessionRetriever = new SessionRetriever(filter1, filter2);

		List<Flashlist> flashists = new ArrayList<>();
		flashists.add(createFlashlist("abc1", 1));
		flashists.add(createFlashlist("abc2", 1));
		final Iterator<Flashlist> iterator = flashists.iterator();

		FlashlistRetriever flashlistRetriever = new FlashlistRetrieverStub(iterator);
		SessionDetector sessionDetector = new SessionDetector(sessionRetriever, flashlistRetriever);

		Assert.assertTrue(sessionDetector.detectNewSession());
		Assert.assertTrue(sessionDetector.detectNewSession());
	}

	public static Flashlist createFlashlist(String hwcfg, int sid) {
		Flashlist flashlist = new Flashlist(FlashlistType.LEVEL_ZERO_FM_STATIC);
		ArrayNode rowsNode = JsonNodeFactory.instance.arrayNode();
		ObjectNode row = JsonNodeFactory.instance.objectNode();
		row.set(SessionRetriever.FMURL_COLUMN_NAME, JsonNodeFactory.instance.textNode(filter1 + filter2));
		row.set(SessionRetriever.TIMESTAMP_COLUMN_NAME,
				JsonNodeFactory.instance.textNode("Wed, Aug 31 2016 00:00:00 GMT"));
		row.set(SessionRetriever.HWKEY_COLUMN_NAME, JsonNodeFactory.instance.textNode(hwcfg));
		row.set(SessionRetriever.SID_COLUMN_NAME, JsonNodeFactory.instance.numberNode(sid));
		rowsNode.add(row);
		flashlist.setRowsNode(rowsNode);
		return flashlist;
	}

	class FlashlistRetrieverStub implements FlashlistRetriever {

		public FlashlistRetrieverStub(Iterator<Flashlist> iterator) {
			this.iterator = iterator;
		}

		private final Iterator<Flashlist> iterator;

		@Override
		public Pair<Flashlist, Integer> retrieveFlashlist(FlashlistType flashlistType) {
			return Pair.of(iterator.next(), 0);
		}

		@Override
		public Map<FlashlistType, Flashlist> retrieveAllFlashlists(int sessionId) {
			return null;
		}

	}
}

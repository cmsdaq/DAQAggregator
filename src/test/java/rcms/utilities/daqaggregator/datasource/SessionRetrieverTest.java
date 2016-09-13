package rcms.utilities.daqaggregator.datasource;

import static org.hamcrest.CoreMatchers.is;

import java.util.Date;

import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Session retriever test class
 * 
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public class SessionRetrieverTest {

	private static final String fmURLFilter1 = "testfilter1";
	private static final String fmURLFilter2 = "testfilter2";

	SessionRetriever sr = new SessionRetriever(fmURLFilter1, fmURLFilter2);

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void noFlashlistTest() {
		thrown.expect(RuntimeException.class);
		thrown.expectMessage(is(SessionRetriever.EXCEPTION_FLASHLIST_NULL_MESSAGE));
		Flashlist flashlist = null;
		sr.retrieveSession(flashlist);
	}

	@Test
	public void wrongFlashlistTest() {
		thrown.expect(RuntimeException.class);
		thrown.expectMessage(is(SessionRetriever.EXCEPTION_WRONG_FLASHLIST_MESSAGE));
		Flashlist flashlist = new Flashlist(FlashlistType.BU);
		sr.retrieveSession(flashlist);
	}

	@Test
	public void emptyFlashlistTest() {
		thrown.expect(RuntimeException.class);
		thrown.expectMessage(is(SessionRetriever.EXCEPTION_NO_DATA_MESSAGE + " @timestamp " + new Date(1L)));
		Flashlist flashlist = new Flashlist(FlashlistType.LEVEL_ZERO_FM_STATIC);
		flashlist.retrievalDate = new Date(1L);
		sr.retrieveSession(flashlist);
	}

	@Test
	public void missingRowSatisfingFiltersTest() {
		thrown.expect(RuntimeException.class);
		thrown.expectMessage(is(SessionRetriever.EXCEPTION_MISSING_ROW_MESSAGE));
		Flashlist flashlist = new Flashlist(FlashlistType.LEVEL_ZERO_FM_STATIC);
		ArrayNode rowsNode = JsonNodeFactory.instance.arrayNode();
		ObjectNode row = JsonNodeFactory.instance.objectNode();
		row.set(SessionRetriever.FMURL_COLUMN_NAME, JsonNodeFactory.instance.textNode(""));
		row.set(SessionRetriever.TIMESTAMP_COLUMN_NAME, JsonNodeFactory.instance.textNode(""));
		row.set(SessionRetriever.HWKEY_COLUMN_NAME, JsonNodeFactory.instance.textNode(""));
		row.set(SessionRetriever.SID_COLUMN_NAME, JsonNodeFactory.instance.textNode(""));
		rowsNode.add(row);
		flashlist.setRowsNode(rowsNode);
		sr.retrieveSession(flashlist);
	}

	@Test
	public void problemParsingTimestampTest() {
		thrown.expect(RuntimeException.class);
		thrown.expectMessage(is(SessionRetriever.EXCEPTION_PARSING_DATE_PROBLEM_MESSAGE));
		Flashlist flashlist = new Flashlist(FlashlistType.LEVEL_ZERO_FM_STATIC);
		ArrayNode rowsNode = JsonNodeFactory.instance.arrayNode();
		ObjectNode row = JsonNodeFactory.instance.objectNode();
		row.set(SessionRetriever.FMURL_COLUMN_NAME, JsonNodeFactory.instance.textNode(fmURLFilter1 + fmURLFilter2));
		row.set(SessionRetriever.TIMESTAMP_COLUMN_NAME, JsonNodeFactory.instance.textNode(""));
		row.set(SessionRetriever.HWKEY_COLUMN_NAME, JsonNodeFactory.instance.textNode(""));
		row.set(SessionRetriever.SID_COLUMN_NAME, JsonNodeFactory.instance.textNode(""));
		rowsNode.add(row);
		flashlist.setRowsNode(rowsNode);
		sr.retrieveSession(flashlist);
	}

	@Test
	public void simpleTest() {
		Flashlist flashlist = new Flashlist(FlashlistType.LEVEL_ZERO_FM_STATIC);
		ArrayNode rowsNode = JsonNodeFactory.instance.arrayNode();
		ObjectNode row = JsonNodeFactory.instance.objectNode();
		row.set(SessionRetriever.FMURL_COLUMN_NAME, JsonNodeFactory.instance.textNode(fmURLFilter1 + fmURLFilter2));
		// EEE, MMM dd yyyy HH:mm:ss Z
		row.set(SessionRetriever.TIMESTAMP_COLUMN_NAME,
				JsonNodeFactory.instance.textNode("Wed, Aug 31 2016 00:00:00 GMT"));
		row.set(SessionRetriever.HWKEY_COLUMN_NAME, JsonNodeFactory.instance.textNode("abc"));
		row.set(SessionRetriever.SID_COLUMN_NAME, JsonNodeFactory.instance.textNode("2"));
		rowsNode.add(row);
		flashlist.setRowsNode(rowsNode);
		Triple<String, Integer, Long> result = sr.retrieveSession(flashlist);

		long expectedTimestamp = 1472601600000L;
		int expectedSid = 2;
		String expectedHWCfg = "abc";

		Assert.assertEquals(expectedTimestamp, (long) result.getRight());
		Assert.assertEquals(expectedSid, (int) result.getMiddle());
		Assert.assertEquals(expectedHWCfg, result.getLeft());
	}
}

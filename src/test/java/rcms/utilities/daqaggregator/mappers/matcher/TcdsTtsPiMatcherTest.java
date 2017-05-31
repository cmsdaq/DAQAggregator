package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.datasource.Flashlist;
import rcms.utilities.daqaggregator.datasource.FlashlistType;

public class TcdsTtsPiMatcherTest {

	final JsonNodeFactory factory = JsonNodeFactory.instance;

	@Test
	public void testNonSessionContextFlaslhist() {

		TcdsTtsPiMatcher sut = new TcdsTtsPiMatcher();

		Flashlist testFlashlist = new Flashlist(FlashlistType.TCDS_PI_TTS_SUMMARY);
		ArrayNode rowsNode = factory.arrayNode();
		Collection<FED> objects = new ArrayList<>();

		appendTestDataToFlashlist(1, rowsNode, "http://a.cms:1000", "service1", 1);
		objects.add(generateTestFed(1, "a.cms", 1000, "service1", 1));

		testFlashlist.setRowsNode(rowsNode);
		Map<FED, JsonNode> a = sut.match(testFlashlist, objects);
		Assert.assertEquals(1, a.size());

		Assert.assertEquals(1, a.values().iterator().next().get("id").asInt());
		Assert.assertEquals(1, a.keySet().iterator().next().getSrcIdExpected());

	}

	private FED generateTestFed(int id, String hostname, int port, String service, int io) {
		FED fed = new FED();
		FMM fmm = new FMM();
		FMMApplication fmma = new FMMApplication();

		fed.setSrcIdExpected(id);
		fmma.setHostname(hostname);
		fmma.setPort(port);
		fmm.setServiceName(service);
		fed.setFmmIO(io);

		fed.setFmm(fmm);
		fmm.getFeds().add(fed);
		fmm.setFmmApplication(fmma);

		return fed;
	}

	private void appendTestDataToFlashlist(int id, ArrayNode rows, String context, String service, int i) {
		ObjectNode node = factory.objectNode();
		node.put("id", id);
		node.put("context", context);
		node.put("service", service);
		node.put("tts_slot1", 1 * i);
		node.put("tts_slot2", 2 * i);
		node.put("tts_slot3", 3 * i);
		node.put("tts_slot4", 4 * i);
		node.put("tts_slot5", 5 * i);
		node.put("tts_slot6", 6 * i);
		node.put("tts_slot7", 7 * i);
		node.put("tts_slot8", 8 * i);
		node.put("tts_slot9", 9 * i);
		node.put("tts_slot10", 10 * i);
		rows.add(node);
	}

}

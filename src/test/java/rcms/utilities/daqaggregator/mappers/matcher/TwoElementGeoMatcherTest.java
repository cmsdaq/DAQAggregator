package rcms.utilities.daqaggregator.mappers.matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import rcms.utilities.daqaggregator.datasource.Flashlist;
import rcms.utilities.daqaggregator.datasource.FlashlistType;

public class TwoElementGeoMatcherTest {
	final JsonNodeFactory factory = JsonNodeFactory.instance;

	@Test
	public void testNonSessionContextFlaslhist() {

		TwoElementGeoMatcher<Pair<String, Integer>> sut = new TwoElementGeoMatcherStub(1234);

		Flashlist testFlashlist = new Flashlist(FlashlistType.JOB_CONTROL);
		ArrayNode rowsNode = factory.arrayNode();
		appendTestDataToFlashlist(rowsNode, 1, "a.cms");
		appendTestDataToFlashlist(rowsNode, 2, "b.cms");

		testFlashlist.setRowsNode(rowsNode);

		Collection<Pair<String, Integer>> objects = new ArrayList<>();
		objects.add(Pair.of("a.cms", 1));
		objects.add(Pair.of("c.cms", 3));

		Map<Pair<String, Integer>, JsonNode> a = sut.match(testFlashlist, objects);

		Assert.assertEquals(1, a.size());

		Assert.assertEquals(1, a.values().iterator().next().get("number").asInt());
		Assert.assertEquals("a.cms", a.values().iterator().next().get("text").asText());
		Assert.assertEquals(1, a.keySet().iterator().next().getRight().intValue());
		Assert.assertEquals("a.cms", a.keySet().iterator().next().getLeft());

	}

	@Test
	public void testSessionContextFlaslhist() {

		TwoElementGeoMatcher<Pair<String, Integer>> sut = new TwoElementGeoMatcherStub(1234);

		Flashlist testFlashlist = new Flashlist(FlashlistType.BU);
		ArrayNode rowsNode = factory.arrayNode();
		appendTestDataWithSessionToFlashlist(rowsNode, 1, "a.cms", 1234);
		appendTestDataWithSessionToFlashlist(rowsNode, 2, "b.cms", 1234);

		appendTestDataWithSessionToFlashlist(rowsNode, 1, "a.cms", 123456);
		appendTestDataWithSessionToFlashlist(rowsNode, 2, "b.cms", 123456);

		testFlashlist.setRowsNode(rowsNode);

		Collection<Pair<String, Integer>> objects = new ArrayList<>();
		objects.add(Pair.of("a.cms", 1));
		objects.add(Pair.of("c.cms", 3));

		Map<Pair<String, Integer>, JsonNode> a = sut.match(testFlashlist, objects);

		Assert.assertEquals(1, a.size());

		Assert.assertEquals(1, a.values().iterator().next().get("number").asInt());
		Assert.assertEquals("a.cms", a.values().iterator().next().get("text").asText());
		Assert.assertEquals(1, a.keySet().iterator().next().getRight().intValue());
		Assert.assertEquals("a.cms", a.keySet().iterator().next().getLeft());

	}

	private void appendTestDataToFlashlist(ArrayNode rows, int number, String text) {
		ObjectNode node = factory.objectNode();
		node.put("number", number);
		node.put("text", text);
		rows.add(node);
	}

	private void appendTestDataWithSessionToFlashlist(ArrayNode rows, int number, String text, int sessionid) {
		ObjectNode node = factory.objectNode();
		node.put("number", number);
		node.put("text", text);
		node.put("sessionid", sessionid);
		rows.add(node);
	}

}

class TwoElementGeoMatcherStub extends TwoElementGeoMatcher<Pair<String, Integer>> {

	public TwoElementGeoMatcherStub(int sessionId) {
		super(sessionId);
	}

	@Override
	public String getHostname(Pair<String, Integer> e) {
		return e.getLeft();
	}

	@Override
	public Integer getGeoslot(Pair<String, Integer> e) {
		return e.getRight();
	}

	@Override
	public String getFlashlistHostnameKey() {
		return "text";
	}

	@Override
	public String getFlashlistGeoslotKey() {
		return "number";
	}

}

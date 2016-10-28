package rcms.utilities.daqaggregator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import rcms.utilities.daqaggregator.datasource.Flashlist;
import rcms.utilities.daqaggregator.datasource.FlashlistType;
import rcms.utilities.daqaggregator.persistence.PersistenceFormat;
import rcms.utilities.daqaggregator.persistence.PersistorManager;

public class FlashlistPersistenceTest {

	@Test
	public void test() throws JsonGenerationException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		FlashlistStub flashlist = new FlashlistStub(FlashlistType.BU);
		flashlist.setRetrievalDate(new Date());
		ArrayNode rowsNode = JsonNodeFactory.instance.arrayNode();
		rowsNode.add(JsonNodeFactory.instance.textNode("a"));
		flashlist.setRowsNode(rowsNode);

		PersistorManager persistorManager = new PersistorManager("", "", null, PersistenceFormat.JSON);
		String path = persistorManager.persistFlashlist(flashlist, "/tmp/flashlist-test/");

		System.out.println("Persisted here: " + path);
		System.out.println(flashlist);

		Flashlist flashlistDeserialized = null;

		ObjectInputStream in = null;
		FileInputStream fileIn = null;
		flashlistDeserialized = mapper.readValue(new File(path), Flashlist.class);

		System.out.println(flashlistDeserialized);

		assertEquals(flashlist.getRowsNode(), flashlistDeserialized.getRowsNode());
		assertEquals(flashlist.getFlashlistType(), flashlistDeserialized.getFlashlistType());
		assertEquals(flashlist.getRetrievalDate(), flashlistDeserialized.getRetrievalDate());

	}

}

class FlashlistStub extends Flashlist {

	public FlashlistStub(FlashlistType type) {
		super(type);
	}

	public void setRetrievalDate(Date retrievalDate) {
		this.retrievalDate = retrievalDate;
	}

}
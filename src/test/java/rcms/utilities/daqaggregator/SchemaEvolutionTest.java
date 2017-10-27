package rcms.utilities.daqaggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.galan.verjson.core.*;
import de.galan.verjson.step.ProcessStepException;
import de.galan.verjson.step.transformation.Transformation;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static de.galan.verjson.util.Transformations.obj;

public class SchemaEvolutionTest {


    private static String oldObjectFileName = "src/test/resources/schema-evolution/object-old.json";
    private static String newObjectFileName = "src/test/resources/schema-evolution/object-new.json";
    private static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void deserializeOldObjectTest() {
        TestObject object = deserializeFromFile(oldObjectFileName);
        testObject(object);
    }

    @Test
    public void deserializeNewObjectTest() {
        TestObject object = deserializeFromFile(newObjectFileName);
        testObject(object);
    }

    private void testObject(TestObject object){
        Assert.assertNotNull(object);
        Assert.assertEquals(1, object.getWrapper().getNumber());
        Assert.assertEquals("value", object.getWrapper().getText());
        Assert.assertEquals("other value", object.getOther());
    }

    private TestObject deserializeFromFile(String filename) {

        try {
            Verjson<TestObject> verjson = Verjson.create(TestObject.class, new ExampleBeanVersions());
            JsonNode jsonNode = mapper.readTree(new File(filename));
            TestObject object = verjson.read(jsonNode);
            return object;

        } catch (IOException | ProcessStepException | VersionNotSupportedException | IOReadException | NamespaceMismatchException e) {
            e.printStackTrace();
            Assert.fail("Exception during deserialization occurred");
            return null;
        }
    }

}

/**
 * Transformation from old object to new
 */
class Transformation1 extends Transformation {
    @Override
    protected void transform(JsonNode node) {

        System.out.println("Old object found, transformation1 will be applied: " + node);

        ObjectNode wrapperObject = JsonNodeFactory.instance.objectNode();
        wrapperObject.set("text", node.get("text"));
        wrapperObject.set("number", node.get("number"));

        obj(node).put("wrapper", wrapperObject);
        obj(node).remove("text");
        obj(node).remove("number");

        System.out.println("Transformation1 complete, final object: " + node);
    }
}

class ExampleBeanVersions extends Versions {

    @Override
    public void configure() {

        /* If you find file with version 1 apply this transformation */
        add(1L, new Transformation1());
    }

}


class TestObject {
    private Wrapper wrapper;
    private String other;

    public TestObject(Wrapper wrapper, String other) {
        this.wrapper = wrapper;
        this.other = other;
    }

    public TestObject() {
    }

    public Wrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}

class Wrapper {
    private String text;
    private int number;

    public Wrapper(String text, int number) {
        this.text = text;
        this.number = number;
    }

    public Wrapper() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}


package rcms.utilities.daqaggregator.mappers.helper;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContextHelperTest {

	@BeforeClass
	public static void setup() {
		ContextHelper.setNetworkSuffix(".tld");
	}
	
	@Test
	public void hostnameSimpleTest() {
		String actual = ContextHelper.getHostnameFromContext("abc.tld:123");
		Assert.assertEquals("abc.tld", actual);
	}

	@Test
	public void hostnameProtocolHttpTest() {
		String actual = ContextHelper.getHostnameFromContext("http://abc.tld:123");
		Assert.assertEquals("abc.tld", actual);
	}

	@Test
	public void hostnameProtocolHttpsTest() {
		String actual = ContextHelper.getHostnameFromContext("https://abc.tld:123");
		Assert.assertEquals("abc.tld", actual);
	}

	@Test
	public void portTest() {
		int actual = ContextHelper.getPortFromContext("https://abc.tld:123");
		Assert.assertEquals(123, actual);
	}
}

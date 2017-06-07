package rcms.utilities.daqaggregator.mappers.helper;

import org.junit.Assert;
import org.junit.Test;

public class ContextHelperTest {

	@Test
	public void hostnameSimpleTest() {
		String actual = ContextHelper.getHostnameFromContext("abc.cms:123");
		Assert.assertEquals("abc.cms", actual);
	}

	@Test
	public void hostnameProtocolHttpTest() {
		String actual = ContextHelper.getHostnameFromContext("http://abc.cms:123");
		Assert.assertEquals("abc.cms", actual);
	}

	@Test
	public void hostnameProtocolHttpsTest() {
		String actual = ContextHelper.getHostnameFromContext("https://abc.cms:123");
		Assert.assertEquals("abc.cms", actual);
	}

	@Test
	public void portTest() {
		int actual = ContextHelper.getPortFromContext("https://abc.cms:123");
		Assert.assertEquals(123, actual);
	}
}

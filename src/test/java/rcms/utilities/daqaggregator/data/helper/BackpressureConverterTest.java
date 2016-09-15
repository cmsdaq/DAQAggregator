package rcms.utilities.daqaggregator.data.helper;

import org.junit.Assert;
import org.junit.Test;

public class BackpressureConverterTest {

	@Test
	public void simpleTest() {

		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(0, backpressureConverter.calculate(0, 1000), 0.0001);
		Assert.assertEquals(10, backpressureConverter.calculate(10, 2000), 0.0001);
		Assert.assertEquals(5, backpressureConverter.calculate(15, 3000), 0.0001);
		Assert.assertEquals(0, backpressureConverter.calculate(15, 4000), 0.0001);
		Assert.assertEquals(15, backpressureConverter.calculate(30, 5000), 0.0001);
		Assert.assertEquals(2, backpressureConverter.calculate(40, 10000), 0.0001);
		Assert.assertEquals(2, backpressureConverter.calculate(44, 12000), 0.0001);
	}
	
	@Test
	public void restartTest() {

		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(0, backpressureConverter.calculate(0, 1000), 0.0001);
		Assert.assertEquals(10, backpressureConverter.calculate(10, 2000), 0.0001);
		Assert.assertEquals(5, backpressureConverter.calculate(15, 3000), 0.0001);
		Assert.assertEquals(0, backpressureConverter.calculate(0, 4000), 0.0001);
		Assert.assertEquals(10, backpressureConverter.calculate(10, 5000), 0.0001);
	}

	@Test
	public void repeatTest() {

		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(0, backpressureConverter.calculate(0, 1000), 0.0001);
		Assert.assertEquals(10, backpressureConverter.calculate(10, 2000), 0.0001);
		Assert.assertEquals(10, backpressureConverter.calculate(15, 2000), 0.0001);
		Assert.assertEquals(10, backpressureConverter.calculate(15, 2000), 0.0001);
		Assert.assertEquals(20, backpressureConverter.calculate(30, 3000), 0.0001);
	}

	@Test
	public void formatSimpleTest() {
		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(0, backpressureConverter.calculate(0, "Tue, Sep 13 2016 15:35:22 GMT"), 0.0001);
		Assert.assertEquals(10, backpressureConverter.calculate(10, "Tue, Sep 13 2016 15:35:23 GMT"), 0.0001);
		Assert.assertEquals(5, backpressureConverter.calculate(15, "Tue, Sep 13 2016 15:35:24 GMT"), 0.0001);
		Assert.assertEquals(0, backpressureConverter.calculate(15, "Tue, Sep 13 2016 15:35:25 GMT"), 0.0001);
		Assert.assertEquals(15, backpressureConverter.calculate(30, "Tue, Sep 13 2016 15:35:26 GMT"), 0.0001);
	}

	@Test
	public void formatProblemTest() {
		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(0, backpressureConverter.calculate(0, "Tue, Sep 13 2016 15:35:22 GMT"), 0.0001);
		Assert.assertEquals(10, backpressureConverter.calculate(10, "Tue, Sep 13 2016 15:35:23 GMT"), 0.0001);
		Assert.assertEquals(10, backpressureConverter.calculate(15, "X"), 0.0001);
		Assert.assertEquals(2.5, backpressureConverter.calculate(15, "Tue, Sep 13 2016 15:35:25 GMT"), 0.0001);
	}

}

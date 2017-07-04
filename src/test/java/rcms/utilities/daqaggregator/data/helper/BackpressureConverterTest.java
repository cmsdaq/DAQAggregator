package rcms.utilities.daqaggregator.data.helper;

import org.junit.Assert;
import org.junit.Test;

public class BackpressureConverterTest {

	private static final double delta = 0.0001;

	@Test
	public void fullBackpressureTest() {

		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(1, backpressureConverter.calculate(1, 1), delta);
		Assert.assertEquals(1, backpressureConverter.calculate(2, 2), delta);
		Assert.assertEquals(1, backpressureConverter.calculate(3, 3), delta);
		Assert.assertEquals(1, backpressureConverter.calculate(4, 4), delta);
		Assert.assertEquals(1, backpressureConverter.calculate(5, 5), delta);
		Assert.assertEquals(1, backpressureConverter.calculate(10, 10), delta);
		Assert.assertEquals(1, backpressureConverter.calculate(12, 12), delta);
	}

	@Test
	public void halfBackpressureTest() {

		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(0.5, backpressureConverter.calculate(0.5, 1), delta);
		Assert.assertEquals(0.5, backpressureConverter.calculate(1, 2), delta);
		Assert.assertEquals(0.5, backpressureConverter.calculate(1.5, 3), delta);
		Assert.assertEquals(0.5, backpressureConverter.calculate(2, 4), delta);
		Assert.assertEquals(0.5, backpressureConverter.calculate(2.5, 5), delta);
		Assert.assertEquals(0.5, backpressureConverter.calculate(5, 10), delta);
		Assert.assertEquals(0.5, backpressureConverter.calculate(6, 12), delta);
	}

	@Test
	public void simpleTest() {

		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(0, backpressureConverter.calculate(0, 1), delta);
		Assert.assertEquals(1, backpressureConverter.calculate(1, 2), delta);
		Assert.assertEquals(.5, backpressureConverter.calculate(1.5, 3), delta);
		Assert.assertEquals(0, backpressureConverter.calculate(1.5, 4), delta);
		Assert.assertEquals("More than 1", 1.5, backpressureConverter.calculate(3, 5), delta);
		Assert.assertEquals(.2, backpressureConverter.calculate(4, 10), delta);
		Assert.assertEquals(.2, backpressureConverter.calculate(4.4, 12), delta);
	}

	@Test
	public void restartTest() {

		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(0, backpressureConverter.calculate(0, 1.000001d), delta);
		Assert.assertEquals(1, backpressureConverter.calculate(1, 2), delta);
		Assert.assertEquals(.5, backpressureConverter.calculate(1.5, 3), delta);
		Assert.assertEquals(0, backpressureConverter.calculate(0, 4), delta);
		Assert.assertEquals(1, backpressureConverter.calculate(1, 5), delta);

	}

	@Test
	public void repeatTest() {

		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(0, backpressureConverter.calculate(0, 1), delta);
		Assert.assertEquals(1, backpressureConverter.calculate(1, 2), delta);
		Assert.assertEquals("abnormal, value ignored, last correct result repeated", 1,
				backpressureConverter.calculate(1.5, 2), delta);
		Assert.assertEquals("abnormal, value ignored, last correct result repeated", 1,
				backpressureConverter.calculate(1.5, 2), delta);
		Assert.assertEquals("return to normal, last not ignored value used", .75,
				backpressureConverter.calculate(1.75, 3), delta);

	}

	@Test
	public void percentageValueTest() {

		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(0, backpressureConverter.calculatePercent(0, 1, false), delta);
		Assert.assertEquals(100, backpressureConverter.calculatePercent(1, 2, false), delta);
		Assert.assertEquals(50, backpressureConverter.calculatePercent(1.5, 3, false), delta);
		Assert.assertEquals(0, backpressureConverter.calculatePercent(1.5, 4, false), delta);
		Assert.assertEquals("More than 100%", 150, backpressureConverter.calculatePercent(3, 5, false), delta);
		Assert.assertEquals(20, backpressureConverter.calculatePercent(4, 10, false), delta);
		Assert.assertEquals(20, backpressureConverter.calculatePercent(4.4, 12, false), delta);
	}

	@Test
	public void roundingValueTest() {
		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(0, backpressureConverter.calculatePercent(0, 1.0017d, true), delta);
		Assert.assertEquals("not round", 100.1702, backpressureConverter.calculatePercent(1, 2, false), delta);
		Assert.assertEquals("round to 1 points", 100.2, backpressureConverter.calculatePercent(1, 2, true), delta);
	}

	/**
	 * Assumption: the accumulatedBackpressureSeconds resets when latchedSeconds
	 * restets
	 */
	@Test
	public void timeCounterResetTest() {

		BackpressureConverter backpressureConverter = new BackpressureConverter();
		Assert.assertEquals(.1, backpressureConverter.calculate(.1, 1), delta);
		Assert.assertEquals(.3, backpressureConverter.calculate(.4, 2), delta);
		Assert.assertEquals(.5, backpressureConverter.calculate(.9, 3), delta);

		Assert.assertEquals("Should reset", .1, backpressureConverter.calculate(.1, 1), delta);
		Assert.assertEquals(.3, backpressureConverter.calculate(.4, 2), delta);
		Assert.assertEquals(.5, backpressureConverter.calculate(.9, 3), delta);
	}

}

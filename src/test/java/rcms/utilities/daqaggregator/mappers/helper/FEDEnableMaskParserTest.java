package rcms.utilities.daqaggregator.mappers.helper;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

public class FEDEnableMaskParserTest {

	@Test
	public void fullTest() {

		FEDEnableMaskParser femp = new FEDEnableMaskParser();

		/* FRL, FMM */
		Assert.assertEquals(Pair.of(true, true), femp.parseValue(0));
		Assert.assertEquals(Pair.of(false, true), femp.parseValue(1));
		Assert.assertEquals(Pair.of(true, false), femp.parseValue(2));
		Assert.assertEquals(Pair.of(false, false), femp.parseValue(3));

		Assert.assertEquals(Pair.of(true, true), femp.parseValue(4));
		Assert.assertEquals(Pair.of(false, true), femp.parseValue(5));
		Assert.assertEquals(Pair.of(true, false), femp.parseValue(6));
		Assert.assertEquals(Pair.of(false, false), femp.parseValue(7));

		Assert.assertEquals(Pair.of(true, true), femp.parseValue(8));
		Assert.assertEquals(Pair.of(false, true), femp.parseValue(9));
		Assert.assertEquals(Pair.of(true, false), femp.parseValue(10));
		Assert.assertEquals(Pair.of(false, false), femp.parseValue(11));

		Assert.assertEquals(Pair.of(true, true), femp.parseValue(12));
		Assert.assertEquals(Pair.of(false, true), femp.parseValue(13));
		Assert.assertEquals(Pair.of(true, false), femp.parseValue(14));
		Assert.assertEquals(Pair.of(false, false), femp.parseValue(15));
	}

	@Test
	public void fmmDecodingTest() {

		FEDEnableMaskParser femp = new FEDEnableMaskParser();

		Assert.assertEquals(true, femp.decodeFMMMasked(0, 0));
		Assert.assertEquals(false, femp.decodeFMMMasked(0, 1));
		Assert.assertEquals(true, femp.decodeFMMMasked(1, 0));
		Assert.assertEquals(false, femp.decodeFMMMasked(1, 1));
	}

	@Test
	public void frlDecodingTest() {

		FEDEnableMaskParser femp = new FEDEnableMaskParser();

		Assert.assertEquals(true, femp.decodeFrlMasked(0, 0));
		Assert.assertEquals(false, femp.decodeFrlMasked(0, 1));
		Assert.assertEquals(true, femp.decodeFrlMasked(1, 0));
		Assert.assertEquals(false, femp.decodeFrlMasked(1, 1));
	}

}

package rcms.utilities.daqaggregator.mappers.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

/**
 * This class helps parsing the FED_ENABLE_MASK session-aware string, which
 * contains masking information for FEDs
 * 
 * @author Michail Vougioukas (michail.vougioukas@cern.ch)
 *
 */

public class FEDEnableMaskParser {

	private static final String fedEnableMaskFedListSeparator = "%";
	private static final String fedEnableMaskFedKeyValueSeparator = "&";
	private static final Logger logger = Logger.getLogger(FEDEnableMaskParser.class);

	/**
	 * Key is the srcExpectedId of a FED Value is a pair that contains two
	 * booleans, the first of which is the flag for the frlmasked and the second
	 * is the flag for the fmmmasked
	 * 
	 * @return
	 */
	public static Map<Integer, Pair<Boolean, Boolean>> parseMask(String listToDecode) {
		Map<Integer, Pair<Boolean, Boolean>> targetMap = new HashMap<>();
		String[] pairs = listToDecode.split(fedEnableMaskFedListSeparator);
		logger.info("FED enable mask has been parsed and contains " + pairs.length + " entries");
		int problematicEntries = 0;
		for (String pair : pairs) {
			if (pair.contains(fedEnableMaskFedKeyValueSeparator)) {
				String[] fedIdAndMaskedFlag = pair.split(fedEnableMaskFedKeyValueSeparator);
				String fedIdString = "", fedValueString = "";
				try {
					fedIdString = fedIdAndMaskedFlag[0];
					fedValueString = fedIdAndMaskedFlag[1];
					int fedId = Integer.parseInt(fedIdString);
					int maskSum = Integer.parseInt(fedValueString);

					Pair<Boolean, Boolean> resolvedMasks = parseValue(maskSum);

					targetMap.put(fedId, resolvedMasks);
					logger.debug("FED of id: " + fedId + " has value: " + fedValueString);
				} catch (NullPointerException e) {
					problematicEntries++;
					logger.debug("Problem parsing FED enable mask, could not parse the pair: " + pair);
				} catch (NumberFormatException e) {
					problematicEntries++;
					logger.warn("Problem parsing FED enable mask, could not parse the FED id or mask sum from integer: "
							+ fedIdString + " and mask sum: " + fedValueString);
				}
			}
		}
		if (problematicEntries > 0) {
			logger.info("Parsing the FED enable masked (" + pairs.length + " elements in table, parsed to "
					+ targetMap.size() + " map) finished with " + problematicEntries + " problems");
			logger.info(targetMap);
		}

		return targetMap;
	}

	public static Pair<Boolean, Boolean> parseValue(Integer maskSum) {
		int bit1, bit2, bit3, bit0;

		// maskSum in binary format (stringified)
		String binaryMaskSum = Integer.toString(maskSum, 2);

		// String padding
		if (binaryMaskSum.length() == 3) {
			binaryMaskSum = "0" + binaryMaskSum;
		} else if (binaryMaskSum.length() == 2) {
			binaryMaskSum = "00" + binaryMaskSum;
		} else if (binaryMaskSum.length() == 1) {
			binaryMaskSum = "000" + binaryMaskSum;
		}

		// Bit 3 (TTS)
		bit3 = Integer.parseInt(Character.toString(binaryMaskSum.charAt(0)));

		// Bit 2 (SLINK)
		bit2 = Integer.parseInt(Character.toString(binaryMaskSum.charAt(1)));

		// Bit 1 (TTS)
		bit1 = Integer.parseInt(Character.toString(binaryMaskSum.charAt(2)));

		// Bit 0 (SLINK)
		bit0 = Integer.parseInt(Character.toString(binaryMaskSum.charAt(3)));

		Boolean frlMask = decodeFrlMasked(bit2, bit0);
		Boolean fmmMask = decodeFMMMasked(bit3, bit1);

		Pair<Boolean, Boolean> result = Pair.of(frlMask, fmmMask);
		logger.debug("Converting sum " + maskSum + " to " + binaryMaskSum + " (that should be equal to " + bit3 + bit2
				+ bit1 + bit0 + ") to result " + result);
		return result;
	}

	protected static boolean decodeFrlMasked(int bit2, int bit0) {
		boolean frlMask = true;
		/*
		 * FED has and SLINK and the SLINK is inactive (taken out from the
		 * FED/TTS page)
		 */
		if (bit2 == 0 && bit0 == 0) {
			frlMask = true;
		}

		/* FED has an SLINK and the SLINK is active */
		else if (bit2 == 0 && bit0 == 1) {
			frlMask = false;
		}
		/*
		 * FED has an SLINK but the SLINK is permanently masked (code currently
		 * not in use)
		 */
		else if (bit2 == 1 && bit0 == 0) {
			frlMask = true;
		}

		/* FED has no SLINK - mask is not applicable */
		else if (bit2 == 1 && bit0 == 1) {
			frlMask = false;
		}
		return frlMask;
	}

	protected static boolean decodeFMMMasked(int bit3, int bit1) {
		boolean fmmMasked = true;
		/*
		 * FED has a TTS output and the TTS output is inactive (taken out from
		 * the FED/TTS page)
		 */
		if (bit3 == 0 && bit1 == 0) {
			fmmMasked = true;
		}

		/* FED has a TTS output and the TTS output is active */
		else if (bit3 == 0 && bit1 == 1) {
			fmmMasked = false;
		}
		/*
		 * FED has a TTS output but the TTS output is permanently masked (code
		 * currently not in use)
		 */
		else if (bit3 == 1 && bit1 == 0) {
			fmmMasked = true;
		}

		/* FED has no TTS output -mask is not applicable */
		else if (bit3 == 1 && bit1 == 1) {
			fmmMasked = false;
		}
		return fmmMasked;
	}

}

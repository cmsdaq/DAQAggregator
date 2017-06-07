package rcms.utilities.daqaggregator.data.helper;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FRLType;
import rcms.utilities.daqaggregator.mappers.helper.TCDSFlashlistHelpers;

public class FEDHelper {

	private static final Logger logger = Logger.getLogger(FEDHelper.class);

	private static final String SLOT1_KEY = "tts_slot1";
	private static final String SLOT2_KEY = "tts_slot2";
	private static final String SLOT3_KEY = "tts_slot3";
	private static final String SLOT4_KEY = "tts_slot4";
	private static final String SLOT5_KEY = "tts_slot5";
	private static final String SLOT6_KEY = "tts_slot6";
	private static final String SLOT7_KEY = "tts_slot7";
	private static final String SLOT8_KEY = "tts_slot8";
	private static final String SLOT9_KEY = "tts_slot9";
	private static final String SLOT10_KEY = "tts_slot10";

	public static String getTTSState(int slot, JsonNode row) {

		Integer value = null;

		if (slot == 1) {
			value = row.get(SLOT1_KEY).asInt();
		} else if (slot == 2) {
			value = row.get(SLOT2_KEY).asInt();
		} else if (slot == 3) {
			value = row.get(SLOT3_KEY).asInt();
		} else if (slot == 4) {
			value = row.get(SLOT4_KEY).asInt();
		} else if (slot == 5) {
			value = row.get(SLOT5_KEY).asInt();
		} else if (slot == 6) {
			value = row.get(SLOT6_KEY).asInt();
		} else if (slot == 7) {
			value = row.get(SLOT7_KEY).asInt();
		} else if (slot == 8) {
			value = row.get(SLOT8_KEY).asInt();
		} else if (slot == 9) {
			value = row.get(SLOT9_KEY).asInt();
		} else if (slot == 10) {
			value = row.get(SLOT10_KEY).asInt();
		}
		String result = TCDSFlashlistHelpers.decodeTCDSTTSState(value);
		logger.debug(
				"State of FED in slot " + slot + " according to flashlist is : " + value + " which maps to " + result);

		return result;
	}

	public static Pair<Integer, FMM> getFMM(FED findable) {

		FMM fmm = findable.getFmm();
		Integer fmmIO = findable.getFmmIO();

		/*
		 * if (fmm == null) { if (findable.getDependentFeds().size() > 0) {
		 * logger.debug("Could not find FMM in FED " +
		 * findable.getSrcIdExpected() +
		 * " but it has following dependent feds: " +
		 * findable.getDependentFeds());
		 * 
		 * if (findable.getDependentFeds().size() != 1) { logger.warn(
		 * "more than one dependent fed: " +
		 * findable.getDependentFeds().size()); } for (FED dep :
		 * findable.getDependentFeds()) { if (dep.getFmm() != null) { fmm =
		 * dep.getFmm(); fmmIO = dep.getFmmIO(); logger.debug( "fmmIO in fed: "
		 * + findable.getFmmIO() + ", while in dependent: " + dep.getFmmIO()); }
		 * } } }
		 */

		return Pair.of(fmmIO, fmm);
	}

	public static String getFEDType(FED fed) {
		String printtype = "";
		if (fed.getFrl() == null) {
			printtype = "NO-FRL";
		} else if (fed.getFrl().getType() == null) {
			printtype = "EMPTY_FRL_TYPE";
		} else if (fed.getFrl().getType() == FRLType.FEROL40_10G || fed.getFrl().getType() == FRLType.FEROL40_10G) {
			printtype = "FEROL40";
		} else if (fed.getFrl().getType() == FRLType.SLINK || fed.getFrl().getType() == FRLType.SLINKEXPRESS10G
				|| fed.getFrl().getType() == FRLType.SLINKEXPRESS6G) {
			printtype = "SLINK";
		} else if (fed.getFrl().getType() == FRLType.NOLINK) {
			printtype = "NOLINK";
		} else {
			printtype = "[?]";
		}

		return printtype;

	}

}

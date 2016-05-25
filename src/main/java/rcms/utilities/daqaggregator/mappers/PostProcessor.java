package rcms.utilities.daqaggregator.mappers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqaggregator.data.TTCPartition;

public class PostProcessor {

	private final DAQ daq;

	private static final Logger logger = Logger.getLogger(PostProcessor.class);

	public PostProcessor(DAQ daq) {
		super();
		this.daq = daq;
	}

	public void postProcess() {
		calculateDerivedValuesForSubFeds();
		calculateDerivedValuesForTTCPs();
		calculateDerivedValuesForRUs();
		calculateDerivedValuesForFRLPcs();

		daq.getBuSummary().calculateDerivedValues();
		daq.getFedBuilderSummary().calculateDerivedValues();

		summarizeFeds();
		summarizeCrashed();
		summarizeDAQ();

		MappingReporter.get().summarize();
		MappingReporter.get().detailedSummarize();
	}

	private void summarizeDAQ() {
		String daqStatus = daq.getDaqState();
		String lhcBeamMode = daq.getLhcBeamMode();
		String lhcMachineMode = daq.getLhcMachineMode();
		String levelZeroState = daq.getLevelZeroState();

		Map<String, Integer> states = new HashMap<>();

		for (SubSystem sub : daq.getSubSystems()) {
			if (!states.containsKey(sub.getStatus())) {
				states.put(sub.getStatus(), 0);
			}
			states.put(sub.getStatus(), states.get(sub.getStatus()) + 1);
		}

		logger.info("DAQ state: " + daqStatus + ", LHC beam mode: " + lhcBeamMode + ", LHC machine mode: "
				+ lhcMachineMode + ", L0 state: " + levelZeroState + ", Subsystems states: " + states);

	}

	private void summarizeCrashed() {
		int frlPcsCrashed = 0;
		for (FRLPc frlPc : daq.getFrlPcs()) {
			if (frlPc.isCrashed())
				frlPcsCrashed++;
		}

		int fmmappCrashed = 0;
		for (FMMApplication fmma : daq.getFmmApplications()) {
			if (fmma.isCrashed())
				fmmappCrashed++;
		}

		logger.info("Crashed: FRLPc " + frlPcsCrashed + "/" + daq.getFrlPcs().size() + ", FMMApplications "
				+ fmmappCrashed + "/" + daq.getFmmApplications().size());
	}

	private void summarizeFeds() {
		int feds = 0;
		int fedsFmmMasked = 0;
		int fedsFrlMasked = 0;
		for (FMMApplication fmma : daq.getFmmApplications()) {
			for (FMM fmm : fmma.getFmms()) {
				for (FED fed : fmm.getFeds()) {
					feds++;
					if (fed.isFmmMasked())
						fedsFmmMasked++;
					if (fed.isFrlMasked())
						fedsFrlMasked++;
				}
			}
		}
		logger.info("FED raport: [" + fedsFmmMasked + "|" + fedsFrlMasked + "]/" + feds
				+ ", [fmm masked|frl masked]/all FEDS");
	}

	private void calculateDerivedValuesForSubFeds() {
		Set<SubFEDBuilder> sfbs = new HashSet<SubFEDBuilder>();

		for (FEDBuilder fb : daq.getFedBuilders()) {
			for (SubFEDBuilder sfb : fb.getSubFedbuilders()) {
				sfbs.add(sfb);
			}
		}

		// calculate min/max triger per SubFEDBuilder
		for (SubFEDBuilder subFedBuilder : sfbs) {
			subFedBuilder.calculateDerived();
		}

	}

	private void calculateDerivedValuesForRUs() {
		int masked = 0;
		int all = 0;
		int enabledRus = 0;
		int evms = 0;
		for (FEDBuilder fedBuilder : daq.getFedBuilders()) {
			RU ru = fedBuilder.getRu();
			if (!ru.isEVM()) {
				ru.calculateDerivedValues();
				all++;
				if (ru.isMasked()) {
					masked++;
				}
				if (ru.getStateName().equalsIgnoreCase("enabled"))
					enabledRus++;
				if (ru.isEVM())
					evms++;
			} else {
				evms++;
			}
		}

		logger.info(
				"RU raport: [" + enabledRus + "|" + masked + "|" + evms + "]/" + all + " [enabled|masked|evms]/all");

	}

	private void calculateDerivedValuesForTTCPs() {

		int masked = 0;
		int withoutFMM = 0;
		int all = 0;

		for (SubSystem subsystem : daq.getSubSystems()) {
			for (TTCPartition ttcp : subsystem.getTtcPartitions()) {
				all++;
				if (ttcp.getFmm() != null) {
					ttcp.calculateDerivedValues();
					if (ttcp.isMasked())
						masked++;
				} else {
					withoutFMM++;
				}
			}
		}

		logger.info("TTCP derived values report: [" + masked + "|" + withoutFMM + "]/" + all
				+ " [masked|missing FMM]/all TTCPs");

	}

	private void calculateDerivedValuesForFRLPcs() {
		int masked = 0;
		int all = 0;
		for (FRLPc frlPc : daq.getFrlPcs()) {
			all++;
			frlPc.calculateDerivedValues();
			if (frlPc.isMasked()) {
				masked++;
			}

		}

		logger.info("FRLPc raport: " + masked + "/" + all + " masked/all");

	}

}

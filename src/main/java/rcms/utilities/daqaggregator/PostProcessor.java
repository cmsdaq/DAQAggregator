package rcms.utilities.daqaggregator;

import java.util.HashSet;
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

		daq.getBuSummary().calculateDerivedValues();
		daq.getFedBuilderSummary().calculateDerivedValues();

		summarizeFeds();
		summarizeCrashed();
		summarizeRus();
		summarizeDAQ();
	}

	private void summarizeDAQ() {
		String daqStatus = daq.getDaqState();
		String lhcBeamMode = daq.getLhcBeamMode();
		String lhcMachineMode = daq.getLhcMachineMode();
		String levelZeroState = daq.getLevelZeroState();

		logger.info("DAQ state: " + daqStatus + ", LHC beam mode: " + lhcBeamMode + ", LHC machine mode: "
				+ lhcMachineMode + ", L0 state: " + levelZeroState);

	}

	private void summarizeRus() {
		int enabledRus = 0, rus = 0, evms = 0;

		for (FEDBuilder fedBuilder : daq.getFedBuilders()) {
			RU ru = fedBuilder.getRu();
			rus++;
			if (ru.getStatus().equalsIgnoreCase("enabled"))
				enabledRus++;
			if (ru.isEVM())
				evms++;

		}
		logger.info("RU raport: [" + enabledRus + "|" + evms + "]/" + rus + " [enabled|evms]/all");

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

	private void calculateDerivedValuesForTTCPs() {

		int masked = 0;
		int withoutFMM = 0;

		for (TTCPartition ttcp : daq.getTtcPartitions()) {
			if (ttcp.getFmm() != null) {
				ttcp.calculateDerivedValues();
				if (ttcp.isMasked())
					masked++;
			} else {
				withoutFMM++;
			}
		}

		logger.info("TTCP derived values report: [" + masked + "|" + withoutFMM + "]/" + daq.getTtcPartitions().size()
				+ " [masked|missing FMM]/all TTCPs");

	}

	private void summarizeFeds() {
		int feds = 0;
		int fedsMasked = 0;
		for (FMMApplication fmma : daq.getFmmApplications()) {
			for (FMM fmm : fmma.getFmms()) {
				for (FED fed : fmm.getFeds()) {
					feds++;
					if (fed.isFmmMasked())
						fedsMasked++;
				}
			}
		}
		logger.info("FED in structure: " + fedsMasked + "/" + feds + " masked");
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

}

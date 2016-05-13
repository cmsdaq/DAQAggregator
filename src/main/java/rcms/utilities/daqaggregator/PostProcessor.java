package rcms.utilities.daqaggregator;

import java.util.HashSet;
import java.util.Set;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;

public class PostProcessor {

	private final DAQ daq;

	public PostProcessor(DAQ daq) {
		super();
		this.daq = daq;
	}

	public void postProcess() {
		calculateDerivedValues();
		daq.getBuSummary().summarize();
		daq.getFedBuilderSummary().summarize();
	}

	private void calculateDerivedValues() {
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

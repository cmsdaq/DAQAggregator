package rcms.utilities.daqaggregator.vis.tree;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;

public class FlowVisualizer extends TreeVisualizer {

	public FlowVisualizer(DAQ daq) {
		super(daq);
	}

	@Override
	public SizedElement visualize() {
		// create DAQ
		SizedElement root = new SizedElement("DAQ");

		// create FMMApplication
		for (FEDBuilder fedBuilder : daq.getFedBuilders()) {
			// RU ru =
			SizedElement fedBuilderElement = new SizedElement("FB: " + fedBuilder.getName());
			root.getChildren().add(fedBuilderElement);

			for (SubFEDBuilder subFedBuilder : fedBuilder.getSubFedbuilders()) {
				SizedElement subFedBuilderElement = new SizedElement(
						"SFB: " + subFedBuilder.getTtcPartition().getName());
				fedBuilderElement.getChildren().add(subFedBuilderElement);
				for (FRL frl : subFedBuilder.getFrls()) {
					SizedElement frlElement = new SizedElement("FRL: " + frl.getGeoSlot());
					subFedBuilderElement.getChildren().add(frlElement);
					for (FED fed : frl.getFeds().values()) {
						SizedElement fedElement = new SizedElement("FED: " + fed.getId());
						frlElement.getChildren().add(fedElement);
					}

				}
			}

		}

		return root;
	}

	@Override
	public String getName() {
		return "flow.json";
	}

}

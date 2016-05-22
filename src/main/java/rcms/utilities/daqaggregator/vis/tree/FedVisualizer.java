package rcms.utilities.daqaggregator.vis.tree;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;

public class FedVisualizer extends TreeVisualizer {

	public FedVisualizer(DAQ daq) {
		super(daq);
	}

	@Override
	public Element visualize() {
		// create DAQ
		Element root = new Element("DAQ");

		// create FMMApplication
		for (FMMApplication fmmApplication : daq.getFmmApplications()) {
			Element fmmApplicationElement = new Element("FMMApp " + fmmApplication.getHostname());
			root.getChildren().add(fmmApplicationElement);

			// create FMM
			for (FMM fmm : fmmApplication.getFmms()) {
				Element fmmElement = new Element("FMM " + fmm.getGeoslot());
				fmmApplicationElement.getChildren().add(fmmElement);

				// create FED
				for (FED fed : fmm.getFeds()) {
					Element fedElement = new Element("FED:" + fed.getId());
					fmmElement.getChildren().add(fedElement);

				}
			}

		}

		return root;
	}

	@Override
	public String getName() {
		return "tree-feds.json";
	}

}

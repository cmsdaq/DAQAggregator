package rcms.utilities.daqaggregator.vis.tree;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FED;
import rcms.utilities.daqaggregator.data.FMM;
import rcms.utilities.daqaggregator.data.FMMApplication;

public class ControlVisualizer extends TreeVisualizer {

	public ControlVisualizer(DAQ daq) {
		super(daq);
	}

	@Override
	public SizedElement visualize() {
		// create DAQ
		SizedElement root = new SizedElement("DAQ");

		// create FMMApplication
		for (FMMApplication fmmApplication : daq.getFmmApplications()) {
			SizedElement fmmApplicationElement = new SizedElement("FMMApp " + fmmApplication.getHostname());
			root.getChildren().add(fmmApplicationElement);

			// create FMM
			for (FMM fmm : fmmApplication.getFmms()) {
				SizedElement fmmElement = new SizedElement("FMM " + fmm.getGeoslot());
				fmmApplicationElement.getChildren().add(fmmElement);

				// create FED
				for (FED fed : fmm.getFeds()) {
					SizedElement fedElement = new SizedElement("FED:" + fed.getId());
					fmmElement.getChildren().add(fedElement);

				}
			}

		}

		return root;
	}

	@Override
	public String getName() {
		return "control.json";
	}

}

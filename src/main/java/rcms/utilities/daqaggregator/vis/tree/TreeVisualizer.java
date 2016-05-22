package rcms.utilities.daqaggregator.vis.tree;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.vis.Visualizer;

public abstract class TreeVisualizer implements Visualizer {

	protected final DAQ daq;

	public TreeVisualizer(DAQ daq) {
		this.daq = daq;
	}

	public abstract TreeElement visualize();

	public String getType() {
		return "tree";
	}

}

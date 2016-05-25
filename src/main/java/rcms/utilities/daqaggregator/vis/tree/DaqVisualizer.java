package rcms.utilities.daqaggregator.vis.tree;

import rcms.utilities.daqaggregator.data.BU;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FMMApplication;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.SubSystem;

public class DaqVisualizer extends TreeVisualizer {

	@Override
	public String getName() {
		return "tree-daq.json";
	}

	public DaqVisualizer(DAQ daq) {
		super(daq);
	}

	@Override
	public Element visualize() {

		// create DAQ
		Element root = new Element("DAQ");

		Element fmmApplications = new Element("FMM Applications " + daq.getFmmApplications().size());
		Element ttcPartitions = new Element("SubSystems " + daq.getSubSystems().size());
		Element frlPcs = new Element("FRL PCs " + daq.getFrlPcs().size());
		Element bus = new Element("BUs " + daq.getBus().size());
		Element fedBuilders = new Element("FBs " + daq.getFedBuilders().size());
		root.getChildren().add(fmmApplications);
		root.getChildren().add(ttcPartitions);
		root.getChildren().add(frlPcs);
		root.getChildren().add(bus);
		root.getChildren().add(fedBuilders);

		// create FMMApplications
		for (FMMApplication fmmApplication : daq.getFmmApplications()) {
			Element fmmApplicationElement = new Element(fmmApplication.getHostname());
			fmmApplications.getChildren().add(fmmApplicationElement);
		}

		// create FMMApplications
		for (SubSystem subsystem : daq.getSubSystems()) {
			Element ttcPartitionElement = new Element(subsystem.getName());
			ttcPartitions.getChildren().add(ttcPartitionElement);
		}

		// create FRL PCs
		for (FRLPc frlpc : daq.getFrlPcs()) {
			Element frlpcElement = new Element(frlpc.getHostname());
			frlPcs.getChildren().add(frlpcElement);
		}

		// create BUs
		for (BU bu : daq.getBus()) {
			Element buElement = new Element(bu.getHostname());
			bus.getChildren().add(buElement);
		}

		// create FBs
		for (FEDBuilder fb : daq.getFedBuilders()) {
			Element fbElement = new Element(fb.getName());
			fedBuilders.getChildren().add(fbElement);

			for (SubFEDBuilder subFedBuilder : fb.getSubFedbuilders()) {
				Element subFedBuilderElement = new Element("sfb: " + subFedBuilder.getFrlPc().getHostname());
				fbElement.getChildren().add(subFedBuilderElement);
				//
				// for (FRL frl : subFedBuilder.getFrls()) {
				// Element frlElement = new Element("frl");
				// subFedBuilderElement.getChildren().add(frlElement);
				// }
			}
		}

		return root;

	}

}

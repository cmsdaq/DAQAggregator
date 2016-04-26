package rcms.utilities.daqaggregator.vis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.StructureMapper;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.vis.graph.BuilderVisualizer;
import rcms.utilities.daqaggregator.vis.graph.DAQViewStyleVisualizer;
import rcms.utilities.daqaggregator.vis.tree.DaqVisualizer;
import rcms.utilities.daqaggregator.vis.tree.FedVisualizer;

public class VisualizerManager {

	private final DAQ daq;

	private List<Visualizer> visualizers;

	Logger logger = Logger.getLogger(VisualizerManager.class);

	private final StructureMapper structureMapper;

	public VisualizerManager(DAQ daq, StructureMapper structureMapper) {
		visualizers = new ArrayList<>();
		this.structureMapper = structureMapper;
		this.daq = daq;
	}

	public void persistVisualizations() {
		visualizers.add(new FedVisualizer(daq));
		visualizers.add(new DaqVisualizer(daq));
		visualizers.add(new DAQViewStyleVisualizer(structureMapper));
		visualizers.add(new BuilderVisualizer(structureMapper));

		for (Visualizer visualizer : visualizers) {
			try {
				logger.info("Visualizing " + visualizer.getName());
				persistVisualization(visualizer);
			} catch (RuntimeException e) {
				logger.error("Problem visualizing " + visualizer.getName());
			}
		}

	}

	public void persistVisualization(Visualizer visualizer) throws RuntimeException {

		Object root = visualizer.visualize();

		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writerWithDefaultPrettyPrinter()
					.writeValue(new File("visualization/" + visualizer.getType() + "/" + visualizer.getName()), root);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

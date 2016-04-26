package rcms.utilities.daqaggregator.vis.graph;

import rcms.utilities.daqaggregator.StructureMapper;
import rcms.utilities.daqaggregator.vis.Visualizer;

public abstract class GraphVisualizer implements Visualizer{
	
	protected final StructureMapper structureMapper;

	public GraphVisualizer(StructureMapper structureMapper){
		this.structureMapper = structureMapper;
	}

	public abstract Data visualize();
	
	public String getType(){
		return "graph";
	}
	
}

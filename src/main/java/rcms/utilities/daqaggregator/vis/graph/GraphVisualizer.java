package rcms.utilities.daqaggregator.vis.graph;

import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.vis.Visualizer;

public abstract class GraphVisualizer implements Visualizer{
	
	protected final MappingManager structureMapper;

	public GraphVisualizer(MappingManager structureMapper){
		this.structureMapper = structureMapper;
	}

	public abstract Data visualize();
	
	public String getType(){
		return "graph";
	}
	
}

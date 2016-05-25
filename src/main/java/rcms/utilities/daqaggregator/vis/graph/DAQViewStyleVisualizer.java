package rcms.utilities.daqaggregator.vis.graph;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.FRLPc;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.data.TTCPartition;
import rcms.utilities.daqaggregator.mappers.MappingManager;
import rcms.utilities.daqaggregator.vis.Visualizer;

public class DAQViewStyleVisualizer extends GraphVisualizer {

	public DAQViewStyleVisualizer(MappingManager structureMapper) {
		super(structureMapper);
	}

	@Override
	public Data visualize() {

		Node.global_id = 0;

		Map<Integer, Node> fbMap = new HashMap<>();
		Map<Integer, Node> sfbMap = new HashMap<>();
		Map<Integer, Node> ttcpMap = new HashMap<>();
		Map<Integer, Node> frlPcMap = new HashMap<>();

		Data data = new Data();

		// create FBs
		for (Entry<Integer, FEDBuilder> fb : structureMapper.getObjectMapper().fedBuilders.entrySet()) {
			Node fbNode = new Node("FB: " + fb.getValue().getName(), "fb");
			data.getNodes().add(fbNode);
			fbMap.put(fb.getKey(), fbNode);
		}

		// create SubFBs
		for (Entry<Integer, SubFEDBuilder> sfb : structureMapper.getObjectMapper().subFedBuilders.entrySet()) {
			Node subFedBuilderNode = new Node("SFB", "subfeds");
			data.getNodes().add(subFedBuilderNode);
			sfbMap.put(sfb.getKey(), subFedBuilderNode);
		}

		// create TTCP
		for (Entry<Integer, TTCPartition> ttcp : structureMapper.getObjectMapper().ttcPartitions.entrySet()) {

			if (isTTCPConnected(ttcp, structureMapper)) {
				Node ttcPartitionNode = new Node("TTCP: " + ttcp.getValue().getName(), "TTCP");
				data.getNodes().add(ttcPartitionNode);
				ttcpMap.put(ttcp.getKey(), ttcPartitionNode);
			}
		}

		// create FRLPc
		for (Entry<Integer, FRLPc> frlPc : structureMapper.getObjectMapper().frlPcs.entrySet()) {

			if (isFrlPcConnected(frlPc, structureMapper)) {
				Node frlPcNode = new Node(frlPc.getValue().getHostname(), "FRLPc");
				data.getNodes().add(frlPcNode);
				frlPcMap.put(frlPc.getKey(), frlPcNode);
			}

		}

		// create links subFED - TTCP
		for (Entry<Integer, Integer> relation : structureMapper.getRelationMapper().subFedBuilderToTTCP.entrySet()) {

			Link link = new Link();
			link.setSource(sfbMap.get(relation.getKey()).intId);
			link.setTarget(ttcpMap.get(relation.getValue()).intId);

			link.setValue(1);
			data.getLinks().add(link);

		}

		// create links subFED - FRLPc
		for (Entry<Integer, Integer> relation : structureMapper.getRelationMapper().subFedBuilderToFrlPc.entrySet()) {

			Link link = new Link();
			link.setSource(sfbMap.get(relation.getKey()).intId);
			link.setTarget(frlPcMap.get(relation.getValue()).intId);

			link.setValue(1);
			data.getLinks().add(link);

		}

		// create links FED - subFED
		for (Entry<Integer, Set<Integer>> relation : structureMapper.getRelationMapper().fedBuilderToSubFedBuilder
				.entrySet()) {

			for (Integer targetId : relation.getValue()) {
				Link link = new Link();
				Node source = fbMap.get(relation.getKey());
				Node target = sfbMap.get(targetId);
				link.setSource(source.intId);
				link.setTarget(target.intId);
				link.setValue(2);
				data.getLinks().add(link);
			}
		}

		// conenect all ttcp to one
		Node ttcPartiotionsNode = new Node("TTCPartitions", "END");
		data.getNodes().add(ttcPartiotionsNode);
		for (Node ttcpNode : ttcpMap.values()) {
			Link link = new Link();
			link.setSource(ttcpNode.intId);
			link.setTarget(ttcPartiotionsNode.intId);
			link.setValue(1);
			data.getLinks().add(link);
		}

		// conenect all frlpc to one
		Node frlPcsNode = new Node("FRLPcs", "END");
		data.getNodes().add(frlPcsNode);
		for (Node frlpc : frlPcMap.values()) {
			Link link = new Link();
			link.setSource(frlpc.intId);
			link.setTarget(frlPcsNode.intId);
			link.setValue(1);
			data.getLinks().add(link);
		}
		return data;
	}

	private boolean isFrlPcConnected(Entry<Integer, FRLPc> frlPc, MappingManager structureMapper) {

		for (Entry<Integer, Integer> relation : structureMapper.getRelationMapper().subFedBuilderToFrlPc.entrySet()) {
			if (relation.getValue().equals(frlPc.getKey()))
				return true;
		}
		return false;
	}

	private boolean isTTCPConnected(Entry<Integer, TTCPartition> ttcp, MappingManager structureMapper) {

		for (Entry<Integer, Integer> relation : structureMapper.getRelationMapper().subFedBuilderToTTCP.entrySet()) {
			if (relation.getValue().equals(ttcp.getKey()))
				return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return "daqview.json";
	}

}

package rcms.utilities.daqaggregator.vis.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import rcms.utilities.daqaggregator.data.FEDBuilder;
import rcms.utilities.daqaggregator.data.FRL;
import rcms.utilities.daqaggregator.data.RU;
import rcms.utilities.daqaggregator.data.SubFEDBuilder;
import rcms.utilities.daqaggregator.mappers.StructureMapper;

public class BuilderVisualizer extends GraphVisualizer {

	public BuilderVisualizer(StructureMapper structureMapper) {
		super(structureMapper);
	}

	@Override
	public String getName() {
		return "builders.json";
	}

	@Override
	public Data visualize() {
		Node.global_id = 0;

		Map<Integer, Node> fbMap = new HashMap<>();
		Map<Integer, Node> sfbMap = new HashMap<>();
		Map<Integer, Node> frlMap = new HashMap<>();
		Map<Integer, Node> ruMap = new HashMap<>();
		Data data = new Data();
		int i = 0;
		// create FBs
		for (Entry<Integer, FEDBuilder> fb : structureMapper.getObjectMapper().fedBuilders.entrySet()) {
			Node fbNode = new Node("FB: " + fb.getValue().getName(), "fb");
			data.getNodes().add(fbNode);
			fbMap.put(fb.getKey(), fbNode);
			i++;
		}

		// create SubFBs
		for (Entry<Integer, SubFEDBuilder> sfb : structureMapper.getObjectMapper().subFedBuilders.entrySet()) {
			Node subFedBuilderNode = new Node("sfb: " + sfb.getValue().getFrlPc().getHostname() + ", TTCP: "
					+ sfb.getValue().getTtcPartition().getName(), "subfeds");
			data.getNodes().add(subFedBuilderNode);
			sfbMap.put(sfb.getKey(), subFedBuilderNode);
			i++;
		}

		int frlMax = 5000;
		int frlAdded = 0;

		// create RUs
		for (Entry<Integer, RU> ru : structureMapper.getObjectMapper().rus.entrySet()) {

			Node ruNode = new Node("RU " + ru.getValue().getHostname(), "RUs");
			data.getNodes().add(ruNode);
			ruMap.put(ru.getKey(), ruNode);

		}

		// create FRLs
		for (Entry<Integer, FRL> frl : structureMapper.getObjectMapper().frls.entrySet()) {
			if (frlAdded < frlMax && isFrlConnected(frl, structureMapper)) {

				Node frlNode = new Node(
						"FRL: GeoSlot: " + frl.getValue().getGeoSlot() + ", type: " + frl.getValue().getType(), "frls");
				data.getNodes().add(frlNode);
				frlMap.put(frl.getKey(), frlNode);
				frlAdded++;
			}
		}

		// create links subFED - FRL
		for (Entry<Integer, Set<Integer>> relation : structureMapper.getRelationMapper().subFedBuilderToFrl
				.entrySet()) {

			for (Integer target : relation.getValue()) {
				Link link = new Link();
				link.setSource(sfbMap.get(relation.getKey()).intId);

				if (frlMap.containsKey(target)) {
					link.setTarget(frlMap.get(target).intId);
					link.setValue(1);
					data.getLinks().add(link);
				}
			}
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
				link.setValue(1);
				data.getLinks().add(link);
			}
		}

		// create links RU - FB
		for (Entry<Integer, Integer> relation : structureMapper.getRelationMapper().ruToFedBuilder.entrySet()) {

			Link link = new Link();
			Node source = ruMap.get(relation.getKey());
			Node target = fbMap.get(relation.getValue());
			link.setSource(source.intId);
			link.setTarget(target.intId);
			link.setValue(1);
			data.getLinks().add(link);
		}

		return data;
	}

	private boolean isFrlConnected(Entry<Integer, FRL> frl, StructureMapper structureMapper) {

		for (Entry<Integer, Set<Integer>> relation : structureMapper.getRelationMapper().subFedBuilderToFrl
				.entrySet()) {
			if (relation.getValue().contains(frl.getKey()))
				return true;
		}
		return false;
	}
}

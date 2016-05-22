package rcms.utilities.daqaggregator.vis.graph;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Node {
	@JsonIgnore
	Object object;
	@JsonIgnore
	public static int global_id = 0;

	@JsonIgnore
	private static int lastGroupX = 10;

	@JsonIgnore
	public int intId;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String id;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String getFixed() {
		return fixed;
	}

	public void setFixed(String fixed) {
		this.fixed = fixed;
	}

	@JsonIgnore
	private int x;

	@JsonIgnore
	private int y;

	@JsonIgnore
	private String fixed = "TRUE";

	public static Map<String, Integer> groupY = new HashMap<>();
	public static Map<String, Integer> groupX = new HashMap<>();

	public Node() {
		intId = global_id;
		global_id++;
	}

	public Node(String name, String group) {
		intId = global_id++;
		this.name = name;
		this.group = group;
		if (!groupY.containsKey(group)) {
			groupY.put(group, 1);
		} else {
			int curr = groupY.get(group);
			groupY.put(group, ++curr);
		}

		if (!groupX.containsKey(group)) {
			lastGroupX = lastGroupX +  100;
			groupX.put(group, lastGroupX);
		}
		x = groupX.get(group);
		y = groupY.get(group) * 10;
		
		id = Integer.toString(intId);

	}

	String name;

	@JsonIgnore
	String group;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

}

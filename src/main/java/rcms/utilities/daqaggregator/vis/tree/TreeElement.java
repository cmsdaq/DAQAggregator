package rcms.utilities.daqaggregator.vis.tree;

import java.util.HashSet;
import java.util.Set;

public abstract class TreeElement {

	private String name;
	private Set<TreeElement> children;

	public TreeElement(String name) {

		this.name = name;
		this.children = new HashSet<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<TreeElement> getChildren() {
		return children;
	}

	public void setChildren(Set<TreeElement> children) {
		this.children = children;
	}
}

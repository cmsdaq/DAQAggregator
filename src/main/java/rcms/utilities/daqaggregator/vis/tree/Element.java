package rcms.utilities.daqaggregator.vis.tree;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Element {

	private String name;
	private Set<Element> children;

	
	public Element(){
		this("unnamed");
	}
	
	public Element(String name){
		this.name = name;
		this.children = new HashSet<>();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Element> getChildren() {
		return children;
	}

	public void setChildren(Set<Element> children) {
		this.children = children;
	}

}

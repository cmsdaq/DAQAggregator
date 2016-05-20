package rcms.utilities.daqaggregator.vis.tree;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Element extends TreeElement {

	public Element(String name) {
		super(name);
	}

	public Element() {
		super("unnamed");
	}

}

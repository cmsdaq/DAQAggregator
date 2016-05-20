package rcms.utilities.daqaggregator.vis.tree;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SizedElement extends TreeElement {

	private int size;

	public SizedElement() {
		this("unnamed");
	}

	public SizedElement(String name) {
		super(name);
		this.setSize(1);
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}

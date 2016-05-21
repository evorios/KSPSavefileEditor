package at.woelfel.philip.kspsavefileeditor.backend;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.BDDAssertions.then;

public class NodeAssert extends AbstractAssert<NodeAssert, Node> {
	protected NodeAssert(Node actual) {
		super(actual, NodeAssert.class);
	}
	
	public NodeAssert hasName(String name) {
		then(actual.getNodeName()).isEqualTo(name);
		return myself;
	}

	public NodeAssert hasNoSubnodes() {
		then(actual.getSubNodes()).isEmpty();
		return myself;
	}

	public NodeAssert hasNoEntries() {
		then(actual.getEntries()).isEmpty();
		return myself;
	}
}

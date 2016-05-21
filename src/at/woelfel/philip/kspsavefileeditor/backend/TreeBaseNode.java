package at.woelfel.philip.kspsavefileeditor.backend;

/**
 * Dummy base class for Node and Entry.
 */
public abstract class TreeBaseNode {
	private Node mParentNode;
	private boolean mExpanded = false;

	public TreeBaseNode(Node parentNode) {
		mParentNode = parentNode;
	}

	public void setExpanded(boolean isExpanded) {
		mExpanded = isExpanded;
	}

	public boolean isExpanded() {
		return mExpanded;
	}

	public Node getParentNode() {
		return mParentNode;
	}

	public void setParentNode(Node mParentNode) {
		this.mParentNode = mParentNode;
	}

	public boolean hasParent() {
		return mParentNode != null;
	}

	public String print(int tabs) {
		final StringBuilder builder = new StringBuilder();
		print(tabs, builder);
		return builder.toString();
	}

	public abstract void print(int tabs, StringBuilder sb);
}

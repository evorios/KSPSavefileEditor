package at.woelfel.philip.kspsavefileeditor.gui;

import javax.swing.AbstractListModel;

import at.woelfel.philip.kspsavefileeditor.backend.Entry;
import at.woelfel.philip.kspsavefileeditor.backend.Node;
import at.woelfel.philip.kspsavefileeditor.backend.TreeBaseNode;

@SuppressWarnings("serial")
public abstract class AbstractNodeListModel<E extends TreeBaseNode> extends AbstractListModel<E> {

	protected Node mNode;
	
	private int mMode;
	public static final int MODE_SUBNODES = 1; // show subnodes in list
	public static final int MODE_ENTRIES = 2; // show entries in list

	private AbstractNodeListModel(int mode) {
		mMode = mode;
	}

	public static class NodeListModel extends AbstractNodeListModel<Node> {
		public NodeListModel() {
			super(MODE_SUBNODES);
		}

		@Override
		public Node getElementAt(int index) {
			if (mNode != null) {
				return mNode.getSubNode(index);
			}
			return null;
		}
	}

	public static class EntryListModel extends AbstractNodeListModel<Entry> {
		public EntryListModel() {
			super(MODE_ENTRIES);
		}

		@Override
		public Entry getElementAt(int index) {
			if (mNode != null) {
				return mNode.getEntry(index);
			}
			return null;
		}
	}

	@Override
	public int getSize() {
		if(mNode != null){
			if (AbstractNodeListModel.MODE_SUBNODES == mMode) {
				return mNode.getSubNodeCount();
			} else if (AbstractNodeListModel.MODE_ENTRIES == mMode) {
				return mNode.getEntryCount();
			}
		}
		return 0;

	}

	public Node getNode() {
		return mNode;
	}

	public void nodeUpdated(){
		if (AbstractNodeListModel.MODE_SUBNODES == mMode) {
			fireContentsChanged(mNode, 0, mNode.getSubNodeCount());
		} else if (AbstractNodeListModel.MODE_ENTRIES == mMode) {
			fireContentsChanged(mNode, 0, mNode.getEntryCount());
		}
	}
	
	public void setNode(Node mNode) {
		if(mNode != null){
			this.mNode = mNode;
			if (AbstractNodeListModel.MODE_SUBNODES == mMode) {
				fireContentsChanged(mNode, 0, mNode.getSubNodeCount());
			} else if (AbstractNodeListModel.MODE_ENTRIES == mMode) {
				fireContentsChanged(mNode, 0, mNode.getEntryCount());
			}
		}
	}

}

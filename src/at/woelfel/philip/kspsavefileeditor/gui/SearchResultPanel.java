package at.woelfel.philip.kspsavefileeditor.gui;

import at.woelfel.philip.kspsavefileeditor.backend.TreeBaseNode;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class SearchResultPanel extends JPanel implements SearchListener {
	private static final String SEARCH = "Search: ";
	private final JLabel mTermLabel;
//	private final JTree mResultTree;
private final JList<ListElement> mResultList;
	private final DefaultListModel<ListElement> mListModel;
	private MainGui mMainGui;

	public SearchResultPanel(MainGui mainGui) {
		mMainGui = mainGui;
		
		setLayout(new BorderLayout());
		
		mTermLabel = new JLabel(SEARCH + "%PLACEHOLDER%", SwingConstants.LEFT);
		add(mTermLabel, BorderLayout.NORTH);
		
		// TODO: Use special tree
//		mResultTree = new JTree(new NodeTreeModel(null));
		mResultList = new JList<>();
		mResultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mResultList.setLayoutOrientation(JList.VERTICAL);
		mResultList.setVisibleRowCount(-1);

		mListModel = new DefaultListModel<>();
		mResultList.setModel(mListModel);

		mResultList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = mResultList.locationToIndex(e.getPoint());
					if (index == -1) return;
					navigate(mListModel.get(index));
				}
			}
		});
		mResultList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					mResultList.getSelectedValuesList().stream().forEach(it -> navigate(it));
				}
			}
		});
		
		add(new JScrollPane(mResultList), BorderLayout.CENTER);
	}

	private void navigate(ListElement element) {
		if (element == null) return;
		element.mHandler.navigate(element.mNode);
	}

	public void onSearchComplete(String term, List<TreeBaseNode> found, GoToHandler handler) {
		clean();
		if (!found.isEmpty()) {
			setTerm(term);
			showResults(found, handler);
			mMainGui.showSearchResults();
		}
	}

	@Override
	public void onSearchStart(String term) {
	}

	private void showResults(List<TreeBaseNode> found, GoToHandler handler) {
		found.stream().map(treeBaseNode -> new ListElement(treeBaseNode, handler)).forEach(mListModel::addElement);
	}

	private void setTerm(String term) {
		mTermLabel.setText(SEARCH + term);
	}

	private void clean() {
		mListModel.clear();
	}

	public void removeElementsWithHandler(GoToHandler handler) {
		if (handler == null) return;
		final int size = mListModel.getSize();
		int start = -1;
		int end = -1;
		for (int i = 0; i < size; i++) {
			final ListElement element = mListModel.getElementAt(i);
			if (element.mHandler == handler) {
				if (start == -1) {
					start = i;
					end = i;
				} else if (end == i - 1) {
					end = i;
				} else {
					assert false;
				}
			} else if (start != -1) {
				mListModel.removeRange(start, end);
				i -= (end - start + 1 + 1);
				start = -1;
				end = -1;
			}
		}
		if (start != -1) {
			mListModel.removeRange(start, end);
		}
	}

	private static class ListElement {
		private final TreeBaseNode mNode;
		private final GoToHandler mHandler;
		private final TreePath mString;

		public ListElement(TreeBaseNode node, GoToHandler handler) {
			mNode = node;
			mHandler = handler;
			mString = NodeTree.toPath(node);
		}

		@Override
		public String toString() {
			return mString.toString();
		}
	}
}

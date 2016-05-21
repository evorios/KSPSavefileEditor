package at.woelfel.philip.kspsavefileeditor.gui;

import at.woelfel.philip.kspsavefileeditor.backend.TreeBaseNode;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;

public class SearchResultPanel extends JPanel implements SearchListener {
	public static final String SEARCH = "Search: ";
	private final JLabel mTermLabel;
//	private final JTree mResultTree;
	private final JList<TreePath> mResultList;
	private final DefaultListModel<TreePath> mListModel;
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
		
		add(new JScrollPane(mResultList), BorderLayout.CENTER);
	}
	
	public void onSearchComplete(String term, List<TreeBaseNode> found) {
		clean();
		if (!found.isEmpty()) {
			setTerm(term);
			showResults(found);
			mMainGui.showSearchResults();
		}
	}

	@Override
	public void onSearchStart(String term) {
	}

	private void showResults(List<TreeBaseNode> found) {
		final List<TreePath> paths = NodeTree.toPaths(found);
		paths.forEach(mListModel::addElement);
	}

	private void setTerm(String term) {
		mTermLabel.setText(SEARCH + term);
	}

	private void clean() {
		mListModel.clear(); 
	}
}

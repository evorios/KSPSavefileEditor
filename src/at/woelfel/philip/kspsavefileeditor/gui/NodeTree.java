package at.woelfel.philip.kspsavefileeditor.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import at.woelfel.philip.kspsavefileeditor.backend.ChangeListener;
import at.woelfel.philip.kspsavefileeditor.backend.Entry;
import at.woelfel.philip.kspsavefileeditor.backend.Node;
import at.woelfel.philip.kspsavefileeditor.backend.NodeTableModel;
import at.woelfel.philip.kspsavefileeditor.backend.NodeTreeModel;
import at.woelfel.philip.kspsavefileeditor.backend.Parser;
import at.woelfel.philip.kspsavefileeditor.backend.TreeBaseNode;
import at.woelfel.philip.tools.Logger;
import at.woelfel.philip.tools.Tools;

@SuppressWarnings("serial")
public class NodeTree extends JTree implements TreeSelectionListener, ChangeListener, ActionListener, GoToHandler {

	// private JTree mNodeTree;
	private NodeTreeModel mNodeTreeModel;
	private NodeTableModel mNodeTableModel;
	private File mFile;

	private Node mRootNode;

	private EntryEditor mEntryEditor;
	private NodeEditor mNodeEditor;

	private JPopupMenu mRCPopup; // Right Click Popup
	private JMenuItem mRCNewNodeMenu;
	private JMenuItem mRCNewEntryMenu;
	private JMenuItem mRCEditMenu;
	private JMenuItem mRCDeleteMenu;
	private JMenuItem mRCSearchMenu;
	private JMenuItem mRCCopyMenu;
	private JMenuItem mRCPasteMenu;
	private JMenuItem mRCRemoveDuplicatesMenu;
	private List<SearchListener> mSearchListeners = new ArrayList<>(1);


	/**
	 * @param f The File which should be loaded.
	 * @param hasRoot If the file that should be loaded has a root node or not (generally save files have one, crafts don't have a root node).
	 * @param nodeTableModel The table model of the table in which the entries should be displayed.
	 */
	public NodeTree(File f, boolean hasRoot, NodeTableModel nodeTableModel){
		this(null, nodeTableModel);
		load(f, hasRoot);
	}
	
	/**
	 * @param rootNode The root node of the tree.
	 * @param nodeTableModel The table model of the table in which the entries should be displayed.
	 */
	public NodeTree(Node rootNode, NodeTableModel nodeTableModel) {
		super();

		mRootNode = rootNode;
		if(mRootNode != null){
			mRootNode.setExpanded(true);
		}
		mNodeTableModel = nodeTableModel;
		mNodeTableModel.addChangeListener(this);
		
		mNodeTreeModel = new NodeTreeModel(mRootNode);
		addTreeWillExpandListener(mNodeTreeModel);
		setModel(mNodeTreeModel);
		addTreeSelectionListener(this);
		setEditable(true);
		getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		setScrollsOnExpand(true);

		ImageIcon rocket = Tools.readImage("rocket.png");
		ImageIcon rocketFly = Tools.readImage("rocket-fly.png");
		IconNodeRenderer renderer = new IconNodeRenderer();
		// renderer.setLeafIcon(rocket);
		renderer.setClosedIcon(rocket);
		renderer.setOpenIcon(rocketFly);
		setCellRenderer(renderer);
		setCellEditor(new NonLeafTreeEditor(this, renderer));

		createPopupMenu();
		mEntryEditor = new EntryEditor();
		mEntryEditor.addChangeListener(this);

		mNodeEditor = new NodeEditor(mEntryEditor);
		mNodeEditor.addChangeListener(this);

	}

	/**
	 * Loads and parses the File and shows it in this tree.
	 * @param f source file to load
	 * @param hasRoot if the file has a root node or not
	 */
	public void load(File f, final boolean hasRoot) {
		mFile = f;
		final Parser p = new Parser(f);
		ProgressScreen.showProgress("Parsing savefile...", this);
		ProgressScreen.updateProgressBar(0);
		Thread th = new Thread(() -> {
			try {
				setRootNode(p.parse(hasRoot));
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Error parsing file!\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
			ProgressScreen.hideProgress();
		});
		th.start();
	}
	
	/**
	 * Saves the current tree to the file it was loaded from.
	 */
	public void save(){
		if(mFile!=null){
			saveAs(mFile);
		}
	}

	/**
	 * Saves the current tree to the File.
	 * @param f destination file
	 */
	public void saveAs(final File f) {
		ProgressScreen.showProgress("Saving file...", this);
		ProgressScreen.updateProgressBar(0);
		Thread th = new Thread(() -> {

			String content = mRootNode.print(0);
			try {
				BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f, true), StandardCharsets.UTF_8));
				ProgressScreen.updateProgressBar(20);
				fw.write(content);
				ProgressScreen.updateProgressBar(50);
				fw.flush();
				ProgressScreen.updateProgressBar(85);
				fw.close();
				ProgressScreen.updateProgressBar(95);
			} catch (IOException e1) {
				e1.printStackTrace();
				System.err.println("Couldn't write file!\n" + e1);
			}
			ProgressScreen.hideProgress();

		});
		th.start();
		
	}

	
	
	public ArrayList<TreeBaseNode> search(TreePath[] paths, String search){
		ArrayList<Node> nodesToSearch = new ArrayList<>();
		
		// get the nodes we want to search through
		// TODO: don't search subnodes of already selected nodes --> duplicate results!
		for (TreePath path : paths) {
			Object lpc = path.getLastPathComponent();
			if (lpc instanceof Node) {
				nodesToSearch.add((Node) lpc);
			} else if (lpc instanceof Entry) {
				nodesToSearch.add(((Entry) lpc).getParentNode());
			}
		}
		
		return search(nodesToSearch, search);
	}

	public ArrayList<TreeBaseNode> search(ArrayList<Node> nodes, String search) {
		
		if(search != null && search.length() != 0 && nodes != null && nodes.size() > 0){
			ArrayList<TreeBaseNode> results = new ArrayList<>();
			for (Node node : nodes) {
				List<TreeBaseNode> tp = node.multiSearch(search);
				results.addAll(tp); // add found elements to result list
			}
			return results;
		}
		
		return null;
	}
	
	public void doSearch(){
		String search = JOptionPane.showInputDialog(null, "Please enter search term", "Search", JOptionPane.QUESTION_MESSAGE);
		if(search != null && search.length() != 0){
			TreePath[] paths = getSelectionPaths(); // get all selected paths
			ArrayList<TreeBaseNode> results;
			if(paths != null && paths.length>0){
				// search from selection
				results = search(paths, search);
			}
			else{
				// global search
				ArrayList<Node> tmp = new ArrayList<>(1);
				tmp.add(mRootNode);
				results = search(tmp, search);
			}

			if (results != null && !results.isEmpty()) {
				Logger.log("Found something: " + results);
				for (SearchListener listener : mSearchListeners) {
					listener.onSearchComplete(search, results, this);
				}
			}
			else {
				JOptionPane.showMessageDialog(null, "Didn't find anything!", "No Search Result", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public static TreePath toPath(TreeBaseNode node) {
		if (node instanceof Node) {
			return ((Node) node).getTreePathToRoot();
		} else if (node instanceof Entry) {
			final ArrayList<TreeBaseNode> path = node.getParentNode().getPathToRoot();
			path.add(node);
			return new TreePath(path.toArray());
		} else {
			throw new IllegalStateException();
		}
	}

	public void setSelection(TreePath sel) {
		setSelectionPath(sel);
		scrollPathToVisible(sel);
	}

	public Node getRootNode() {
		return mRootNode;
	}

	public void setRootNode(Node mRootNode) {
		if(mRootNode!=null){
			this.mRootNode = mRootNode;
			mRootNode.setExpanded(true);
			mNodeTreeModel.setRootNode(mRootNode);
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath tp = e.getPath();
		if(tp!=null){
			Object lPC = tp.getLastPathComponent();
			if (lPC != null){
				if(lPC instanceof Node) {
					mNodeTableModel.setNode((Node) lPC);
				}
			}
		}
	}
	

	@Override
	public void onEntryChanged(Entry e) {
		mNodeTreeModel.fireTreeStructureChanged(e);
	}

	@Override
	public void onNodeChanged(Node n) {
		mNodeTreeModel.fireTreeStructureChanged(n);
	}

	@Override
	public void onEntryAdded(Entry e) {
		mNodeTreeModel.fireTreeStructureChanged(e);
	}

	@Override
	public void onNodeAdded(Node n) {
		mNodeTreeModel.fireTreeStructureChanged(n);
	}

	@Override
	public void onEntryRemoved(Entry e) {
		mNodeTreeModel.fireTreeStructureChanged(e);
	}

	@Override
	public void onNodeRemoved(Node n) {
		mNodeTreeModel.fireTreeStructureChanged(n);
	}

	private void createPopupMenu() {
		mRCPopup = new JPopupMenu();
		JMenu rcNewMenu = new JMenu("New");
		rcNewMenu.setIcon(Tools.readImage("new.png"));
		mRCNewNodeMenu = Tools.initializeMenuItem(rcNewMenu, "Node", this);
		mRCNewEntryMenu = Tools.initializeMenuItem(rcNewMenu, "Entry", this);
		mRCPopup.add(rcNewMenu);
		mRCEditMenu = Tools.initializeMenuItem(mRCPopup, "Edit", this, Tools.readImage("edit.png"));
		mRCDeleteMenu = Tools.initializeMenuItem(mRCPopup, "Delete", this, Tools.readImage("delete.png"));
		mRCSearchMenu = Tools.initializeMenuItem(mRCPopup, "Search", this, Tools.readImage("search.png"));
		mRCCopyMenu = Tools.initializeMenuItem(mRCPopup, "Copy", this, Tools.readImage("copy.png"));
		mRCPasteMenu = Tools.initializeMenuItem(mRCPopup, "Paste", this, Tools.readImage("paste.png"));
		mRCRemoveDuplicatesMenu = Tools.initializeMenuItem(mRCPopup, "Remove Duplicate Entries", this, Tools.readImage("broom.png"));
		
		MouseListener popupListener = new PopupListener(mRCPopup);
		addMouseListener(popupListener);
		
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),"copy");
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),"paste");
		getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),"search");
		getInputMap().put(KeyStroke.getKeyStroke((char)KeyEvent.VK_DELETE), "delete");
		
		getActionMap().put("copy", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doCopy();
			}
		});
		getActionMap().put("paste", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doPaste();
			}
		});
		getActionMap().put("search", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSearch();
			}
		});
		getActionMap().put("delete", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doDelete();
			}
		});
	}

	public void addSearchListener(SearchListener listener) {
		mSearchListeners.add(listener);
	}

	@Override
	public void navigate(TreeBaseNode node) {
		if (node == null) return;
		final TreePath path = toPath(node);
		if (path == null) return;
		setSelectionPath(path);
		scrollPathToVisible(path);
	}


	class PopupListener extends MouseAdapter {
		JPopupMenu popup;

		PopupListener(JPopupMenu popupMenu) {
			popup = popupMenu;
		}

		public void mousePressed(MouseEvent e) {
			if(!showPopup(e)){
				TreePath selPath = getPathForLocation(e.getX(), e.getY());
				if (selPath != null) {
					if (e.getClickCount() == 2) {
						Object lpc = selPath.getLastPathComponent();
						if (lpc instanceof Node) {
							mNodeEditor.showForEdit((Node) lpc);
						}
						else if (lpc instanceof Entry) {
							mEntryEditor.showForEdit((Entry) lpc);
						}
					}
				}
			}
		}

		public void mouseReleased(MouseEvent e) {
			showPopup(e);
		}

		private boolean showPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				int x = e.getX();
				int y = e.getY();
				JTree tree = (JTree) e.getSource();
				TreePath path = tree.getPathForLocation(x, y);
				if (path == null)
					return false;

				if (tree.getSelectionCount() <= 1) { // if we have nothing or only one node selected, set selection to current item
					tree.setSelectionPath(path);
				}

				popup.show(e.getComponent(), e.getX(), e.getY());
				return true;
			}
			return false;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) (e.getSource());

		if (source == mRCNewNodeMenu) {
			// get selected element
			TreePath path = getSelectionPath();
			Object selection = path.getLastPathComponent();
			// create new node and add it with selection as parent
			Node parent = null;
			if (selection instanceof Entry) {
				parent = ((Entry) selection).getParentNode();
			}
			else if (selection instanceof Node) {
				parent = (Node) selection;
			}
			mNodeEditor.showForNew(parent);
		}
		else if (source == mRCNewEntryMenu) {
			// get selected element
			TreePath path = getSelectionPath();
			if (path != null) {
				Object selection = path.getLastPathComponent();
				// create new entry and add it with selection as parent
				Node parent = null;
				if (selection instanceof Entry) {
					parent = ((Entry) selection).getParentNode();
				}
				else if (selection instanceof Node) {
					parent = (Node) selection;
				}
				mEntryEditor.showForNew(parent);
			}
		}
		else if (source == mRCEditMenu) {
			// get selected element
			TreePath path = getSelectionPath();
			if (path != null) {
				Object selection = path.getLastPathComponent();
				if (selection instanceof Entry) {
					// edit Entry
					mEntryEditor.showForEdit((Entry) selection);
				}
				else if (selection instanceof Node) {
					// edit Node
					mNodeEditor.showForEdit((Node) selection);
				}
			}
		}
		else if (source == mRCDeleteMenu) {
			doDelete();
		}
		else if (source == mRCSearchMenu) {
			doSearch();
		}
		else if (source == mRCCopyMenu) {
			doCopy();
		}
		else if (source == mRCPasteMenu){
			doPaste();
		}
		else if (source == mRCRemoveDuplicatesMenu) {
			doRemoveDuplicateEntriesFromSelection();
		}
	}
	
	private void doRemoveDuplicateEntriesFromSelection() {
		Logger.log("doRemoveDupliacteNodesFromHere()");
		int yesno = JOptionPane.showConfirmDialog(null, "Do you really want to remove duplicate Entries from within the selected Node?", "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (yesno == JOptionPane.YES_OPTION) {
			TreePath[] paths = getSelectionPaths();

			if (paths != null) for (TreePath path : paths) {
				Object selection = path.getLastPathComponent();

				if (selection instanceof Node) {
					removeDuplicateEntriesFromNode((Node) selection);
				}
			}
		}
	}
	
	private void removeDuplicateEntriesFromNode(Node parent) {	
		for (int i = 0; i < parent.getSubNodeCount(); i++) {
			removeDuplicateEntriesFromNode(parent.getSubNode(i));
		}
		
		if (parent.getEntryCount() > 0) {
			HashSet<String> unique = new HashSet<>();
			
			ArrayList<Entry> entries = parent.getEntries();
			for (Entry entry : new ArrayList<>(entries)) {
				if (unique.contains(entry.toString())) {
					Logger.log("Removing duplicate entry " + entry.toString());
					parent.removeEntry(entry);
				} else {
					Logger.log("Keeping entry " + entry.toString());
					unique.add(entry.toString());
				}
			}
			
			mNodeTreeModel.fireTreeStructureChanged(parent);
		}
	}
	
	private void doDelete() {		
		int yesno = JOptionPane.showConfirmDialog(null, "Do you really want to delete this Node/Entry?", "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (yesno == JOptionPane.YES_OPTION) {
			TreePath[] paths = getSelectionPaths();
			if (paths != null) for (TreePath path : paths) {
				Object selection = path.getLastPathComponent();
				int pc = path.getPathCount();
				if (pc >= 2) { // we got at least two nodes --> remove from parent
					Node parent = (Node) path.getPathComponent(pc - 2);
					if (selection instanceof Entry) {
						parent.removeEntry((Entry) selection);
						onEntryRemoved((Entry) selection);
					} else if (selection instanceof Node) {
						parent.removeSubNode((Node) selection);
						onNodeRemoved((Node) selection);
					}
				} else { // we are deleting the root node!
					// TODO: delete the root node?
				}
			}
		}
	}

	private void doPaste(){
		Logger.log("doPaste()");
		try {
			final TreePath path = getSelectionPath();
			if (path != null) {
				String data = (String) (Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
				final Parser p = new Parser(data);
				ProgressScreen.showProgress("Parsing clipboard data...", this);
				ProgressScreen.updateProgressBar(0);
				Thread th = new Thread(() -> {
					try {
						ArrayList<TreeBaseNode> data1 = p.parseClipBoard();
						Object o = path.getLastPathComponent();
						Node parent1 = null;
						if(o instanceof Node){
							parent1 = (Node)o;
						}
						else if(o instanceof Entry){
							parent1 = ((Entry)o).getParentNode();
						} 
						if(parent1 != null){
							for (TreeBaseNode n : data1) {
								if(n!=null){
									if(n instanceof Node){
										parent1.addSubNode((Node)n);
										onNodeAdded((Node)n);
									}
									else if(n instanceof Entry){
										parent1.addEntry((Entry)n);
										onEntryAdded((Entry)n);
									}
								}
							}
						}
					} catch (Exception e) {
						ProgressScreen.hideProgress();
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "Error parsing clipboard!\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
					ProgressScreen.hideProgress();
				});
				th.start();
			}
		} catch (UnsupportedFlavorException e1) {
			JOptionPane.showMessageDialog(null, "Unsupported clipboard data!\n", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "Error:" +e1.getLocalizedMessage() +"!\n", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void doCopy(){
		Logger.log("doCopy()");
		TreePath[] paths = getSelectionPaths(); // get all selected paths
		if(paths != null && paths.length>0){
			StringBuilder sb = new StringBuilder();
			for (TreePath path : paths) {
				Object o = path.getLastPathComponent();
				if (o instanceof TreeBaseNode) {
					((TreeBaseNode) o).print(0, sb);
				} else {
					sb.append(o);
					sb.append(System.lineSeparator());
				}
			}
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
		}
	}
	
	public File getFile(){
		return mFile;
	}
	
	public void cleanup(){
		removeTreeSelectionListener(this);
		removeTreeWillExpandListener(mNodeTreeModel);
		mNodeEditor.removeChangeListener(this);
		mEntryEditor.removeChangeListener(this);
		mNodeTableModel.removeChangeListener(this);
		mNodeTreeModel.cleanup();
		mNodeTreeModel = null;
		mRootNode = null;
	}
}

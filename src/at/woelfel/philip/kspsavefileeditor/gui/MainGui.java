package at.woelfel.philip.kspsavefileeditor.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import at.woelfel.philip.kspsavefileeditor.backend.Entry;
import at.woelfel.philip.kspsavefileeditor.backend.Node;
import at.woelfel.philip.kspsavefileeditor.backend.NodeTableModel;
import at.woelfel.philip.kspsavefileeditor.backend.Settings;
import at.woelfel.philip.tools.Logger;
import at.woelfel.philip.tools.Tools;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.*;

@SuppressWarnings("serial")
public class MainGui extends JFrame implements ActionListener, ItemListener, TreeSelectionListener{
	/*
	 * TODO: search & replace:
	 * Show found elements with checkboxes so user could select what elements should be replaced
	 */
	private FileChooser mFileChooser;
	private DirectoryChooser mDirectoryChooser;

	private JFXPanel mJFXPanel;
	
	private JTabbedPane mTabPane;
	private JTable mEntryTable;
	private ArrayList<NodeTreeWindow> mTreeWindows;
	private JLabel mPathLabel;
	
	
	private JMenuItem mFileOpenSFSItem;
	private JMenuItem mFileOpenOtherItem;
	private JMenuItem mFileSaveItem;
	private JMenuItem mFileSettingsItem;
	
	private JMenuItem mEditSearchItem;
	
	private JMenuItem mAboutInfoItem;
	private JCheckBoxMenuItem mAboutDebugItem;
	private JCheckBoxMenuItem mAboutFileDebugItem;
	
	private NodeTableModel mNodeTableModel;

	
	public MainGui() {
		Logger.setEnabled(false);

		mJFXPanel = new JFXPanel();
		add(mJFXPanel);
		mJFXPanel.setVisible(false);
		Platform.runLater(() -> {
			Group root = new Group();
			Scene scene = new Scene(root);
			mJFXPanel.setScene(scene);
		});

		mFileChooser = new FileChooser();
		mFileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("KSP Save or Craft files", "*.sfs", "*.txt", "*.craft", "*.cfg")
		);

		mDirectoryChooser = new DirectoryChooser();
		mDirectoryChooser.setTitle("KSP Folder...");

		updateChoosersInitDirectory();
		
			
		// ################################## Temp Nodes ##################################
		Node mTempNode;
		mTempNode = new Node("GAME", null);
		mTempNode.setIcon(Tools.readImage("nodes/game.png"));
		String[] tmpNodeNames = {"ACTIONGROUPS", "ACTIONS", "CREW", "EDITOR", "EVENTS", "FLIGHTSTATE", "MODULE", "ORBIT", "PART", "PLANETS", "RECRUIT", "ROSTER", "SCIENCE", "TRACKINGSTATION", "VESSEL", "VESSELS/FLAG", "VESSELS/BASE", "VESSELS/PROBE", "VESSELS/SPACEOBJECT"};
		for (String tmpName : tmpNodeNames) {
			Node tmpNode = new Node(tmpName, mTempNode);
			tmpNode.setIcon(Tools.readImage("nodes/" + tmpName.toLowerCase() + ".png"));
			mTempNode.addSubNode(tmpNode);
		}
		/*mRootNode = new Node("Node 0", null);
		for(int i=0;i<10;i++){
			mRootNode.createEntry("entry1" +((char)(97+i)), "value1" +((char)(97+i)));
		}
		for(int i=0;i<10;i++){
			Node subnode = new Node("Node 0"+i, mRootNode);
			for(int j=0;j<10;j++){
				subnode.createEntry("entry0"+i+((char)(97+j)), "value0"+i+((char)(97+j)));
			}
			mRootNode.addSubNode(subnode);
		
			for(int j=0;j<10;j++){
				Node subsubnode = new Node("Node0"+i+""+j, subnode);
				for(int k=0;k<10;k++){
					subsubnode.createEntry("entry0"+i+""+j+((char)(97+k)), "value0"+i+""+j+((char)(97+k)));
				}
				subnode.addSubNode(subsubnode);
			}
		}
		
		Parser p = new Parser("persistent_small.sfs");
		mRootNode = p.parse();
		*/
		// ################################## Window ##################################
		setTitle("KSP Savefile Editor");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		//setSize((int)(screen.getWidth()-(screen.getWidth()/10)), (int)(screen.getHeight()-(screen.getHeight()/10)));
		setSize(screen);
		setLayout(new BorderLayout());
		
		
		// ################################## Menu ##################################
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		mFileOpenSFSItem = Tools.initializeMenuItem(fileMenu, "Open...", this, Tools.readImage("load.png"));
		mFileOpenSFSItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mFileOpenOtherItem = Tools.initializeMenuItem(fileMenu, "Open Craft...", this, Tools.readImage("load.png"));
		mFileSaveItem = Tools.initializeMenuItem(fileMenu, "Save...", this, Tools.readImage("save.png"));
		mFileSaveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenu.addSeparator();
		mFileSettingsItem = Tools.initializeMenuItem(fileMenu, "KSP Folder...", this);
		menuBar.add(fileMenu);
		
		JMenu editMenu = new JMenu("Edit");
		mEditSearchItem = Tools.initializeMenuItem(editMenu, "Search...", this, Tools.readImage("search.png"));
		menuBar.add(editMenu);
		
		JMenu aboutMenu = new JMenu("About");
		mAboutInfoItem = Tools.initializeMenuItem(aboutMenu, "Info", this, Tools.readImage("info.png"));
		mAboutDebugItem = Tools.initializeCheckboxMenuItem(aboutMenu, "Enable Debug", this);
		mAboutFileDebugItem = Tools.initializeCheckboxMenuItem(aboutMenu, "Enable File Debug", this);
		menuBar.add(aboutMenu);
		setJMenuBar(menuBar);
		
		
		// ################################## Components ##################################
		mNodeTableModel = new NodeTableModel();
		mEntryTable = new JTable(mNodeTableModel);
		JScrollPane entryTableJSP = new JScrollPane(mEntryTable);
		
		mTabPane = new JTabbedPane();
		mTreeWindows = new ArrayList<>();
		addTreeWindow(mTempNode);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mTabPane, entryTableJSP);
		splitPane.setDividerLocation((screen.height/3)*2); // set to 2/3 of screen height
		add(splitPane, BorderLayout.CENTER);
		
		
		mPathLabel = new JLabel("Path");
		JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pathPanel.add(mPathLabel);
		add(new JScrollPane(pathPanel), BorderLayout.NORTH);
		
		
		//add(nodeTreeJSP,c);
		//add(entryTableJSP, c);
	}

	public void addTreeWindow(Node n){
		NodeTree tmpNT = new NodeTree(n, mNodeTableModel);
		String tabName = n.getNodeName();
		
		addTreeWindow(tmpNT, tabName);
	}
	
	public void addTreeWindow(File f, boolean hasRoot){
		NodeTree tmpNT = new NodeTree(f, hasRoot, mNodeTableModel);
		String tabName = f.getParentFile().getName()+"/"+f.getName();
		
		addTreeWindow(tmpNT, tabName);
	}
	
	public void addTreeWindow(NodeTree nt, String tabName){
		nt.addTreeSelectionListener(this);
		
		NodeTreeWindow tmpNTW = new NodeTreeWindow(nt);
		mTreeWindows.add(tmpNTW);
		
		mTabPane.addTab(tabName, Tools.readImage("rocket.png"), tmpNTW);
		mTabPane.setSelectedComponent(tmpNTW);
		
		int index = mTabPane.getSelectedIndex();
		mTabPane.setTabComponentAt(index, new CloseableTab(mTabPane, this));
	}
	
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JMenuItem) {
			menuClick(e);
		}
	}
	
	private void menuClick(ActionEvent e){
		JMenuItem source = (JMenuItem) (e.getSource());
		String s = "Action event detected.\n" + "\tEvent source: " + source.getText() + " (an instance of " + source.getClass().getSimpleName() + ")";
		Logger.log(s);
		
		
		if(source == mAboutInfoItem){
			new AboutWindow();
			return;
		}
		else if (source == mFileSettingsItem) {
			Platform.runLater(() -> {
				final File directory = mDirectoryChooser.showDialog(mJFXPanel.getScene().getWindow());
				if (directory != null) {
					Settings.setString(Settings.PREF_KSP_DIR, directory.getAbsolutePath());
					updateChoosersInitDirectory();
				}
			});
			return;
		}
		else if (source == mFileOpenSFSItem) {
			Platform.runLater(() -> loadFile(true));
			return;
		}
		else if (source == mFileOpenOtherItem) {
			Platform.runLater(() -> loadFile(false));
			return;
		}
		
		NodeTreeWindow tw = getCurrentTreeWindow();
		if(tw!=null){
			if (source == mFileSaveItem) {
				Platform.runLater(() -> showSaveDialog(tw.getNodeTree()));
			}
			else if (source == mEditSearchItem) {
				tw.getNodeTree().doSearch();
			}
		}
		else{
			JOptionPane.showMessageDialog(this, "Please select a tree window first!", "Error", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void updateChoosersInitDirectory() {
		mFileChooser.setInitialDirectory(new File(Settings.getString(Settings.PREF_KSP_DIR, System.getProperty("user.home"))));
		mDirectoryChooser.setInitialDirectory(new File(Settings.getString(Settings.PREF_KSP_DIR, System.getProperty("user.home"))));
	}

	protected NodeTreeWindow getCurrentTreeWindow(){
		return (NodeTreeWindow) mTabPane.getSelectedComponent();
	}
	
	protected void loadFile(boolean hasRoot){

		File f = mFileChooser.showOpenDialog(mJFXPanel.getScene().getWindow());
		if (f != null) {
			Logger.log("You chose to open this file: " + f.getName());
			for (NodeTreeWindow tw : mTreeWindows){
				try {
					if(tw.getNodeTree().getFile() != null && f.getCanonicalPath().equals(tw.getNodeTree().getFile().getCanonicalPath())){
						int res = JOptionPane.showConfirmDialog(this, "File already opened! Do you want to open the file again?", "File already opened!", JOptionPane.YES_NO_CANCEL_OPTION);
						if(res == JOptionPane.YES_OPTION){
							break;
						}
						else if(res == JOptionPane.NO_OPTION){
							mTabPane.setSelectedComponent(tw);
							return;
						}
						else{
							return;
						}
					}
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
			// we came here, file isn't opened
			addTreeWindow(f, hasRoot);
			
		}
	}

	protected void showSaveDialog(NodeTree tw){
		if(tw!=null){
			File f = mFileChooser.showOpenDialog(mJFXPanel.getScene().getWindow());
			if (f != null) {
				Logger.log("You chose to save this file: " + f.getName());
				if(f.exists()){
					int res = JOptionPane.showConfirmDialog(this, "File exists! Do you want to overwrite the file?", "File exists!", JOptionPane.YES_NO_CANCEL_OPTION);
					if(res == JOptionPane.YES_OPTION){
						tw.saveAs(f);
					}
					else if(res == JOptionPane.NO_OPTION){
						// user choose no --> show save dialog again
						showSaveDialog(tw);
					}
					// cancel --> do nothing
				}
				else{
					tw.saveAs(f);
				}
			}
		}
		else{
			JOptionPane.showMessageDialog(this, "Please select a tree window to save the file!", "Error", JOptionPane.WARNING_MESSAGE);
		}
	}


	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getSource() == mAboutDebugItem){
			if(e.getStateChange() == ItemEvent.DESELECTED){
				Logger.setEnabled(false);
			}
			else{
				Logger.setEnabled(true);
			}
		}
		else if(e.getSource() == mAboutFileDebugItem){
			if(e.getStateChange() == ItemEvent.DESELECTED){
				Logger.setFileEnabled(false);
			}
			else{
				// Only file selection supported now!
				File selected = mFileChooser.showSaveDialog(mJFXPanel.getScene().getWindow());
				if (selected != null) {
					try {
						Logger.setLogFile(selected);
						Logger.setFileEnabled(true);
					} catch (FileNotFoundException e1) {
						JOptionPane.showMessageDialog(this, "Error creating logfile!\n"+e1.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		
	}




	@Override
	public void valueChanged(TreeSelectionEvent e) {
		TreePath tp = e.getPath();
		if(tp!=null){
			mPathLabel.setText(pathToString(tp));
		}
	}
	
	public String pathToString(TreePath treePath){
		StringBuilder sb = new StringBuilder();
		// sb.append("Path: ");
		
		Object[] path = treePath.getPath();
		for (int i = 0; i < path.length; i++) {
			final Object o = path[i];
			if (o instanceof Entry) {
				sb.append(((Entry) o).getKey());
			} else {
				sb.append(o);
			}
			if (i != path.length - 1) {
				sb.append(" > ");
			}
		}
		
		return sb.toString();
	}

	public void removeTab(int i) {
		NodeTreeWindow ntw = (NodeTreeWindow)mTabPane.getComponentAt(i);
		ntw.getNodeTree().removeTreeSelectionListener(this);
		ntw.cleanup();
		ntw.setNodeTree(new NodeTree(null, mNodeTableModel));
		
		mTabPane.remove(ntw);
		mTreeWindows.remove(ntw);
	}

	
	
	

}

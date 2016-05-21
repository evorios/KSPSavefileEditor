package at.woelfel.philip.kspsavefileeditor.backend;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.tree.TreePath;

import at.woelfel.philip.tools.Tools;

public class Node extends TreeBaseNode {
	
	private String mNodeName;
	
	private ArrayList<Entry> mEntries;
	private ArrayList<Node> mSubNodes;

	private ImageIcon mIcon;
	
	
	public Node(String name, Node parent) {
		super(parent);
		mNodeName = name;
		mEntries = new ArrayList<>();
		mSubNodes = new ArrayList<>();
	}

		
	public String getNodeName() {
		return mNodeName;
	}

	public void setNodeName(String mNodeName) {
		this.mNodeName = mNodeName;
	}
	
	

	public ArrayList<Entry> getEntries() {
		return mEntries;
	}
	
	public Entry getEntry(int id){
		if(mEntries!=null){
			return mEntries.get(id);
		}
		return null;
	}
	
	public Entry getEntry(String key){
		for (Entry e : mEntries) {
			if(e.getKey().equals(key)){
				return e;
			}
		}
		return null;
	}
	
	public boolean hasEntry(String key){
		for (Entry e : mEntries) {
			if(e.getKey().equals(key)){
				return true;
			}
		}
		return false;
	}
	
	public void addEntry(Entry entry){
		mEntries.add(entry);
		entry.setParentNode(this);
	}
	
	public void createEntry(String key, String value){
		Entry e = new Entry(this, key, value);
		addEntry(e);
	}
	
	public void removeEntry(Entry entry){
		mEntries.remove(entry);
	}
	
	public int getEntryCount(){
		return mEntries!=null?mEntries.size():0;
	}



	public ArrayList<Node> getSubNodes() {
		return mSubNodes;
	}

	public void setSubNodes(ArrayList<Node> mSubNodes) {
		this.mSubNodes = mSubNodes;
	}
	
	public Node getSubNode(int id){
		return mSubNodes!=null?mSubNodes.get(id):null;
	}
	
	public void addSubNode(Node node){
		if(mSubNodes!=null){
			mSubNodes.add(node);
			node.setParentNode(this);
		}
	}
	
	public void createSubNode(String nodeName){
		Node n = new Node(nodeName, this);
		addSubNode(n);
	}
	
	public void removeSubNode(Node node){
		if(mSubNodes!=null){
			mSubNodes.remove(node);
		}
	}
	
	public int getSubNodeCount(){
		return mSubNodes!=null?mSubNodes.size():0;
	}
	
	public boolean hasSubNodes(){
		return !(mSubNodes == null || mSubNodes.isEmpty());
	}
	
	
	@Override
	public String toString() {
		for(Entry e : getEntries()){
			if("name".equals(e.getKey()) || "part".equals(e.getKey()) || "id".equals(e.getKey())){
				return mNodeName +" (" +e.getValue() +")";
			}
		}
		return mNodeName;
	}
	
	public ArrayList<TreeBaseNode> getPathToRoot(){
		final Node parent = getParentNode();
		if(parent==null){
			ArrayList<TreeBaseNode> path = new ArrayList<>();
			path.add(this);
			return path;
		}
		else{
			ArrayList<TreeBaseNode> path = parent.getPathToRoot();
			path.add(this);
			return path;
		}
	}
	
	public TreePath getTreePathToRoot(){
		return new TreePath(getPathToRoot());
	}

	public void print(int tabs, StringBuilder sb) {
		
		if(!"".equals(getNodeName())){ // empty node name means we don't want to save the node name to file (root node for craft or config files)
			appendLine(sb, tabs, getNodeName());
			appendLine(sb, tabs, "{");
		}
		else{
			tabs--; // decrease tabs so it isn't indented
		}
		for (Entry entry : getEntries()) {
			entry.print(tabs + 1, sb);
		}
		for (Node node : getSubNodes()){
			node.print(tabs + 1, sb);
		}
		if(!"".equals(getNodeName())){
			appendLine(sb, tabs, "}");
		}
	}
	
	public static void appendLine(StringBuilder sb, int tabs, String o){
		appendTabs(tabs, sb);
		sb.append(o);
		sb.append(System.lineSeparator());
	}
	
	public static void appendTabs(int tabs, StringBuilder sb){
		for(int i=0;i<tabs;i++){
			sb.append("\t");
		}
	}
	
	public TreeBaseNode search(String search){
		final ArrayList<TreeBaseNode> collector = new ArrayList<>(1);
		doSearch(search, collector, true);
		if (collector.isEmpty()) return null;
		else return collector.iterator().next();
	}
	
	public List<TreeBaseNode> multiSearch(String search){
		ArrayList<TreeBaseNode> nodes = new ArrayList<>();
		doSearch(search, nodes, false);
		return nodes;
	}
	
	private List<TreeBaseNode> doSearch(String search, List<TreeBaseNode> results, boolean stopOnFirst){
		if(results==null){
			results = new ArrayList<>();
		}
		// check yourself
		if(getNodeName().contains(search)){
			results.add(this);
			if (stopOnFirst) return results;
		}
		// check entries
		for (Entry entry : getEntries()) {
			if(entry.search(search)){
				results.add(entry);
				if (stopOnFirst) return results;
			}
		}
		// check subnodes
		final ArrayList<Node> subNodes = getSubNodes();
		if (subNodes != null) for (Node node : subNodes) {
			node.doSearch(search, results, stopOnFirst);
			if (stopOnFirst && !results.isEmpty()) return results;
		}
		
		return results;
	}

	public ImageIcon getIcon() {
		if(mIcon!=null){
			// manually set icon, return this
			return mIcon;
		}
		if("VESSEL".equals(getNodeName())){
			// vessel --> check type
			if (hasEntry("type")) {
				ImageIcon tmpIcon =  Tools.readImage("nodes/vessels/"+getEntry("type").getValue().toLowerCase() +".png");
				if (tmpIcon != null) {
					return tmpIcon;
				}
			}
		}
		// get icon for name
		return Tools.readImage("nodes/"+getNodeName().toLowerCase() +".png");
	}

	public void setIcon(ImageIcon mIcon) {
		this.mIcon = mIcon;
	}
}

package at.woelfel.philip.kspsavefileeditor.backend;

public interface ChangeListener {

	void onEntryChanged(Entry e);
	void onNodeChanged(Node n);
	
	void onEntryAdded(Entry e);
	void onNodeAdded(Node n);
	
	void onEntryRemoved(Entry e);
	void onNodeRemoved(Node n);
}

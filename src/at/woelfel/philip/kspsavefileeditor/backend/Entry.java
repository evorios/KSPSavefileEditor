package at.woelfel.philip.kspsavefileeditor.backend;

public class Entry extends TreeBaseNode{

	private String mKey;
	private String mValue;
	
	public Entry() {
		super(null);
	}
	
	public Entry(Node parentNode, String key, String value){
		super(parentNode);
		mKey = key;
		mValue = value;
	}

	/**
	 * @return the mKey
	 */
	public String getKey() {
		return mKey;
	}

	/**
	 * @param mKey the mKey to set
	 */
	public void setKey(String mKey) {
		this.mKey = mKey;
	}

	/**
	 * @return the mValue
	 */
	public String getValue() {
		return mValue;
	}

	/**
	 * @param mValue the mValue to set
	 */
	public void setValue(String mValue) {
		this.mValue = mValue;
	}
	
	@Override
	public String toString() {
		return mKey +" = " +mValue;
	}

	@Override
	public void print(int tabs, StringBuilder sb) {
		Node.appendLine(sb, tabs, mKey + " = " + mValue);
	}

	public boolean search(String search){
		final String slc = search.toLowerCase();
		return getKey().toLowerCase().contains(slc) || getValue().toLowerCase().contains(slc);
	}
}

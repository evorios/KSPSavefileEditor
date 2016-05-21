package at.woelfel.philip.kspsavefileeditor;



import at.woelfel.philip.kspsavefileeditor.gui.MainGui;

public class MainClass {

	/**
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		 javax.swing.SwingUtilities.invokeLater(() -> {
			 final MainGui gui = new MainGui();
			 gui.setVisible(true);
		 });
	}

}

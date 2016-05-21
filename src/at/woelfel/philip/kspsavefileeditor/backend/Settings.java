package at.woelfel.philip.kspsavefileeditor.backend;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings {
	private static Preferences preferences;
	private static final String prefName = "/at/woelfel/philip/ksp/savefileeditor";
	
	public static final String PREF_KSP_DIR="ksp_dir";

	private static void check() {
		if (preferences == null) {
			preferences = Preferences.userRoot().node(Settings.prefName);
		}
	}
	
	
	public static String getString(String key, String defaultValue){
		check();
		return preferences.get(key, defaultValue);
	}
	
	public static boolean setString(String key, String value){
		check();
		preferences.put(key, value);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			return false;
		}
		return true;
	}
}

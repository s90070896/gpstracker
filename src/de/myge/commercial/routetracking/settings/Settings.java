package de.myge.commercial.routetracking.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import de.myge.commercial.routetracking.errorhandling.ErrorHandling;

/**
 * Diese Klasse verwaltet die Einstellung zu dieser App.
 * Aktuell wird nur die Locale gespeichert. Weitere Parameter
 * sollen mit neuen Minor-Versionen kommen.
 * @author Jan
 *
 */
public class Settings {
	private static Settings instance;
	private Activity activity;
	private SharedPreferences settings;
	private static final String SPEED_IN_KMH = "speedInKmh";
	
	private Settings(Activity activity) {
		ErrorHandling.checkForNull(activity, "activity");
		this.activity = activity;
		settings = this.activity.getSharedPreferences("ROUTE_TRACKING", Activity.MODE_PRIVATE);
	}
	
	/**
	 * Gibt eine Instanz dieser Klasse zur�ck.
	 * Mit dieser Instanz kann aktuell nur der Parameter Locale 
	 * editiert und abgefragt werden.
	 * @return
	 */
	public static Settings getInstance(Activity activity) {
		if (instance == null)
				instance = new Settings(activity);
		return instance;
	}
	
	public void setSpeedInKmh(boolean speedInKmh) {
		ErrorHandling.checkForNull(speedInKmh, "locale");
		Editor editor = this.settings.edit();
		editor.putBoolean(SPEED_IN_KMH, speedInKmh);
		editor.commit();
	}
	
	/**
	 * 
	 */
	public boolean isSpeedInKmh() {
		return this.settings.getBoolean(SPEED_IN_KMH, true);
	}
}

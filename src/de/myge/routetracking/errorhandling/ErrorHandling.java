package de.myge.routetracking.errorhandling;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Diese Klasse beinhaltet Methoden zum Errorhandling.
 * Dazu gehören Methoden für Post und Precontitions, sowie
 * Methoden zum Ausgeben von Fehlern Als Toast oder als Dialog.
 * @author Jan
 *
 */
public class ErrorHandling {
	/**
	 * Prüft ein Objekt, ob es null ist oder nicht. Wenn das Objekt null ist, wird eine IllegalArgumentException geworfen.
	 * @param obj das zu prüfende Objekt
	 * @param name Variablenname des Objekts.
	 */
	public static void checkForNull(Object obj, String name) {
		if (obj == null) throw new IllegalArgumentException(name + " cannot be null");
	}
	
	/**
	 * Zeigt einen Fehler dem Benutzer als Toast.
	 */
	public static void showError(Context c, Exception e) {
		checkForNull(e, "exception");
		checkForNull(c, "context");
		Log.e("SmsToSpeech", e.getLocalizedMessage(), e);
		Toast.makeText(c, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
	}
	/**
	 * Zeigt einen Fehler dem Benutzer als Toast.
	 */
	public static void showError(Context c, String e) {
		checkForNull(e, "exception");
		checkForNull(c, "context");
		Log.e("SmsToSpeech", e);
		Toast.makeText(c, e, Toast.LENGTH_LONG).show();
	}
}

package de.myge.routetracking.sync;

import android.content.Context;
import android.content.SharedPreferences;

public class AccountInformation {
	private static final String sharedPreferencesName = "GPS_TRACKER";
	private static final String userName = "USERNAME";
	private static final String password = "PASSWORD";
	private SharedPreferences settings;
	
	public AccountInformation(Context c) {
		settings = c.getSharedPreferences(sharedPreferencesName, 0);
	}
	
	/**
	 * @return den Benutzernamen, wenn einer vorhanden ist, ansonsten null.
	 */
	public String getUserName() {
		return settings.getString(userName, null);
	}
	
	public String getPassword() {
		return settings.getString(password, null);
	}
}

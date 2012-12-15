package de.myge.routetracking.arrayadapter;

import android.content.Context;
import de.myge.routetracking.database.Profile;

public class Model {

	private Profile profile;
	private boolean selected;
	private Context c;
	
	public Model(Profile profile, Context c) {
		this.profile = profile;
		this.c = c;
		selected = false;
	}

	public Context getC() {
		return c;
	}
	public void setC(Context c) {
		this.c = c;
	}
	
	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile name) {
		this.profile = name;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
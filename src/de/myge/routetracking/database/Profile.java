package de.myge.routetracking.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Diese Klasse speichert den Namen eines Profils und ist die Parent Tabelle von
 * GpsCoordinates.
 * 
 * @author Jan
 * 
 */
@DatabaseTable(tableName = "profile")
public class Profile {

	public Profile() {}
	
	public Profile(String profileName, String description) {
		if(profileName == null || profileName.equals("")) {
			throw new IllegalArgumentException("Name of tracked route cannot be empty");
		}
		setProfileName(profileName);
		setDescription(description);
	}
	
	@DatabaseField(generatedId=true)
	private int id;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@DatabaseField
	private String profileName;

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	
	@DatabaseField(canBeNull=true)
	private String description;
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}

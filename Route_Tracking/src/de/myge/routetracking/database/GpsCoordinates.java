package de.myge.routetracking.database;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Diese Klasse speichert nur nur eine GPS Position, sowie den Zeitpunkt.
 * Diese Klasse wird mittels JPA annotiert, damit die Informationen in einer
 * lokalen SQLite Datenbank gespeichert werden k�nnen. 
 * @author Jan
 *
 */
@DatabaseTable(tableName="coordinates")
public class GpsCoordinates {

	public GpsCoordinates() {}
	
	
	// id is generated by the database and set on the object automagically
	@DatabaseField(generatedId = true)
	int id;
	
	public static final String LATITUDE_FIELD_NAME = "latitude"; 
    
    @DatabaseField(canBeNull = false, columnName = LATITUDE_FIELD_NAME)
	private double latitude;
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	@DatabaseField
	private double longitude;
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	@DatabaseField
	private Date timestamp;
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	@DatabaseField
	private float speed;
	public float getSpeed() {
		return speed;
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	@DatabaseField(foreign=true, canBeNull=false)
	private Profile profile;
	public void setProfileId(Profile profileId) {
		this.profile = profileId;
	}
	public Profile getProfileId() {
		return profile;
	}
	
	/**
	 * Ermittelt alle GPS-Koordinaten eine Profils.
	 * @param profile
	 * @param c
	 * @return
	 * @throws SQLException
	 */
	public static List<GpsCoordinates> selectAllGpsCoordinatesFromProfile(Profile profile, Context c) throws SQLException {
		if (profile == null || c == null) throw new IllegalArgumentException("profile or context is null");
		CreateDatabase db = new CreateDatabase(c);
		return db .getCoordinatesDao().queryBuilder().where().eq("profile_id", profile).query();
	}
	
	/**
	 * Berechnet die Dauer einer Route
	 * @param profile
	 * @param c
	 * @return
	 * @throws SQLException
	 */
	public static long calculateDurationOfRoute(Profile profile, Context c) throws SQLException {
		if (profile == null || c == null) throw new IllegalArgumentException("profile or context is null");
		CreateDatabase db = new CreateDatabase(c);
		
		String sqlStart = "select min(timestamp) from coordinates where profile_id = '"+profile.getId()+"'";
		List<Object[]> list = db.getCoordinatesDao().queryRaw(sqlStart, new DataType[] { DataType.DATE_STRING}).getResults();
		
		Date start = (Date) list.get(0)[0]; 
		
		// wenn der Startwert null ist, soll als Dauer 0 zurück gegeben werden, da keine GPS-Korrdinaten vorhanden sind.
		if (start == null) return 0;
		
		String sqlEnd = "select max(timestamp) from coordinates where profile_id = '"+profile.getId()+"'";
		list = db.getCoordinatesDao().queryRaw(sqlEnd, new DataType[] { DataType.DATE_STRING}).getResults();
		Date end = (Date) list.get(0)[0];
		
		// Get msec from each, and subtract.
		long diff = end.getTime() - start.getTime();
		return (diff / (1000 * 60));
			
	}
	
	public static float calculateAvgSpeed(Profile profile, Context c) throws SQLException {
		if (profile == null || c == null) throw new IllegalArgumentException("profile or context is null");
		CreateDatabase db = new CreateDatabase(c);
		String maxSpeed = "select avg(speed) from coordinates where profile_id = '"+profile.getId()+"'";
		List<String[]> list = db.getCoordinatesDao().queryRaw(maxSpeed).getResults();
		return Float.valueOf(list.get(0)[0]).floatValue();
	}
	
	/**
	 * berechnet die Distanz der getrackten Route.
	 * @param profile
	 * @param c
	 * @return
	 * @throws SQLException
	 */
	public static float calculateDistance(Profile profile, Context c) throws SQLException {
		if (profile == null || c == null) throw new IllegalArgumentException("profile or context is null");
		List<GpsCoordinates> list = selectAllGpsCoordinatesFromProfile(profile, c);
		
		// wenn der Startwert null ist, soll als Dauer 0 zurück gegeben werden, da keine GPS-Korrdinaten vorhanden sind.
		if (list == null || list.isEmpty()) return 0;
		
		float distance = 0;
		GeoPoint gp1;
		GeoPoint gp2 = null;
		for(int i = 0; i < list.size(); i++) {
			
			gp1 = new GeoPoint((int)(list.get(i).getLatitude()* 1E6), (int)(Double.parseDouble(list.get(i).getLongitude()* 1E6 + "")));
			if(i != 0) {
				distance += calculateDistanceBetween2GeoPoints(gp1, gp2);  
			}
			gp2 = gp1;
			i++;
		}
		return distance;
	}
	
	private static float calculateDistanceBetween2GeoPoints(GeoPoint gp1, GeoPoint gp2) {
		if(gp1 == null | gp2 == null) throw new IllegalArgumentException("geopoints cannot be null");
		
		Location locationA = new Location("point A");  

		locationA.setLatitude(gp1.getLatitudeE6()/ 1E6);  
		locationA.setLongitude(gp1.getLongitudeE6()/ 1E6);  

		Location locationB = new Location("point B");  

		locationB.setLatitude(gp2.getLatitudeE6()/ 1E6);  
		locationB.setLongitude(gp2.getLongitudeE6()/ 1E6);  

		return locationA.distanceTo(locationB); 
	}
	
	/**
	 * berechnet die Höchstgeschwindigkeit, die auf der Route erreicht wurde.
	 * @param profile
	 * @param c
	 * @return
	 * @throws SQLException
	 */
	public static float calculateTopSpeed(Profile profile, Context c) throws SQLException {
		if (profile == null || c == null) throw new IllegalArgumentException("profile or context is null");
		
		CreateDatabase db = new CreateDatabase(c);
		
		String maxSpeed = "select max(speed) from coordinates where profile_id = '"+profile.getId()+"'";
			List<String[]> list = db.getCoordinatesDao().queryRaw(maxSpeed).getResults();
			if (list.get(0)[0] != null) {
			return Float.valueOf(list.get(0)[0]).floatValue();
		} else {
			return 0.0f;
		}
		
//		List<GpsCoordinates> list = selectAllGpsCoordinatesFromProfile(profile, c);
//		
//		// wenn der Startwert null ist, soll als Dauer 0 zurück gegeben werden, da keine GPS-Korrdinaten vorhanden sind.
//		if (list == null || list.isEmpty()) return 0;
//		
//		float topSpeed = 0;
//		GeoPoint gp1;
//		GeoPoint gp2 = null;
//		long dateGp1;
//		long dateGp2 = 0;
//		for(int i = 0; i < list.size(); i++) {
//			
//			gp1 = new GeoPoint((int)(list.get(i).getLatitude()* 1E6), (int)(Double.parseDouble(list.get(i).getLongitude()* 1E6 + "")));
//			Calendar cal = Calendar.getInstance();
//			cal.setTime(list.get(i).timestamp);
//			dateGp1 = cal.getTimeInMillis();
//			if(i != 0) {
//				// Get msec from each, and subtract.
//				long diff = dateGp1 - dateGp2; 
//				float speed = (calculateDistanceBetween2GeoPoints(gp1, gp2))/((float)diff/1000);
//				if (speed > topSpeed) topSpeed = speed;				
//			}
//			gp2 = gp1;
//			dateGp2 = dateGp1;
//			i++;
//		}
	}
}

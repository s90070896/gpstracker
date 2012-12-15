package de.myge.commercial.routetracking;

import java.sql.SQLException;
import java.util.Date;

import android.app.NotificationManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import de.myge.commercial.routetracking.database.CreateDatabase;
import de.myge.commercial.routetracking.database.GpsCoordinates;
import de.myge.commercial.routetracking.database.Profile;

/**
 * Diese Klasse trackt die aktuelle Position des Ger�ts und speichert die
 * Koordinaten in einer lokalen Datenbank zur sp�teren Auswertung.
 * @author Bolle
 *
 */
public class LocationTracker implements LocationListener{

	private GpsTrackingService trackerService;
	private NotificationManager mNM;
	private Dao<Profile, Integer> profileDao;
	private Dao<GpsCoordinates, Integer> gpsDao;
	private Profile profile;
	
	public LocationTracker(GpsTrackingService trackerService, Profile profile) throws SQLException {
		if(trackerService == null || profile == null) throw new IllegalArgumentException();
		this.trackerService = trackerService;
		mNM = (NotificationManager) trackerService.getSystemService(GpsTrackingService.NOTIFICATION_SERVICE);
		CreateDatabase db = new CreateDatabase(trackerService.getApplicationContext());
		profileDao = db.getProfileCoordinatesDao();
		gpsDao = db.getCoordinatesDao();
		this.profile = profile;
		
		profileDao.create(this.profile);
		
	}
	
	@Override
	public void onLocationChanged(Location location) {
		try {
			GpsCoordinates gps = new GpsCoordinates();
			gps.setLatitude(location.getLatitude());
			gps.setLongitude(location.getLongitude());
			gps.setTimestamp(new Date());
			
			gps.setSpeed(location.hasSpeed() ? location.getSpeed() : 0.0f);
			
			gps.setProfileId(profile);
			
			// GPS-Koordinaten persistieren
			gpsDao.create(gps);
			gps = null;
		} catch (Exception e) {
			Log.e("Location Tracker", "Exception", e);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

}

package de.myge.commercial.routetracking;

import java.sql.SQLException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import de.myge.commercial.routetracking.database.Profile;

public class GpsTrackingService extends Service{

	private LocationManager mlocManager;
	private LocationTracker mlocListener;
	private String profileName;
	private Profile profile;
	
	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		GpsTrackingService getService() {
			return GpsTrackingService.this;
		}
	}

	@Override
	public void onCreate() {
		/* Use the LocationManager class to obtain GPS locations */
		mlocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		if (intent == null) throw new IllegalArgumentException("Can't start service.");
		Bundle extras = intent.getExtras();
		profileName = extras.getString("profileName");
		profile = new Profile(profileName, "");
				
		try {
			if (mlocListener == null) {
				mlocListener =  new LocationTracker(this, profile);
			} else {
				
			}
		} catch (SQLException e) {
			Log.e("Route Tracker", "", e);
		}
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
//		mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		mlocManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		mlocManager.removeUpdates(mlocListener);
		mlocManager = null;	
		mlocListener = null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();

}

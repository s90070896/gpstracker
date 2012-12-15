package de.myge.routetracking;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import de.myge.routetracking.database.CreateDatabase;
import de.myge.routetracking.database.GpsCoordinates;
import de.myge.routetracking.database.Profile;

public class DrawRouteThread {
	private Context c;
	private CreateDatabase db;
	private MapView mapView;
	private Profile profile;
	private Handler handler;
	private ArrayList<Profile> list;
	
	public DrawRouteThread(Context c, CreateDatabase db, MapView mapView, Profile profile, Handler handler) {
		if (c == null) throw new IllegalArgumentException("c cannot be null");
		if (db == null) throw new IllegalArgumentException("db cannot be null");
		if (mapView == null) throw new IllegalArgumentException("mapview cannot be null");
		if (profile == null) throw new IllegalArgumentException("profile cannot be null");
		if (handler == null) throw new IllegalArgumentException("handler cannot be null");
		
		this.c = c;
		this.db = db;
		this.mapView = mapView;
		this.profile = profile;
		this.handler = handler;
	}
	public DrawRouteThread(Context c, CreateDatabase db, MapView mapView, Handler handler, ArrayList<Profile> list) {
		if (c == null) throw new IllegalArgumentException("c cannot be null");
		if (db == null) throw new IllegalArgumentException("db cannot be null");
		if (mapView == null) throw new IllegalArgumentException("mapview cannot be null");
		if (handler == null) throw new IllegalArgumentException("handler cannot be null");
		
		this.c = c;
		this.db = db;
		this.mapView = mapView;
		this.handler = handler;
		this.list = list;
	}
	
	public void run() {
		try {
		Thread.sleep(1000);
			if (this.profile != null) {
				handleRoutesOnMapView(this.profile);
			} else if(!list.isEmpty()) {
				mapView.getOverlays().clear();
				for (Profile profile : list) {
					handleRoutesOnMapView(profile);
				}
			} else {
				mapView.getOverlays().clear();
			}
		} catch (Exception e) {
			Log.e(c.getResources().getString(R.string.app_name), e.getLocalizedMessage(), e);
		} finally {
			handler.sendEmptyMessage(0);
		}
	}
	private void handleRoutesOnMapView(Profile profile) {
		List<GpsCoordinates> gpsCoord;
		try {
			gpsCoord = db.getCoordinatesDao().queryBuilder().where().eq("profile_id", profile.getId()).query();
			for(int i = 0; i < gpsCoord.size(); i++) {
				GeoPoint gp1 = new GeoPoint((int)(gpsCoord.get(i).getLatitude()* 1E6), (int)(Double.parseDouble(gpsCoord.get(i).getLongitude()* 1E6 + "")));
				i++;
				GeoPoint gp2;
				if(i < gpsCoord.size()) {
					 gp2 = new GeoPoint((int)(gpsCoord.get(i).getLatitude()* 1E6), (int)(Double.parseDouble(gpsCoord.get(i).getLongitude()* 1E6 + "")));
				} else {
					gp2 = gp1;
				}
				mapView.getOverlays().add(new DrawRoute(gp1, gp2));
			}
		} catch (Exception e) {
			Log.e(c.getResources().getString(R.string.app_name), e.getLocalizedMessage(), e);
		} 
	}
}

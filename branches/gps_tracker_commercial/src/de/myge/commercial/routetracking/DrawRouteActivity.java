package de.myge.commercial.routetracking;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SlidingDrawer;

import com.flurry.android.FlurryAgent;
import com.google.ads.AdView;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.j256.ormlite.dao.Dao;

import de.myge.commercial.routetracking.arrayadapter.Model;
import de.myge.commercial.routetracking.database.CreateDatabase;
import de.myge.commercial.routetracking.database.Profile;
import de.myge.commercial.routetracking.export.ExportActivity;
 
/**
 * MainActivity dessen Hauptansicht GoogleMaps ist. Diese Klasse steuert das
 * Starten sowie das Stoppen des Service {@link GpsTrackingService}, das
 * Zeichnen und das Löschen einer Route, sowie die Anzeige über bereits
 * getrackte Routen in form einer Liste. Die getrackten Routen werden mit einem
 * {@link SlidingDrawer} aufgelistet.
 * 
 * @author Bolle
 * 
 */
public class DrawRouteActivity extends MapActivity {

	private MapView mapView;
	private CreateDatabase db;
	private Button startStopTrack;
	private ProgressDialog myProgressDialog;
	private NotificationManager notificationManager;
	public static final int NOTIFICATION_ID = 1;
	private SharedPreferences mPrefs;
		/** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.show_tracked_route);  
	                
	        // App Bewertung aktivieren
	        AppRater.app_launched(this);        
	        
	        // Instanz vom NotificationManager holen
	        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	        
	        // neue Instanz für den Zugriff auf die DB erzeugen
	        db = new CreateDatabase(getApplicationContext());
			
	        // MapView instanziieren
	        mapView = (MapView) findViewById(R.id.mapView);
	        // Karten Zoomelemente einblenden
	        mapView.setBuiltInZoomControls(false);
	         
	        selectProfileNames();
	        handleTracking();
	        
	    }
	
	@Override
		protected void onResume() {
			super.onResume();
			handleTracking();
		}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		FlurryAgent.onStartSession(this, "DTIGV9KAUKGBE9JHK9XG");
	}
	
	@Override
    protected void onStop() {
    	super.onStop();
    	FlurryAgent.onEndSession(this);
    }
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private void handleTracking() { 
		/*
		 *  Wenn der Button zum Starten und Stoppen vom Tracken von Routen
		 *  noch nicht instanziiert wurde, wird dies gemacht.
		 */ 
		if(startStopTrack == null)
			startStopTrack = (Button) findViewById(R.id.slider_start_stop_btn);
		
		/*
		 * Buttonnamen ermitteln in Abhängigkeit ob der RouteTrackingService schon
		 * läuft oder nicht. 
		 */
		if(isTrackServiceRunning(DrawRouteActivity.this)) {
			startStopTrack.setText(getString(R.string.control_service_stop_btn_txt));
		} else {
			startStopTrack.setText(getString(R.string.control_service_start_btn_txt));
		}
		
		// handelt ob eine neue Route getrackt wird bzw. beendet das Tracken der Route
		startStopTrack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				final Intent service = new Intent(DrawRouteActivity.this, GpsTrackingService.class);
				
				// TODO doppelter Sourcecode.
				if(startStopTrack.getText().equals(getString(R.string.control_service_stop_btn_txt))) {
					
					// Wenn der Service bereits läuft, soll er beendet werden.
					// Wenn der Service nicht läuft, soll der Service gestartet werden.
					if (isTrackServiceRunning(DrawRouteActivity.this)) {
						stopTracking(service);
						
						try {
							RouteTrackingDialog.showDialog(DrawRouteActivity.this, RouteTrackingDialog.DIALOGTYPE_NEW_ROUTE);
							selectProfileNames();
						} catch (Exception e) {
							Log.e(getString(R.string.app_name), e.getLocalizedMessage(), e);
						}
					}
				} else if(startStopTrack.getText().equals(getString(R.string.control_service_start_btn_txt))) {
					// nur wenn GPS aktiviert ist, den Service starten
					if(RouteTrackingDialog.isEnableGPS(DrawRouteActivity.this, getApplicationContext())) {
						service.putExtra("profileName", Calendar.getInstance().getTime() + "");
						service.putExtra("routeDescription", "");
						startService(service);
						startStopTrack.setText(getString(R.string.control_service_stop_btn_txt));
						showNotification(getString(R.string.tracking_service_active));
					}
				}				
			}

			private void stopTracking(final Intent service) {
				stopService(service);
				startStopTrack.setText(getString(R.string.control_service_start_btn_txt));
				notificationManager.cancel(NOTIFICATION_ID);
			}
		});
	}
	
	/**
	 * Prüft ob der {@link GpsTrackingService} läuft oder nicht.
	 * @return Wenn der Service läuft, wird true zurück gegeben, andernfalls false.
	 */
	public static boolean isTrackServiceRunning(Context c) {
		return RouteTrackingDialog.isMyServiceRunning("de.myge.commercial.routetracking.GpsTrackingService", c);
	}
	private Model get(Profile profile, Context c) {
		return new Model(profile, c);
	}
	public void selectProfileNames() {
		try {
			List<Model> listModel = getAllProfiles();
			
			ListView listView = (ListView) findViewById(R.id.listview_route_datasets);
	        
			ArrayAdapter<Model> adapter = new InteractiveArrayAdapter(this, listModel);
			listView.setAdapter(adapter);				
			
		} catch (SQLException e) {
			Log.e(getString(R.string.app_name), e.getLocalizedMessage(),
					e.fillInStackTrace());
		}
	}

	private List<Model> getAllProfiles() throws SQLException {
		final Dao<Profile, Integer> dao = db.getProfileCoordinatesDao();
		List<Profile> list = dao.queryForAll();

		List<Model> listModel = new ArrayList<Model>();
		
		for (Profile profile : list) {
			listModel.add(get(profile, this));
		}
		return listModel;
	}
	
	public void drawRoute(Profile profile) throws SQLException {
		if (profile == null) throw new IllegalArgumentException("profile cannot be null");
		// Display an indeterminate Progress-Dialog
        myProgressDialog = ProgressDialog.show(this,getString(R.string.please_wait), getString(R.string.drawing_route), true);
		DrawRouteThread thread = new DrawRouteThread(this, db, mapView, profile, handler);
		thread.run();
	}

	public void deleteRoute(ArrayList<Profile> list) throws SQLException {
		// Display an indeterminate Progress-Dialog
        myProgressDialog = ProgressDialog.show(this,getString(R.string.please_wait), getString(R.string.drawing_route), true);
		DrawRouteThread thread = new DrawRouteThread(this, db, mapView, handler, list);
		thread.run();
	}
	
	private void showNotification(String text) {
		Notification notification = new Notification(R.drawable.tracker, text, System.currentTimeMillis());
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		
		
		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
		new Intent(getApplicationContext(),DrawRouteActivity.class),
		PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(this, getString(R.string.app_name), text, contentIntent);
		
		notificationManager.notify(NOTIFICATION_ID, notification);
	}
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	myProgressDialog.dismiss();
        }
    };
    
    @Override
	  public void onDestroy() {
	    super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.load_route_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.configuration:
            	Intent i = new Intent(DrawRouteActivity.this, ConfigurationActivity.class);
            	startActivity(i);
            	return true;
            case R.id.routeCharts:
            	i = new Intent(DrawRouteActivity.this, ChartActivity.class);
            	startActivity(i);
            	return true;
            case R.id.export:
            	i = new Intent(DrawRouteActivity.this, ExportActivity.class);
            	startActivity(i);
//			try {
//				List<Model> listModel = getAllProfiles();
//				Export export = new Export(listModel.get(0).getProfile(), db);
//				export.export();
//			} catch (Exception e) {
//				Log.e("GPS-Tracker", e.getLocalizedMessage(), e);
//			}
            	return true;
            default:
            	return false;
        }
    }
}
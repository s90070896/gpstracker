package de.myge.commercial.routetracking;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.j256.ormlite.stmt.QueryBuilder;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import de.myge.commercial.routetracking.database.CreateDatabase;
import de.myge.commercial.routetracking.database.GpsCoordinates;
import de.myge.commercial.routetracking.database.Profile;
import de.myge.commercial.routetracking.errorhandling.ErrorHandling;

public class ChartActivity extends Activity{
	private CreateDatabase db;
	private List<Profile> profiles;
	private AdView adView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chart);
		
		// Wenn ein TrackingService läuft, soll der Zugriff auf die Datenbank unterbunden werden, da
		// es sonst zu einem Programmabsturz kommt.
		if (RouteTrackingDialog.isMyServiceRunning("de.myge.routetracking.GpsTrackingService", getApplicationContext())) {
			ErrorHandling.showError(getApplicationContext(), getResources().getString(R.string.error_visualise_routes));
		} else {
			// Datenbankzugriff holen, damit das Geschwindigkeitschart zu einem Profil geladen werden kann
			db = new CreateDatabase(this);
			
			// Spinner initialisieren. Dieser Spinner dient der Auswahl der getrackten Routen. 
			final Spinner spinner = (Spinner) findViewById(R.id.routes_charts);
			
			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			        String item = (String) parent.getItemAtPosition(pos);
			        
			        // Index ist die ID des Profils, das Ausgewählt wurde
			        int index = 0;
			        for (int i = 0; i < profiles.size(); i++) {
			        	if (profiles.get(i).getProfileName().equals(item)) {
			        		index = profiles.get(i).getId();
			        		break;
			        	}
			        }
			         
					List<GpsCoordinates> gpsCoord;
					try {
	//					gpsCoord = db.getCoordinatesDao().queryBuilder().where().eq("profile_id", index).query();
						QueryBuilder<GpsCoordinates, Integer> qb = db.getCoordinatesDao().queryBuilder();
						qb.where().eq("profile_id", index);
						qb.orderBy("timestamp", true);
						gpsCoord = qb.query();
						if (gpsCoord.size()>0) {
							ArrayList<GraphViewData> graphViewData = new ArrayList<GraphView.GraphViewData>(); 
							for (int i = 0; i < gpsCoord.size(); i++) {
								if (de.myge.commercial.routetracking.settings.Settings.getInstance(ChartActivity.this).isSpeedInKmh()) {
									graphViewData.add(new GraphViewData(i, gpsCoord.get(i).getSpeed()*3.6f));
								} else {
									graphViewData.add(new GraphViewData(i, gpsCoord.get(i).getSpeed()*3.6f*0.6213712f));
								}
							}
							
							// init example series data
							GraphViewSeries exampleSeries = new GraphViewSeries(graphViewData.toArray(new GraphViewData[graphViewData.size()]));
		
							GraphView graphView = new LineGraphView(
									ChartActivity.this // context
									, item // heading
							);
							
							Date start = gpsCoord.get(0).getTimestamp();
							Date end = gpsCoord.get(gpsCoord.size()-1).getTimestamp();
							
							SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm");
							
							graphView.setHorizontalLabels(new String[] {sdf.format(start), sdf.format(end)});  
							
							graphView.addSeries(exampleSeries);
							graphView.setScrollable(true);
							graphView.setScalable(true);
							LinearLayout layout = (LinearLayout) findViewById(R.id.chartLayout);
							layout.removeAllViews();
							layout.addView(spinner);
							layout.addView(graphView);
						}
					} catch (SQLException e) {
						ErrorHandling.showError(getApplicationContext(), e);
					}
					
				}
	
				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
			
			try {
				// Alle Routen auflisten
				profiles = db.getProfileCoordinatesDao().queryForAll();
				// Routennamen in ein Array packen, damit die Routennamen dem Spinner zur Auwahl übergeben werden können 
				String profileNames[] = new String[profiles.size()]; 
				for (int i = 0; i < profiles.size(); i++) {
					profileNames[i] = profiles.get(i).getProfileName();
				}
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, profileNames);
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(adapter);
			} catch (SQLException e) {
				ErrorHandling.showError(getApplicationContext(), e);
			}
		}
	}
}

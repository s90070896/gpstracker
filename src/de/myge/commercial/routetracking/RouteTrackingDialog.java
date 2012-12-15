package de.myge.commercial.routetracking;

import java.sql.SQLException;
import java.text.DecimalFormat;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;

import de.myge.commercial.routetracking.database.CreateDatabase;
import de.myge.commercial.routetracking.database.GpsCoordinates;
import de.myge.commercial.routetracking.database.Profile;

public class RouteTrackingDialog implements Runnable{
	private static Context context;
	private static View view;
	private static Activity activity;
	private static AlertDialog alertDialog;
	public static final int DIALOGTYPE_NEW_ROUTE = 1;
	public static final int DIALOGTYPE_ROUTE_PROPERTIES = 2;
	public static Profile profile;
	private static long durationTime;
	private static float calculateDistance;
	private static float topSpeedFloat;
	private static ProgressDialog myProgressDialog;
	private static TextView duration;
	private static TextView distance;
	private static TextView avgSpeed;
	private static TextView topSpeed;
	private static TextView routeTrackName;
	private static CreateDatabase db;
	
	public static void showDialog(Context c, int dialogType, Profile profile) throws SQLException, InterruptedException {
		if (profile == null) throw new IllegalArgumentException("profile cannot be null");
		RouteTrackingDialog.profile = profile;
		// neue Instanz für den Zugriff auf die DB erzeugen
		db = new CreateDatabase(c);
		// Display an indeterminate Progress-Dialog
        myProgressDialog = ProgressDialog.show(c, c.getResources().getString(R.string.please_wait), c.getResources().getString(R.string.calculate_information), true);
		showDialog(c, dialogType);
	}
	
	public static void showDialog(Context c, int dialogType) throws SQLException, InterruptedException {
        if (c == null) throw new IllegalArgumentException("context cannot be null");
        context = c;
        // neue Instanz für den Zugriff auf die DB erzeugen
     	db = new CreateDatabase(c);
        activity = (Activity) context;
        
        /*  Auswahl zwischen den verschiedenen Dialogtypen.
         *  Aktuell existieren nur 2 verschiedene Dialogtypen:
         *  1. Neue Route:
         *  	Eine Neue Route soll getrackt werden.
         *  2. Routendetails:
         *  	Hier werden Routendetails wie Topspeed,
         *  	Durchschnittsgeschwindigkeit, Distanz und
         *  	Dauer angezeigt. Zusätzlich kann über dieses
         *  	Menü auch die Route gelöscht werden.
         */
        
        Dialog dialog;
		switch(dialogType) {
        case DIALOGTYPE_NEW_ROUTE:
        	
        	dialog = new Dialog(context);
        	
        	dialog.setContentView(R.layout.new_track);
        	// use a custom View defined in xml
        	view = LayoutInflater.from(context).inflate(R.layout.new_track, (ViewGroup) activity.findViewById(R.id.layout_root));     
        	createAndHandleDialogButton();
        	
        	AlertDialog.Builder builder = new AlertDialog.Builder(c); 
            builder.setView(view);

            alertDialog = builder.create();
            
            alertDialog.setTitle(activity.getResources().getString(R.string.enter_route_name));
            
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(alertDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.FILL_PARENT;
            lp.height = WindowManager.LayoutParams.FILL_PARENT;
            alertDialog.show();      
            alertDialog.getWindow().setAttributes(lp);
            
        	break;
        case DIALOGTYPE_ROUTE_PROPERTIES:
        	
        	dialog = new Dialog(context);
        	
        	dialog.setContentView(R.layout.route_details_dialog);
        	
        	
        	// use a custom View defined in xml
        	view = LayoutInflater.from(context).inflate(R.layout.route_details_dialog, (ViewGroup) activity.findViewById(R.id.root_route_details));
        	routeTrackName = (TextView) view.findViewById(R.id.track_name_string);
        	routeTrackName.setText(profile.getProfileName());
        	
        	duration = (TextView) view.findViewById(R.id.duration_time);
        	distance = (TextView) view.findViewById(R.id.distance_km);
        	avgSpeed = (TextView) view.findViewById(R.id.avg_speed);
        	topSpeed = (TextView) view.findViewById(R.id.top_speed);        	
			
        	Button deleteBtn = (Button) view.findViewById(R.id.delete_track);
        	// Listener für den Löschbutton erzeugen
        	deleteBtn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(DialogInterface dialog, int which) {
					        switch (which){
					        // Wenn das Löschen bestätigt wurde, wird das Profil gelöscht.
					        case DialogInterface.BUTTON_POSITIVE:
					        	deleteProfile();
					            break;

					        // Wenn nein gedrückt wurde, wird nichts gemacht.
					        case DialogInterface.BUTTON_NEGATIVE:
					            //No button clicked
					            break;
					        }
					    }
					};

					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage(context.getResources().getString(R.string.delete_confirm_message)).setPositiveButton(context.getResources().getString(R.string.yes), dialogClickListener)
					    .setNegativeButton(context.getResources().getString(R.string.no), dialogClickListener).show();
					
				}

			});
        	
        	 Thread thread = new Thread(new RouteTrackingDialog());
             thread.start();
        	
        	break;
        	default:
        }
	}
	
	private static void deleteProfile() {	
		try {
			// Profil aus der Datenbank entfernen
			Dao<Profile, Integer> list = db.getProfileCoordinatesDao();
			list.delete(profile);
			// Routenliste aktualisieren
			Activity activity = (Activity) context;
			DrawRouteActivity ac = (DrawRouteActivity) activity;
			// Dialog schließen, da der Datensatz nicht mehr in der Datenbank existiert
			alertDialog.cancel();
			ac.selectProfileNames();
		} catch (Exception e) {
			Log.e(context.getResources().getString(R.string.app_name), e.getLocalizedMessage(), e);
		}
	}
	private static void createAndHandleDialogButton() {
		
		final Button dialogButton = (Button) view.findViewById(R.id.start_tracking_btn);
//		final Button shareOnFacebook = (Button) view.findViewById(R.id.share_information_on_facebook);
		
		// Listener auf den Button setzen, damit der Service gestartet und beendet werden kann
        dialogButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// Objekt um den Routennamen zu ermitteln
				EditText trackName = (EditText) view.findViewById(R.id.route_name);
				// Nur wenn ein Routenname eingegeben wurde
				if(trackName != null && !trackName.getText().toString().equals("")) {
					String descriptionRoute = "";
					// aufgezeichnete Route einen Namen geben und die Beschreibung speichern
					try {
						String maxId = "update profile set profileName='"+trackName.getText()+"', description='"+descriptionRoute+"' where id = (select max(id) from profile)";
						db.getCoordinatesDao().queryRaw(maxId);
						DrawRouteActivity activity = (DrawRouteActivity) context;
						activity.selectProfileNames();
						alertDialog.cancel();
					} catch (SQLException e) {
						Log.e(context.getResources().getString(R.string.app_name), e.getLocalizedMessage(), e);
					}
				} else {
					Toast.makeText(context, context.getResources().getString(R.string.track_name_exist_error_message), Toast.LENGTH_LONG).show();
				}
			}
		});
    }
	
    /**
     * Diese Methode pr�ft, ob ein bestimmer Service l�uft.
     * @return Ist das der Fall, wird true zur�ck gegeben. andernfalls false.
     */
    public static boolean isMyServiceRunning(String serviceName, Context c) {
    	if(serviceName.equals("") || c == null) throw new IllegalArgumentException();
        ActivityManager manager = (ActivityManager) c.getSystemService(Activity.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isEnableGPS(Activity activity, Context context){
        String provider = Settings.Secure.getString(activity.getContentResolver(),
          Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
           if(provider.contains("gps")){
               //GPS Enabled
            Toast.makeText(context, context.getResources().getString(R.string.gps_enabled) + " " + provider,
              Toast.LENGTH_LONG).show();
            return true;
           }else{
        	   Toast.makeText(context, context.getResources().getString(R.string.gps_disabled), Toast.LENGTH_LONG).show();
        	   Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        	   activity.startActivity(intent);
               return false;
           }

       }

	@Override
	public void run() {
		durationTime = 0;
		calculateDistance = 0;
		topSpeedFloat = 0;
		try {
			durationTime = GpsCoordinates.calculateDurationOfRoute(profile, context);
	    	calculateDistance = GpsCoordinates.calculateDistance(profile, context);
	    	topSpeedFloat = GpsCoordinates.calculateTopSpeed(profile, context)*3.6f;
		} catch (Exception e) {
			Log.e(context.getResources().getString(R.string.app_name), e.getLocalizedMessage(), e);
		} finally {
			handler.sendEmptyMessage(0);
		}
	}
	
	private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
	        	myProgressDialog.dismiss();
	        	duration.setText(durationTime + " " + context.getResources().getString(R.string.minutes));
	        	DecimalFormat f = new DecimalFormat("#0.00"); 
	        	
	    		
	        	if (de.myge.commercial.routetracking.settings.Settings.getInstance(activity).isSpeedInKmh()) {
	        		distance.setText(f.format((calculateDistance/1000)) + " km");
	        	} else {
	        		distance.setText(f.format(((calculateDistance/1000) * 0.6213712)) + " miles");
	        	}
	        	
	    		if (calculateDistance > 0 && durationTime > 0) {
	    			if (de.myge.commercial.routetracking.settings.Settings.getInstance(activity).isSpeedInKmh()) {
	    				avgSpeed.setText((int)(GpsCoordinates.calculateAvgSpeed(profile, context)*3.6) + " km/h");
	    			} else {
	    				avgSpeed.setText((int)(GpsCoordinates.calculateAvgSpeed(profile, context)*3.6)* 0.6213712 + " mp/h");
	    			}
	    		}

	    		
	    		if (topSpeedFloat > 0) {
	    			if (de.myge.commercial.routetracking.settings.Settings.getInstance(activity).isSpeedInKmh()) {
	    				topSpeed.setText((int)topSpeedFloat + " km/h");
	    			} else {
	    				topSpeed.setText((int)topSpeedFloat * 0.6213712 + " mp/h");
		    		}
	    		}
	    		
	    		
	    		AlertDialog.Builder builder = new AlertDialog.Builder(context); 
	            builder.setView(view);
	            alertDialog = builder.create();
	            alertDialog.show();
        	} catch (Exception e) {
        		Log.e(context.getResources().getString(R.string.app_name), e.getLocalizedMessage(), e);
        	}
        }
	};
}
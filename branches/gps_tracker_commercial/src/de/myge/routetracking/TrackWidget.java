package de.myge.routetracking;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class TrackWidget extends AppWidgetProvider {
	 
	private static final String ACTION_WIDGET_RECEIVER = "ActionRecieverWidget";
	private RemoteViews views = new RemoteViews("de.myge.routetracking", R.layout.widgetlayout);
	
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
            //called when widgets are deleted
            //see that you get an array of widgetIds which are deleted
            //so handle the delete of multiple widgets in an iteration
            super.onDeleted(context, appWidgetIds);
           
    }

    @Override
    public void onDisabled(Context context) {
            super.onDisabled(context);
            //runs when all of the instances of the widget are deleted from
            //the home screen
            //here you can do some setup
    }

    @Override
    public void onEnabled(Context context) {
            super.onEnabled(context);
            //runs when all of the first instance of the widget are placed
            //on the home screen
    }

    @Override
    public void onReceive(Context context, Intent intent) {
            //all the intents get handled by this method
            //mainly used to handle self created intents, which are not
            //handled by any other method
           
           
            //the super call delegates the action to the other methods
           
            //for example the APPWIDGET_UPDATE intent arrives here first
            //and the super call executes the onUpdate in this case
            //so it is even possible to handle the functionality of the
            //other methods here
            //or if you don't call super you can overwrite the standard
            //flow of intent handling
            super.onReceive(context, intent);
            
            if(intent.getAction().equals(ACTION_WIDGET_RECEIVER)) {
            	 chooseWidgetImage(context);
            }
            AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, TrackWidget.class), views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                    int[] appWidgetIds) {
            //runs on APPWIDGET_UPDATE
            //here is the widget content set, and updated
            //it is called once when the widget created
            //and periodically as set in the metadata xml
           
            //the layout modifications can be done using the AppWidgetManager
            //passed in the parameter, we will discuss it later
           
            //the appWidgetIds contains the Ids of all the widget instances
            //so here you want likely update all of them in an iteration
           
            //we will use only the first creation run
            super.onUpdate(context, appWidgetManager, appWidgetIds);
            
            chooseWidgetImage(context);
            
            for(int i = 0; i < appWidgetIds.length; i++) {
    			int appWidgetId = appWidgetIds[i];
    	        Intent intent = new Intent(context, TrackWidget.class);  
    	        intent.setAction(ACTION_WIDGET_RECEIVER);  
    	        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);  
    			views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent);
    			appWidgetManager.updateAppWidget(appWidgetId, views);  
    		}
    }

    /**
     * Prüft, ob der TrackingServie schon läuft oder nicht. 
     * Wenn der Service schon läuft, wird das Widget farbig angezeigt.
     * Wenn der Service nicht läuft, wird das Widget in schwarz Weiß angezeigt. 
     * @param context
     */
	private void chooseWidgetImage(Context context) {
		if (DrawRouteActivity.isTrackServiceRunning(context)) {
			views.setImageViewResource(R.id.widget_icon, R.drawable.tracker);
		} else {
			views.setImageViewResource(R.id.widget_icon, R.drawable.tracker_blackwhite);
		}
	}

}
package de.myge.routetracking.export;

import java.sql.SQLException;
import java.util.List;

import android.util.Log;
import de.myge.routetracking.database.CreateDatabase;
import de.myge.routetracking.database.GpsCoordinates;
import de.myge.routetracking.database.Profile;

public class Export {
	private final Profile profile;
	private final CreateDatabase db;
	public Export(Profile profile, CreateDatabase db) {
		if (profile == null) throw new IllegalArgumentException("Profile cannot be null");
		if (db == null) throw new IllegalArgumentException("Database cannot be null");
		this.profile = profile;
		this.db = db;
	}
	/**
	 * Exportiert eine Route als KML Datei auf das Dateisystem.
	 * @return
	 * @throws SQLException 
	 */
	public void export() throws SQLException {
//		final Kml kml = KmlFactory.createKml();
//		final Document document = kml.createAndSetDocument().withName("LineStyle.kml").withOpen(true);
//		final Style style = document.createAndAddStyle().withId("linestyleExample");
//
//		style.createAndSetLineStyle()
//		.withColor("7f0000ff")
//		.withWidth(4.0d);

		List<GpsCoordinates> gpsCoord = db.getCoordinatesDao().queryBuilder().where().eq("profile_id", profile.getId()).query();
		
		Log.i("GPS Tracker", gpsCoord.get(0).getLatitude() + "");
		
//		document.createAndAddPlacemark().withName("LineStyle Example").withStyleUrl("#linestyleExample")
//		.createAndSetLineString().withExtrude(true).withTessellate(true)
//		.addToCoordinates("-122.364383,37.824664,0")
//		.addToCoordinates("-122.364152,37.824322,0");
	}
}

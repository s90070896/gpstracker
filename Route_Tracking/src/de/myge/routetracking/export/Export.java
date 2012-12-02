package de.myge.routetracking.export;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Route;
import org.alternativevision.gpx.beans.Track;
import org.alternativevision.gpx.beans.Waypoint;

import android.os.Environment;

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
	 * @throws TransformerException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	public void export() throws SQLException, ParserConfigurationException, TransformerException, IOException {
		List<GpsCoordinates> gpsCoord = db.getCoordinatesDao().queryBuilder().where().eq("profile_id", profile.getId()).query();
		GPXParser p = new GPXParser();
		FileOutputStream out = new FileOutputStream("/mnt/sdcard/" + profile.getProfileName() + ".gpx");
		GPX gpx = new GPX();
		gpx.setCreator("Myge");
		gpx.setVersion("1.0");
		for(GpsCoordinates gps : gpsCoord) {			
			Waypoint waypoint = new Waypoint();
			waypoint.setLatitude(gps.getLatitude());
			waypoint.setLongitude(gps.getLongitude());
			gpx.addWaypoint(waypoint);
		}
		gpx.addRoute(new Route());
		gpx.addTrack(new Track());
		p.writeGPX(gpx, out);
		out.close();
	}
}

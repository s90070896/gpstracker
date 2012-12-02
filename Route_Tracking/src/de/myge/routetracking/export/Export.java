package de.myge.routetracking.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import android.os.Environment;
import android.util.Log;
import de.myge.routetracking.database.CreateDatabase;
import de.myge.routetracking.database.GpsCoordinates;
import de.myge.routetracking.database.Profile;

public class Export {
	
	private String kmlFileStart = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:ns2=\"http://www.w3.org/2005/Atom\" xmlns:ns3=\"urn:oasis:names:tc:ciq:xsdschema:xAL:2.0\" xmlns:ns4=\"http://www.google.com/kml/ext/2.2\">" +
				"<Document>" +
					"<name>LineStyle.kml</name>" +
					"<open>true</open>" +
					"<Style id=\"linestyleExample\">" +
						"<LineStyle>" +
							"<color>7f0000ff</color>" +
							"<width>4.0</width>" +
						"</LineStyle>" +
					"</Style>" +
					"<Placemark>" +
					"<name>LineStyle Example</name>" +
					"<styleUrl>#linestyleExample</styleUrl>" +
						"<LineString>" +
							"<extrude>true</extrude>" +
							"<tessellate>true</tessellate>" +
							"<altitudeMode>clampToGround</altitudeMode>" +
							"<coordinates>";
							
	private String kmlFileEnd = "<coordinates>" +
								"</LineString>" +
								"</Placemark>" +
							"</Document>" +
						"</kml>";
	
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
		StringBuffer gpsBuffer = new StringBuffer();
		List<GpsCoordinates> gpsCoord = db.getCoordinatesDao().queryBuilder().where().eq("profile_id", profile.getId()).query();
//		"<coordinates>-122.364383,37.824664 -122.364152,37.824322</coordinates>" +
		for(GpsCoordinates gps : gpsCoord) {
			gpsBuffer.append(gps.getLatitude() + "," + gps.getLongitude() + " ");
		}
		
		String filename = "filename.txt";
		File file = new File(Environment.getExternalStorageDirectory(), filename);
		FileOutputStream fos;
		String output = kmlFileStart + gpsBuffer.toString() + kmlFileEnd;
		byte[] data = output.getBytes();
		try {
		    fos = new FileOutputStream(file);
		    fos.write(data);
		    fos.flush();
		    fos.close();
		} catch (FileNotFoundException e) {
		    // handle exception
		} catch (IOException e) {
		    // handle exception
		}
	}
}

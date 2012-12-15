package de.myge.routetracking.database;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class CreateDatabase extends OrmLiteSqliteOpenHelper {

	
	
	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "routeTrack.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 10;

	// this uses h2 but you can change it to match your database
	String databaseUrl = "jdbc:h2:mem:"+DATABASE_NAME;
	
	// the DAO object we use to access the GpsCoordinates table
	private Dao<Profile, Integer> simpleDao = null;
	
	// the DAO object we use to access the GpsCoordinates table
		private Dao<GpsCoordinates, Integer> gpsDao = null;

	public CreateDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(CreateDatabase.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, Profile.class);
			TableUtils.createTable(connectionSource, GpsCoordinates.class);

		} catch (SQLException e) {
			Log.e(CreateDatabase.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try { 
			Log.i(CreateDatabase.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Profile.class, true);
			TableUtils.dropTable(connectionSource, GpsCoordinates.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (Exception e) {
			Log.e(CreateDatabase.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the Database Access Object (DAO) for our GpsCoordinates class. It will create it or just give the cached
	 * value
	 */
	public Dao<Profile, Integer> getProfileCoordinatesDao() throws java.sql.SQLException {
		if (simpleDao == null) {
			simpleDao = getDao(Profile.class);
		}
		return simpleDao;
	}
	

	/**
	 * Returns the Database Access Object (DAO) for our GpsCoordinates class. It will create it or just give the cached
	 * value
	 */
	public Dao<GpsCoordinates, Integer> getCoordinatesDao() throws java.sql.SQLException {
		if (gpsDao == null) {
			gpsDao = getDao(GpsCoordinates.class);
		}
		return gpsDao;
	}
	
	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		simpleDao = null;
		gpsDao = null;
	}
}
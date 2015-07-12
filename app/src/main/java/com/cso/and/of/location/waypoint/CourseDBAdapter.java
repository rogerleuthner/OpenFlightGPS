/*
 * ******************************************************************************
 *  * OpenFlightGPS is Copyright 2009-2015 by Roger B. Leuthner
 *  *
 *  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * Commercial Distribution License
 *  * If you would like to distribute OpenFlightGPS (or portions thereof) under a license other than
 *  * the "GNU General Public License, version 2", contact Roger B. Leuthner through GitHub.
 *  *
 *  * GNU Public License, version 2
 *  * All distribution of OpenFlightGPS must conform to the terms of the GNU Public License, version 2.
 *  *****************************************************************************
 */

package com.cso.and.of.location.waypoint;

import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.util.GeoPoint;

import com.cso.and.of.config.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Public static methods do all database handling; private methods expect
 * input database and caller to handle database close.
 * 
 * Public methods handle the global "course" object maintenance as well as
 * returning requested data directly in some cases.
 */
public class CourseDBAdapter {
	private static final String DATABASE_NAME = "openflight";

	// TODO maybe some defines for the table/column names
	public static final String ID_COLUMN = "rowid";
	
	private static final int DATABASE_VERSION = 5;
	
	public static String UNSAVED = Integer.toString( 0 );		
	
	// take advantage of the builtin 'rowid' as the primary key of the tables
	
	// freakn sqlite version may not support foreign keys
	
	// order of create is important for the foreign keys
	private static final String AIRCRAFT_CREATE = "CREATE TABLE aircraft ("
		+ "name TEXT UNIQUE, "
		+ "speed INTEGER )";
	
	private static final String NAVAID_CREATE = "CREATE TABLE navaid ("
		+ "name TEXT UNIQUE, "
		+ "desc TEXT, "
		// ...
		+ "latitude DOUBLE, " 
		+ "longitude DOUBLE )";	
	
	private static final String COURSE_CREATE = "CREATE TABLE course ("
			+ "name TEXT UNIQUE, "
			+ "aircraft_id integer )";
//			+ "FOREIGN KEY(aircraft_id) REFERENCES aircraft(ROWID) ON DELETE CASCADE )";  // 1-1 

	// note not unique on text - could be multiple with same name, but different foreign keys
	// so key using either the rowid, or the name + course_id; but since we don't do integrity on the compound key,
	// prefer rowid
	private static final String WAYPOINT_CREATE = "CREATE TABLE waypoint ("
		+ "name TEXT, "
		+ "desc TEXT, "
		+ "waypoint_order INTEGER, "
		+ "latitude DOUBLE, "  // 0 if navaid_id is not null
		+ "longitude DOUBLE, " // 0 if navaid_id is not null
		+ "course_id integer NOT NULL, "
		+ "navaid_id integer )";
//		+ "FOREIGN KEY(course_id) REFERENCES course(ROWID) ON DELETE CASCADE, "   // many-1
//		+ "FOREIGN KEY(navaid_id) REFERENCES navaid(ROWID) ON DELETE CASCADE )";	// 1-1, null if lat/lon waypoint

	
	// TODO create indexes on the name fields as those are probably the primary search keys
	// TODO the navaids should be loaded into the database enmasse, then referenced by waypoint
	
	
	/**
	 * Deletes any existing course with this name and inserts a new one,
	 * or creates from scratch if doesn't exist already
	 * 
	 * @param course
	 */
	public static final void saveCourse( String name ) {
		Utils.getUtils().getOpenFlight().getCurrentCourse().setName( name );
		saveCourse();
	}
	
	/**
	 * Create a new course, leaving any existing courses untouched.  If user saves to
	 * an existing name, the course is not changed.
	 * 
	 * @param name
	 */
	public static final void saveCourseAs( String name ) {
    	Course c = Utils.getUtils().getOpenFlight().getCurrentCourse();
    	c.setId( Integer.parseInt( CourseDBAdapter.UNSAVED ) );
    	c.saveCourse( name );		
	}
	
	public static final void saveCourse() {
		SQLiteDatabase db = null;
		Course course = Utils.getUtils().getOpenFlight().getCurrentCourse();
		
		try {		
			db = open();
			
			long course_id;
			
			db.beginTransaction();
	
			/* if user intended to save the course under a new name (leaving original
			 * intact), this will have been 'set'
			 */
			if ( ! course.getId().equals( UNSAVED ) )
				deleteCourse( db, Long.parseLong( course.getId() ) );
			
			course_id = createCourse( db, course.getName(), null );
			
			int i = 1;
			for( WaypointItem item : course.getWaypoints() ) {
				ContentValues vals = new ContentValues();
				vals.put( "name", item.mTitle );
				vals.put( "desc", item.mDescription );
				vals.put( "latitude", item.getPoint().getLatitudeE6() );
				vals.put( "longitude", item.getPoint().getLongitudeE6() );
				vals.put( "course_id", course_id );
				vals.put( "waypoint_order", i++ );
				db.insert( "waypoint", null, vals );
			}
			db.setTransactionSuccessful();

			course.setDirty( false );
			
		} finally {
			db.endTransaction();
			close( db );
		}
	}	

	
	/**
	 * Delete the flight with a given name
	 * 
	 * @param String course name
	 */
	public static void deleteCourse( String name ) {		
		SQLiteDatabase db = null;
		
		try {
			db = open();
			db.beginTransaction();		
			deleteCourse( db, getRowId( db, "course", name ) );
			db.setTransactionSuccessful();

		} finally {
			db.endTransaction();
			close( db );
		}
	}		
	
	
	/**
	 * Delete the current flight
	 * 
	 * @param long course id
	 */
	public static void deleteCourse( ) {		
		SQLiteDatabase db = null;
		
		try {
			db = open();
			db.beginTransaction();
			deleteCourse( db, Long.parseLong( Utils.getUtils().getOpenFlight().getCurrentCourse().getId() ) );
			db.setTransactionSuccessful();

		} finally {
			db.endTransaction();
			close( db );
			
	 		// reset our current course
	 		Utils.getUtils().getOpenFlight().getCurrentCourse().reset();	 				
		}
	}	
	
	/**
	 * Reset and then read a course from the database identified by the unique name into the current course.
	 * Return a pointer to the course.
	 * 
	 * TODO finally and cleanup
	 * 
	 * @param name
	 * @return
	 */
	public static Course getCourse( String name ) {		
		SQLiteDatabase db = null;
		Course course = null;
		
		try {			
			db = open();
			
			course = Utils.getUtils().getOpenFlight().getCurrentCourse();
			Cursor c = db.query( "course", new String[]{"rowid","name"}, "name = ?", new String[]{name}, null, null, null );	
			course.reset();
			c.moveToFirst();
			course.setId( c.getLong( 0 ) );
			course.setName( c.getString( 1 ) );
			c.close();
			
			c = db.query( "waypoint", new String[]{"rowid","name","desc","waypoint_order","latitude","longitude"},
							"course_id = ?", new String[]{course.getId()}, null, null, "waypoint_order ASC" );
			
			for( c.moveToFirst(); ! c.isAfterLast() ; c.moveToNext() ) {
				GeoPoint p = new GeoPoint( c.getInt( 4 ), c.getInt( 5 ) );
				WaypointItem i = new WaypointItem( Long.toString( c.getLong( 0 ) ), c.getString( 1 ), c.getString( 2 ), p );
				course.getWaypoints().add( i );
			}
	
			c.close();
		} finally {
			close( db );
		}
		return course;
	}
		
	public static List<Course>getCourses() {
		ArrayList<Course>courses = new ArrayList<Course>();
		SQLiteDatabase db = null;
		
		try {
			db = open();
			Cursor c = db.query( "course", new String[]{"rowid","name"}, null, null, null, null, "name DESC" );
			for( c.moveToFirst(); ! c.isAfterLast(); c.moveToNext() )
				courses.add( new Course( c.getLong( 0 ), c.getString( 2 ) ) );
	
			c.close();
		} finally {
			close( db );
		}
		return courses;
	}
	
	public static List<String>getCourseNames() {
		ArrayList<String>courses = new ArrayList<String>();
		SQLiteDatabase db = null;
		try {
			
			db = open();
			Cursor c = db.query( "course", new String[]{"name"}, null, null, null, null, "name DESC" );
			for( c.moveToFirst(); ! c.isAfterLast(); c.moveToNext() )
				courses.add( c.getString( 0 ) );

			c.close();
		} finally {
			close( db );
		}
		return courses;
	}	


	private static final SQLiteDatabase open() throws SQLException {
		return new DatabaseHelper( Utils.getUtils().getApplicationContext() ).getWritableDatabase();
	}

	private static final void close( SQLiteDatabase db ) {
		if ( db != null && db.isOpen() )
			db.close();
	}	
	
	// NOTE FOLLOWING PRIVATE METHODS DO NOT DO DATABASE OR TRANSACTION HANDLING
	
	// Delete the flight with a given id
	private static void deleteCourse( SQLiteDatabase db, long courseId ) {
		db.delete( "course", "rowid = ?", new String[] {Long.toString( courseId )} );
		db.delete( "waypoint", "course_id = ?", new String[] {Long.toString( courseId )} );
	}	

	// Create a course with no waypoints; will overwrite existing course of same name if there.
	private static final long createCourse( SQLiteDatabase db, String courseName, String aircraftName ) {
		ContentValues vals = new ContentValues();
		vals.put( "name", courseName );
		return db.insert( "course", null, vals );
	}
	
	// Find rowid of the row keyed with column "name" in table
	private static final long getRowId( SQLiteDatabase db, String table, String name ) {		
		Cursor c = db.query( table, new String[] {"rowid"}, "name = ?", new String[] {name}, null, null, null );
		c.moveToFirst();
		long rowId = c.getInt( 0 );
		c.close();

		return rowId;
	}		
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL( COURSE_CREATE );
			db.execSQL( WAYPOINT_CREATE );
			db.execSQL( NAVAID_CREATE );
			db.execSQL( AIRCRAFT_CREATE );
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//			if (oldVersion >= minCompatibleDBVersion)
//				Log.d(TAG, "Skipping DB upgrade, schema has not changed.");
//			else {
//				Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
//						+ ", which will destroy all old data");
//
//				// Not needed, the docs say the baseclass will make transactions
//				// db.beginTransaction();
//				db.execSQL("DROP TABLE IF EXISTS " + LOCINFO_TABLE);
//				db.execSQL("DROP TABLE IF EXISTS " + FLTINFO_TABLE);
//				db.execSQL("DROP TABLE IF EXISTS " + WAYPOINT_TABLE);
//				db.execSQL("DROP TABLE IF EXISTS " + ROUTE_TABLE);
//				db.execSQL("DROP TABLE IF EXISTS " + RCONTENTS_TABLE);
//
//				onCreate(db);
//				// db.setTransactionSuccessful();
//				// db.endTransaction();
//			}
		}
	}
}




///**
// * Add simple (lat/lon) waypoint to 'the end' of a course
// * 
// * @param courseId
// * @param name
// * @param lon
// * @param lat
// * @return waypoint rowid
// */
//public final void addCourseWaypoint( long courseId, String name, double lon, double lat ) {
//
//	ContentValues vals = new ContentValues();
//	vals.put( "name", name );
//	vals.put( "course_id", courseId );
//	vals.put( "latitude", lat );
//	vals.put( "longitude", lon );
//
//	// use the kludgy exec so we can subselect to get the order
//	db.execSQL(
//			" INSERT INTO waypoint( name, waypoint_order, latitude, longitude, course_id, navaid_id ) "
//			+ " VALUES ( '" + name + "', " 
//						+ " ( SELECT MAX( waypoint_order ) + 1 FROM waypoint WHERE courseId = " + courseId + "), "
//						+ lat + ", "
//						+ lon + ", "
//						+ courseId + ", "
//						+ " null ) "							
//			);
//}

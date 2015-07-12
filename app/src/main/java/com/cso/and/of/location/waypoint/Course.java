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

import java.util.List;

import android.view.View;
import android.widget.Toast;

import com.cso.and.of.config.Utils;

public class Course {

		long id = 0;
		private String name;
		private List<WaypointItem>waypoints;
		boolean dirty = false;
		private static final String UNSAVED_COURSE = "unsaved";
		
		public Course( long id, String name ) {
			this.id = id;		
			this.name = name;
		}
		public Course(  ) {
			this.name = UNSAVED_COURSE;
		}
		
		public void reset( ) {
			waypoints.clear();
			id = 0;
			name = UNSAVED_COURSE;
			dirty = false;
		}
		
		public String getId() { return Long.toString( id ); }
		public void setId( long id ) { this.id = id; }
		public String getName() { return name; }
		public void setName( String name ) { this.name = name; }
		public void setWaypoints( List<WaypointItem> waypoints ) { this.waypoints = waypoints; }		
		public List<WaypointItem>getWaypoints() { return waypoints; }
		public boolean isDirty() { return dirty; }
		public void setDirty( boolean in ) { dirty = in; }
		
		
		/// course handling routines that require any extraneous logic related to the user interface 
		
	 	public void saveCourse( String courseName ) {
	 		if ( waypoints == null || waypoints.size() == 0 ) { 			
	 			Toast.makeText(Utils.getUtils().getApplicationContext(), "No waypoints to save", Toast.LENGTH_SHORT ).show(); 			
	 		} else { 			
	 			CourseDBAdapter.saveCourse( courseName );
	 		}
	 	}
	 	
	 	public void saveCourseAs( String courseName ) {
	 		if ( waypoints == null || waypoints.size() == 0 ) { 			
	 			Toast.makeText(Utils.getUtils().getApplicationContext(), "No waypoints to save", Toast.LENGTH_SHORT ).show(); 			
	 		} else { 			
	 			CourseDBAdapter.saveCourseAs( courseName );
	 		}
	 	}
	 	
	 	public Course loadCourse( String courseName, View view ) {
	 		Course course = CourseDBAdapter.getCourse( courseName );
	 		view.invalidate();
	 		return course;
	 	}
	 	
	 	public void clearCourse( View view ) {
	 		Utils.getUtils().getOpenFlight().getCurrentCourse().reset();
	 		view.invalidate();
	 	} 	
	 	
	 	public void deleteCourse( View view ) { 		
	 		CourseDBAdapter.deleteCourse();
			view.invalidate();
	 	}		
	 	
		public void deleteWaypoint( WaypointItem item, View view ) {
			this.waypoints.remove(item);
			Utils.getUtils().getOpenFlight().getCurrentCourse().setDirty( true );
			view.invalidate();
		}
		
		public void moveWaypoint( WaypointItem waypoint, int latE6, int lonE6, View view ) {
			for( WaypointItem it : Utils.getUtils().getOpenFlight().getCurrentCourse().getWaypoints() ) {
				if ( it.equals( waypoint ) ){
					it.mGeoPoint.setLatitudeE6( latE6 );
					it.mGeoPoint.setLongitudeE6( lonE6 );
					Utils.getUtils().getOpenFlight().getCurrentCourse().setDirty( true );
					break;
				}
			}
			view.invalidate();
		}
		
}

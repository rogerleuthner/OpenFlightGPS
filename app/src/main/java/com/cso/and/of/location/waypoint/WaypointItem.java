
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

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlayItem;

import android.app.Activity;


public class WaypointItem extends OpenStreetMapViewOverlayItem {

	/* TODO temp string keys for passing info into WaypointInfoActivity */
	public static String TITLE = "title";
	public static String DESC = "desc";
	public static String LAT = "lat";
	public static String LON = "lon";
	public static String COURSE = "course";
	public static String NEXT_WAY_LON = "nextLon";
	public static String NEXT_WAY_LAT = "nextLat";	
	public static String NEXT_WAY_TITLE = "nextWaypoint";
	
	private String id = CourseDBAdapter.UNSAVED;
	
	/**
	 * Reading/updating waypoints
	 * 
	 * @param id
	 * @param name
	 * @param gp
	 */
	public WaypointItem( String id, String name, String desc, GeoPoint gp ) {
		super( name, desc, gp );
		this.id = id;
	}
	/**
	 * New waypoint, no id yet
	 * 
	 * @param name
	 * @param gp
	 */
	public WaypointItem(String name, String desc, GeoPoint gp) {
		super( name, desc, gp );
	}
	
	// TODO base type??
	public String getId() { return id; }

	public void handleTap(Activity context) {
	}
}

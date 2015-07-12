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

package com.cso.and.of;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Application;
import android.content.Intent;

import com.cso.and.of.config.Utils;
import com.cso.and.of.location.waypoint.Course;
import com.cso.and.of.location.waypoint.WaypointItem;
import com.cso.and.of.ui.SelectMapActivity;
import com.cso.and.of.util.PayPay;

/**
 * The application base.  Sets up the static Utils to have pointer to this base for those uses that are not
 * inside an activity or service (for example Fetcher, which does not extend any android classes).
 * 
 * Also contains general purpose data passing (getData, setData).  Typing must be handled by the users.
 * 
 * Doubles as static defines container.
 * 
 * @author Roger
 *
 */

public class OpenFlight extends Application {
	
	public static final String AUTHOR = "©Roger B. Leuthner 2010-2015";
	
	// note this string has to match that embedded in the code as well
	public final static String KEY_ENTRY = "I Agree";
	public static final int INSTALL_CODE = 4403;
	public static final int OK_CODE = 4402;	
	public static final int KEYCODE_ENTER = 66;
	
	public final static String DEFAULT_GPS_INTERVAL = "1000";
	public final static String DEFAULT_GPS_DISTANCE = "5.0";
	
	public final static String BASE_OPENFLIGHT_DIRECTORY = "/OpenFlightGPS";
	public final static String DOMAIN_PATH = "http://www.softoutfit.com/OpenFlightGPS";
	public static final String FILE_TYPE = ".ofm";
	public static final String MAP_METADATA_FILE = "mapmetadata.html";
	
	// the map chooser images have to be this size
	public static final int CHOOSER_FIXED_WIDTH = 464;
	public static final int CHOOSER_FIXED_HEIGHT = 280;
	
	public static final int MIN_SPLASH_MILLIS = 3000;	
	
	// this is the time interval for checking if we're off the map
	public static final long MAP_LOC_POLL_MILLIS = 60000;
	
	// minimum speed for doing bearing pointer
	// using >0 value is breaking the ownship pointer for some reason
	public static final float MIN_SPEED_METERS_SEC = 0f;
	
	public static final Logger logger = LoggerFactory.getLogger(OpenFlight.class);	
	
	private static int MAX_RETRIES = 3;
	private static int RETRY_MILLIS = 3000;
	
//	@Override
//	void 	onConfigurationChanged(Configuration newConfig)
//	Called by the system when the device configuration changes while your component is running.
	@Override
	public void onCreate() { //	Called when the application is starting, before any other application objects have been created.
		Utils.init( getApplicationContext(), this );	
	}
	
	private void initCourse() {
		currentCourse = new Course( );
		currentCourse.setWaypoints( new ArrayList<WaypointItem>() );	
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
//		if ( selectMap != null )
//			selectMap.compactImageMapCache();
	}

	Object data;
	public Object getData() { return data; }
	public void setData( Object data ) { this.data = data; }
	
	SelectMapActivity selectMap;
	public SelectMapActivity getSelectMap() {
		
		// some real hackabilly; the activities/application needs better structure to avoid this problem		
		// selectMap could be null if app was backgrounded or terminated by the system while a map is active
		
		if ( selectMap == null ) {
	  			final Intent i = new Intent( Utils.getUtils().getApplicationContext(), SelectMapActivity.class );
	  			i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
	  			startActivity( i );
	  			// now the activity onCreate() sets the selectMap variable when it is created, so
	  			// don't return until that value is present
	  			int j = 0;
	  			while( j++ < MAX_RETRIES ) {
	  				if ( selectMap != null )
	  					break;
	  				try{
	  				  Thread.sleep( RETRY_MILLIS );
	  				}
	  				catch( Exception ie ){
	  					// another thread interrupted; still make sure than selectMap is not null so swallow
	  				}
	  			}	  	
	  			// tried to restart selector, didn't work
	  			if ( j >= MAX_RETRIES )
	  				Utils.killBill();
		}
		
		return selectMap; 
	}
	public void setSelectMap( SelectMapActivity selectMap ) { this.selectMap = selectMap; }
	
	// should this be a pref? is public to allow direct access for perf
	public boolean isScreenActive = true;
	public void setScreenActive( boolean which ) { this.isScreenActive = which; }	
	public boolean isScreenActive() { return isScreenActive; }
	
	// courses
	private Course currentCourse = null;
	// note that WaypointOverlay always needs to share the course with the rest of the program,
	// as it directly manipulates the waypoint list as seen here
	public void setCurrentCourse( Course course ) { currentCourse = course; }
	public Course getCurrentCourse() {
		if ( currentCourse == null ) {
	    	initCourse();			
		}
			
		return currentCourse; 
	}
	
	public void rebuildImageMap() { selectMap.getCurrentImageMap().rebuild(); }
}

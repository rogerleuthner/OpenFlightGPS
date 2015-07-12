
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

package com.cso.and.of.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.cso.and.of.OpenFlight;
import com.cso.and.of.types.AvailableProducts;

// TODO need to ensure that the values typed into the 
// fields are convertible to the desired types

public class Prefs {

	// for keys used more than once; TODO should these be resource strings ??
	private static final String PKEY_DATA_DIR = "data_directory";
	private static final String PKEY_SELECT_MAP_BY_POSITION = "select_map_by_position";
	private static final String PKEY_SEEN_DISLAIMER = "seen_disclaimer";
	private static final String PKEY_GPS_INTERVAL = "gps_update_interval_millisecs";
	private static final String PKEY_GPS_DIST = "gps_update_dist_meters";
	private static final String PKEY_KEEP_OWNSHIPCENTERED = "ownship_centered";
	private static final String PKEY_USE_GPS = "use_gps";
	private static final String PKEY_DEVICE_ID = "elementary";
	
	
	SharedPreferences prefs;

	public Prefs(Context c) {
		prefs = PreferenceManager.getDefaultSharedPreferences(c);
	}
	
	public AvailableProducts getHomeScreen() {		
		return AvailableProducts.getProductByFriendlyName( 
				prefs.getString( "home_screen", "Sectional" ).trim() );
	}
	
	public boolean isIdSaved() {
		if ( prefs.getString( PKEY_DEVICE_ID, PKEY_DEVICE_ID ).equals( PKEY_DEVICE_ID ) )
			return false;
		
		return true;
	}	
	
	public String saveId( String id ) {
		SharedPreferences.Editor e = prefs.edit();
		e.putString( PKEY_DEVICE_ID, id );
		e.commit();
		return getDeviceId();
	}
	
	public String getDeviceId() {
		return prefs.getString( PKEY_DEVICE_ID, "" );
	}
	
	public void seenDisclaimer() {
		SharedPreferences.Editor e = prefs.edit();
		e.putBoolean(PKEY_SEEN_DISLAIMER, true);
		e.commit();	
	}	
	
	public boolean isDisclaimerSeen() {
		return prefs.getBoolean(PKEY_SEEN_DISLAIMER, false);
	}

	public String getDataDirectory() {
		
// XXX create this using the application name as a suffix, to distinguish between
		// versions
		
		// if this has never been run or user has botched it, initialize the prefs with the default value
		// if user has set to a non-writable directory, all bets are off
		if ( prefs.getString( PKEY_DATA_DIR, null ) == null || prefs.getString( PKEY_DATA_DIR, null ).equals( "" ) ) {			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString( PKEY_DATA_DIR, Environment.getExternalStorageDirectory().getPath() + OpenFlight.BASE_OPENFLIGHT_DIRECTORY );
			editor.commit();
		}		
		return prefs.getString(PKEY_DATA_DIR, null ).trim();
	}
	
	public boolean isKeepScreenOn() {
		return prefs.getBoolean("force_screen_on", false);
	}

	public int getUpdateIntervalGPS() {
		String s = prefs.getString( PKEY_GPS_INTERVAL, OpenFlight.DEFAULT_GPS_INTERVAL );
		// input is masked to contain integers only using android:numeric
		int i = Integer.parseInt( s );	
		// if they set < 0, (0 means use min possible) use the default and reset the saved value
		if ( i < 0 ) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString( PKEY_GPS_INTERVAL, OpenFlight.DEFAULT_GPS_INTERVAL );
			editor.commit();
			i = Integer.parseInt( OpenFlight.DEFAULT_GPS_INTERVAL );
		}		
		
		return i;
	}
	
	public float getUpdateDistGPS() {
		String s = prefs.getString( PKEY_GPS_DIST, OpenFlight.DEFAULT_GPS_DISTANCE );
		float f = Float.parseFloat( s );
		// if they set < 0, (0 means use min possible) use the default and reset the saved value
		if ( f < 0f ) {
			try {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString( PKEY_GPS_DIST, OpenFlight.DEFAULT_GPS_DISTANCE );
				editor.commit();
				f = Float.parseFloat( OpenFlight.DEFAULT_GPS_DISTANCE );
			} catch ( Exception e ) {
				throw new RuntimeException( "Tried to fix bad gps_update_interval_millisecs, but failed: " + e.getMessage() );
			}
		}		
		
		return f;		
	}	
	
	public boolean isCompassEnabled() {
		return prefs.getBoolean( "compass_enabled", false );
	}	
	
	/**
	 * User wants map selection by GPS position
	 * @return
	 */
	public boolean isMapByPosition() {
		return prefs.getBoolean( PKEY_SELECT_MAP_BY_POSITION, false );
	}
	
	public void setMapByPosition( boolean val ) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean( PKEY_SELECT_MAP_BY_POSITION, val );
		editor.commit();
	}
	
//	public boolean isGPSFaked() {
//		return prefs.getBoolean( "use_dummy_gps_coords", false );
//	}	
//	
//	public float getFakeLatitude() {
//		String s = prefs.getString( "dummy_gps_latitude", "35.0" );
//		return Float.parseFloat( s );
//	}		
//	
//	public float getFakeLongitude() {
//		String s = prefs.getString( "dummy_gps_longitude", "-107.0" );
//		return Float.parseFloat( s );
//	}
	
	public boolean getKeepOwnshipCentered() {
		return prefs.getBoolean( PKEY_KEEP_OWNSHIPCENTERED, true );
	}	
	
	public boolean getUseGPS() {
		return prefs.getBoolean( PKEY_USE_GPS, true );
	}
	
	public void setUseGPS( boolean which ) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean( PKEY_USE_GPS, which );
		editor.commit();				
	}
	
	public void setKeepOwnshipCentered( boolean which ) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean( PKEY_KEEP_OWNSHIPCENTERED, which );
		editor.commit();		
	}	
}

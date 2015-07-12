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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.andnav.osm.util.GeoPoint;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.Process;
import android.provider.Settings;
import android.widget.Toast;

import com.cso.and.of.OpenFlight;
import com.cso.and.of.gps.FREE.R;
import com.cso.and.of.location.OwnshipOverlay;
import com.cso.and.of.types.AvailableProducts;
import com.cso.and.of.ui.map.MapInfo;
/**
 * Globally available singleton instantiated as application core
 * 
 * @author Roger
 *
 */
public final class Utils {
	
	private static Utils me = null;
	private static OpenFlight openFlight;
	private final Context applicationContext;
	private final Prefs prefs;
	private final NotificationManager notificationManager;
	private final ConnectivityManager connectivityManager;
	private final LocationManager locationManager;
	private final PowerManager powerManager;
	private final SensorManager sensorManager;
	private final WifiManager wifiManager;
	private static final int NOTIFICATION_ID = 666;
	private static final int TABLET_SCREEN_THRESHOLD = 256000; // 480x800
	
	private Utils( final Context applicationContext ) {
		this.applicationContext = applicationContext;
		this.prefs = new Prefs( getApplicationContext() );
		notificationManager = (NotificationManager)applicationContext.getSystemService( Context.NOTIFICATION_SERVICE );
		connectivityManager = (ConnectivityManager)applicationContext.getSystemService( Context.CONNECTIVITY_SERVICE );
		locationManager = (LocationManager)applicationContext.getSystemService( Context.LOCATION_SERVICE );
		powerManager = (PowerManager)applicationContext.getSystemService( Context.POWER_SERVICE );
		sensorManager = (SensorManager)applicationContext.getSystemService( Context.SENSOR_SERVICE );
		wifiManager = (WifiManager)applicationContext.getSystemService( Context.WIFI_SERVICE );
		
		//permissions error
//		this.setSystem( Settings.System.AIRPLANE_MODE_ON, Settings.System.AIRPLANE_MODE_ON );
	}
	
    
    /**
     * Terminate the app.  Should do cleanup here.
     */
    public static void killBill() {
    	Process.killProcess( Process.myPid() );    	
    }	
	
	/*
	 * Determine if screen size is large for the larger tiling
	 */
	public boolean isTabletSize() {

		// honeycomb definitely a tablet -> this may not be always true in the future
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
			return true;
		
		// now if the screen is greater than 400x800, consider it large
		if ( ( applicationContext.getResources().getDisplayMetrics().heightPixels *
				applicationContext.getResources().getDisplayMetrics().widthPixels ) 
			> TABLET_SCREEN_THRESHOLD )
			
			return true;
		
		
		return false;
	}
	
	/*
	 * For those items that need to be disabled if not a certain version 
	 * 
	 * Currently using:
	 *  ActionBar (3.0 honeycomb)
	 */
	public boolean isMaxFeatureOS() {
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
			return true;
		
		return false;
	}
	
	public final static void setupLocationListener( LocationListener listener ) {
		try {
			Utils.getUtils().getLocationManager().requestLocationUpdates( LocationManager.GPS_PROVIDER, 
					Utils.getUtils().getPrefs().getUpdateIntervalGPS(), 
					Utils.getUtils().getPrefs().getUpdateDistGPS(), 
					listener );
		} catch ( IllegalArgumentException e ) {
			Toast.makeText( Utils.getUtils().getApplicationContext(), 
					"Device GPS failure.  Restart application to continue GPS capability.", Toast.LENGTH_LONG );
			Utils.getUtils().notify( "Restart application.", "Device GPS failure.", OpenFlight.class, null );					
		}
	}	
	
	public final OpenFlight getOpenFlight() {
		return openFlight;
	}
	
	public final void setSystem( String key, String value ) {
		Settings.System.putString( applicationContext.getContentResolver(), key, value );
	}
	
	public synchronized static final void init( Context applicationContext, OpenFlight openFlight ) {
		if ( me == null ) {
			me = new Utils( applicationContext.getApplicationContext() /*ensure it's the global context*/ );
			Utils.openFlight = openFlight;
		}
	}

	
	// top level application context
	public final Context getApplicationContext() {
		return applicationContext;
	}
	
	public final NotificationManager getNotificationManager() {
		return notificationManager;
	}
	
	public final ConnectivityManager getConnectivityManager() {
		return connectivityManager;
	}
	
	public final PowerManager getPowerManager() {
		return powerManager;
	}	
	
	public final SensorManager getSensorManager() {
		return sensorManager;
	}
	
	public final WifiManager getWifiManager() {
		return wifiManager;
	}	
	
	public final String getManifestPackageVersion() {
		String fullPackage = applicationContext.getPackageName();
		return fullPackage.substring( fullPackage.lastIndexOf( "." ) + 1 );
	}
	
	public final long getAppInstallTimeLong( ) {	
		long ts = 0;
		try {
			
			ts = applicationContext.getPackageManager().getPackageInfo( applicationContext.getPackageName(), 0 ).firstInstallTime;
		} catch ( NameNotFoundException e ) {
		} catch ( NoSuchFieldError e ) {
			try {
				PackageManager pm = applicationContext.getPackageManager();
				ApplicationInfo appInfo = pm.getApplicationInfo( applicationContext.getPackageName(), 0 );
				String appFile = appInfo.sourceDir;
				ts = new File(appFile).lastModified();
			} catch ( Exception e1 ) {
				// default to permissive	
			}
		}
		return ts;
	}	
	
	public final boolean enableGPS( Context context ) {
		if ( ! isGPSEnabled() ) {

			Toast.makeText( context,
							"The GPS is not enabled.  Enable it and try again.",
							Toast.LENGTH_SHORT ).show();
			
			// if calling context is not an activity, have to create a new task to contain the settings activity
			Intent i = new Intent( "android.settings.LOCATION_SOURCE_SETTINGS" );
			if ( ! ( context instanceof Activity ) )
				i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
			
			context.startActivity( new Intent( "android.settings.LOCATION_SOURCE_SETTINGS" ) );
		}

		return isGPSEnabled();
	}
	
	public final Location getLastLocation( Location location ) {
		if ( location == null ) {
			location = Utils.getUtils().getLocationManager().getLastKnownLocation( LocationManager.GPS_PROVIDER );
//			
//			// didn't get location - if net is on, try that one
//			if ( location == null && locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER ) )
//				location = Utils.getUtils().getLocationManager().getLastKnownLocation( LocationManager.NETWORK_PROVIDER );			
		}
		
		return location;
	}	
	
	public final boolean isGPSEnabled() {
		return locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER );
	}	
	
	public final boolean isCellLocationEnabled() {
		return locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );
	}

	public final LocationManager getLocationManager() {
		return locationManager;
	}	
	
	public final Prefs getPrefs() {
		return prefs;
	}
	
	public final static Utils getUtils() {
		if ( me == null )
			throw new RuntimeException( "Utils must be instantiated in application controller" );
		
		return me;
	}
	
	/**
	 * Generate specific selected file complete with suffix.
	 * Be careful as to whether the subselect is needed - that is dictated by the input parameter
	 * @param ap
	 * @param file
	 * @return
	 */
	// TODO consolidate with AvailableProducts methods
	public final String getFullArchivePath( AvailableProducts ap, String file ) {
		return getBaseArchivePath( ap, file ) + ap.getSuffix();
	}
	public final String getFullArchivePath( MapInfo mapInfo ) {
		return getBaseArchivePath( mapInfo.getMe(), mapInfo.getDirectoryKey() ) + mapInfo.getMe().getSuffix();
	}	
	
	/**
	 * Generate general file; no suffix
	 * @return
	 */
	public final String getBaseArchivePath( AvailableProducts ap, String file ) {
		return prefs.getDataDirectory() + ap.getDataPath() + file;
	}
	
	public final String getDataDirectory() {
		return prefs.getDataDirectory();
	}
	
	/**
	 * Get bitmap asset given resource id.  Might be easier to just use the drawables if you can get a stream to one.
	 * 
	 * @param pResId
	 * @return
	 */
	public final Bitmap getBitmapAsset( final String assetFileName ) {
		InputStream is = null;
		try {
			is = applicationContext.getAssets().open( assetFileName );
			if (is == null) {
				throw new IllegalArgumentException();
			}
			return BitmapFactory.decodeStream(is, null, null);
		} catch ( Exception e ) {
			throw new RuntimeException( e.getMessage() );
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final IOException ignore) {
				}
			}
		}
	}	
	
    public final void notify( final String text, final String title, final Class<?>caller, Context packageContext ) {
    	
    	if ( packageContext == null )
    		packageContext = applicationContext;
    	
    	Notification notification = new Notification( R.drawable.notification_icon, text, System.currentTimeMillis() );   
    	    	
    	Intent notificationIntent = new Intent( packageContext, caller );
    	PendingIntent contentIntent = PendingIntent.getActivity( getApplicationContext(), 0, notificationIntent, 0 );

    	notification.setLatestEventInfo( packageContext, (CharSequence)title, text, contentIntent );

    	notificationManager.notify(NOTIFICATION_ID, notification);    	
    }	

    /**
     * Remove all notifications wrt this app; for app shutdown so make sure we're still in business
     */
	public final void cancelNotify() {
		if ( notificationManager != null )
			notificationManager.cancel( NOTIFICATION_ID );
	}
	
	/**
	 * Return null if not at border, or string map key of the bordering map 
	 * 
	 * @param loc
	 * @param mapInfo
	 * @return
	 */
	public static String isOwnshipAtBorder( Location loc, MapInfo mapInfo ) {
		return null;
	}		
	
	/**
	 * Return null if ownship is not on current map, otherwise the string key of map
	 * 
	 * @param olay
	 * @param mapInfo
	 * @return
	 */	
	public static String isOwnshipOnMap( Location loc, MapInfo mapInfo ) {

		if ( loc != null && mapInfo != null ) 
			return getMapOfOwnship( new GeoPoint( loc ), mapInfo );
			
		return null;
	}	
		
	/**
	 * Return null if ownship is not on current map, otherwise the string key of map
	 * 
	 * @param olay
	 * @param mapInfo
	 * @return
	 */
	public static String isOwnshipOnMap( OwnshipOverlay olay, MapInfo mapInfo ) {
		
		if ( olay.getMyLocation() != null )
			return getMapOfOwnship( olay.getMyLocation(), mapInfo );
			
		return null;
	}
	
	private static String getMapOfOwnship( GeoPoint gp, MapInfo mapInfo ) {
		
		try {
			final String map = Utils.getUtils()
								.getOpenFlight()
	    						.getSelectMap()
	    						.getCurrentImageMap()
	    						.findByLatLonIfExists( 
	    								gp.getLatitudeE6()/1E6, gp.getLongitudeE6()/1E6 );    		     		
	
			if ( map != null && mapInfo.getDirectoryKey().equals( map ) )
				return map;
				
		} catch ( NullPointerException exp ) {
			/*ignore - crashes sometimes on resume or something
		 	* may need to store the selectmap or currentimagemap into a 'onrestore' thing
		 	*/
			/* debugging, not sure where null is */
			if ( Utils.getUtils().getOpenFlight() == null )
				OpenFlight.logger.error( "Utils.getUtils().getOpenFlight() is null" );
			else if ( Utils.getUtils().getOpenFlight().getSelectMap() == null )
				OpenFlight.logger.error( "Utils.getUtils().getOpenFlight().getSelectMap() is null" );
			else if ( Utils.getUtils().getOpenFlight().getSelectMap().getCurrentImageMap() == null )
				OpenFlight.logger.error( "Utils.getUtils().getOpenFlight().getSelectMap().getCurrentImageMap() is null" );
		}
		
		return null;
	}	
}
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

package com.cso.and.of.location;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Location;
import android.view.View;
import android.widget.Toast;

import com.cso.and.of.OpenFlight;
import com.cso.and.of.config.Utils;
import com.cso.and.of.ui.map.MapInfo;

/**
 * show the user's position, but always centered on the screen
 * 
 */
public class CenteredOwnshipOverlay extends OwnshipOverlay {

	protected Point curPixelPos;
	boolean handlingGPSUpdate = false;
	boolean keepShipCentered;
	private final MapInfo mMapInfo;
	private final Context mContext;
	private long mTimer;
	private long mInitTime;

	public CenteredOwnshipOverlay(Context context, OpenStreetMapView mapView, MapInfo mapInfo) {
		super(context, mapView);
		keepShipCentered = Utils.getUtils().getPrefs().getKeepOwnshipCentered();
		mMapInfo = mapInfo;
		mContext = context;
		mTimer = 0;
	}

	public void setKeepShipCentered( boolean in ) {
		keepShipCentered = in;
	}
	
	public boolean getKeepShipCentered( ) {
		return keepShipCentered;
	}	
	
	/**
	 * middle 50% (or whatever of the window)
	 */
	private static final int middleNum = 4, middleDenom = 16;

	/**
	 * Is this point in the middle 50% of the view?
	 * 
	 * @param view
	 * @param pt
	 * @return
	 */
	private static boolean inMiddle(View view, Point pt) {

		int w, min, max;
		w = view.getWidth();
		min = w * middleNum / middleDenom;
		max = w * (middleDenom - middleNum) / middleDenom;
		if (pt.x < min || pt.x > max)
			return false;

		w = view.getHeight();
		min = w * middleNum / middleDenom;
		max = w * (middleDenom - middleNum) / middleDenom;
		if (pt.y < min || pt.y > max)
			return false;

		return true;
	}

	/**
	 * 
	 * @param mapView
	 * @param myLocation
	 * 
	 *            If the user moves outside of the middle section keep em
	 *            visible. We only do this when the user first crosses this
	 *            boundary, so if the user has manually scrolled away from his
	 *            position we won't force him back. We are also careful to only
	 *            do this if we recently received a location update from the gps
	 *            - so click drags won't mess us up.
	 */
	protected void keepCentered(OpenStreetMapView mapView, GeoPoint myLocation) {
		// If we don't have an old position, assume it was centered, so we
		// will zoom if needed. Always update this, so that click drags work
		boolean oldInMiddle = curPixelPos != null ? inMiddle(mapView, curPixelPos) : true;
		curPixelPos = mapView.getProjection().toMapPixels(myLocation, curPixelPos);

		if (handlingGPSUpdate) {
			handlingGPSUpdate = false;

			if (oldInMiddle && !inMiddle(mapView, curPixelPos))
				mapView.getController().setCenter(myLocation);
		}
	}

	/**
	 * Watch for GPS updates.  Periodically check to make sure we're on the shown map; if 
	 * not, see if border map exists; if not, cancel centering until the next resume (reset).
	 * 
	 */
	@Override
	public synchronized void onLocationChanged(final Location location) {
		handlingGPSUpdate = true;
				
		// time should only be 0 on first check, so do that second as it only should be true once
		if ( ( mTimer > OpenFlight.MAP_LOC_POLL_MILLIS ) || mTimer == 0 ) {	
			final String borderMap = Utils.isOwnshipAtBorder( location, mMapInfo );
			final String onMap =  Utils.isOwnshipOnMap( location, mMapInfo );
			if ( onMap == null || borderMap != null ) {				
				if ( Utils.getUtils().getPrefs().getKeepOwnshipCentered() ) {
					if ( borderMap != null )
						// should open bordering map and place craft there
						Toast.makeText( mContext, "Should open map: " + borderMap, Toast.LENGTH_LONG ).show();
					else {
						// temporarily disable centering and following (remains in effect until resume)
						setKeepShipCentered( false );
						followLocation( false );
					}
				}
			}
			// subtract 1, since on fast processors it might not have millis > 0 by the next loop
			// to work right it needs to be > 0 every time after initial loop
			mInitTime = System.currentTimeMillis() - 1;
		}
		// increment timer by time passed since the last check
		mTimer = System.currentTimeMillis() - mInitTime;
		
		super.onLocationChanged( location );			
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.andnav.osm.views.overlay.MyLocationOverlay#onDraw(android.graphics
	 * .Canvas, org.andnav.osm.views.OpenStreetMapView)
	 * 
	 * If the user moves outside of the middle section keep em visible. We only
	 * do this when the user first crosses this boundary, so if the user has
	 * manually scrolled away from his position we won't force him back. We are
	 * also careful to only do this if we recently received a location update
	 * from the gps - so click drags won't mess us up.
	 */
	@Override
	public void onDraw(Canvas c, OpenStreetMapView mapView) {
		if ( getMyLocation() != null && keepShipCentered ) {
			keepCentered(mapView, getMyLocation());
		}

		super.onDraw(c, mapView);
	}

}

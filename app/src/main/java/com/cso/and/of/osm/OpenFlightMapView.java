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

package com.cso.and.of.osm;

import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.util.IOpenStreetMapRendererInfo;
import org.andnav.osm.views.util.OpenStreetMapTileProvider;

import android.graphics.Canvas;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.cso.and.of.OpenFlight;
import com.cso.and.of.config.Utils;
import com.cso.and.of.ui.map.MapActivity;

// TODO casting of double to floats is a little shakey

public class OpenFlightMapView extends OpenStreetMapView {
	
	// performance since this is referenced every click/draw
	private final OpenFlight openFlight;
	private MapActivity mapActivity;
	
	public OpenFlightMapView(final MapActivity mapActivity, final IOpenStreetMapRendererInfo renderer, final OpenStreetMapTileProvider provider ) {
		super( mapActivity, renderer, provider );
		openFlight = Utils.getUtils().getOpenFlight();
		this.mapActivity = mapActivity;
	}
	
	@Override
	public void scrollTo( int x, int y ) {

//		// ignore already-queued up scrolling calls if this activity has been cancelled already
//		// TODO find out how to clear the que, or else just detect when the final scroll has been issued,
//		// and act only upon that one instead of any and all
//		if ( mapActivity == null )
//			return;
		
		if ( mapActivity != null ) {
		
			super.scrollTo( x, y );			
			
			// check if the scroll is finished so we don't redo this a bunch of times
			if ( getScroller().isFinished() ) {
			
				// take center of current display
				// find lat/lon of that - use that lat lon to find adjacent map
				final double lat = getVisibleBoundingBoxE6().getCenter().getLatitudeE6()/1E6;
				final double lon = getVisibleBoundingBoxE6().getCenter().getLongitudeE6()/1E6;		
				
				// if center of the visible area is not on our current map,
				// try to switch to the adjacent map
				
				if (! ( lat >= mapActivity.getMapInfo().getLatitudeMin() && 
						lat <= mapActivity.getMapInfo().getLatitudeMax() &&
						lon <= mapActivity.getMapInfo().getLongitudeMax() &&
						lon >= mapActivity.getMapInfo().getLongitudeMin() ) ) {
		
					if ( Utils.getUtils().getOpenFlight().getSelectMap().goToMap( lat, lon, getZoomLevel() ) ) {
						this.getScroller().forceFinished( true );
						mapActivity.finish();
						mapActivity = null;
					}
					// otherwise, don't have the map so just ignore the scroll to it
				}
			}
		}
	}
	
	@Override
	public void onDraw(final Canvas c) {
		super.onDraw( c );
	}
	
	// user input events intercepted so we can shut off touch screen to avoid spurious inputs
	@Override
	public void onLongPress(MotionEvent e) {
		if ( openFlight.isScreenActive ) {
			super.onLongPress( e );
		}
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if ( openFlight.isScreenActive )
			return super.onSingleTapUp( e );
		
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ( openFlight.isScreenActive )
			return super.onKeyDown( keyCode, event );
		
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if ( openFlight.isScreenActive )
			return super.onKeyDown( keyCode, event );
		
		return true;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if ( openFlight.isScreenActive )		
			return super.onTrackballEvent( event );
		
		return true;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if ( openFlight.isScreenActive ) {
			return super.onTouchEvent( event );
		}
		
		return true;
			
//			final int action = event.getAction();
//			
//			if ( openFlight.isEditingPlan &&
//					( action == MotionEvent.ACTION_SCROLL || action == MotionEvent.ACTION_UP ) ) {
//			
//				// during a scroll, this fires every couple of pixels (?) movement
//				
//				System.out.println( "SCROLLING: " + event.toString() );
//				
//				
//				if ( event.getAction() == MotionEvent.ACTION_UP )
//					System.out.println( "RELEASED: " + event.toString() );
//				
//			} else {
//				return super.onTouchEvent( event );	
//			}
	}	
	
	
	
	

	
//	@Override
//	public void onLongPress(MotionEvent e) {
//		
//		mapView.resetView();

//		GeoPoint p1 = Mercator.projectPoint(  (int)e.getX(), (int)e.getY(), this.getZoomLevel() );
//		
//		System.out.println( "LAT SPAN: " + this.getLatitudeSpanE6() );
//		System.out.println( "SCROLL EXTENT: " + this.computeHorizontalScrollExtent() );
//		System.out.println( "SCROLL RANGE: " + this.computeHorizontalScrollRange() );
//		System.out.println( "MAP CENTER LAT: " + this.getMapCenterLatitudeE6() );
//		System.out.println( "MAP CENTER LON: " + this.getMapCenterLongitudeE6());
//		BoundingBoxE6 bb = this.getDrawnBoundingBoxE6();
//		GeoPoint p = this.getMapCenter();
//		System.out.println( this.getMapCenter().toString() );
		
//		System.out.println( this.getProjection().fromPixels( e.getX(), e.getY() ).toString() );
//		invalidate(); // TODO testing
		
		
		
		
//		this.getScroller().setFinalX(-32101189);
//		this.getScroller().setFinalY(-106743164);
//		
//		super.onLongPress( e );
//	}	

//	public GeoPoint getLatLonFromScreenCoords( float x, float y, int zoom ) {
//		return Mercator.projectPoint((int)x, (int)y, /*(int) (getMyPixelZoomLevel() * 1E6)*/getRenderer().maptileZoom());
//	}
	
//	public int getPointLatitude() {
//		return (int)(Mercator.tile2lat(getScrollY() + getMyWorldSizePx()/2, getMyPixelZoomLevel()) * 1E6);
//	}
//
//	public int getPointLongitude() {
//		return (int)(Mercator.tile2lon(getScrollX() + getMyWorldSizePx()/2, getMyPixelZoomLevel()) * 1E6);
//	}	
//	
//	// copies of the superclass' not visible methods since we want to use them
//	int getMyWorldSizePx() {
//		return (1 << getMyPixelZoomLevel());
//	}

//	int getMyPixelZoomLevel() {
//		return this.getZoomLevel() + getRenderer().maptileZoom();
//	}	
	
	/**
	 * For a description see:
	 * 
	 * @see http://wiki.openstreetmap.org/index.php/Slippy_map_tilenames For a
	 *      code-description see:
	 * @see http://wiki.openstreetmap.org/index.php/Slippy_map_tilenames#
	 *      compute_bounding_box_for_tile_number
	 * @param aLat
	 *            latitude to get the {@link OSMTileInfo} for.
	 * @param aLon
	 *            longitude to get the {@link OSMTileInfo} for.
	 * @return The {@link OSMTileInfo} providing 'x' 'y' and 'z'(oom) for the
	 *         coordinates passed.
	 */
	public static OpenStreetMapTile getMapTileFromCoordinates(IOpenStreetMapRendererInfo renderer,
			final int zoom, final double aLat, final double aLon) {
		final int y = (int) Math.floor((1 - Math.log(Math.tan(aLat * Math.PI / 180) + 1
				/ Math.cos(aLat * Math.PI / 180))
				/ Math.PI)
				/ 2 * (1 << zoom));
		final int x = (int) Math.floor((aLon + 180) / 360 * (1 << zoom));
	
		return new OpenStreetMapTile(renderer, zoom, x, y);
	}		
	
//	// http://developers.cloudmade.com/projects/tiles/examples/convert-coordinates-to-tile-numbers
//	public int getTileXFromLon( float lonDeg  ) {
//		int n = 2 ^ getMyPixelZoomLevel();
//		float tileX = ((lonDeg + 180) / 360) * n;
//		// NOTE this needs to 'round down'
//		return Math.round( tileX );
//	}
//	
//	public int getTileYFromLat( float latDeg  ) {
//		// TODO sloppy casting here ....
//		float latRad = (float) (Math.PI / 180) * latDeg;   // Mercator.DEG2RAD
//		int n = 2 ^ getMyPixelZoomLevel();
//		double tileY = (1 - (Math.log1p(Math.tan(latRad) + Fmath.sec(latRad)) / Math.PI)) / 2 * n;
//		// NOTE this needs to 'round down' 
//		return (int) Math.round( tileY );
//	}
	
}

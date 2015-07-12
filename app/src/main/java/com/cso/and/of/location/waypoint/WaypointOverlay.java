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

import java.util.Iterator;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewItemizedOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewItemizedOverlay.OnItemGestureListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.text.TextPaint;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.cso.and.of.config.Utils;
import com.cso.and.of.location.DataOverlay;
import com.cso.and.of.ui.WaypointInfoActivity;

/**
 * Current course shares the waypoint list (application object has any current 'course', but the waypoints pointed
 * to are maintained in the overlay).
 * 
 * The course waypoints may differ between the database and those in the overlay if the user has edited at all.
 * 
 * @author Roger
 *
 */

public class WaypointOverlay extends OpenStreetMapViewItemizedOverlay<WaypointItem>  implements OnItemGestureListener<WaypointItem> {

	private final Paint mCaptionPaint = new TextPaint();
	private final Paint mLinesPaint = new Paint();
	private final View view;
	private final DataOverlay dataOverlay;	
	private final Activity mapViewActivity;
	private WaypointItem selectedWaypointItem;
	// every time we draw, save the lines we have so we can check for touch on
	//private float[] courseLines;	

	// context menu items (determines order and identity)
	public static final int CANCEL = 1;
	public static final int MOVE = 2;
	public static final int INFO = 3;
	public static final int DELETE = 4;		
	
	private boolean movingWaypoint = false;
	// when canceling move, need old coords to restore
	int savedLatitudeE6 = 0;
	int savedLongitudeE6 = 0;	

	public WaypointOverlay( Activity mapViewActivity, View view, DataOverlay dataOverlay ) {
		// per example, we want the bounds to be centered just below this
		// drawable. We use a alpha channel to not obscure terrain too much...
		// super(boundCenterBottom(context.getResources().getDrawable(R.drawable.blue)));
		super( view.getContext(), Utils.getUtils().getOpenFlight().getCurrentCourse().getWaypoints(), null );
		this.mOnItemGestureListener = this;
		this.view = view;
		this.dataOverlay = dataOverlay;
		this.mapViewActivity = mapViewActivity;
		
		mCaptionPaint.setTextSize(mCaptionPaint.getTextSize() + 3);
		mCaptionPaint.setTextAlign(Align.CENTER);
		mCaptionPaint.setColor(Color.WHITE);
		mCaptionPaint.setShadowLayer(3, 1, 1, Color.BLACK);
		
		mLinesPaint.setColor( Color.rgb( 80, 40, 80 ) );
		mLinesPaint.setStrokeWidth( 8f );
	}
	
	@Override
	public void onDraw(final Canvas canvas, final OpenStreetMapView mapView ) {
		// draw path first so it doesn't overwrite the nodes
		drawPath( canvas, mapView );
		super.onDraw( canvas, mapView );
	}
	
	public void drawPath( final Canvas canvas, final OpenStreetMapView mapView ) {
		int size = mItemList.size();

		if ( size > 1 ) {
			final OpenStreetMapViewProjection prj = mapView.getProjection();
			final Point pt = new Point();
			float[] lines = new float[ (size-1)*4 ]; // 4 pts/segment and size-1 segments
			int i = 0;
			
			for( WaypointItem wpi : mItemList ) {
				final GeoPoint gp = wpi.getPoint();				

				prj.toMapPixelsProjected( gp.getLatitudeE6(), gp.getLongitudeE6(), pt );				
				prj.toMapPixelsTranslated( pt, pt );
				
				lines[ i ] = pt.x;
				lines[ i + 1 ] = pt.y;
				i += 2;

				// reuse size to find first and last entries that are not 'doubled'\
				if ( ( size != 1 ) && ( size != mItemList.size() ) ) {
					lines[ i ] = pt.x;
					lines[ i + 1 ] = pt.y;
					i += 2;					
				}
				size--;
			}
			canvas.drawLines( lines, mLinesPaint );
			//courseLines = lines;
		}
	}	
	
	/*
	 * Find closest point.
	 * 
	 * Do this by iterating through all the points, keeping the 'best' match.
	 * Arbitrarily add the point at the closest points index.
	 * TODO will need way for user to rearrange the order in case this guess is wrong.
	 */
	private void addBetweenNearestNeighbors( GeoPoint gp ) {

		int wpIndex = 0;
		double dist0 = 0.0;				
		
		for( WaypointItem wp : mItemList ) {
			double dist = dist( gp, wp.getPoint() );
			if ( wpIndex == 0 || dist < dist0 )
				dist0 = dist;
			
			wpIndex++;
		}
		// have the least dist point, insert it after the match
		mItemList.add( wpIndex, new WaypointItem( "Point: " + wpIndex, "user waypoint", gp ) );
	}
	
	// apply distance formula to the points
	private double dist( GeoPoint a, GeoPoint b ) {
		return 
			Math.sqrt( Math.pow(b.getLatitudeE6() - a.getLatitudeE6(), 2)
					+ Math.pow( b.getLongitudeE6() - a.getLongitudeE6(), 2) );
	}
	
//	/**
//	 * Fires anywhere on the map
//	 */
//	@Override
//	public boolean onSingleTapUp(final MotionEvent event, final OpenStreetMapView mapView) {
//
//		// show context menu for point: delete, info, move, cancel
//		Toast.makeText( context, "Context menu for: " + new String( event.getX() + ", " + event.getY() ), Toast.LENGTH_SHORT).show();
//		
//		return true;	
//	}

	@Override
	public boolean onLongPress(final MotionEvent event, final OpenStreetMapView mapView) {
		// ignore taps in the data overlay box, it responds to clicks itself
		// and ignore multi-touch events
		if ( dataOverlay != null  && dataOverlay.isClickInDataArea( event ) ) 
			return false;

		if ( movingWaypoint ) {

			GeoPoint gp = mapView.getProjection().fromPixels( event.getX(), event.getY() );
			
			Utils.getUtils().getOpenFlight().getCurrentCourse().moveWaypoint( selectedWaypointItem,
					gp.getLatitudeE6(), gp.getLongitudeE6(), mapView );
					
			
		} else {
				new AlertDialog.Builder( mapViewActivity ).setMessage("Add waypoint?" )
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// TODO find the most probable begin/end and insert there
						GeoPoint gp = mapView.getProjection().fromPixels( event.getX(), event.getY() );
						addBetweenNearestNeighbors( gp );	
						Utils.getUtils().getOpenFlight().getCurrentCourse().setDirty( true );
						mapView.invalidate();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})
				.create()
				.show();			
		}
		return true;

	}	
	
//	private static boolean isTouchOnWaypoint( final MotionEvent event ) {
//		return false;
//	}
		
	
//final int L = 10;	
//	private boolean isTouchOnCourseline( final MotionEvent event, final OpenStreetMapView mapView ) {
//		// if it's within LINEPROXIMITY pixels of a line
//		final GeoPoint gp = mapView.getProjection().fromPixels( event.getX(), event.getY() );
//		final Point pt = new Point();
//		mapView.getProjection().toMapPixelsProjected( gp.getLatitudeE6(), gp.getLongitudeE6(), pt );				
//		mapView.getProjection().toMapPixelsTranslated( pt, pt );		
//		// for each pair of coords, visualize a rectangle of lineproximity*2 width and see if the
//		// touch is within that
//		if ( courseLines != null ) {
//			for( int x = 0; x < courseLines.length; x +=2 ) {
//				Rect rect = new Rect( Math.round( courseLines[ x ] ) - L, Math.round( courseLines[ x ] ) + L, 
//										Math.round( courseLines[ x+1 ] ) - L, Math.round( courseLines[ x+1 ] ) + L );						
//	
//				if ( rect.contains( Math.round( event.getX() ), Math.round( event.getY() ) ) )
//					return true;
//			}
//		}
//		
//		return false;
//	}

//	@Override
//	public boolean onLongPress(final MotionEvent event, final OpenStreetMapView mapView) {
//		// ignore taps in the data overlay box, it responds to clicks itself
//		// and ignore multi-touch events
//		if ( dataOverlay != null  && dataOverlay.isClickInDataArea( event ) ) 
//			return false;
//
//		
//		if ( isTouchOnWaypoint( event ) ) {
//			
//		} else if ( isTouchOnCourseline( event, mapView ) ) {
//
//			
//			
//					new AlertDialog.Builder( mapViewActivity ).setMessage("Touch is within area" )			
//					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int id) {
//							dialog.cancel();
//						}
//					})
//					.create()
//					.show();				
//			
//		
//
//// TODO get rid of moving waypoint junk			
////		if ( movingWaypoint ) {
////
////			GeoPoint gp = mapView.getProjection().fromPixels( event.getX(), event.getY() );
////			
////			Utils.getUtils().getOpenFlight().getCurrentCourse().moveWaypoint( selectedWaypointItem,
////					gp.getLatitudeE6(), gp.getLongitudeE6(), mapView );
//					
//			
//		} else {
//				new AlertDialog.Builder( mapViewActivity ).setMessage("Add waypoint?" )
//				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//						// TODO find the most probable begin/end and insert there
//						GeoPoint gp = mapView.getProjection().fromPixels( event.getX(), event.getY() );
//						addBetweenNearestNeighbors( gp );	
//						Utils.getUtils().getOpenFlight().getCurrentCourse().setDirty( true );
//						mapView.invalidate();
//					}
//				})
//				.setNegativeButton("No", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//						dialog.cancel();
//					}
//				})
//				.create()
//				.show();			
//		}
//		return true;
//
//	}	
	
	/**
	 * Fire when selecting an existing waypoint (only if onLongPress is not implemented)
	 */
	@Override
	public boolean onItemLongPress(int arg0, WaypointItem wp) {
		return true;
	}
	
	/* doesn't seem to get fired unless onSingleTapUp is commented out */
	@Override
	public boolean onItemSingleTapUp(int arg0, WaypointItem wp) {
		selectedWaypointItem = wp;
		mapViewActivity.openContextMenu( view );

		return true;
	}
	
	/**
	 * Since can't pass the waypoint item data to the opencontextmenu, the MapView.onContextItemSelected
	 * is delegated to here for handling, which depends upon the class variable 'selectedWaypointItem' being
	 * set by the touch handler before the menu is opened
	 * 
	 * @param item
	 */
	public void handleWaypointSelected( MenuItem item ) {
		
		if ( selectedWaypointItem != null ) {
		
			switch( item.getItemId() ) {
				case CANCEL:
					if ( movingWaypoint ) {
						// reinstall old point location
						selectedWaypointItem.mGeoPoint.setLatitudeE6( savedLatitudeE6 );
						selectedWaypointItem.mGeoPoint.setLongitudeE6( savedLongitudeE6 );
						savedLatitudeE6 = 0;
						savedLongitudeE6 = 0;
						view.invalidate();
					}
					
					// allow cancel of a move via this too
					movingWaypoint = false;
					break;
				case MOVE:
					if ( ! movingWaypoint ) {
						Toast.makeText( view.getContext(), "Long press destination, then touch new point for options", Toast.LENGTH_LONG ).show();
						savedLatitudeE6 = selectedWaypointItem.mGeoPoint.getLatitudeE6();
						savedLongitudeE6 = selectedWaypointItem.mGeoPoint.getLongitudeE6();
						movingWaypoint = true;
					} else {
						// moving, so now done
						Toast.makeText( view.getContext(), "Moved to: " + selectedWaypointItem.mGeoPoint.getLatitudeE6() +
								", " + selectedWaypointItem.mGeoPoint.getLongitudeE6(), Toast.LENGTH_LONG ).show();						
						movingWaypoint = false;
						savedLatitudeE6 = 0;
						savedLongitudeE6 = 0;
					}
					break;			
				case INFO:
					showWaypointInfo( item );
					break;
				case DELETE:
					Utils.getUtils().getOpenFlight().getCurrentCourse().deleteWaypoint( selectedWaypointItem, view);
					break;
				default:/*noop*/;
			}
		}
				
	}
	
	private void showWaypointInfo( MenuItem item ) {
		
		Intent i = new Intent( view.getContext(), WaypointInfoActivity.class );
		i.putExtra( WaypointItem.TITLE, selectedWaypointItem.mTitle );
		i.putExtra( WaypointItem.DESC, selectedWaypointItem.mDescription );
		i.putExtra( WaypointItem.LAT, selectedWaypointItem.mGeoPoint.getLatitudeE6() );
		i.putExtra( WaypointItem.LON, selectedWaypointItem.mGeoPoint.getLongitudeE6() );
		i.putExtra( WaypointItem.COURSE, Utils.getUtils().getOpenFlight().getCurrentCourse().getName() );

		// they in ordered list, find the next point if there is one
		Iterator<WaypointItem>it = mItemList.iterator();
		while( it.hasNext() ) {
			if ( it.next().equals( selectedWaypointItem ) )
				break;
		}
		if ( it.hasNext() ) {
			WaypointItem next = it.next();
			i.putExtra( WaypointItem.NEXT_WAY_LAT, next.getPoint().getLatitudeE6() );
			i.putExtra( WaypointItem.NEXT_WAY_LON, next.getPoint().getLongitudeE6() );
			i.putExtra( WaypointItem.NEXT_WAY_TITLE, next.getTitle() );
		}
		
		view.getContext().startActivity( i );		
	}

}

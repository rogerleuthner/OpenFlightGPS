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

package com.cso.and.of.ui;

import java.util.Iterator;

import org.andnav.osm.util.GeoPoint;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

import com.cso.and.of.BaseActivity;
import com.cso.and.of.config.Utils;
import com.cso.and.of.gps.FREE.R;
import com.cso.and.of.location.waypoint.WaypointItem;

/**
 * Waypoint information activity, also used to edit waypoint title and description.
 * 
 * @author Roger
 *
 */

// TODO implement location listener that updates as progression occurs

public class WaypointInfoActivity extends BaseActivity implements LocationListener {
    
	// use the name as a key since if the course is not saved yet we don't have an id
	private String oldName; // need to save this in case user changes it (and it's not saved yet) so we can find the point in the list	
	
	private static double METERS_PER_NM = 0.000539956803455724;
	private static final String NOD_FMT = "%.0f";
	private static final String ONED_FMT = "%.1f";
	private static final String THREED_FMT = "%.3f";		
	
	// used in location change computation
	private double nextLon;
	private double nextLat;
	private double thisLon;
	private double thisLat;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
			
		setContentView( R.layout.waypoint );		
		oldName = getIntent().getStringExtra( WaypointItem.TITLE );		
		
		((TextView)findViewById( R.id.waypointName ) ).setText( 
				getIntent().getStringExtra( WaypointItem.TITLE ) );
		
		((TextView)findViewById( R.id.waypointDesc ) ).setText( 
				getIntent().getStringExtra( WaypointItem.DESC ) );

		thisLon = getIntent().getIntExtra( WaypointItem.LON, 0 );
		thisLat = getIntent().getIntExtra( WaypointItem.LAT, 0 );		
		((TextView)findViewById( R.id.latLon ) ).setText( 
				String.format( THREED_FMT, thisLon/1E6 ) + " / " + 
				String.format( THREED_FMT, thisLat/1E6 ) );
		
		((TextView)findViewById( R.id.waypointCourseName ) ).setText( 
				getIntent().getStringExtra( WaypointItem.COURSE ) );		

		// stuff to next waypoint
		nextLon = getIntent().getIntExtra( WaypointItem.NEXT_WAY_LON, 0 );
		nextLat = getIntent().getIntExtra( WaypointItem.NEXT_WAY_LAT, 0 );

		if (  nextLon != 0 && nextLat != 0 ) {
			((TextView)findViewById( R.id.nmToNext ) ).setText(
					nmBetween( thisLat, thisLon, nextLat, nextLon ) );
	
			((TextView)findViewById( R.id.bearingToNext ) ).setText(
					bearingTo( thisLat, thisLon, nextLat, nextLon ) );
			
			((TextView)findViewById( R.id.nextWaypoint ) ).setText(
					getIntent().getStringExtra( WaypointItem.NEXT_WAY_TITLE ) );			
		}
		
		// stuff from current location
		Location currentLoc = Utils.getUtils().getLastLocation( null );
		doLocationDependents( currentLoc );
		
		((TextView)findViewById( R.id.courseTotal ) ).setText( nmTotal() );						
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// get rid of default focus so if user selects it isn't already selected (and thus doesn't select all text)
		// and the activity doesn't open with the keyboard out already
		findViewById( R.id.waypointName ).clearFocus(); 
		findViewById( R.id.waypointView ).requestFocus();
		// update the to next point data as we move
		Utils.setupLocationListener( this );
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Utils.getUtils().getLocationManager().removeUpdates( this );		
	}
	
	/**
	 * On back button, if any changes move them into the course and then set dirty
	 */
    @Override
    public final void onBackPressed() {    	
		for( WaypointItem it : Utils.getUtils().getOpenFlight().getCurrentCourse().getWaypoints() ) {
			if ( it.mTitle.equals( oldName ) ) {				
				if ( ! ((TextView)findViewById( R.id.waypointName ) ).getText().toString().equals( it.mTitle ) 
						||
					 ! ((TextView)findViewById( R.id.waypointDesc ) ).getText().toString().equals( it.mDescription ) ) {
					
					
					it.mTitle = ((TextView)findViewById( R.id.waypointName ) ).getText().toString();
					it.mDescription = ((TextView)findViewById( R.id.waypointDesc ) ).getText().toString();
					oldName = it.mTitle;  // in case they change again before database save
					Utils.getUtils().getOpenFlight().getCurrentCourse().setDirty( true );	
					break;
				}
			}
		}    	
		finish();
    }	
    
    @Override
    public final void onDestroy() {
    	super.onDestroy();
		Utils.getUtils().getLocationManager().removeUpdates(this);    	
    }
	
    private final void doLocationDependents( Location l ) {
    	
    	if ( l != null ) {
    		
			((TextView)findViewById( R.id.positionDistTo ) ).setText(
					nmBetween( l.getLatitude()*1E6, l.getLongitude()*1E6, thisLat, thisLon ) );
	
			((TextView)findViewById( R.id.positionBearingTo ) ).setText(
					bearingTo( l.getLatitude()*1E6, l.getLongitude()*1E6, thisLat, thisLon ) );					
	
				// stuff from both next waypoint and current location
			if (  nextLon != 0 && nextLat != 0 ) {
				((TextView)findViewById( R.id.distFromCurrent ) ).setText(
						nmBetween( l.getLatitude()*1E6, l.getLongitude()*1E6, nextLat, nextLon ) );
	
				((TextView)findViewById( R.id.bearingFromCurrent ) ).setText(
						bearingTo( l.getLatitude()*1E6, l.getLongitude()*1E6, nextLat, nextLon ) );
			}
    	}
    }
    
    private static final String nmTotal( ) {
		float sum = 0;

		Iterator<WaypointItem>it = Utils.getUtils().getOpenFlight().getCurrentCourse().getWaypoints().iterator();
		final float[] s = new float[1];
		
		if ( it.hasNext() ) {		
			WaypointItem w1 = it.next();
			while ( it.hasNext() ) {
				final WaypointItem w2 = it.next();			
				Location.distanceBetween(w1.getPoint().getLatitudeE6()/1E6, w1.getPoint().getLongitudeE6()/1E6, 
						w2.getPoint().getLatitudeE6()/1E6, w2.getPoint().getLongitudeE6()/1E6,
						s);
				sum = sum + s[0];
				w1 = w2;
			}
		}
		
		return String.format( ONED_FMT, sum / (1 / METERS_PER_NM)  );
	}

    private static final String nmBetween( double latStart, double lonStart, double latEnd, double lonEnd ) {

		final float[] results = new float[1]; 
		Location.distanceBetween( latStart/1E6, lonStart/1E6, latEnd/1E6, lonEnd/1E6, results );		
		
		return String.format( ONED_FMT, results[ 0 ] / (1 / METERS_PER_NM) );
	}
	
    private static final String bearingTo( double latStart, double lonStart, double latEnd, double lonEnd ) {

		final GeoPoint gp1 = new GeoPoint( latStart/1e6, lonStart/1e6 );
		final GeoPoint gp2 = new GeoPoint( latEnd/1e6, lonEnd/1e6 );
		
		return String.format( NOD_FMT, gp1.bearingTo( gp2 ) );
	}

	@Override
	public void onLocationChanged(Location l) {
		doLocationDependents( l );		
	}

	@Override
	public void onProviderDisabled(String arg0) {		
	}

	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}	

}

     
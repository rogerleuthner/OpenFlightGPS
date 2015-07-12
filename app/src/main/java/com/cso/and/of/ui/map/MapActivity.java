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

package com.cso.and.of.ui.map;

import java.util.Date;

import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.overlay.OpenStreetMapViewDirectedLocationOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.overlay.ScaleBarOverlay;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.cso.and.of.OpenFlight;
import com.cso.and.of.config.PreferencesMap;
import com.cso.and.of.config.Utils;
import com.cso.and.of.gps.FREE.R;
import com.cso.and.of.location.CenteredOwnshipOverlay;
import com.cso.and.of.location.waypoint.Course;
import com.cso.and.of.location.waypoint.CourseDBAdapter;
import com.cso.and.of.location.waypoint.WaypointOverlay;
import com.cso.and.of.osm.OpenFlightMapRenderer;
import com.cso.and.of.osm.OpenFlightMapTileProviderDirect;
import com.cso.and.of.osm.OpenFlightMapView;
import com.cso.and.of.ui.ImageMap;
import com.cso.and.of.util.Fetcher;

public abstract class MapActivity extends FileActivity {	
	// action bar menu items
	public static final float MAP_BORDER_BUFFER = 0.1f;
	public static final int ACTION_EAST = 99;
	public static final int ACTION_WEST = 98;
	public static final int ACTION_NORTH = 97;
	public static final int ACTION_SOUTH = 96;
	public static final int ACTION_TOGGLE_CENTERING = 95;
	private CenteredOwnshipOverlay myLocationOverlay = null;	
	private WaypointOverlay waypointOverlay = null;
	
	// only view a single map at a time
	protected OpenFlightMapView mapView;
	private GeoPoint homeCoords;
	protected MapInfo mapInfo;
	
	public MapActivity() {
		super();
	}
	
	public abstract int getMinZoom();
	public abstract int getMaxZoom();
	public abstract int getDefaultLocatedZoom();
	
	@SuppressLint("InlinedApi")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        
        if ( Utils.getUtils().isMaxFeatureOS() ) {        
	        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);        
	        setTheme( R.style.ActionBar );
        } else {       
// TODO how to provide the functionality for the cellphone users ?????        	
            // delete notification area to give more real estate
        	requestWindowFeature( Window.FEATURE_NO_TITLE );  
        	getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );          
        }
        
        // gather passed in data
        mapInfo = (MapInfo)this.getIntent().getExtras().get( FileActivity.DOC_KEY );
        final double[] latLon = (double[])getIntent().getExtras().get( FileActivity.LOC_KEY );
        final int zoom = getIntent().getExtras().getInt( FileActivity.ZOM_KEY );
        
        // allow system to create this class only after the intent has been loaded with the extra
        buildTheChart( );        
        
		// if zoomed, override and preserve
		if ( zoom > 0 )
			mapView.getController().setZoom( zoom );        
		// if there is a latlon, go there
		if ( latLon != null )
			mapView.getController().animateTo( new GeoPoint( latLon[0], latLon[1] ) );
        
        setContentView( mapView );
    }	

	
	/**
	 * To be used only on scale, geocorrect maps (sectionals, wacs, enroutes, tacs)
	 * 
	 * @return OpenStreetMapViewOverlay
	 */
	protected OpenStreetMapViewOverlay createScaleBarOverlay() {
        ScaleBarOverlay sbo = new ScaleBarOverlay( this );
        sbo.setVisible( true );
        sbo.enableScaleBar();
        sbo.setLineWidth( 5.0f );
        sbo.drawLatitudeScale( true );
        sbo.drawLongitudeScale( true );
        sbo.setNautical();
        // TODO this text size should take into account the screen size
        sbo.setTextSize( 30 );
        // offset y down to avoid the actionbar if present
        sbo.setScaleBarOffset( 5, Utils.getUtils().isMaxFeatureOS() ? 60 : 5 );
        return sbo;
	}
		
	/**
	 * Add the waypoint/course overlay.  If there is a current course, preserve it as user
	 * might either be editing a course or following a course over a boundary or between
	 * chart types.  Otherwise, create a course as there is always a 'course' associated
	 * with a session - that way, even unsaved courses are preserved across maps.
	 * 
	 * Note that this means that the waypoint list is shared between the global application
	 * object contained 'course' and the list maintained/displayed by the waypointoverlay
	 */
	protected void addWaypointOverlay(  ) {
		// waypoint overlay requires the context menu for waypoint manipulation
		registerForContextMenu( mapView );
		
		// note that myLocationOverlay will be null in the 'hard stop' event
		waypointOverlay = new WaypointOverlay( this, mapView, myLocationOverlay != null ? myLocationOverlay.getDataOverlay(): null );
		mapView.getOverlays().add( waypointOverlay );

		// TODO want to avoid adding the waypoint menu unless we actually execute this method !!
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Waypoint");

		menu.add( 0, WaypointOverlay.CANCEL, WaypointOverlay.CANCEL, "Cancel" );
		menu.add( 0, WaypointOverlay.MOVE, WaypointOverlay.MOVE, "Move/Finish Move" );
		menu.add( 0, WaypointOverlay.INFO, WaypointOverlay.INFO, "Info/Edit" );
		menu.add( 0, WaypointOverlay.DELETE, WaypointOverlay.DELETE, "Delete" );		
	}    
	@Override
	public boolean onContextItemSelected(MenuItem item) {    	

		if ( waypointOverlay != null ) {
			waypointOverlay.handleWaypointSelected( item );
			return true;
		}
		return false;

	} 		
	
	
	private final void buildOwnshipOverlay( ) {		
		myLocationOverlay = new CenteredOwnshipOverlay( mapView.getContext(), (OpenStreetMapView)mapView, mapInfo );
		
		initOverlay();
		
		mapView.getOverlays().add( myLocationOverlay );	  			
	}
	
	// call to setup at initial or at any pause/backgrounding where menu items may have been changed
	private void initOverlay() {
		if ( myLocationOverlay != null ) {			

			if ( Utils.getUtils().getPrefs().isCompassEnabled() )
				myLocationOverlay.enableCompass();
			else
				myLocationOverlay.disableCompass();				

			myLocationOverlay.setLocationUpdateMinDistance( Utils.getUtils().getPrefs().getUpdateDistGPS() );
	        myLocationOverlay.setLocationUpdateMinTime( Utils.getUtils().getPrefs().getUpdateIntervalGPS() );
			
			if ( Utils.getUtils().getPrefs().getUseGPS() )
				myLocationOverlay.enableMyLocation();   // this has to be executed to allow it to be visible, ever
			else
				myLocationOverlay.disableMyLocation();	  	        
	        	        
	        myLocationOverlay.setVisible( true );
	        myLocationOverlay.followLocation( Utils.getUtils().getPrefs().getKeepOwnshipCentered() );	
	        
	        mapView.invalidate();
		}		
	}	
	
	protected void addMyLocationOverlay() {
		if ( Utils.getUtils().getPrefs().getUseGPS() &&
				mapInfo.getMe().isGeoReferenced() ) {	
			
			buildOwnshipOverlay();
		}
	}
	
	protected OpenStreetMapViewOverlay createDirectedLocationOverlay() {
		OpenStreetMapViewDirectedLocationOverlay dlo = new OpenStreetMapViewDirectedLocationOverlay( this );
		dlo.setVisible( true );
		dlo.setShowAccuracy( true );
		
		return dlo;
	} 	
	
	/**
	 * Must be called to actually view the chart; separate from the create since some extenders
	 * have to do setup for some of the arguments via dialog or preferences determined at runtime
	 */
	private void buildTheChart( ) {
		
		try {
			// using 512 tile, best for all devices on 9
			openStreetMapViewBuilder( mapInfo, getMinZoom(), getMaxZoom(), 9 );		
			
	        Date now = new Date( System.currentTimeMillis() );    
	        Date expire = mapInfo.getExpireDate();
	        if ( expire.before( now ) )
	        	showExpireAlert( expire );        
		        
		} catch ( Exception e ) {
			new AlertDialog.Builder(this).setMessage("Exception loading map, delete possibly bad map?")
			   .setCancelable(false)
			   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int id) {
				try {							
					Fetcher fetcher = new Fetcher( mapInfo.getMe(), mapInfo.getDirectoryKey() );
					
					fetcher.remove( MapActivity.this );					
					
					finish();
					
				} catch ( Exception e ) {
					// TODO 
					Utils.getUtils().notify( "Can't remove file: " + e.getMessage(),
							"Delete Failed", this.getClass(), null );									
					}
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

	}

    protected void openStreetMapViewBuilder( MapInfo mapInfo, int minZoom, int maxZoom, int tileZoom ) {	     
    	String filePath = Utils.getUtils().getFullArchivePath( mapInfo );
    	
		// latitudes in NA are always negative, and FAA has 'min' as always being the mathematical min
		homeCoords = new GeoPoint( 
				mapInfo.getLatitudeMin() - 
					( ( Math.abs( mapInfo.getLatitudeMin() ) - Math.abs( mapInfo.getLatitudeMax() ) ) / 2 ),
				mapInfo.getLongitudeMin() + ( ( mapInfo.getLongitudeMax() - mapInfo.getLongitudeMin() ) / 2 ) );
			
		OpenFlightMapTileProviderDirect ofmtpd = new OpenFlightMapTileProviderDirect( filePath, new SimpleInvalidationHandler() );
		
		OpenFlightMapRenderer ofmr = new OpenFlightMapRenderer( null, minZoom, maxZoom, tileZoom, OpenFlight.FILE_TYPE );		
		
    	mapView = new OpenFlightMapView( this, ofmr, ofmtpd  );
    			
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);			
				
        // have a rough location of the map center so it can be reset w/o GPS coords;
        // GPS coords, if obtained, will overwrite the homeCoords as retrieved from config file
        // for those products who are georeferenced
		mapView.getController().setCenter( homeCoords );   	

		// if the location might be somewhat good, zoom in about halfway++
		if ( getMe().isGeoReferenced() && Utils.getUtils().isGPSEnabled() && Utils.getUtils().getLastLocation( null ) != null )
			mapView.getController().setZoom( getDefaultLocatedZoom() );
		else
			mapView.getController().setZoom( getMinZoom() );
    }
        
    public MapInfo getMapInfo() {
    	return mapInfo;
    }
    
	/**
	 * For now single options menu is shared by all
	 * TODO some of these should go in the super so are shared by extenders
	 */
	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		getMenuInflater().inflate( R.menu.map_options, menu);

		MenuItem menuItem = menu.findItem( R.id.preferences_menu );
		menuItem.setIntent(new Intent(this, PreferencesMap.class));		
		
		menuItem = menu.findItem( R.id.delete_map );
		menuItem.setOnMenuItemClickListener( deleteMapListener );			

		menuItem = menu.findItem( R.id.force_download );
		menuItem.setOnMenuItemClickListener( forceDownloadListener );			

		menuItem = menu.findItem( R.id.reset_view );
		menuItem.setOnMenuItemClickListener( resetListener );	
		
		menuItem = menu.findItem( R.id.find_me );
		menuItem.setOnMenuItemClickListener( findMeListener );		
		
		// waypoint/course
		// TODO try to move all of the course stuff into it's overlay
		menuItem = menu.findItem( R.id.course_save );
		menuItem.setOnMenuItemClickListener( saveCourseListener );			

		menuItem = menu.findItem( R.id.course_saveas );
		menuItem.setOnMenuItemClickListener( saveAsCourseListener );					
		
		menuItem = menu.findItem( R.id.course_load );
		menuItem.setOnMenuItemClickListener( loadCourseListener );					
		
		menuItem = menu.findItem( R.id.course_clear );
		menuItem.setOnMenuItemClickListener( clearCourseListener );					
		
		menuItem = menu.findItem( R.id.course_delete );
		menuItem.setOnMenuItemClickListener( deleteCourseListener );							
		
		if ( Utils.getUtils().isMaxFeatureOS() ) {		
			MenuItem actionItem = menu.add( 1, ACTION_TOGGLE_CENTERING, 1, "Toggle Centering");
	        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	        // at startup, the preference determines the icon/centering state
	        if ( Utils.getUtils().getPrefs().getKeepOwnshipCentered() )
	        	actionItem.setIcon( R.drawable.ic_menu_mylocation );
	        else
	        	actionItem.setIcon( R.drawable.ic_menu_notmylocation );
	        actionItem.setOnMenuItemClickListener( toggleCenteringListener );		
			
	        actionItem = menu.add( 1, ACTION_WEST, 2, "West Map");
	        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	        actionItem.setIcon( R.drawable.leftarrow );		
	        actionItem.setOnMenuItemClickListener( mapNavListener );				
	        
			// add special directional buttons on the action bar
	        actionItem = menu.add( 1, ACTION_EAST, 3, "East Map");
	        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	        actionItem.setIcon( R.drawable.rightarrow );		
	        actionItem.setOnMenuItemClickListener( mapNavListener );
	
	        actionItem = menu.add( 1, ACTION_NORTH, 4, "North Map");
	        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	        actionItem.setIcon( R.drawable.uparrow );		
	        actionItem.setOnMenuItemClickListener( mapNavListener );
	
	        actionItem = menu.add( 1, ACTION_SOUTH, 5, "South Map");
	        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
	        actionItem.setIcon( R.drawable.downarrow );		
	        actionItem.setOnMenuItemClickListener( mapNavListener );
		}
		
		return true;
	} 	
	
	protected OnMenuItemClickListener findMeListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick( MenuItem m ) {
			if ( Utils.getUtils().getOpenFlight().getSelectMap().goToMyLocation() )
				MapActivity.this.finish();
				
			return true;
		}
	};  	
	
	protected OnMenuItemClickListener toggleCenteringListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick( MenuItem m ) {
			if ( myLocationOverlay != null ) {			
				
				if ( Utils.getUtils().getPrefs().getKeepOwnshipCentered() ) {
					m.setIcon( R.drawable.ic_menu_notmylocation );
					myLocationOverlay.disableMyLocation();
					Utils.getUtils().getPrefs().setKeepOwnshipCentered( false );
					myLocationOverlay.followLocation( false );
				} else {
					m.setIcon( R.drawable.ic_menu_mylocation );
					myLocationOverlay.enableMyLocation();
					Utils.getUtils().getPrefs().setKeepOwnshipCentered( true );
					myLocationOverlay.followLocation( true );
				}
				
				mapView.invalidate();
			}
			return true;
		}
	};	
	
	// only active if this is max feature OS
	protected OnMenuItemClickListener mapNavListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick( MenuItem m ) {

			double lat, lon;
					
			// take center of current display
			// find lat/lon of that - use that lat lon to find adjacent map
			GeoPoint p = mapView.getMapCenter();
			lat = p.getLatitudeE6()/1E6;
			lon = p.getLongitudeE6()/1E6;
			
			switch( m.getItemId() ) {
				case ACTION_WEST:
					lon = mapInfo.getLongitudeMin() - MAP_BORDER_BUFFER;
					break;
				case ACTION_EAST:
					lon = mapInfo.getLongitudeMax() + MAP_BORDER_BUFFER;
					break;
				case ACTION_NORTH:
					lat = mapInfo.getLatitudeMax() + MAP_BORDER_BUFFER;
					break;
				case ACTION_SOUTH:
					lat = mapInfo.getLatitudeMin() - MAP_BORDER_BUFFER;
					break;
				default: ;
			}
			
			// if we successfully switched, close current otherwise do nothing
			if ( Utils.getUtils().getOpenFlight().getSelectMap().goToMap( lat, lon, mapView.getZoomLevel() ) )
				finish();
			else
				Toast.makeText( mapView.getContext(), "Don't have the map for your location (" + lat +
						", " + lon + ") downloaded", Toast.LENGTH_SHORT ).show();
			
			return true;
		}
	};	

	protected OnMenuItemClickListener resetListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick( MenuItem m ) {
			return resetView( );
		}
	};  
	
	protected OnMenuItemClickListener deleteMapListener = new OnMenuItemClickListener() {
		
		@Override
		public boolean onMenuItemClick( MenuItem m ) {
			
	    	new AlertDialog.Builder( MapActivity.this ).setMessage("Delete map?" )
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
			   			try {							
							Fetcher fetcher = new Fetcher( mapInfo.getMe(), mapInfo.getDirectoryKey() );
							
							Utils.getUtils().notify( "Removing: " + mapInfo.getDirectoryKey(), "Delete map", this.getClass(), null );
							
							fetcher.remove( MapActivity.this );
							
							// kill this chart, since we deleted it
							MapActivity.this.finish();
							
						} catch ( Exception e ) {
							// TODO 
							Utils.getUtils().notify( "Can't remove file: " + e.getMessage(),
									"Delete Failed", this.getClass(), null );									
						}
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       })
		       .create()
		       .show();				
			
			return true;
		}
	};	
	
	protected OnMenuItemClickListener forceDownloadListener = new OnMenuItemClickListener() {
		
		@Override
		public boolean onMenuItemClick( MenuItem m ) {
			
			try {							
				Fetcher fetcher = new Fetcher( mapInfo.getMe(), mapInfo.getDirectoryKey() );
				
				Utils.getUtils().notify( "Attempting to download: " + mapInfo.getDirectoryKey(),
						"Downloading...", this.getClass(), null );
				fetcher.fetch( MapActivity.this, false /*imagemap already thinks we have this chart*/ );
				return true;
			} catch ( Exception e ) {
				// TODO 
				Utils.getUtils().notify( "Failed download: " + e.getMessage(),
						"Failed", this.getClass(), null );									
			}
			return false;
		}
	};
	
	/**
	 * Recenter the view -
	 * 	if the GPS is enabled and location is available, make sure the current map displayed contains
	 *  the location coords - if it does not, ask the user if they want to open the map containing
	 *  the current GPS location
	 *  
	 *  default falls through to centering on the homecoords as retrieved from the map description XML
	 * 
	 * @return
	 */
    public boolean resetView( ) {
    	
    	if ( getMe().isGeoReferenced() &&
    			Utils.getUtils().isGPSEnabled() &&
    			myLocationOverlay != null && 
    			myLocationOverlay.isVisible() &&
    			myLocationOverlay.isMyLocationEnabled() ) {
    		
    		final String map = Utils.isOwnshipOnMap( myLocationOverlay, mapInfo );

    		if ( map != null ) {
    			
        		// if we already have this map up, just use it,
        		if ( this.mapInfo.getDirectoryKey().equals( map ) ) {
        			
	        		mapView.getController().setCenter( myLocationOverlay.getMyLocation() );
	        		mapView.invalidate();	        		
	        		
        		} else { // see if they want to go to the new map area
        		
        			new AlertDialog.Builder(this).setMessage("Open the map with current position?")
						.setCancelable(false)
						.setPositiveButton("Yes", 
							new DialogInterface.OnClickListener() {
			     			   public void onClick(DialogInterface dialog, int id) {
			     				   ImageMap im = Utils.getUtils().getOpenFlight().getSelectMap().getCurrentImageMap();
			     				   
			     				   final Intent i = new Intent( MapActivity.this, im.getMe().getClazz() );
			     				   im.getCurrentMapInfo().setDirectoryKey( map );
			     				   i.putExtra(FileActivity.DOC_KEY, im.getCurrentMapInfo() );
			     				   startActivity( i );	
			     				   // stop myself now that a new map is opening
			     				   MapActivity.this.finish();			     				   
			     			   }
							})
						.setNegativeButton("No", new DialogInterface.OnClickListener() {
		     			       public void onClick(DialogInterface dialog, int id) {
		     			            dialog.cancel();
		     			    		mapView.getController().setCenter( homeCoords );
		     			    		mapView.invalidate();		     			            
		     			       }
		     			  	})
		     			.create()
		     			.show();	 
        			
        		}
    			return true;        		
    		}
    	}
    	
    	// either don't have the map we're "at", or no GPS
    		
    	if ( homeCoords != null ) {
    		mapView.getController().setCenter( homeCoords );
    		mapView.invalidate();
    		return true;
    	} 
    	
    	return false;
    }
    
    /**
     * Save current course under a new name or the same name.
     */
	protected OnMenuItemClickListener saveCourseListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick( MenuItem m ) {
			
			final EditText input = new EditText(MapActivity.this);
			if ( Utils.getUtils().getOpenFlight().getCurrentCourse() != null )
				input.setText( Utils.getUtils().getOpenFlight().getCurrentCourse().getName() );
        	input.selectAll();

        	
			new AlertDialog.Builder(MapActivity.this)
			    .setTitle("Course Name")
			    .setMessage("Enter or edit Course Name")
			    .setView(input)
			    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			        	
			        	Utils.getUtils().getOpenFlight().getCurrentCourse().saveCourse( input.getText().toString() );
			        }
			    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            // Do nothing.
			        }
			    }).show();	
			

			return true;
		}
	};    
	
	/**
	 * Save the course as a new course, leaving any existing courses alone.  
	 * If they type the same name, then two courses with the same
	 * name will appear in the course chooser list.
	 */
	protected OnMenuItemClickListener saveAsCourseListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick( MenuItem m ) {
			
			final EditText input = new EditText(MapActivity.this);
			if ( Utils.getUtils().getOpenFlight().getCurrentCourse() != null )
				input.setText( Utils.getUtils().getOpenFlight().getCurrentCourse().getName() + " " + CourseDBAdapter.UNSAVED );
        	input.selectAll();
        	
			new AlertDialog.Builder(MapActivity.this)
			    .setTitle("New Course Name")
			    .setMessage("Edit Name")
			    .setView(input)
			    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {			        	
			        	Utils.getUtils().getOpenFlight().getCurrentCourse().saveCourseAs( input.getText().toString() );
			        }
			    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            // Do nothing.
			        }
			    }).show();	
			

			return true;
		}
	}; 	
	
	protected OnMenuItemClickListener loadCourseListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick( MenuItem menuItem ) {	
			final String[] courses = CourseDBAdapter.getCourseNames().toArray( new String[0] );		
			final AlertDialog loadDialog = new AlertDialog.Builder( MapActivity.this )					        
		        .setItems( courses, 
	        		new DialogInterface.OnClickListener() {
			            public void onClick( DialogInterface dialog, int which ) {			            	
			            	
			            	Course course = Utils.getUtils().getOpenFlight().getCurrentCourse().loadCourse(courses[ which ], 
			            			mapView );
			            	
			            	if ( course.getWaypoints() != null && course.getWaypoints().size() > 0 ) {
			            		final double lat = course.getWaypoints().get( 0 ).getPoint().getLatitudeE6()/1E6;
			            		final double lon = course.getWaypoints().get( 0 ).getPoint().getLongitudeE6()/1E6;
			            		
				    			// if we successfully switched, close current otherwise do nothing
				    			if ( Utils.getUtils().getOpenFlight().getSelectMap().goToMap( lat, lon, mapView.getZoomLevel() ) )
				    				finish();
				    			else
				    				Toast.makeText( mapView.getContext(), "Don't have the map for your location (" + lat +
				    						", " + lon + ") downloaded", Toast.LENGTH_SHORT ).show();	
			            	}				            	
			            }
		        	} )
		        .setTitle("Select Course to Load")
		        .create();
			
			if ( Utils.getUtils().getOpenFlight().getCurrentCourse().isDirty() ) {
				new AlertDialog.Builder( MapActivity.this )
					.setTitle( "Unsaved changes" )
					.setMessage( "'" + Utils.getUtils().getOpenFlight().getCurrentCourse().getName() + "' " + "has unsaved changes, save it first?" )
					.setPositiveButton( "Yes", 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								CourseDBAdapter.saveCourse();
							}
						}).setNeutralButton( "No", new DialogInterface.OnClickListener() {
							public void onClick( DialogInterface dialog, int whichButton ) {	
								loadDialog.show();
							}
						})
					.create()
					.show();				
			} else {
				loadDialog.show();
			}
					
			return true;
		}
	}; 	
	
	protected OnMenuItemClickListener clearCourseListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick( MenuItem menuItem ) {		
			
			if ( Utils.getUtils().getOpenFlight().getCurrentCourse().isDirty() ) {
				new AlertDialog.Builder( MapActivity.this )
					.setTitle( "Unsaved changes" )
					.setMessage( "'" + Utils.getUtils().getOpenFlight().getCurrentCourse().getName() + "' " + "has unsaved changes, save it first?" )
					.setPositiveButton( "Yes", 
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								
								final EditText input = new EditText(MapActivity.this);
								
								if ( Utils.getUtils().getOpenFlight().getCurrentCourse() != null )
									input.setText( Utils.getUtils().getOpenFlight().getCurrentCourse().getName() );	
								
					        	input.selectAll();
					        	
								new AlertDialog.Builder(MapActivity.this)
								    .setTitle("Course Name")
								    .setMessage("Type Course Name")
								    .setView(input)
								    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
								        public void onClick(DialogInterface dialog, int whichButton) {
								        	Utils.getUtils().getOpenFlight().getCurrentCourse().saveCourse(input.getText().toString());
								        	Utils.getUtils().getOpenFlight().getCurrentCourse().clearCourse( mapView );
								        }
								    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								        public void onClick(DialogInterface dialog, int whichButton) {
								            // Do nothing.
								        }
								    }).show();									
								

							}
						}).setNeutralButton( "No", new DialogInterface.OnClickListener() {
							public void onClick( DialogInterface dialog, int whichButton ) {	
								Utils.getUtils().getOpenFlight().getCurrentCourse().clearCourse( mapView );
							}
						})
					.create()
					.show();				
			} else {
				Utils.getUtils().getOpenFlight().getCurrentCourse().clearCourse( mapView );
			}				
			
			return true;
		}
	}; 	
	
	protected OnMenuItemClickListener deleteCourseListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick( MenuItem menuItem ) {			

			new AlertDialog.Builder( MapActivity.this )
			.setTitle( "Confirm delete course" )
			.setMessage( "Delete course: '" + Utils.getUtils().getOpenFlight().getCurrentCourse().getName() + "'?"  )
			.setPositiveButton( "Yes", 
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Utils.getUtils().getOpenFlight().getCurrentCourse().deleteCourse( mapView );
					}
			}).setNeutralButton( "No", new DialogInterface.OnClickListener() {
				public void onClick( DialogInterface dialog, int whichButton ) {
					// do nothing
				}
			}).create()
			.show();			
					
			return true;
		}
	}; 	
	    
    // redraw the map when a real map tile has been read in
	private class SimpleInvalidationHandler extends Handler {

		@Override
		public void handleMessage(final Message msg) {
			if ( msg != null ) {
				switch (msg.what) {
					case OpenStreetMapTile.MAPTILE_SUCCESS_ID:
						if ( mapView != null ) {
							mapView.invalidate();
							break;
						}
				}
			}
		}
	}    
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		cleanStart();
	}	
	
	@Override protected void onPause() {
		super.onPause();
		
		cleanPauseStop();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		cleanPauseStop();
	}
	
	@Override 
	protected void onDestroy() {
		super.onDestroy();
		
		myLocationOverlay = null;			
		mapView = null;
		homeCoords = null;
		mapInfo = null;
	}
	
    @Override
    public final void onBackPressed() {
    	new AlertDialog.Builder(this).setMessage("Exit Map?")
	       .setCancelable(false)
	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   cleanPauseStop();
	        	   MapActivity.super.onBackPressed();
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
	       
	protected void cleanPauseStop() {
		// suspend any listeners
 	   if ( myLocationOverlay != null ) {
		   myLocationOverlay.disableMyLocation();
		   myLocationOverlay.disableCompass();
 	   }	
		
		Utils.getUtils().cancelNotify();
		mapView.setKeepScreenOn( false );
	}
	
	
	
	// call after pause or at map init to get things going
	@SuppressLint("NewApi")
	protected void cleanStart() {
		// restart location
		if ( myLocationOverlay != null ) {
			initOverlay();		
		}
		
		if ( Utils.getUtils().isMaxFeatureOS() ) {
			// this has to go here and not in oncreate since in oncreate the bar will not exist yet
	        final ActionBar ab = getActionBar();
	        if ( ab != null ) {
	        	ab.setDisplayShowTitleEnabled( true );
	        	ab.setDisplayUseLogoEnabled( false );
	        	ab.setDisplayShowHomeEnabled( false );
	        	ab.setTitle( this.mapInfo.getDirectoryKey() );
	        }		
		}

		mapView.setKeepScreenOn( Utils.getUtils().getPrefs().isKeepScreenOn() );		
	}
	
	private void showExpireAlert( Date date ) {
		
    	new AlertDialog.Builder(this).setMessage("Map is expired: " + date.toGMTString() + ".  Download a new version?" )
	       .setCancelable(true)
	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   try {
					Fetcher fetcher = new Fetcher( getMe(), mapInfo.getDirectoryKey() );
					fetcher.fetch( MapActivity.this, false /*imagemap already thinks we have this chart*/ );
					Utils.getUtils().getOpenFlight().getSelectMap().getCurrentImageMap().rebuild();
				} catch (Exception e) {
					throw new RuntimeException ( e.getMessage() );
				}
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


}




//// MINIMAP
//final RelativeLayout masterLay = new RelativeLayout(this);  
// createMinimapOverlay( masterLay );
// masterLay.addView( mapView, new RelativeLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ) );  	
//protected void createMinimapOverlay( RelativeLayout masterLay ) {
//		/*
//		 * Create another OpenStreetMapView, that will act as the MiniMap for the 'MainMap'.
//		 * They will share the TileProvider.
//		 */
//		OpenStreetMapView mOsmvMinimap = new OpenStreetMapView( getApplicationContext(), ofmr, mapView );
//		final int aZoomDiff = 3; // Use OpenStreetMapViewConstants.NOT_SET to disable
//									// autozooming of this
//									// minimap
//		mapView.setMiniMap(mOsmvMinimap, aZoomDiff);
//
//		/*
//		 * Create RelativeLayout.LayoutParams that position the MiniMap on the top-right corner
//		 * of the RelativeLayout.
//		 */
//		final RelativeLayout.LayoutParams minimapParams = new RelativeLayout.LayoutParams(90,
//				90);
//		minimapParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		minimapParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//		minimapParams.setMargins(5, 5, 5, 5);
//		masterLay.addView(mOsmvMinimap, minimapParams);			
//}	
    
	// MINIMAP
//	OpenFlightMapView miniMe = new OpenFlightMapView( mapView.getContext(), ofmr, ofmtpd  );
//	miniMe.setVisibility( View.VISIBLE );
//	miniMe.setOverrideMiniMapVisiblity(View.VISIBLE);
//	mapView.setMiniMap( miniMe, 3 );
//	mapView.setOverrideMiniMapVisiblity( View.VISIBLE );







//protected OnMenuItemClickListener myLocationListener = new OnMenuItemClickListener() {
//@Override
//public boolean onMenuItemClick( MenuItem m ) {
//	
//	// if it's off, turn it on; otherwise turn it off
//	// in either case, show the change
//	if ( ! myLocationOverlay.isVisible() ) {
//		
//		myLocationOverlay.toggleMyLocation();
//		
//		Utils.getUtils().enableGPS( mapView.getContext() );
//		
//		// warn
//		if ( myLocationOverlay.getMyLocation() == null ) {
//			Toast.makeText( mapView.getContext(), "Searching for location, ownship will appear when found", Toast.LENGTH_SHORT ).show();
//		}
//		
//		myLocationOverlay.setVisible( true );
//		
//	} else {
//		myLocationOverlay.toggleMyLocation();
//		myLocationOverlay.setVisible( false );
//	}
//	
//	mapView.invalidate();
//	
//	return true;
//}
//};	


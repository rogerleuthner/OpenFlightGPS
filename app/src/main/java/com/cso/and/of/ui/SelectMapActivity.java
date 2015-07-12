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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.cso.and.of.BaseActivity;
import com.cso.and.of.config.DocActivity;
import com.cso.and.of.config.Preferences;
import com.cso.and.of.config.SplashActivity;
import com.cso.and.of.config.Utils;
import com.cso.and.of.gps.FREE.R;
import com.cso.and.of.location.waypoint.Course;
import com.cso.and.of.location.waypoint.CourseDBAdapter;
import com.cso.and.of.types.AvailableProducts;
import com.cso.and.of.ui.map.FileActivity;
import com.cso.and.of.ui.map.MapInfo;
import com.cso.and.of.util.Fetcher;

/**
 * Central point for starting map/fileviewer activities.  This may be killed/restarted during program execution.
 * 
 * @author Roger
 *
 */

public class SelectMapActivity extends BaseActivity implements OnGestureListener {
    
	private ImageView imageView = null;
	private ImageMap currentImageMap = null;
	  // cache, by enum ordinal; note that the cache is large enough to contain all existing,
	  // when only a subset are ultimately availabe to this user
	private ImageMap[] imageMapCache = new ImageMap[ AvailableProducts.values().length ];
	private Location location = null;
	protected Dialog mSplashDialog;	
	
    private static final int SWIPE_MIN_DISTANCE = 75;
    private static final int SWIPE_THRESHOLD_VELOCITY = 50;	
//	private Animation slideLeftIn;
//	private Animation slideLeftOut;
//	private Animation slideRightIn;
//    private Animation slideRightOut;
//    private ViewFlipper viewFlipper;	
    private GestureDetector gestureDetector;
    
	public ImageMap getCurrentImageMap() { return currentImageMap; }	
	
	/*
	 * This selectmap is over; app may still be running, but everything will require reinitialization.
	 * 
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		imageView = null;
		currentImageMap = null;
		imageMapCache = null;
		location = null;
		mSplashDialog = null;			
	    gestureDetector = null;			    
	}
	
	/**
	 * Rebuild the image map cache, removing all then rebuilding the currently open one.
	 * 
	 */
	public void compactImageMapCache() {
		AvailableProducts currentOpen = currentImageMap.getMe();
		
		// this prob isn't necessary
		for( ImageMap im : imageMapCache ) {			
			if ( im != null ) {
				im = null;
			}
		}
		
		imageMapCache = new ImageMap[ AvailableProducts.values().length ];
		doImageMap( currentOpen );
	}
	
    @Override
    protected void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
		
        setContentView( R.layout.map_selector );    
        
        // causing CRASHING when quitting IFR maps on the Galaxy
        // + the slide out animation is not working right
//        viewFlipper = (ViewFlipper)findViewById( MyR.id.get( "flipper" ) );
//        
//        slideLeftIn = AnimationUtils.loadAnimation(this, MyR.anim.get( "slide_left_in" ) );
//        slideLeftOut = AnimationUtils.loadAnimation(this, MyR.anim.get( "slide_left_out" ) );
//        slideRightIn = AnimationUtils.loadAnimation(this, MyR.anim.get( "slide_right_in" ) );
//        slideRightOut = AnimationUtils.loadAnimation(this, MyR.anim.get( "slide_right_out" ) );        
        
        // try to get it from saved state first
//        if ( getLastNonConfigurationInstance() != null ) {
//        	imageMapCache = (ImageMap[])getLastNonConfigurationInstance();
//        	p = AvailableProducts.valueOf( savedInstanceState.getString( "product" ) );
//        } else {
//        	imageMapCache = new ImageMap[ AvailableProducts.values().length ];
    		// fire off the splash screen, this time with the info splash
    		// that always shows; this extra 'if' is here so we don't get
    		// two of the initial splash screens if they don't agree.
    		// also, if the activity has been rotated out of memory (sleep for example)
    		// and the chooser is showing, don't want to splash them again.
//    		if ( Utils.getUtils().getPrefs().isDisclaimerSeen() || 
//    				( savedInstanceState != null && ! savedInstanceState.getBoolean( "seensplash" ) ) ) {
//    			
//    			startActivity( new Intent( getApplicationContext(), SplashActivity.class ) );			
//    		}   
    		
//        }

		Utils.getUtils().getOpenFlight( ).setSelectMap( this );      
        
        imageView = (ImageView)findViewById( R.id.imageMap );    
        
        gestureDetector= new GestureDetector(this);        
        
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
            	
                if ( gestureDetector.onTouchEvent( event ) ) {
                    return false;
                }
                
                return true;
            }
        });
    	   
		doImageMap( Utils.getUtils().getPrefs().getHomeScreen() );	
		
//        registerForContextMenu( imageView );          		
	    

    }
    
    @Override 
    protected void onPause() {
    	super.onPause();
    }
        
    @Override
    protected void onResume() {
    	super.onResume();	    	
    }    
    	
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    	// Right to left
        if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        	
        	// go to next cardinal map slot

        	// if map not in the cache, create and insert it
        	AvailableProducts nap = currentImageMap.getMe().getNextOrdinally( );
        	if ( imageMapCache[ nap.ordinal() ] == null ) {			
        		currentImageMap = new ImageMap( nap, imageView, getApplicationContext(), this.getWindowManager().getDefaultDisplay());			
        	    imageMapCache[ nap.ordinal() ] = currentImageMap;

        	// just point to it and go
        	} else {
        		currentImageMap = imageMapCache[ nap.ordinal() ];
        	}
        	currentImageMap.show();
        	Utils.getUtils().notify( nap.getFriendlyName(), "Product Selected", SelectMapActivity.class, null );   
        	Toast.makeText( getApplicationContext(), nap.getFriendlyName(), Toast.LENGTH_SHORT ).show();
        	
//        	viewFlipper.setInAnimation(slideLeftIn);
//            viewFlipper.setOutAnimation(slideLeftOut);
//        	viewFlipper.showNext();        	
        	
            return true; 
            
            // Left to right
        }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        	
        	// go to next prevous map slot

        	// if map not in the cache, create and insert it
        	AvailableProducts nap = currentImageMap.getMe().getPreviousOrdinally( );
        	if ( imageMapCache[ nap.ordinal() ] == null ) {			
        		currentImageMap = new ImageMap( nap, imageView, getApplicationContext(), this.getWindowManager().getDefaultDisplay());			
        	    imageMapCache[ nap.ordinal() ] = currentImageMap;

        	// just point to it and go
        	} else {
        		currentImageMap = imageMapCache[ nap.ordinal() ];
        	}
        	currentImageMap.show();
        	Utils.getUtils().notify( nap.getFriendlyName(), "Product Selected", SelectMapActivity.class, null );  
        	Toast.makeText( getApplicationContext(), nap.getFriendlyName(), Toast.LENGTH_SHORT ).show();
      	
//            viewFlipper.setOutAnimation(slideLeftOut);        	
//        	viewFlipper.setInAnimation(slideRightIn);
//        	viewFlipper.showPrevious();          	
        	        	
            return true;
        }
//        if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//            return false; // Bottom to top
//        }  else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//            return false; // Top to bottom
//        }
        return false;
    }

	@Override
	public void onLongPress(MotionEvent event) {
		
 	   if ( currentImageMap.isState() ) {
		   
	    	new AlertDialog.Builder(this).setMessage( "Open " + currentImageMap.getKey() + "?" )
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		   				showChart();
						currentImageMap.unHighlight();			    
						currentImageMap.reset();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
						currentImageMap.unHighlight();			    
						currentImageMap.reset();

		                dialog.cancel();
		           }
		       })
		       .create()
		       .show();
	   }		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		if ( currentImageMap.isState() ) {
			currentImageMap.unHighlight();
			currentImageMap.reset();
			return true;
			// consider it handled if there was a 'cancel' (move off)
		}
		
		// might be a map switchd
		return false;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		
	    if ( currentImageMap.isState() ) {
			showChart();
			currentImageMap.unHighlight();			
			// don't do imagemap reset yet; as the modal asynch dialog is going to
			// go up, and we need the state data after that dialog completes			
			return true;
	    }
		
		return false;
	}
	
    @Override
    public void onShowPress(MotionEvent event) {
    	if ( currentImageMap.isState() ) {
    		Utils.getUtils().notify( currentImageMap.getKey(), "Map Selected", SelectMapActivity.class, null );
    	}
    }
    
	@Override
	public boolean onDown(MotionEvent event) {

		Utils.getUtils().notify( currentImageMap.getKey(), "Map Selected", SelectMapActivity.class, null );
		if ( currentImageMap.makeCurrentIfExists( event.getRawX(), event.getRawY() ) ) {
			currentImageMap.highlight();  // TODO should that invalidate be done by ImageMap?
		}
		
		// never handle, as it might be a fling on inactive map area
		return false;
	}    

    
//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//    	menu.setHeaderTitle("Select Chart Type");
//    	
//		for( AvailableProducts ap : AvailableProducts.values() )
//			menu.add( ap.getFriendlyName() );
//    }    
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {    	
//    	AvailableProducts ap = AvailableProducts.getProductByFriendlyName( item.toString() );
//    	
//    	doImageMap( ap );
//    	
//    	return true;
//    }     
    
//  
//	final AsyncProgressDialog progress = new AsyncProgressDialog(this, "Loading maps", "Please wait...") {
//		@Override
//		protected void doInBackground() {
//
//			try {
//
//				doImageMap( this, Utils.getUtils().getPrefs().getHomeScreen() );	
//			} catch (Exception ex) {
//				showCompletionDialog("Retrieve failed: ", ex.toString());
//			}
//		}
//
//		@Override
//		protected void onPostExecute(Void unused) {
//			super.onPostExecute(unused);
//			showCompletionDialog("Status", "Maps Recognized" );
//		}
//	};
//
//	progress.execute();	
        
    
//    @Override
//    protected void onSaveInstanceState(Bundle savedInstanceState) {
//    	// TODO if this gets more complicated, implement an interface or object which
//    	// encapsulates the state needing saving
//    	
//    	// save that the splash has been seen
//		super.onSaveInstanceState(savedInstanceState);
//		savedInstanceState.putBoolean("seensplash", true);
//		if ( currentImageMap != null )
//			savedInstanceState.putString("product", currentImageMap.getMe().toString() );
//    }
//    
//    /**
//     * This object is pretty expensive to create, so save it
//     */
//    @Override
//    public Object onRetainNonConfigurationInstance() 
//    {
//      if ( imageMapCache != null )
//          return( imageMapCache );
//      
//      return super.onRetainNonConfigurationInstance();
//    }    

    // delegate initial splash handling to the splash screen
    @Override
    protected void onStart() {
    	super.onStart();
    	SplashActivity.initialSplash( this );
    }	

//    // this is only fired for the initial install
//    @Override
//    public void onActivityResult( int requestCode, int resultCode, Intent data ) {
//    	// quit if initial splash isn't happy
//    	if ( ! SplashActivity.handleActivityResult( requestCode, resultCode ) )
//    		finish();
//
//    	// passed the disclaimer, now make sure they see the info screen on first run
//    	startActivity( new Intent( getApplicationContext(), SplashActivity.class ) );
//    }
	
 
	@Override
	public final boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate( R.menu.shared_options, menu);

		MenuItem menuItem = menu.findItem( R.id.preferences_menu );
		menuItem.setIntent(new Intent(this, Preferences.class));

		menuItem = menu.findItem( R.id.about_menu );
		menuItem.setIntent(new Intent(this, SplashActivity.class).putExtra( SplashActivity.TIMEOUT_KEY, false ));
		
		menuItem = menu.findItem( R.id.doc_menu );
		menuItem.setIntent(new Intent(this, DocActivity.class));
		
		menuItem = menu.findItem( R.id.find_me );
		menuItem.setOnMenuItemClickListener( findMeListener );		
				
		menuItem = menu.findItem( R.id.course_load );
		menuItem.setOnMenuItemClickListener( loadCourseListener );							

		menuItem = menu.findItem( R.id.products_menu );
		menuItem.setOnMenuItemClickListener(								
			new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					new AlertDialog.Builder(SelectMapActivity.this)
			        .setTitle("Select Chart Type")
			        .setItems(AvailableProducts.getAvailableProductNames(), 
			        		new DialogInterface.OnClickListener() {
					            public void onClick(DialogInterface dialog, int which) {
			
					                /* User clicked so do some stuff */
					                String[] items = AvailableProducts.getAvailableProductNames();
					                
					                doImageMap( AvailableProducts.getProductByFriendlyName( items[ which ] ) );
	
					            }
			        	})
			        .create()
			        .show();		
					return true;
				}
			});
		
		return true;
	}    
	
	protected OnMenuItemClickListener loadCourseListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick( MenuItem menuItem ) {			
			final String[] courses = CourseDBAdapter.getCourseNames().toArray( new String[0] );			
			
			new AlertDialog.Builder( SelectMapActivity.this )					        
		        .setItems( courses, 
		        		new DialogInterface.OnClickListener() {
				            public void onClick( DialogInterface dialog, int which ) {		

				            	// load a course into current course and open the starting point chart
				            	Course course = CourseDBAdapter.getCourse( courses[ which ] );
				            	if ( course.getWaypoints() != null && course.getWaypoints().size() > 0 ) {
				            		final double lat = course.getWaypoints().get( 0 ).getPoint().getLatitudeE6()/1E6;
				            		final double lon = course.getWaypoints().get( 0 ).getPoint().getLongitudeE6()/1E6;
				            		SelectMapActivity.this.goToLocation( lat, lon );
				            	}
				        }})
		        .setTitle("Select Course to Open")
		        .create()
		        .show();	
					
			return true;
		}
	}; 		
	
	private OnMenuItemClickListener findMeListener = new OnMenuItemClickListener() {
		@Override
		public boolean onMenuItemClick( MenuItem m ) {
			return goToMyLocation();
		}
	};  	
	
	public boolean goToMap( double lat, double lon, int zoom ) {
		String map = currentImageMap.findByLatLonIfExists( lat, lon );
		
		if ( map != null ) {
			Toast.makeText( this, "Opening chart " + map, Toast.LENGTH_SHORT ).show();
			showCurrentChart( lat, lon, zoom );
			return true;
		} else {
			return false;
		}		
	}
		
	/**
	 * If GPS is on and locked, and we have a map for the location, open the map with the
	 * GPS pointer in place.
	 */
	private boolean setLocationMap( Location location ) {
		String map = currentImageMap.findByLatLonIfExists( location.getLatitude(), location.getLongitude() );
		
		if ( map != null ) {
			Utils.getUtils().notify( "Going to " + map, "Found chart", SelectMapActivity.class, null );
			return true;
		} else {
			Toast.makeText( this, "Don't have the map for your location (" + location.getLatitude() +
					", " + location.getLongitude() + ") downloaded", Toast.LENGTH_SHORT ).show();
			return false;
		}
	}

	// TODO might want to ask user if they want to download if does not exist; this will require
	// knowing the map coordinates without having the .zip file, though
	public boolean goToMyLocation() {
		
		Utils.getUtils().enableGPS( this );		
		
		if ( ! goToLocation( 0, 0 ) ) {
			Toast.makeText( this, "Don't have a location yet", Toast.LENGTH_SHORT ).show();
		}
		
		return false;
	}
	
	public boolean goToLocation( double latitude, double longitude ) {
		
		Location l = Utils.getUtils().getLastLocation( location );
		
		if ( latitude != 0 && longitude != 0 ) {
			if ( l == null )  // no location yet, but have coords passed in
				l = new Location( "dummy" );			
			l.setLatitude( latitude );
			l.setLongitude( longitude );
		}
		
		if ( l != null ) {
			if ( setLocationMap( l ) ) {
				showCurrentChart( l.getLatitude(), l.getLongitude(), 0 /*default zoom*/ );
				return true;
			}
		} // else, no location and no coords
		
		return false;
	}	
	
	private void doImageMap( final AvailableProducts ap ) {
		
		// if map not in the cache, create and insert it
		if ( imageMapCache[ ap.ordinal() ] == null ) {			
			currentImageMap = new ImageMap( ap, imageView, getApplicationContext(), this.getWindowManager().getDefaultDisplay());			
		    imageMapCache[ ap.ordinal() ] = currentImageMap;

		// just point to it and go
		} else {
			currentImageMap = imageMapCache[ ap.ordinal() ];
		}
		currentImageMap.show();
		Utils.getUtils().notify( ap.getFriendlyName(), "Product Selected", SelectMapActivity.class, null );
	}		
	
	private void showChart() {
		try {			
			chart();
		} catch ( Exception e ) {			
			// failed to show the chart; put up an error, but still allow user to 'go to' the 
			// 'chart' since the might want to delete it or force download
			// TODO put up dummy 'chart' with instructions
		}
	}	
	
	
	private void chart() {
		try {							
			Fetcher fetcher = new Fetcher( currentImageMap.getMe(), currentImageMap.getFileName() );
			
			if ( ! fetcher.exists() ) {		
				
				// we are being nice and checking here so user doesn't have to go to the download screen
				download( fetcher );
			// since above fetch is non-blocking, can't just show the map after download
			}  else {
				showCurrentChart( currentImageMap.getFileName() );
			}
			
		} catch ( Exception e ) {
			// TODO 
			Utils.getUtils().notify( "Failed download: " + e.getMessage(),
					"Downloading...", SelectMapActivity.class, null );									
		}		
	}
	
	private void download( final Fetcher fetcher ) {
		new AlertDialog.Builder(this)
		.setIcon( R.drawable.icon )
		.setTitle( "Download '" + currentImageMap.getKey() + "'?" )
		.setMessage( R.string.download_msg )
		.setPositiveButton( "Yes", 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

					try {							
						Utils.getUtils().notify( "Attempting to download: " + currentImageMap.getKey(),
								"Downloading...", SelectMapActivity.class, null );
						fetcher.fetch( SelectMapActivity.this, true );
													
					} catch ( Exception e ) {
						// TODO 
						Utils.getUtils().notify( "Download: " + e.getMessage(),
								"Failed", SelectMapActivity.class, null );									
					}
				}
		}).setNeutralButton( "No", new DialogInterface.OnClickListener() {
			public void onClick( DialogInterface dialog, int whichButton ) {

				Utils.getUtils().notify( "Cancelled",
						"Download", SelectMapActivity.class, null );	
			}
		}).create()
		.show();		
	}
	
	
	private void showCurrentChart( final String key ) {
		final Intent i = new Intent( this, currentImageMap.getMe().getClazz() );
		currentImageMap.getCurrentMapInfo().setDirectoryKey( key );
		i.putExtra(FileActivity.DOC_KEY, currentImageMap.getCurrentMapInfo() );
		startActivity( i );		
	}	
	
	// show the chart with point centered
	private void showCurrentChart( double lat, double lon, int zoom ) {

		// later on we use the mapinfo for things like expire date.
		// so, manually get the mapinfo corresponding to the map at these coords
		// if it exists
				
		MapInfo mi = currentImageMap.getCurrentMapBlockInfo();

		// map may not be on the device
		if ( mi != null ) {
			final Intent i = new Intent( this, currentImageMap.getMe().getClazz() );				
			i.putExtra( FileActivity.DOC_KEY, mi );
			i.putExtra( FileActivity.LOC_KEY, new double[] {lat,lon} );
			i.putExtra( FileActivity.ZOM_KEY, zoom );
			startActivity( i );	
			return;
		}
		Toast.makeText( this, "Can't find map for current location", Toast.LENGTH_SHORT ).show();
	}	

    @Override
    public final void onBackPressed() {    	  
    	
    	String message;
    	
    	if ( Utils.getUtils().getOpenFlight().getCurrentCourse() != null && 
    			Utils.getUtils().getOpenFlight().getCurrentCourse().isDirty() ) {
    		message = "Course has unsaved changes, exit WITHOUT saving?";
    	} else {
    		message = "Exit OpenFlightGPS?";
    	}
    		    	
    	new AlertDialog.Builder(this).setMessage( message )
    	       .setCancelable(false)
    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	        	   Utils.killBill();	
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

     
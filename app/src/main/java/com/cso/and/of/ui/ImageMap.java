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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.view.Display;
import android.widget.ImageView;
import android.widget.Toast;

import com.cso.and.of.OpenFlight;
import com.cso.and.of.config.ParseMetadata;
import com.cso.and.of.config.Utils;
import com.cso.and.of.types.AvailableProducts;
import com.cso.and.of.ui.map.MapInfo;
import com.cso.and.of.util.Fetcher;


/**
 * Handle image map display highlighting, lookups and selection.  Load the image map
 * from XML resource as created by GIMP image map creator, with the references as "File".
 * Use the name of the map files directory as the file name.
 * 
 * To avoid multiple lookups, a 'current' pointer is maintained to the currently found map entry
 * when the 'exists' method is called.  Function of all other methods are dependent upon that
 * current pointer (so make sure exists is called and returns true, or nothing will happen).
 * 
 * NOTE this is only for 4 corner polygons
 * 
 * The display size is inversely applied to the screen point selected, while the "blocks" are
 * scaled commensurate with the bitmap size.
 * 
 * @author Roger
 *
 */

// TODO propagate exception if init of blocks fail !!!!

// TODO this might work better as homemade buttons instead of manually drawn polys

public class ImageMap extends BroadcastReceiver {
	// these match the GIMP web image map plugin when created with a 'file' reference
	private static final String TAG_AREA = "area";
	private static final String TAG_COORDS = "coords";
	private static final String TAG_HREF = "href";
	private static final int COLOR_HIGHLIGHTED = Color.RED;
	private static final int COLOR_NONE = Color.GREEN;
	private static final String TAG_FILE_PREFIX = ""; // none, currently
	
	private static final int NO_SUBSELECT = -1;
	
	private final ImageView imageView;
	private final Paint paint = new Paint();
	private final Map<String,MapBlock> maps = new HashMap<String,MapBlock>();	
	private final AvailableProducts me;
	private final Display display;
	private final Matrix matrix = new Matrix();
	private final Context context;
	private final Bitmap immutableBitmap;
	private Canvas canvas;
	private Bitmap mutableBitmap;
	
	private Entry<String,MapBlock> current;
	private int subSelected = NO_SUBSELECT;
	private String fileName;	
	
	public MapInfo getCurrentMapInfo() {
		if ( current != null )
			return current.getValue().getMapInfo();
		
		return null;
	}
	
	public MapInfo getCurrentMapBlockInfo() {
		if ( current != null )
			return current.getValue().getMapInfo();
		
		return null;
	}
	
	/**
	 * Set cardinal value in the subselects which has been chosen
	 * @param which
	 */
	public void setSubSelected( int which ) {
		subSelected = which;
	}

	public final void addToFileName( String s ) {
		fileName = fileName + s;
	}
	public final String getFileName() {
		return fileName;
	}
	
	public ImageMap( final AvailableProducts ap, final ImageView imageView, 
					Context context, final Display display ) {
		// this must be off for the bitmap, but need to allow android:anyDensity="false" for the
		// map menu and data bar to appear on nexus 7
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inScaled = false;
		immutableBitmap = BitmapFactory.decodeResource( context.getResources(), ap.getImageMapId(), opt );        
	        // create mutable map so drawing is supported (resource one is immutable)
		mutableBitmap = immutableBitmap.copy( immutableBitmap.getConfig(), true );
		
		me = ap;
		this.imageView = imageView;
		canvas = new Canvas( mutableBitmap );
		this.display = display;
		this.context = context;
		
		matrix.setScale( (float)display.getHeight()/(float)immutableBitmap.getHeight(), (float)display.getWidth()/(float)immutableBitmap.getWidth());		
		
		// TODO figure out how to do a transparent color, and fill tiles
		// and/or erase existing color
		paint.setAntiAlias( true );		
		paint.setStrokeMiter( 5 );
		paint.setStrokeWidth( 5 );
		paint.setStyle( Style.STROKE );
//		paint.setStyle( Style.FILL_AND_STROKE );
		
		reset();
		
		initDrawBlocksFromXml( context.getResources().getXml( ap.getXmlMapId() ) );
		
		// receiver has to be used to redraw the map after download, since the download code
		// is in a separate thread, and only the original thread can 'touch the threads views'
		// other users will just use the OpenFlight.rebuildmap()
		context.registerReceiver( this, new IntentFilter( ap.getClazz().getCanonicalName() ) );
	}	
	
	public final AvailableProducts getMe() { return me; }

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		this.rebuild();
	}	
    
    /**
     * Data has changed.  Rebuild the map blocks.
     */
    public void rebuild() {
    	maps.clear();
    	reset();
    	mutableBitmap.recycle(); // frequently get out of memory
    	mutableBitmap = immutableBitmap.copy( immutableBitmap.getConfig(), true );
		canvas = new Canvas( mutableBitmap );
		initDrawBlocksFromXml( context.getResources().getXml( me.getXmlMapId() ) );
		show();
    }
    
    public void show() {
    	imageView.setImageBitmap( mutableBitmap );
    	imageView.invalidate();
    }
    
	/**
	 * Build the local name of the file for the current product
	 * 
	 * @param imageMap
	 * @param prefs
	 * @return
	 */
	public final String getFullArchivePath( ) {
		return getMe().getFullArchivePath( getFileName() );
	}   
	
	/**
	 * Reset internal state (cancel)
	 */
	public final void reset() {
		current = null;
		subSelected = NO_SUBSELECT;
		fileName = null;
//		paint.setARGB( 200, Color.red( ImageMap.COLOR_NONE ), Color.green( ImageMap.COLOR_NONE ), Color.blue( ImageMap.COLOR_NONE ) );
	}
	/**
	 * Is in a stateful operation
	 * 
	 * @return boolean true if in a state
	 */
	public final boolean isState() {
		return current != null;
	}

	/**
	 * If a containing block exists, internal pointer is set to that block and return true
	 * 
	 * @param float x
	 * @param float y
	 * @return boolean
	 * @sideeffect set internal pointer to existing block
	 */
	public final boolean makeCurrentIfExists( float x, float y ) {		
				
		// this still ain't right
		y = y * (float)immutableBitmap.getHeight()/(float)display.getHeight();
		x = x * (float)immutableBitmap.getWidth()/(float)display.getWidth();
		
		if ( findEnclosing( x,y ) != null )
			return true;
		return false;
	}
	
	/**
	 * Finds map by lat,lon if it exists on device.  Sets 'current' map to 
	 * that map.  Otherwise, returns null.
	 * @param lat
	 * @param lon
	 * @return
	 */
	public final String findByLatLonIfExists( double lat, double lon ) {		
		final Iterator<Entry<String,MapBlock>>it = maps.entrySet().iterator();
		while ( it.hasNext() ) {
			Entry<String,MapBlock>e = (Entry<String,MapBlock>)it.next();
			
			MapInfo mi = e.getValue().getMapInfo();
			if ( mi != null ) {
				
				try {
					if ( lat >= mi.getLatitudeMin() && 
							lat <= mi.getLatitudeMax() &&
							lon <= mi.getLongitudeMax() &&
							lon >= mi.getLongitudeMin() ) {
						
						current = e;
						fileName = mi.getDirectoryKey();
						return fileName;
					}
					
				// processing error or bad map metadata; notify user and continue
				} catch ( Exception exc ) {
					Toast.makeText( context, "Check map, failed to parse coords for map: " + mi.getDirectoryKey(), Toast.LENGTH_LONG ).show();
				}
			}
		}
		return null;
	}
	
	/**
	 * Return key of current internal map block, or null if none exists
	 * 
	 * @return String
	 */
 	public final String getKey() {
 		if ( current != null )
 			return current.getKey();
 		return null;
 	}

 	/**
 	 * Highlight current internal map block
 	 */
 	public final void highlight( ) {
 		if ( current != null )
 			current.getValue().highlight();
 	}

 	/**
 	 * Remove highlight from current internal map block
 	 */
	public final void unHighlight( ) {
		if ( current != null )
			current.getValue().unhighlight();
	}
//
//	private final void mark( final MapBlock mb, final int color ) {
//		paint.setColor( color );
////		paint.setARGB( 100, Color.red( color ), Color.green( color ), Color.blue( color ) );
////		mb.draw( erase );
//		mb.draw( paint );
//		imageView.invalidate();		
//	}	

	
	private final void initDrawBlocksFromXml( final XmlResourceParser parser ) {

		try {
			parser.next();
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				
				if(eventType == XmlPullParser.START_TAG) {					
					
					if ( parser.getName().equals( TAG_AREA ) ) {
						String coords8commaSep = null;
						String key = null;
						
						// pick out the coords, href skipping anything else
						for( int i = 0; i < parser.getAttributeCount(); i++ ) {
							if ( parser.getAttributeName( i ).equals( TAG_COORDS ) ) {
								coords8commaSep = parser.getAttributeValue( i );								
							} else if ( parser.getAttributeName( i ).equals( TAG_HREF ) ) {
								key = parser.getAttributeValue( i ).substring( TAG_FILE_PREFIX.length() );
							} // else ignore {						
						}
						
						maps.put( key, new MapBlock( 
											key,
											getMe(),
											imageView, canvas, paint,
											parseAndTranslateCoords( coords8commaSep ) ) );						
					} // else ignore					
				}
				
				eventType = parser.next();
			}
		} catch( Exception e ) {
			// TODO need some exception propagation
			System.out.println("Failed to parse: " + e.getMessage() );
		} finally {
			parser.close();
		}
	}
	
	// parse the coords, scale them to the current screen size deal
	private final float[] parseAndTranslateCoords( final String coords8commaSep ) {
		final float[] coords = new float[8];
		final String[] t = coords8commaSep.split( "," );
		for( int i = 0; i < 8; i++ ) {
			coords[ i ] = Integer.parseInt( t[ i ] );  // those coords are all ints
		}
	
		// scale to the image/display
		matrix.mapPoints( coords );

		return coords;
	}	
		
	private final Entry<String,MapBlock> findEnclosing( final float x, final float y ) {
		
		final Iterator<Entry<String,MapBlock>>it = maps.entrySet().iterator();
		while ( it.hasNext() ) {
			Entry<String,MapBlock>e = (Entry<String,MapBlock>)it.next();
			if ( e.getValue().computeEnclosing( x, y ) ) {
				current = e;
				fileName = current.getKey();
				return current;
			}
		}
		current = null;
		fileName = null; 	
		return null;
	} 	

	
	final class MapBlock {
    	final Canvas canvas;
    	final ImageView imageView;
		final float[][] aPolygon;
		int countExists = 0;
		// when the map has none existing, swap the colors to show the press as different color
		int colorHighlighted;
		int colorNormal;
		// specific info about each individual component (n/s, plates, etc)
		MapInfo mapInfo;
		
    	public MapBlock( String key, AvailableProducts ap, ImageView imageView, Canvas canvas, Paint paint, float[] pts ) {

    		try {
        		final Fetcher fetcher = new Fetcher( ap, key );
	    			
	        	// fetch metadata - date effective, date expired, long, lat
	        	InputStream metafile = null;
	        	try {
	        		metafile = fetcher.fetchFile( OpenFlight.MAP_METADATA_FILE );
	        		if ( metafile != null ) {
	        			mapInfo = new MapInfo( ap );
	        			mapInfo.setDirectoryKey( key );
	        			countExists++;
	        			ParseMetadata.setMapInfoFromStream( ap.getClazz(), metafile, mapInfo, null );
	        			// NOTE that this thing excepts almost every file, so nothing
	        			// important should be put after the parse line
	        		}

	        	} catch ( Exception e ) {
	        		// TODO the FAA xml is pretty hosed up; fortunately we can get our desired symbols
	        		// before unmatched tags are encountered ??!!
	        		// so ignore the errors
	        	} finally {
	        		try {
	        			if ( metafile != null )
	        				metafile.close();
	    			} catch (IOException e) {
	    				// TODO
	    			}
	        	}  

    		} catch ( Exception e ) {
    			 // TODO probably should bail
    			throw new RuntimeException( e.getMessage() );
    		}
    		
    		
    		this.canvas = canvas;

    		Matrix inverse = new Matrix();
    		matrix.invert( inverse );
    		inverse.mapPoints( pts );
    		
    		// prepare for draw and enclosing calculation
    		aPolygon = new float[][] { {pts[0],pts[1]}, {pts[2],pts[3]}, {pts[4],pts[5]}, {pts[6],pts[7]} };
    		    		
    		// done separately so we can invalidate the whole thing at one time instead of every square
    		this.imageView = imageView;    		
    		
    		setColor( paint );
    		
    		draw( paint );    		
    	}
    	
    	public final void highlight() {
    		paint.setColor( colorHighlighted );
    		draw( paint );
    		imageView.invalidate();		
    	}  
    	
    	public final void unhighlight() {
    		paint.setColor( colorNormal );
    		draw( paint );
    		imageView.invalidate();		
    	}     	
    	
    	// color reverses if there are no maps backing
    	public final void setColor( Paint paint ) {
    		if( countExists > 0 ) {
    			colorNormal = COLOR_NONE;
    			colorHighlighted = COLOR_HIGHLIGHTED;
    		} else {
    			colorNormal = COLOR_HIGHLIGHTED;
    			colorHighlighted = COLOR_NONE;
    		}
    		
    		paint.setColor( colorNormal );
    	}
    	
    	/**
    	 * More like 'map block info', since if there are distinct parts of the map (n/s, various plates)
    	 * then each part that is present will have details in 'getParts()'.
    	 * 
    	 * This will exist whether there are individual map parts present on the device or not.
    	 * 
    	 * @return MapInfo
    	 */
    	public MapInfo getMapInfo() {
    		return mapInfo; 
    	}
    	    	
    	/**
    	 * Draw using a path so that a fill style may be applied
    	 * 
    	 * @param paint
    	 */
    	public final void draw( Paint paint ) {    		
    		
    		final Path path = new Path();

    		path.moveTo( aPolygon[0][0], aPolygon[0][1] );
    		
    		if ( countExists == 0 ) { // none
    			path.lineTo( aPolygon[2][0], aPolygon[2][1] );
    			path.moveTo( aPolygon[3][0], aPolygon[3][1] );
    			path.lineTo( aPolygon[1][0], aPolygon[1][1] );    			
    		} else { // all
	    		path.lineTo( aPolygon[1][0], aPolygon[1][1] );
	    		path.lineTo( aPolygon[2][0], aPolygon[2][1] );
	    		path.lineTo( aPolygon[3][0], aPolygon[3][1] );
	    		path.lineTo( aPolygon[0][0], aPolygon[0][1] );
	    		
    		}
    		canvas.drawPath(path, paint);    		    		
    	}    	    	
    	
    	// http://local.wasp.uwa.edu.au/~pbourke/geometry/insidepoly/  		    
    	private final boolean computeEnclosing( double xT, double yT ) {
    		int counter = 0;
    		int i;
    		double xinters;
    		double p1x, p1y, p2x, p2y;
    		    		
    		// status bar at the top for phones, bottom for tablets
    		if ( Utils.getUtils().isTabletSize() )
    			yT += ((float)display.getHeight())/70f;
    		else
    			yT -= ((float)display.getWidth())/35f;
    		
    		p1x = aPolygon[0][0];
    		p1y = aPolygon[0][1];
    		
    		for( i = 1; i <= aPolygon.length; i++ ) {
    			p2x = aPolygon[ i % aPolygon.length ][ 0 ];
    			p2y = aPolygon[ i % aPolygon.length ][ 1 ];
    			if ( yT > Math.min( p1y, p2y ) ) {
    				if ( yT <= Math.max( p1y, p2y ) ) {
    					if ( xT <= Math.max( p1x, p2x ) ) {
    						if ( p1y != p2y ) {
    							xinters = ( ( yT - p1y ) * ( p2x - p1x ) / ( p2y - p1y ) ) + p1x;
    							if ( p1x == p2x || xT <= xinters )
    								counter++;
    						}
    					}
    				}
    			}
    			p1x = p2x;
    			p1y = p2y;
    		}

    		if (counter % 2 == 0)
    			return false;
    		else
    			return true;
    	}       	
  
	}

}
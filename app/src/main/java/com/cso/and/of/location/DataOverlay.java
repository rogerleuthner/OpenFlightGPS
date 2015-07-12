
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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;

import com.cso.and.of.config.Utils;

/**
 * Data overlay could be converted to a real overlay by uncommenting below and doing something like handling
 * updates for the coords.  It's prob. faster to do inline like this, tho.
 * 
 * @author Roger
 *
 */

public class DataOverlay { //extends OpenStreetMapViewOverlay  {

	private final Matrix mDataMatrix = new Matrix();
	
	// TODO preferences
	public static final String distanceUnit = "(ft)";
	public static final String speedUnit = "(mph)";	
	
	// number of data 'boxes'
	public static final int NUMBER_OF_DATA_ELEMENTS = 6;
	
	// tablets have the command bar embedded in teh screen
	public static final int BOTTOM_BORDER = Utils.getUtils().isTabletSize()? 50 : 5;
	public static final int TEXT_MARGIN = 2;
	public static final int ALPHA_VALUE = 100;
	public static final int COLOR_RED = 0;
	public static final int COLOR_GREEN = 128;
	public static final int COLOR_BLUE = 128;	
	// box and background
	private final Paint boxPaint = new Paint();
	// box outline and separator lines
	private final Paint outlinePaint = new Paint();
	// text values
	private final Paint textPaint = new Paint();
	
	// computed coordinates
	private float TEXT_BOX_HEIGHT;
	private float TEXT_BOX_WIDTH;
	private float LABEL_BOX_HEIGHT;
	private float LABEL_TEXT_FONT;
	private float VALUE_TEXT_FONT;
	private float CORNER_X;
	private float CORNER_Y;
	private float SINGLE_ITEM_WIDTH;
	
	private static final String LL_FMT = "%.2f";
	private static final String LL_FMT_NO_FP = "%.0f";	
	private static final String DEGS = "°";
	
	private boolean doubleSize = false;
	
	public DataOverlay(/*Context context, OwnshipOverlay ownshipOverlay*/) {
//		super( context );

		boxPaint.setARGB(ALPHA_VALUE, COLOR_RED, COLOR_GREEN, COLOR_BLUE);
		boxPaint.setStyle(Style.FILL);		

		outlinePaint.setStyle( Style.STROKE ); 	
		outlinePaint.setColor( Color.WHITE );
		
		textPaint.setAntiAlias( true );
		textPaint.setColor( Color.WHITE );
		textPaint.setTypeface( Typeface.DEFAULT_BOLD );
	}
	
	/**
	 * Toggle double size of the data box.
	 * Return true if this event is completely handled here, otherwise it will be passed up through the superclass handlers 
	 * 
	 * @param event
	 * @return
	 */
	public boolean handleMotionEvent( MotionEvent event ) {
		
		if ( isClickInDataArea( event ) ) {

			doubleSize = ! doubleSize;
			return true;
		}
		
		return false;
	}
	
	/**
	 * on press within the dataoverlay area?
	 * @return
	 */
	public boolean isClickInDataArea( MotionEvent event ) {
		if ( event.getY() < CORNER_Y && ( event.getY() > ( CORNER_Y - TEXT_BOX_HEIGHT ) ) &&
				event.getX() > CORNER_X && ( event.getX() < ( CORNER_X + TEXT_BOX_WIDTH ) ) ) {
			return true;
		}
		return false;
	}

	/**
	 * Draw latitude, longitude, altitude, speed and bearing and altitude
	 */
	public void drawData( Canvas c, double lat, double lon, double alt, float spd, float brg, float acc ) {

		// convert to desired units
// TODO set in preferences (knots, meters)
		// speed is in meters/sec; we're currently fixed at MPH
		spd = spd * 2.2369363f;
		// altitude is in meters; we're currently in feet
		alt = alt * 3.2808399d;
		// accuracy is in meters; we're currently in feet
		acc = acc * 3.2808399f;
				
		
		computeCoords( c, NUMBER_OF_DATA_ELEMENTS );  // have to do this every time or else detect orientation change and recompute then
		
		mDataMatrix.reset();
		mDataMatrix.postTranslate( CORNER_X, CORNER_Y - TEXT_BOX_HEIGHT );				
		c.save();
		c.setMatrix( mDataMatrix );
		
		// draw box and outline
		final Rect r = new Rect();
		r.set( (int)CORNER_X, (int)TEXT_BOX_HEIGHT, (int)TEXT_BOX_WIDTH, (int)CORNER_X );
		c.drawRect( r, outlinePaint );
		c.drawRect( r, boxPaint );
		
		// draw label box underline 
		final Path p = new Path();
		p.moveTo( CORNER_X, LABEL_BOX_HEIGHT );
		p.lineTo( TEXT_BOX_WIDTH, LABEL_BOX_HEIGHT );	
		
		// draw item separator lines
		for( int i = 1; i <= NUMBER_OF_DATA_ELEMENTS; i++ ) {
			p.moveTo( CORNER_X + ( i * SINGLE_ITEM_WIDTH ), TEXT_MARGIN );
			p.lineTo( CORNER_X + ( i * SINGLE_ITEM_WIDTH ), TEXT_BOX_HEIGHT );
		}
		p.close();
		c.drawPath(p, outlinePaint);	
				
		
		// draw changeable texts
		
		// LAT
		// draw label
		textPaint.setTextSize( LABEL_TEXT_FONT );
		c.drawText( "LATITUDE (" + DEGS + ")", CORNER_X + TEXT_MARGIN, LABEL_BOX_HEIGHT - TEXT_MARGIN, textPaint );		
		// draw data
		textPaint.setTextSize( VALUE_TEXT_FONT );
		c.drawText( String.format( LL_FMT, lat ), 
				CORNER_X + TEXT_MARGIN, TEXT_BOX_HEIGHT - TEXT_MARGIN, textPaint );		

		// LON
		// draw label
		textPaint.setTextSize( LABEL_TEXT_FONT );
		c.drawText( "LONGITUDE (" + DEGS + ")", CORNER_X + TEXT_MARGIN + SINGLE_ITEM_WIDTH, LABEL_BOX_HEIGHT - TEXT_MARGIN, textPaint );		
		// draw data
		textPaint.setTextSize( VALUE_TEXT_FONT );
		c.drawText( String.format( LL_FMT, lon ), 
				CORNER_X + TEXT_MARGIN + SINGLE_ITEM_WIDTH, TEXT_BOX_HEIGHT - TEXT_MARGIN, textPaint );		
		
		// ALT
		// draw label
		textPaint.setTextSize( LABEL_TEXT_FONT );
		c.drawText( "ALTITUDE " + distanceUnit, CORNER_X + TEXT_MARGIN + ( SINGLE_ITEM_WIDTH * 2), LABEL_BOX_HEIGHT - TEXT_MARGIN, textPaint );		
		// draw data
		textPaint.setTextSize( VALUE_TEXT_FONT );
		c.drawText( String.format( LL_FMT_NO_FP, alt ), 
				CORNER_X + TEXT_MARGIN + ( SINGLE_ITEM_WIDTH * 2 ), TEXT_BOX_HEIGHT - TEXT_MARGIN, textPaint );		
		
		// SPD
		// draw label
		textPaint.setTextSize( LABEL_TEXT_FONT );
		c.drawText( "SPEED " + speedUnit, CORNER_X + TEXT_MARGIN + ( SINGLE_ITEM_WIDTH * 3), LABEL_BOX_HEIGHT - TEXT_MARGIN, textPaint );		
		// draw data
		textPaint.setTextSize( VALUE_TEXT_FONT );
		c.drawText( String.format( LL_FMT_NO_FP, spd ), CORNER_X + TEXT_MARGIN + ( SINGLE_ITEM_WIDTH * 3 ), 
				TEXT_BOX_HEIGHT - TEXT_MARGIN, textPaint );		
		
		// BRG
		// draw label
		textPaint.setTextSize( LABEL_TEXT_FONT );
		c.drawText( "BEARING (" + DEGS + ")", CORNER_X + TEXT_MARGIN + ( SINGLE_ITEM_WIDTH * 4), LABEL_BOX_HEIGHT - TEXT_MARGIN, textPaint );		
		// draw data
		textPaint.setTextSize( VALUE_TEXT_FONT );
		c.drawText( String.format( LL_FMT_NO_FP, brg ), CORNER_X + TEXT_MARGIN + ( SINGLE_ITEM_WIDTH * 4 ), 
				TEXT_BOX_HEIGHT - TEXT_MARGIN, textPaint );			
		
		// ACC
		// draw label
		textPaint.setTextSize( LABEL_TEXT_FONT );
		c.drawText( "ACCURACY " + distanceUnit, CORNER_X + TEXT_MARGIN + ( SINGLE_ITEM_WIDTH * 5), LABEL_BOX_HEIGHT - TEXT_MARGIN, textPaint );		
		// draw data
		textPaint.setTextSize( VALUE_TEXT_FONT );
		c.drawText( String.format( LL_FMT_NO_FP, acc), CORNER_X + TEXT_MARGIN + ( SINGLE_ITEM_WIDTH * 5 ), 
				TEXT_BOX_HEIGHT - TEXT_MARGIN, textPaint );			
		
		c.restore();			
	}
	
	
	private void computeCoords( Canvas c, int items ) {
		if ( doubleSize )
			TEXT_BOX_HEIGHT = c.getHeight()/10;
		else
			TEXT_BOX_HEIGHT = c.getHeight()/20;
			
		TEXT_BOX_WIDTH = c.getWidth() - (TEXT_MARGIN*2);
		LABEL_BOX_HEIGHT = (TEXT_BOX_HEIGHT/4) + (TEXT_MARGIN*2);
		LABEL_TEXT_FONT = TEXT_BOX_HEIGHT/4;
		VALUE_TEXT_FONT = (float)(LABEL_TEXT_FONT * 2.5);
		
		CORNER_X = TEXT_MARGIN/2;
		CORNER_Y = c.getHeight() - BOTTOM_BORDER;		
		
		SINGLE_ITEM_WIDTH = (TEXT_BOX_WIDTH / items) + (items - 1);
	}	

	


//	@Override
//	public void onDraw(final Canvas c, final OpenStreetMapView osmv) {
//		this.drawTextInTransparentBox(c, "hi");
//	}
}

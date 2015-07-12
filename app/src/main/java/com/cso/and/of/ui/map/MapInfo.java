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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.cso.and.of.types.AvailableProducts;

public final class MapInfo implements Serializable {
	private static final long serialVersionUID = -2217686162020532474L;
	private String longitudeMin;
	private String latitudeMin;
	private String longitudeMax;
	private String latitudeMax;
	
	// TODO remove me after all wacs no longer have this
	private String ALTERNATE_DATE_FMT = "MMMM dd, yyyy";
	
	// TODO make these dates
	private Date startDate;
	private Date expireDate;
	private String directoryKey;
	private AvailableProducts ap;
	public MapInfo( AvailableProducts ap ) {
		this.ap = ap;
	}

	public void setStartDate( String date, String fmt ) { 
		SimpleDateFormat sdf = new SimpleDateFormat( fmt );		
		try {
			startDate = sdf.parse( date );
		} catch (ParseException e) {
			// try an alternate, since 1 of the WACs still has this
			try {
				startDate = new SimpleDateFormat( ALTERNATE_DATE_FMT ).parse( date );
			} catch ( ParseException e1 ) {
				// no joy
				startDate = new Date(0);
			}
		}			
	}
	public void setExpireDate( String date, String fmt ) { 
		SimpleDateFormat sdf = new SimpleDateFormat( fmt );
		try {
			expireDate = sdf.parse( date );
		} catch (ParseException e) {
			// try an alternate, since 1 of the WACs still has this			
			try {
				expireDate = new SimpleDateFormat( ALTERNATE_DATE_FMT ).parse( date );
			} catch ( ParseException e1 ) {
				// no joy
				expireDate = new Date(0);
			}			
		}		
	}	

	public Date getExpireDate() { return expireDate; }
	public Date getStartDate() { return startDate; }
	
	public void setLatitudeMin( String in ) { this.latitudeMin = in; }
	public void setLongitudeMin( String in ) { this.longitudeMin = in; }
	public void setLatitudeMax( String in ) { this.latitudeMax = in; }
	public void setLongitudeMax( String in ) { this.longitudeMax = in; }
	
	
	// TODO why is this not negative naturally from the XML ????
	// TODO may have to get the coords from the mapresource.xml, not the FAA mapmetadata.xml
	// TODO this null check should not be required ... why are we not getting this sometimes?
	public double getLatitudeMin() {
		if ( latitudeMin != null )
			return Double.valueOf( latitudeMin );

		return 0;
	}
	public double getLongitudeMin() {
		if ( longitudeMin != null )
			return Double.valueOf( longitudeMin );
	
		return 0;
	}
	public double getLatitudeMax() {
		if ( latitudeMax != null )		
			return Double.valueOf( latitudeMax );
		
		return 0;
	}
	public double getLongitudeMax() {
		if ( longitudeMax != null ) 
			return Double.valueOf( longitudeMax );
		
		return 0;
	}
	
	
//	public int getLatitudeE6() { return Integer.valueOf( latitude ); }
//	public int getLongitudeE6() { return Integer.valueOf( longitude ); }
	public String getDirectoryKey() { return directoryKey; }
	public void setDirectoryKey( String in ) { this.directoryKey = in; }
	public AvailableProducts getMe() { return ap; }
}
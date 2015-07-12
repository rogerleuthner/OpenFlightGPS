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

import java.io.InputStream;
import java.util.Properties;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.cso.and.of.ui.map.MapInfo;
import com.cso.and.of.ui.map.products.EnRouteLow;

public class ParseMetadata { //implements ParseConfig {	

	/*
	 * This hackery is so the product classes don't have to be instantiated for every mapblock
	 */
	public static void setMapInfoFromStream( @SuppressWarnings("rawtypes") Class c, InputStream is, MapInfo mapInfo, String unused ) throws Exception {
		if ( c == EnRouteLow.class )
			ENRsetMapInfoFromStream( is, mapInfo, unused );
		else
			DEFsetMapInfoFromStream( is, mapInfo, unused );
	}

///////////// Enroute low parsing

	private final static String DATE_FORMAT = "MM-dd-yyyy";
	private final static String PROPERTY_START_KEY = "dc.coverage.t.min";
	private final static String PROPERTY_END_KEY = "dc.coverage.t.max";
	private final static String PROPERTY_LAT_MIN = "dc.coverage.y.min";
	private final static String PROPERTY_LAT_MAX = "dc.coverage.y.max";	
	private final static String PROPERTY_LON_MIN = "dc.coverage.x.min";
	private final static String PROPERTY_LON_MAX = "dc.coverage.x.max";

	public static void ENRsetMapInfoFromStream( InputStream is, MapInfo mapInfo, String unused ) throws Exception {
		Properties p = new Properties();
		p.load( is );

		// eff=11-18-2010&amp;end=01-13-2011
		mapInfo.setStartDate( p.getProperty( PROPERTY_START_KEY ), DATE_FORMAT );
		mapInfo.setExpireDate( p.getProperty( PROPERTY_END_KEY ), DATE_FORMAT );
		mapInfo.setLatitudeMax(p.getProperty( PROPERTY_LAT_MAX ) );
		mapInfo.setLatitudeMin( p.getProperty( PROPERTY_LAT_MIN ) );
		mapInfo.setLongitudeMax( p.getProperty( PROPERTY_LON_MAX ) );
		mapInfo.setLongitudeMin( p.getProperty( PROPERTY_LON_MIN ) );		
	}	





/////////////// DEFAULT PARSING


	// defines for the parse of the FAA meta data
	private static String FAA_DATE_FORMAT = "yyyyMMdd";
	private static String TAG_META_BASE = "meta";
	private static String TAG_NAME = "name";
	private static String TAG_MAP_X_MIN = "dc.coverage.x.min";
	private static String TAG_MAP_Y_MIN = "dc.coverage.y.min";
	private static String TAG_MAP_X_MAX = "dc.coverage.x.max";
	private static String TAG_MAP_Y_MAX = "dc.coverage.y.max";    
	private static String TAG_MAP_DATE_EFFECTIVE = "dc.coverage.t.min";
	private static String TAG_MAP_DATE_EXPIRE = "dc.coverage.t.max";
	private static String TAG_NAME_CONTENT = "content";

	public static void DEFsetMapInfoFromStream( InputStream is, MapInfo mapInfo, String dateFormat ) throws Exception {

		// f'n WAC date format differs from sectional
		if ( dateFormat != null )
			FAA_DATE_FORMAT = dateFormat;

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setValidating( false );
		XmlPullParser parser = factory.newPullParser();
		// auto-detect the encoding from the stream
		parser.setInput( is, null);
		int eventType = parser.getEventType();
		parser.next();
		while (eventType != XmlPullParser.END_DOCUMENT) {

			try { // format errors during tag processing are ignored so we can proceed thru docs

				if(eventType == XmlPullParser.START_TAG) {					
					// TODO defines
					if ( parser.getName().equals( TAG_META_BASE ) ) {

						final String[] attributes = new String[ parser.getAttributeCount() ];
						final String[] values = new String[ parser.getAttributeCount() ];

						for( int i = 0; i < parser.getAttributeCount(); i++ ) {
							attributes[ i ] = parser.getAttributeName( i );
							values[ i ] = parser.getAttributeValue( i );
						}

						for( int i = 0; i < parser.getAttributeCount(); i++ ) {
							if ( attributes[ i ].equals( TAG_NAME ) &&  values[ i ].equals( TAG_MAP_X_MIN ) ) {
								// content has what we want
								for( int j = 0; j < parser.getAttributeCount(); j++ ) {
									if ( attributes[ j ].equals( TAG_NAME_CONTENT ) ) {
										mapInfo.setLongitudeMin( values[ j ] );
									}
								}
							} else if ( attributes[ i ].equals( TAG_NAME ) &&  values[ i ].equals( TAG_MAP_X_MAX ) ) {
								// content has what we want
								for( int j = 0; j < parser.getAttributeCount(); j++ ) {
									if ( attributes[ j ].equals( TAG_NAME_CONTENT ) ) {
										mapInfo.setLongitudeMax( values[ j ] );
									}
								}							
							} else if ( attributes[ i ].equals( TAG_NAME ) &&  values[ i ].equals( TAG_MAP_Y_MIN ) ) {
								// content has what we want
								for( int j = 0; j < parser.getAttributeCount(); j++ ) {
									if ( attributes[ j ].equals( TAG_NAME_CONTENT ) ) {
										mapInfo.setLatitudeMin( values[ j ] );
									}
								}		
							} else if ( attributes[ i ].equals( TAG_NAME ) &&  values[ i ].equals( TAG_MAP_Y_MAX ) ) {
								// content has what we want
								for( int j = 0; j < parser.getAttributeCount(); j++ ) {
									if ( attributes[ j ].equals( TAG_NAME_CONTENT ) ) {
										mapInfo.setLatitudeMax( values[ j ] );
									}
								}							
							} else if ( attributes[ i ].equals( TAG_NAME ) &&  values[ i ].equals( TAG_MAP_DATE_EFFECTIVE ) ) {
								// content has what we want
								for( int j = 0; j < parser.getAttributeCount(); j++ ) {
									if ( attributes[ j ].equals( TAG_NAME_CONTENT ) ) {
										mapInfo.setStartDate( values[ j ], FAA_DATE_FORMAT );
									}
								}							
							} else if ( attributes[ i ].equals( TAG_NAME ) &&  values[ i ].equals( TAG_MAP_DATE_EXPIRE ) ) {
								// content has what we want
								for( int j = 0; j < parser.getAttributeCount(); j++ ) {
									if ( attributes[ j ].equals( TAG_NAME_CONTENT ) ) {
										mapInfo.setExpireDate( values[ j ], FAA_DATE_FORMAT );
									}
								}							
							} // else ignore
						}

					} // else ignore					
				}

			} catch ( Exception e ) {
				System.out.println( e.getMessage() );
				/*silently ignore format errors, continue to next start tag*/
			}

			eventType = parser.next();
		}
	}
}
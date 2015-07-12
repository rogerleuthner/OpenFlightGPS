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

package com.cso.and.of.types;

import java.io.Serializable;

import com.cso.and.of.config.Utils;
import com.cso.and.of.gps.FREE.R;
import com.cso.and.of.ui.map.products.EnRouteLow;
import com.cso.and.of.ui.map.products.Sectional;
import com.cso.and.of.ui.map.products.TerminalFly;
import com.cso.and.of.ui.map.products.TerminalTac;
import com.cso.and.of.ui.map.products.WorldArea;

/**
 * NOTE must MANUALLY keep in sync with arrays.xml->home_screen_enum_names
 * !!including friendly names!!!
 * @author Roger
 *
 */

public enum AvailableProducts implements Serializable {
    Sectional ( Sectional.class, "Sectional", R.drawable.faa_sect, 
    		R.xml.vfr, "/VFR/", ".zip", true ),
//    Plates ( Plates.class, "IFR Plates", R.drawable.faa_sect, R.xml.vfr, "Plates/", ".pdf", null ),
    EnrouteLow ( EnRouteLow.class, "EnRoute Low", R.drawable.faa_low_enr, 
    		R.xml.enr, "/ENR/", ".zip", true ),
    TerminalFLY ( TerminalFly.class, "Terminal Area (Flyway)", R.drawable.faa_sect, 
    		R.xml.fly, "/TAC/", ".zip", false ),
    TerminalTAC ( TerminalTac.class, "Terminal Area (Chart)", R.drawable.faa_sect, 
    		R.xml.tac, "/TAC/", ".zip", true ),   		    		
    WorldArea ( WorldArea.class, "World Area", R.drawable.faa_wac, 
    		R.xml.wac, "/WAC/", ".zip", true );
//    AirportsFacilities ( PDFView.class, "Airport/Facility", 0, 0, "Plates/", ".pdf", null );    
    
    private final String name;
    private final int imageMapId;
    private final int xmlMapId;
    private final Class<?> clazz;
    private final String dataPath;
    private final String suffix;
    private boolean geoReferenced;
    public static int EXISTS_NONE = 0;
    public static int EXISTS_PARTIAL = 1;
    public static int EXISTS_ALL = 2;
    public static String SUBSELECTOR = "_";

    // each wraps around ...
    public final AvailableProducts getNextOrdinally( ) {
    	if ( ordinal() < AvailableProducts.values().length - 1 )
    		return AvailableProducts.values()[ ordinal() + 1 ]; 
    		
    	return AvailableProducts.values()[ 0 ];
    }
    public final AvailableProducts getPreviousOrdinally(  ) {
    	if ( ordinal() == 0 )
    		return AvailableProducts.values()[ AvailableProducts.values().length - 1 ];
    	
    	return AvailableProducts.values()[ ordinal() - 1 ];
    }

    /**
     * Input file must include any subselect file designator
     * 
     * @param file
     * @return
     */
    // TODO consolidate with Utils methods
    public final String getFullArchivePath( String file ) {
    	return Utils.getUtils().getFullArchivePath( this, file );    	
    }
    public final String getBaseArchivePath( String file ) {
    	return Utils.getUtils().getBaseArchivePath( this, file );    	
    }    
    
    AvailableProducts( Class<?> clazz, String name, int imageMapId, int xmlMapId, 
    		String dataPath, String suffix, boolean geoReferenced ) {
    	this.clazz = clazz;
    	this.name = name;
    	this.imageMapId = imageMapId;
    	this.xmlMapId = xmlMapId;
    	this.dataPath = dataPath;
    	this.suffix = suffix;
    	this.geoReferenced = geoReferenced;
    }
    public Class<?> getClazz() { return clazz; }
    public String getFriendlyName() { return name; }
    public int getImageMapId() { return imageMapId; }
    public int getXmlMapId() { return xmlMapId; }
    public String getDataPath() { return dataPath; }
    public String getSuffix() { return suffix; }
    public boolean isGeoReferenced() { return geoReferenced; }
    
    public static String[] getAvailableProductNames() {
    	String[] prods = new String[ AvailableProducts.values().length ];
    	int i = 0;
    	for( AvailableProducts a : AvailableProducts.values() )
    		prods[ i++ ] = a.getFriendlyName();
    	
    	return prods;
    }
    
    public static AvailableProducts getProductByFriendlyName( String name ) {
    	for( AvailableProducts ap : AvailableProducts.values() ) {
    		if ( ap.getFriendlyName().equals( name ) )
    			return ap;
    	}
    	return null;
    }
    
    public static AvailableProducts getProductByClass( Class<?>clazz ) {
    	for( AvailableProducts ap : AvailableProducts.values() ) {
    		if ( ap.getClazz().equals( clazz) )
    			return ap;
    	}
    	return null;
    }    
}
 
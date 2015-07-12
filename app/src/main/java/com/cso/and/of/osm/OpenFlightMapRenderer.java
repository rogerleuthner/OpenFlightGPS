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

import java.io.InputStream;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.tileprovider.CloudmadeException;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCloudmadeTokenCallback;
import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.views.util.OpenStreetMapRendererBase;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class OpenFlightMapRenderer extends OpenStreetMapRendererBase {

	public OpenFlightMapRenderer(String aName, int aZoomMinLevel,
			int aZoomMaxLevel, int aMaptileZoom, String aImageFilenameEnding ) {
		super(aName, aZoomMinLevel, aZoomMaxLevel, aMaptileZoom, aImageFilenameEnding, (String)null);
	}

	@Override
	public String getTileURLString(
			OpenStreetMapTile aTile,
			IOpenStreetMapTileProviderCallback aMCallback,
			IOpenStreetMapTileProviderCloudmadeTokenCallback aCloudmadeTokenCallback)
			throws CloudmadeException {
		return aTile.getZoomLevel() + "/" + aTile.getX() + "/" + aTile.getY() + mImageFilenameEnding;
	}

	@Override
	public String localizedName(ResourceProxy proxy) {
		return null;
	}
	
	@Override
	public Drawable getDrawable(final InputStream aFileInputStream) {		
		if ( aFileInputStream != null ) {
			try {			
				return new BitmapDrawable( BitmapFactory.decodeStream( aFileInputStream ) );
								
			} catch (final OutOfMemoryError e) {
				// just return null - try again
			}
		}
		return null; // input stream was null or out of memory
	}	
	
}

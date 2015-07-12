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

import java.io.IOException;
import java.io.InputStream;

import org.andnav.osm.tileprovider.CloudmadeException;
import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.views.util.OpenStreetMapTileProvider;

import android.graphics.drawable.Drawable;
import android.os.Handler;

/**
 * Extend the OpenStreetMapTileProvider even though we basically ignore all the implementation inherited; the extension
 * is to retain compatibility in the OpenFlightMapView (extending OpenStreetMapView)
 * @author Roger
 *
 */

public class OpenFlightMapTileProviderDirect extends OpenStreetMapTileProvider implements IOpenStreetMapTileProviderCallback {

	private final OpenFlightMapTileZipFileProvider mZipFileProvider;
	protected final OpenFlightMapTileCache myTileCache;
	
	public OpenFlightMapTileProviderDirect(String filePath, final Handler pDownloadFinishedListener ) {
		super(pDownloadFinishedListener);
		myTileCache = new OpenFlightMapTileCache();
		mZipFileProvider = new OpenFlightMapTileZipFileProvider( filePath, this);
	}
	
	@Override
	public void detach() {		
		// this recycles each individual bitmap
		// @see LRUMapTileCache.clear
		myTileCache.clear();
	}

	@Override
	public Drawable getMapTile(final OpenStreetMapTile pTile) {
		if (myTileCache.containsTile(pTile)) {
			return myTileCache.getMapTile(pTile);
		} else {
			mZipFileProvider.loadMapTileAsync(pTile);
			return null;
		}
	}
	
	@Override
	public void mapTileRequestCompleted( final OpenStreetMapTile pTile, final InputStream pTileInputStream ) {
		try {
			
			Drawable d = pTile.getRenderer().getDrawable( pTileInputStream );
			if ( d != null ) {
				myTileCache.putTile( pTile, d );
				// tell our caller we've finished, tile not guaranteed to have been gotten -
				// may be out of mem or other condition where the tiles was not retrieved
				mDownloadFinishedHandler.sendEmptyMessage( OpenStreetMapTile.MAPTILE_SUCCESS_ID );
			}
			// if d is null, don't do anything so they try again later
			
		} finally {
			// although tiles are loaded in individual threads, the execution of the load
			// is done - signaled by this message.  so the input stream is completed, and we
			// do a screen refresh if that was a real (vs null) stream.
			try {
				if ( pTileInputStream != null ) {
					pTileInputStream.close();
				}
				
			} catch(final IOException ignore) {}
		}
	}

	@Override
	public String getCloudmadeKey() throws CloudmadeException {
		return "AINTCLOUDMADE";
	}
}



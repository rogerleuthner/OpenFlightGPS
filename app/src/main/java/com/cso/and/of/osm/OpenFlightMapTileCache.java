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

import org.andnav.osm.tileprovider.OpenStreetMapTile;
import org.andnav.osm.views.util.LRUMapTileCache;

import android.graphics.drawable.Drawable;

public final class OpenFlightMapTileCache  {
	// ===========================================================
	// Constants
	// ===========================================================
	// seems to be the only value that works well
	private static final int MAX_MAP_TILES_CACHE = 25;
	// ===========================================================
	// Fields
	// ===========================================================

	protected LRUMapTileCache mCachedTiles;

	// ===========================================================
	// Constructors
	// ===========================================================

	public OpenFlightMapTileCache() {
		this( MAX_MAP_TILES_CACHE );
	}

	/**
	 * @param aMaximumCacheSize Maximum amount of MapTiles to be hold within.
	 */
	public OpenFlightMapTileCache(final int aMaximumCacheSize){
		this.mCachedTiles = new LRUMapTileCache(aMaximumCacheSize);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	
	public int getCapacity() {
		return mCachedTiles.getMaxCapacity();
	}

	public synchronized Drawable getMapTile(final OpenStreetMapTile aTile) {
		return this.mCachedTiles.get(aTile);
	}

	// editing out the null check on this method is the only reason this class exists; everything else
	// is copied exactly from the OpenStreetMapTileCache
	public synchronized void putTile(final OpenStreetMapTile aTile, final Drawable aDrawable) {
		// want to allow null tiles to be put in there so that the keys are stored and not-retrietrieved
		this.mCachedTiles.put(aTile, aDrawable);
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public synchronized boolean containsTile(final OpenStreetMapTile aTile) {
		return this.mCachedTiles.containsKey(aTile);
	}

	public synchronized void clear() {
		this.mCachedTiles.clear();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
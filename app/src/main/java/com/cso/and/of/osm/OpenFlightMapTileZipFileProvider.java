
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

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.andnav.osm.tileprovider.IOpenStreetMapTileProviderCallback;
import org.andnav.osm.tileprovider.OpenStreetMapTile;

import android.os.Environment;

public class OpenFlightMapTileZipFileProvider  {


//	private static final Logger logger = LoggerFactory.getLogger(OpenStreetMapTileFilesystemProvider.class);

	private ZipFile zipFile;

	protected final IOpenStreetMapTileProviderCallback mCallback;
	
	private final ExecutorService executor = Executors.newCachedThreadPool();

	public OpenFlightMapTileZipFileProvider( final String filePath, final IOpenStreetMapTileProviderCallback aCallback ) {
		mCallback = aCallback;

		checkSdCard();

		findZipFile( filePath );
	}

	public void loadMapTileAsync(final OpenStreetMapTile aTile) {
		
		executor.execute( new TileLoader( aTile ) );
	}	

	private void findZipFile( final String filePath ) {

		final File f = new File( filePath );
		
		if ( f.canRead() ) {
			try {
				zipFile = new ZipFile( f );
			} catch (final Throwable e) {
				throw new RuntimeException("Error opening zip file: " + f, e);
			}
		}
	}	

	
	public InputStream fileFromOneZip(final OpenStreetMapTile aTile) {
		final String path = aTile.getZoomLevel() + "/" + aTile.getX() + "/" + aTile.getY() + aTile.getRenderer().imageFilenameEnding();
			try {
				final ZipEntry entry = zipFile.getEntry(path);
				if (entry != null) {
					final InputStream in = zipFile.getInputStream(entry);
					return in;
				}
			} catch(final Throwable e) {
				throw new RuntimeException("Error getting zip stream: " + aTile, e);
			}

		return null;
	}	

	private void checkSdCard() {
		final String state = Environment.getExternalStorageState();
//		logger.info("sdcard state: " + state);
		if (! Environment.MEDIA_MOUNTED.equals( state ) ) {
			// TODO need message of some kind here!!!
//			mZipFiles.clear();
		}
	}

	private final class TileLoader implements Runnable {
		
		private final OpenStreetMapTile theTile;
		
		public TileLoader( final OpenStreetMapTile tile ) {
			theTile = tile;
		}

		final public void run() {
			mCallback.mapTileRequestCompleted( theTile, fileFromOneZip( theTile ) );
		}		

	}

}


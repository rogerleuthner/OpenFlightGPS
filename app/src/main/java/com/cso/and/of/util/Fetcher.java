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

package com.cso.and.of.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.cso.and.of.OpenFlight;
import com.cso.and.of.config.Utils;
import com.cso.and.of.types.AvailableProducts;


/**
 * Handle remote object fetching.
 * 
 * @author Roger
 *
 */

public class Fetcher {
	
	// TODO get this from a config file
	// use a static temp file name so there isn't cruft buildup
	private final static String TEMP_FILENAME = Utils.getUtils().getDataDirectory() + "/DELETEME_CHART_TEMP";	
	private final String localPath;
	private final String downloadPath;
	private final AvailableProducts ap;
	private final static int READ_CHUNK_SIZE = 8092; // keep buffer on the smaller side
	private final static int READCONNECT_TIMEOUT = 1000*20;  // 20 second timeout on connect or read
	
	public Fetcher( AvailableProducts ap, String fileName ) throws Exception {
		downloadPath = OpenFlight.DOMAIN_PATH + "/" + fileName + ap.getSuffix();
		localPath = ap.getFullArchivePath( fileName );
		this.ap = ap;
	}	
	
	public boolean exists( ) {
		File f = new File( localPath );
		if ( f.canRead() )
			return true;
		return false;
	}
	
	public static int countZips( final String directory, final FileFilter filePattern ) {
		final File d = new File( directory );
		if ( ! d.canRead() || ! d.isDirectory() )
			return 0;
		
		final File[] files = d.listFiles( filePattern );
		return files.length;
	}
	
	public boolean remove( Context c ) {
		final File f = new File( localPath );
		if ( f.canRead() ) {
			if ( f.canWrite() ) {
				f.delete();
				// since this is in the same thread as the view builder, update directly
				Utils.getUtils().getOpenFlight().rebuildImageMap();
				return true;
			}
		}
		return false;
	}	
	
	private void updateChooser( ) {
		// FIXME why can't this get targeted directly as the ImageMap component?
		Utils.getUtils().getApplicationContext().sendBroadcast( new Intent( ap.getClazz().getCanonicalName() ) );
	}
	
	public void fetch( final Activity caller, final boolean updateImageMap ) throws Exception {

		// defer the license check until now so that user can use their existing maps,
		// just prevent getting new ones.
		// this is here in addition to selectmap since a user might try 'force download',
		// which circumvents the selectmap invocation
		
		final AsyncProgressDialog progress = new AsyncProgressDialog( caller, "Retrieving Map", "Please wait...") {
			@Override
			protected void doInBackground() {
 
				try {
					fetchOnly( this, updateImageMap );

				} catch (Exception ex) {
					// be a little private about the download location
					final String s = ex.getMessage().replace( OpenFlight.DOMAIN_PATH, "" );
					showCompletionDialog( "Retrieve failed: ", s );
				}
			}

			@Override
			protected void onPostExecute(Void unused) {
				super.onPostExecute(unused);
				showCompletionDialog( "Status", "Done" );	
			}
		};

		progress.execute();		
	}	
		
	/**
	 * Attempt to transfer the indicated file, showing progress to arg.
	 * Any failure causes partial data to be removed, and an exception to be emitted.
	 * Download is to a temp file which is renamed upon success since there are so many
	 * reasons a download might fail.
	 * 
	 * @param progress
	 * @throws Exception
	 */
	private void fetchOnly( final AsyncProgressDialog progress, final boolean updateImageMap ) throws Exception {
		InputStream in = null;
		FileOutputStream outStream = null;
		int lengthTarget = 0;
		int lengthRead = 0;
		File temp = new File( Fetcher.TEMP_FILENAME );
		WakeLock wakeLock = Utils.getUtils().getPowerManager().newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "OpenFlight");
		
		try {	
			wakeLock.acquire();
			
			final URL requestURL = new URL( downloadPath );
			final URLConnection connection = requestURL.openConnection();
			// time out the connection in a reasonable amount of time
			// the exception would be ignored, but the file size check removes partial download
			connection.setConnectTimeout( READCONNECT_TIMEOUT );
			connection.setReadTimeout( READCONNECT_TIMEOUT );
			final int length = connection.getContentLength();
			
			if ( length <= 0 ) {
				// show file sans domain
				progress.showCompletionDialog( "Failed", "Can't read file (" + 
						downloadPath.substring( OpenFlight.DOMAIN_PATH.length() + 1 ) + 
						").  Report to developer." );
				
			} else {
			
				lengthTarget = length;
				in = new BufferedInputStream( connection.getInputStream() );
				final byte[] data = new byte[ READ_CHUNK_SIZE ];		
				
				// make sure dirs exist; need to chop off the name of the archive since it's part of the localPath			
				final File dirs = new File( localPath.substring( 0, localPath.lastIndexOf( "/" ) ) );
				dirs.mkdirs();

				if ( temp.exists() )
					temp.delete();
				outStream = new FileOutputStream( temp );
				
				progress.setMax( length );			
				
				int read = 0;
				while ( ( read = in.read( data ) ) != -1 ) {
					progress.addProgress( read );
					outStream.write( data, 0, read );
					lengthRead += read;
				}

			}
			
		} finally {
			wakeLock.release();
			try {
				if ( in != null )
					in.close();
				if ( outStream != null )
					outStream.close();
			} finally {
				if ( lengthRead != lengthTarget ) {
					if ( temp != null )
						temp.delete();		
	
					progress.showCompletionDialog( "Failed", "File not expected length" );					
					
				} else {
					if ( temp != null ) {
						File outFile = new File( localPath );
						
						if ( outFile.exists() ) {
							outFile.delete();
						}
						
						if ( temp.renameTo( outFile ) ) {
//							progress.showCompletionDialog( "Success", "File " + outFile.getName() + " OK" );
							if ( updateImageMap) 
								updateChooser();
							
						} else {
							temp.delete();
							progress.showCompletionDialog( "Failed", "Check internet connection" );
						}
					}
				}
			}
		}
	}	
	
	/**
	 * Get a specific file from an existing zip file
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public InputStream fetchFile( String fileName ) throws IOException {
		final File f = new File( localPath );

		if ( f.canRead() ) {
			try {
				ZipFile zipFile = new ZipFile( f );
				
				final ZipEntry entry = zipFile.getEntry( fileName );
				if ( entry != null ) {
					final InputStream in = zipFile.getInputStream( entry );
					return in;
				}				
			} catch (final Throwable e) {
				throw new IOException( e.getMessage() );
			}
		}
		return null;
	}	
	
	// TODO use buffered output stream
	public void unzip(ZipInputStream zin, ZipEntry ze, String localDirectory )  throws IOException {
		
		String fullDirectory = localDirectory + "/" + ze.getName();
		
		if ( ze.isDirectory() ) {
			File f = new File( fullDirectory );
			f.mkdirs();
			
		} else {
			String newFile = fullDirectory;
			FileOutputStream out = null;
			
			try {
				out = new FileOutputStream(newFile);
				byte [] b = new byte[1024];
				int len = 0;
				while ( ( len = zin.read( b ) )!= -1 )
					out.write( b, 0, len );

			} finally {
				if ( out != null )
					out.close();
			}
		}
	}
}
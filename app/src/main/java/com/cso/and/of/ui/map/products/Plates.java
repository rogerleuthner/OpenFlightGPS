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

package com.cso.and.of.ui.map.products;

import android.content.Intent;
import android.os.Bundle;

import com.cso.and.of.ui.map.FileActivity;
import com.cso.and.of.ui.map.MapInfo;

/**
 * Find the target airport zip file.
 * 
 * List the contents of the zip file JPGS
 * 
 * @author Roger
 *
 */


public class Plates extends FileActivity {
	
	MapInfo mapInfo;
	
	public Plates() {
		super();		
	}	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		
		// fire off airport/chart selector for the selected state
		mapInfo = (MapInfo)this.getIntent().getExtras().get( DOC_KEY );	
		
		showPlateSelector( mapInfo.getDirectoryKey() );
		
	}	  
	
	public static final int SELECT_PLATE = 0;
	public static final int DOWNLOAD_PLATE = SELECT_PLATE + 1;
	public void onActivityResult( int requestCode, int resultCode, Intent data ) {
		switch ( requestCode ) {
		case SELECT_PLATE:
			// get the string name of the item selected, load it into a bitmap viewer
			;
		case DOWNLOAD_PLATE:
			;
		}
	}
	
	private final void showPlateSelector( String key ) {
		final Intent i = new Intent( this, PlatesSelector.class );
		i.putExtra(FileActivity.DOC_KEY, key );
		startActivityForResult( i, SELECT_PLATE );		
	}		

	@Override
	protected void cleanPauseStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void cleanStart() {
		// TODO Auto-generated method stub
		
	}
	
}

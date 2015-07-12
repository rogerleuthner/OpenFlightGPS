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

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import com.cso.and.of.BaseActivity;
import com.cso.and.of.config.Utils;
import com.cso.and.of.types.AvailableProducts;
import com.cso.and.of.ui.SelectMapActivity;

public abstract class FileActivity extends BaseActivity {
	
	public String DATA_ROOT;
	public static final String DOC_KEY = "theChart";
	public static final String LOC_KEY = "location";
	public static final String ZOM_KEY = "zoom";
	
	protected abstract void cleanPauseStop();
	
	protected abstract void cleanStart();
	
	public String getDataRoot() {
		return Utils.getUtils().getPrefs().getDataDirectory();
	}	
	
	public AvailableProducts getMe() {
		return AvailableProducts.getProductByClass( this.getClass() );
	}
		
	public FileActivity() {}			
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add( "Chart Chooser" )
		 .setIcon( android.R.drawable.ic_menu_mapmode )
		 .setOnMenuItemClickListener( chartChooserListener );	
		
		menu.add( "Lock Screen" )
		 .setIcon( android.R.drawable.ic_menu_close_clear_cancel )
		 .setOnMenuItemClickListener( lockScreenListener );

		return true;
	}
	
	// TODO convert this into a button on the screen in addition to a menu item after figure out
	// how to get a button onto the map!!
	protected OnMenuItemClickListener lockScreenListener = new OnMenuItemClickListener() {
		
		@Override
		public boolean onMenuItemClick( MenuItem m ) {
			
			if ( Utils.getUtils().getOpenFlight().isScreenActive ) {
				m.setTitle( "Unlock Screen" );				
				m.setIcon( android.R.drawable.ic_menu_edit );
				Utils.getUtils().getOpenFlight().setScreenActive( false );
			} else {
				m.setTitle( "Lock Screen" );
				m.setIcon( android.R.drawable.ic_menu_close_clear_cancel );
				Utils.getUtils().getOpenFlight().setScreenActive( true );
			}
			
			return true;
		}
	};
	
	protected OnMenuItemClickListener chartChooserListener = new OnMenuItemClickListener() {
		
// THESE COMMENTS IF REMOVED WILL ENABLE CONFIRM OF EXIT OF MAP BEFORE CHOOSER COMES UP		
		
		@Override
		public boolean onMenuItemClick( MenuItem m ) {		
			
//	    	new AlertDialog.Builder(FileActivity.this).setMessage("Exit Map?")
//	 	       .setCancelable(false)
//	 	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//	 	           public void onClick(DialogInterface dialog, int id) {
//	 	  				 	  			
//	 	  			// need to turn this off, else SelectMap just starts this up again; 
//	 	  			// TODO might just want to include an extra piece of data and avoid touching the setting
	 	  			Utils.getUtils().getPrefs().setMapByPosition( false );
	 	  			
	 	  			final Intent i = new Intent( Utils.getUtils().getApplicationContext(), SelectMapActivity.class );
	 	  			
	 	  			// without this flag, a new chart chooser would be created which would then have to
	 	  			// be clicked through on application close (as many time as this was executed)
	 	  			// a similar effect can be used with the android:lauchMode directive, but then when
	 	  			// on a map, then 'home' button, then go back to openflightmap, the map has been closed
	 	  			// and you are back at the chooser
	 	  			i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
	 	  			startActivity( i );
	 	  			
	 	  			cleanPauseStop();
	 	  			
	 	  			// finish this map now otherwise there is an annoying flicker when opening up another map
	 	  			// from the chart chooser
	 	  			finish();
//	 	           }
//	 	       })
//	 	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
//	 	           public void onClick(DialogInterface dialog, int id) {
//	 	                dialog.cancel();
//	 	           }
//	 	       })
//	 	       .create()
//	 	       .show();

			return false;
		}
	};		
	
	
	
	
	
	/**
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//
//		case R.id.myloc_menu:
//			zoomToLocation();
//			return true;
//		}
		return super.onOptionsItemSelected(item);
	}	
	
}

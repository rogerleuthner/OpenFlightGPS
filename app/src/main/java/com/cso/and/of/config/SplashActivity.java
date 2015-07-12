
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

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.TextView;

import com.cso.and.of.OpenFlight;
import com.cso.and.of.gps.FREE.R;
import com.cso.and.of.ui.SelectMapActivity;


public class SplashActivity extends Activity {
	EditText password;

	public static final String TIMEOUT_KEY = "timeout"; 
	public static final int MINIMUM_SPLASH_MILLIS = 5000;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		if ( ! Utils.getUtils().getPrefs().isDisclaimerSeen() ) {		
			
			setContentView( R.layout.install_splash );
			
			TextView tv = (TextView)findViewById( R.id.label_beta_text );			
			tv.setMovementMethod( new ScrollingMovementMethod() );
			
			password = (EditText)findViewById( R.id.edit_beta_password );
			password.setOnKeyListener( klisten );	
			return;			
		} 
			
		setContentView( R.layout.splash );
			
        // Start animating the image
//	    final ImageView splashImageView = (ImageView) findViewById( MyR.id.get( "SplashImageView" ) );
//	    splashImageView.setBackgroundResource( MyR.drawable.get( "splash_anim" ) );
//	    final AnimationDrawable frameAnimation = (AnimationDrawable)splashImageView.getBackground();
//	    splashImageView.post(new Runnable(){
//			@Override
//			public void run() {
//				frameAnimation.start();				
//			}	    	
//	    });						
		
	    /* When started from the SelectMap menu item, the TIMEOUT key is set,
	     * so we know not to start another SelectMap.
	     * When run from app initialization, this key is not present, so we know
	     * to start the select map activity.
	     */
	    if ( getIntent().getBooleanExtra( TIMEOUT_KEY, true ) ) {		    
	    	TimerTask task = new TimerTask() {
	    		@Override
	    		public void run() {
	    			Intent i = new Intent().setClass(SplashActivity.this, SelectMapActivity.class );
	    			startActivity( i );	   
	    			finish();
	    		}
	    	};
	    	
	    	Timer timer = new Timer();
	    	timer.schedule( task, MINIMUM_SPLASH_MILLIS );
	    }	    
	}	

//	private final void setContentTexts( int[] reasons ) {
//		
//		((TextView)findViewById( MyR.id.get( "splashVersion" ) ) ).setText( Utils.getUtils().getOpenFlight().getPayPay().getVersionLicensed() );
//		((TextView)findViewById( MyR.id.get( "splashAvailable" ) ) ).setText( Utils.getUtils().getOpenFlight().getPayPay().getMapsLicensed() == PayPay.UNLIMITED_MAPS ? "unlimited"  
//				: Integer.toString( Utils.getUtils().getOpenFlight().getPayPay().getMapsLicensed() ) );
//		((TextView)findViewById( MyR.id.get( "splashUsed" ) ) ).setText( Integer.toString( Utils.getUtils().getOpenFlight().getPayPay().getMapsUsed() ) );
//		((TextView)findViewById( MyR.id.get( "splashInstalled" ) ) ).setText( Utils.getUtils().getOpenFlight().getPayPay().getAppInstallTime() );
//		((TextView)findViewById( MyR.id.get( "splashExpires" ) ) ).setText( Utils.getUtils().getOpenFlight().getPayPay().getAppExpireTime() );
//		try {
//			((TextView)findViewById( MyR.id.get( "splashRevision" ) ) ).setText( getPackageManager().getPackageInfo( getPackageName(), 0 ).versionName );
//		} catch ( Exception e ) {
//			((TextView)findViewById( MyR.id.get( "splashRevision" ) ) ).setText( "Bad package name: " + e.getMessage() );
//		}		
//		
//		// mark any problems in red
//		if ( reasons != null ) {
//			
//			for( int i = 0; i < reasons.length; i ++ ) {
//				if ( reasons[ i ] == PayPay.FAIL_MAPS_EXCEEDED ) {
//					((TextView)findViewById( MyR.id.get( "splashUsed" ) ) ).setText( 
//							"licensed: " + Integer.toString( Utils.getUtils().getOpenFlight().getPayPay().getMapsLicensed() ) + 
//							" / found: " + Integer.toString( Utils.getUtils().getOpenFlight().getPayPay().countLoadedLicensableMaps() ) +
//							"   --->>GPS FEATURES DEACTIVATED"							
//							);
//					((TextView)findViewById( MyR.id.get( "splashUsed" ) ) ).setTextColor( Color.RED );
//				}
//				if ( reasons[ i ] == PayPay.FAIL_TIME_EXPIRED ) 
//					((TextView)findViewById( MyR.id.get( "splashExpires" ) ) ).setTextColor( Color.RED );
//			}
//		}
//	}

	private final OnKeyListener klisten = new OnKeyListener() {

		@Override
		public final boolean onKey( View v, int keyCode, KeyEvent event ) {
			validateInstallConfirm( keyCode );
			return false;
		}

	};

	private final void validateInstallConfirm( int keyCode ) {
		if ( keyCode == OpenFlight.KEYCODE_ENTER ) {
			if ( password.getText().toString()
				.trim()
				.toLowerCase()
				.equals( OpenFlight.KEY_ENTRY.toLowerCase() ) ) {

				Utils.getUtils().getPrefs().seenDisclaimer();
				
				setResult( RESULT_OK );
				
//		    	new AlertDialog.Builder(this).setMessage( R.string.hard_stop )
//		 	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//		 	           public void onClick(DialogInterface dialog, int id) {
//		   	    			Intent i = new Intent().setClass(SplashActivity.this, SelectMapActivity.class );
//		   	    			startActivity( i );	   
//		   	    			finish();
//		 	           }
//		 	       })
//		 	       .create()
//		 	       .show();											
				
			} else {
				setResult( RESULT_CANCELED );
			}
			
			finish();
		} 		
	}

	public static void initialSplash( Activity context ) {
		if ( ! Utils.getUtils().getPrefs().isDisclaimerSeen()  ) {
			Intent i = new Intent( context, SplashActivity.class );
			context.startActivityForResult( i, OpenFlight.INSTALL_CODE );
		}	
	}
	
	/**
	 * If this is a result from our beta screen, handle it correctly (probably
	 * by terminating the app)
	 * 
	 * @param requestCode
	 * @param resultCode
	 */
	public static boolean handleActivityResult( int requestCode, int resultCode ) {
		if ( resultCode == RESULT_OK ) {
			Utils.getUtils().getPrefs().seenDisclaimer();
			return true;
		} else {
			return false;
		}
	}
}

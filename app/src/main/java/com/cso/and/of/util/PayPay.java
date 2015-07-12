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

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.provider.Settings;

import com.cso.and.of.config.Utils;
import com.cso.and.of.types.AvailableProducts;


public class PayPay {
	
	public static final AvailableProducts[] LICENSED_MAPS = new AvailableProducts[] {
		AvailableProducts.WorldArea,
		AvailableProducts.Sectional,
		AvailableProducts.TerminalTAC,
		AvailableProducts.EnrouteLow
	};

    public PayPay( Context ctx ) {
		if ( ! Utils.getUtils().getPrefs().isIdSaved() ) {			
			Utils.getUtils().getPrefs().saveId( getDeviceId() );		    
		}
    }
	
    /**
     * Construct ID for the app.  This is meant to be executed once and
     * then stored for later use, since it is not exactly deterministic,
     * and depending upon the device/os version and environment, it might 
     * not return the same result from one run to the next.
     * 
     * @return String
     */
    public String getDeviceId() {
    	    	
    	StringBuffer id = new StringBuffer( System.getProperty( "user.name" ) );
    	
    	if ( Settings.Secure.ANDROID_ID != null ) {
    		id.append( Settings.Secure.ANDROID_ID );
    	}
    	
    	WifiInfo wif = Utils.getUtils().getWifiManager().getConnectionInfo();
    	
    	if ( wif != null ) {
    		id.append( wif.getMacAddress() );
    	}
    	
    	return id.toString();
    }        

	public final String getAppInstallTime() {			
		long ts = Utils.getUtils().getAppInstallTimeLong();	
		SimpleDateFormat sdf = new SimpleDateFormat( "yyyy MM dd HH:mm:ss" );
		return sdf.format( new Date( ts ) );			
	}
	
	public String getAppExpireTime() {
		return "Never!  You're welcome!";
	}		
		
	
	

}



/*	
    private static final byte[] SALT = new byte[] {
        -75, 92, 55, -12, 94, 43, 22, 98, 32, 9, 65,
        31, 99, 2, 18, -46, -25, 49, 81, 38
        };

    public static byte[] getSalt() { return SALT; }
    
    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
	
	// concatenate these values in the order of the id to get it
	private static String KEYP2 = "/vqBuuPgz+qx9+4KSpZ1RyMAHQ/jJeczAON28p8AxzM17DuAoXnlKTKiWxObNkexs0mpF74+pzX4ebp5RNkUXmRFqMzTY6CgBOe+MazYJSwZbQ7z/";
	private static String KEYP4 = "QNdU6lzYskpGRMtgayzTwxb9VsDwhwiZZomBsI1Cczg839h4FaNfd4zPaZ+f5uc6LSJ0Lw1+dDcM6oHQIDAQAB";
	private static String KEYP1 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu2TtV7Ey0Ic0Y9sBFKxR20wyC5tlTU7FxGxf/";		
	private static String KEYP3 = "wm7Xj9wsJcI2REfJCRx8A6Ifv1/g+si17H/pgi8lyjOK+h14Lg6edfXZLzIQE2qa5adiROaRSaJHjxgSw0xJ6O5OygUAyoKky3aMrgZ0WrARcvGt";	
	
	public String getKey() { 
		StringBuffer k = new StringBuffer();
		k.append( KEYP1 );
		k.append( KEYP2 );
		k.append( KEYP3 );
		k.append( KEYP4 );
		return k.toString();
	}
	
    public PayPay( Context ctx ) {
		
		// to retain backward compatibility, make sure that the installation unique id
		// has been generated even though this might not be the initial install		
    	// this will create the id and stick it into the preferences which will be the exclusive
    	// place that the id is checked from
// TODO if they clear the application data, will this work OK?
// it will have to recreate the id ...
		if ( ! Utils.getUtils().getPrefs().isIdSaved() ) {			
			Utils.getUtils().getPrefs().saveId( getDeviceId() );		    
		}
    	
        mLicenseCheckerCallback = new MyLicenseCheckerCallback();

        // Construct the LicenseChecker with a Policy.
        mChecker = new LicenseChecker(
            ctx, new ServerManagedPolicy( ctx,
                new AESObfuscator( PayPay.getSalt(), ctx.getPackageName(), Utils.getUtils().getPrefs().getDeviceId() ) ),
                getKey()
            );    	
    }
            
    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow() {
//            if (activity.isFinishing()) {
//                // Don't update UI if Activity is finishing.
//                return;
//            }

        }

        public void dontAllow() {
//            if (activity.isFinishing()) {
//                // Don't update UI if Activity is finishing.
//                return;
//            }
            // Should not allow access. An app can handle as needed,
            // typically by informing the user that the app is not licensed
            // and then shutting down the app or limiting the user to a
            // restricted set of features.
        }

		@Override
		public void applicationError(ApplicationErrorCode errorCode) {
			// TODO Auto-generated method stub
			// LICENSED
			// LICENSED_OLD_KEY
			// NOT_LICENSED
			// @see http://developer.android.com/guide/publishing/licensing.html#server-response-codes
		}
    }	    
*/   
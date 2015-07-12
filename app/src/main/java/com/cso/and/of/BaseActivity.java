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

package com.cso.and.of;

import android.app.Activity;
import android.os.Bundle;

import com.cso.and.of.config.Utils;

/**
 * Every (significant) activity in OFM must extend this class.
 *  
 * @author Roger
 *
 */
public class BaseActivity extends Activity {
    
    @Override
    protected void onCreate( final Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
    }
    
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);    	
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
    	return super.onRetainNonConfigurationInstance();
    }    

    @Override
    protected void onStart() {
    	super.onStart();
    }	

    
    @Override
    protected void onStop() {
    	super.onStop();
    	Utils.getUtils().cancelNotify();
    }
        
}

     
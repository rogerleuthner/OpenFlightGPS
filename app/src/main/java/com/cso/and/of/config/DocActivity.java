
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

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cso.and.of.gps.FREE.R;

public class DocActivity extends Activity {
	
	private static final String ASSET_MANUAL_INDEX = "file:///android_asset/manual/index.html";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		setContentView( R.layout.about_main );
		super.onCreate(savedInstanceState);

		WebView view = (WebView) findViewById( R.id.about_main );
		// required to allow user to follow links to other pages
		view.setWebViewClient( new WebViewClient() );

		// view.getSettings().setJavaScriptEnabled(true);
		view.loadUrl( ASSET_MANUAL_INDEX );
	}

}

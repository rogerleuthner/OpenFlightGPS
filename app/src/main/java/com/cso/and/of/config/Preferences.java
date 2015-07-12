
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

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.cso.and.of.gps.FREE.R;

public class Preferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource( R.xml.preferences );
	}


//	// debug
//	private void dump() {
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//
//		Map<String, ?> allPrefs = prefs.getAll();
//
//		for (Entry<String, ?> ent : allPrefs.entrySet()) {
//			String desc = ent.getKey() + "=" + ent.getValue().toString();
//
//			Log.d(getClass().getSimpleName(), desc);
//		}
//	}
}

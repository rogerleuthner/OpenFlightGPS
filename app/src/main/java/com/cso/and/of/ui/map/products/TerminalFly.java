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

import android.os.Bundle;

import com.cso.and.of.ui.map.MapActivity;


public class TerminalFly extends MapActivity {
	
	public TerminalFly() {
		super();
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );
		
	}	

	@Override
	public int getMaxZoom() {
		return 4;
	}

	@Override
	public int getMinZoom() {
		return 2;
	}

	@Override
	public int getDefaultLocatedZoom() {
		// FLY isn't georef'd, so zoom fully out
		return getMinZoom();
	}
}

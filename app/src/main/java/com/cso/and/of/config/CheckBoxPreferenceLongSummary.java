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

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * For those situations where checkbox summary is being used as documentation.
 * Seems that the standard summary max len is only two or three lines.
 *
 */

public class CheckBoxPreferenceLongSummary extends CheckBoxPreference{

	public final int MAX_SUMMARY_LINES = 10;
	
    public CheckBoxPreferenceLongSummary(Context context) {
        super( context );
    }

    public CheckBoxPreferenceLongSummary(Context context, AttributeSet attrs) {
        super( context, attrs );
    }
    
    public CheckBoxPreferenceLongSummary(Context context, AttributeSet attrs, int defStyle) {
        super( context, attrs, defStyle );
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView summaryView = (TextView)view.findViewById( android.R.id.summary );
        summaryView.setMaxLines( MAX_SUMMARY_LINES );
    }
}

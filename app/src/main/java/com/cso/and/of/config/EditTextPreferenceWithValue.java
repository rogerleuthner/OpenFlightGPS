
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
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.cso.and.of.gps.FREE.R;


public class EditTextPreferenceWithValue extends EditTextPreference {

	private TextView mValueText;

	public EditTextPreferenceWithValue(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource( R.layout.preference_with_value );
	}

	public EditTextPreferenceWithValue(Context context) {
		super(context);
		setLayoutResource( R.layout.preference_with_value );
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mValueText = (TextView) view.findViewById( R.id.preference_value );
		if (mValueText != null) {
			mValueText.setText(getText());
		}
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		if (mValueText != null) {
			mValueText.setText(getText());
		}
	}

}

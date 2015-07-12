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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public abstract class AsyncProgressDialog extends AsyncTask<Void, Void, Void> {

	private String completionMessage = null;
	private String completionDialogTitle = null;
	private ProgressDialog dialog;

	protected abstract void doInBackground();

	public AsyncProgressDialog(Context context, String dialogTitle, String dialogMessage) {
		dialog = new ProgressDialog( context );
		dialog.setTitle( dialogTitle );
		dialog.setMessage( dialogMessage );
//		dialog.setDismissMessage(  new Message() );
		dialog.setIndeterminate( false );
		dialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
		dialog.show();
	}
	
	public int getProgress() { return dialog.getProgress(); }
	
	public void addProgress( int i ) {
		dialog.setProgress( dialog.getProgress() + i );
	}
	
	public void setMax( int i ) {
		dialog.setMax( i );
	}

	protected boolean isShowingDialog() {
		return completionDialogTitle != null;
	}

	protected void showCompletionToast(String message) {
		completionMessage = message;
	}

	protected void showCompletionDialog(String dialogTitle, String message) {
		completionMessage = message;
		completionDialogTitle = dialogTitle;
	}

	protected void onPostExecute(Void unused) {
		Context context = dialog.getContext();

		dialog.dismiss();
		
		if (isShowingDialog()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(completionDialogTitle);
			builder.setMessage(completionMessage);
			builder.setPositiveButton( "OK", null );

			AlertDialog alert = builder.create();
			alert.show();
		} else if (completionMessage != null) {
			Toast.makeText(context, completionMessage, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected Void doInBackground(Void... params) {
		doInBackground();

		return null;
	}
}
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

import java.io.File;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.cso.and.of.ui.map.FileActivity;

/**
 * Non-geo located PDF viewer
 * 
 * @author roger
 *
 */

public class PDFView extends FileActivity {
	
	// TODO find out how to pass the notifier handle into started activities, then standardize it here
	
	public static final String PDF_MIME = "application/pdf";
	public static final String FILE_TYPE = ".pdf";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        
        File file = new File( "asdf asdf " );        		

        // assume file exists
        Uri path = Uri.fromFile(file);
        
    	Intent PDFintent;

		List<ResolveInfo> PDFintents;
		
		PDFintent = new Intent( Intent.ACTION_VIEW );
		PDFintent.setType( PDF_MIME );

		PDFintents = getPackageManager().queryIntentActivities( PDFintent, PackageManager.MATCH_DEFAULT_ONLY  );
		
		if ( PDFintents == null || PDFintents.size() == 0 ) {
	        Toast.makeText(PDFView.this, 
	                "No Application Available to View PDF", 
	                Toast.LENGTH_SHORT).show();
		}

		PDFintent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        PDFintent.setDataAndType( path, PDF_MIME );
        
        startActivity( PDFintent );        
    }

	@Override
	protected void cleanPauseStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void cleanStart() {
		// TODO Auto-generated method stub
		
	}
}

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

import com.cso.and.of.ui.map.FileActivity;
import com.cso.and.of.ui.map.MapInfo;


public class PlatesSelector extends ExpandableListActivity {

    ExpandableListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapInfo mapInfo = (MapInfo)this.getIntent().getExtras().get( FileActivity.DOC_KEY );
        
        // Set up our adapter
        mAdapter = new MyExpandableListAdapter( mapInfo.getDirectoryKey() );
        setListAdapter(mAdapter);
        registerForContextMenu(getExpandableListView());
        	
        
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Available States");
        menu.add(0, 0, 0, "Choose State");
        
        /* here prob want to allow addition of a new airport */
        /* this may not be usefule here */
        
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();

        String title = ((TextView) info.targetView).getText().toString();
        
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition); 
            int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition); 
            Toast.makeText(this, title + ": Child " + childPos + " clicked in group " + groupPos,
                    Toast.LENGTH_SHORT).show();
            return true;
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition); 
            Toast.makeText(this, title + ": Group " + groupPos + " clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return false;
    }

    /**
     * A simple adapter which maintains an ArrayList of photo resource Ids. 
     * Each photo is displayed as an image. This adapter supports clearing the
     * list of photos and adding a new photo.
     *
     */
    public class MyExpandableListAdapter extends BaseExpandableListAdapter {
        // Sample data set.  children[i] contains the children (String[]) for groups[i].
        private String[] airports = { "People Names", "Dog Names", "Cat Names", "Fish Names" };
        private String[][] plates = {
                { "Arnold", "Barry", "Chuck", "David" },
                { "Ace", "Bandit", "Cha-Cha", "Deuce" },
                { "Fluffy", "Snuggles" },
                { "Goldy", "Bubbles" }
        };
        
        final ArrayList<ZipFile> zipFiles = new ArrayList<ZipFile>();        
        
        public MyExpandableListAdapter( String state ) {
        	airports = listAirports( state );
        	plates = new String[airports.length][];
        	
//        	// for each airport
//        	for( String s : airports ) {
//        	
//        	}
        }
        
        public Object getChild(int groupPosition, int childPosition) {
            return plates[groupPosition][childPosition];
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return plates[groupPosition].length;
        }

        public TextView getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 64);

            TextView textView = new TextView(PlatesSelector.this);
            textView.setLayoutParams(lp);
            // Center the text vertically
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }
        
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getChild(groupPosition, childPosition).toString());
            return textView;
        }

        public Object getGroup(int groupPosition) {
            return airports[groupPosition];
        }

        public int getGroupCount() {
            return airports.length;
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getGroup(groupPosition).toString());
            return textView;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }
        
    	private String[] listAirports( String root ) {
    		final File[] z = new File( root ).listFiles(new FileFilter() {
    			@Override
    			public boolean accept(final File aFile) {
    				return aFile.isFile() && aFile.getName().endsWith(".zip");
    			}
    		});

    		if (z != null) {
    			for (final File file : z) {
    				try {
    					zipFiles.add(new ZipFile(file));
    				} catch (final Throwable e) {
//    					logger.warn("Error opening zip file: " + file, e);
    				}
    			}
    			return zipFiles.toArray( new String[0] );
    		}
    		return null;
    	}	
    	    	
    	public String[] listPlates( String zipFile ) throws Exception {
    		ZipInputStream zin = null;
    		InputStream in = null;
    		ArrayList<String>plates = new ArrayList<String>();

    		try {
    			
    			in = new BufferedInputStream( new FileInputStream( zipFile ) );
    			zin = new ZipInputStream(in);
    			ZipEntry e;
    	
    			while( ( e = zin.getNextEntry() ) !=  null ) {
    				if ( ! e.isDirectory() )
    					plates.add( e.getName() );
    			}
    	
    		} finally {
    			if ( zin != null )
    				zin.close();
    			if ( in != null )
    				in.close();
    		}

    		return plates.toArray( new String[ 0 ] );
    	}        

    }
}
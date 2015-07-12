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

package com.cso.and.of.location;

/**
*
* @author Simon Thépot <simon.thepot@gmail.com> from Makina Corpus
*
*/


import java.util.ArrayList;
import java.util.List;

import org.andnav.osm.ResourceProxy;
import org.andnav.osm.util.BoundingBoxE6;
import org.andnav.osm.util.MyMath;
import org.andnav.osm.views.util.Mercator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.MotionEvent;

import com.cso.and.of.ui.map.MapActivity;

public class BoundedTileOverlay { //extends TilesOverlay {

//	private static final String TAG = "BoundedTileOverlay >>>: ";
//	private final BoundingBoxE6 bb;
//
//	protected final Paint mPaint = new Paint();
//	private Paint whitePaint = new Paint();
//	
//	private boolean bdebug = false;
//	
//	// do not touch this; reused
//	private Point topleft, bottomr;
//	private Rect tmpRect = new Rect();
//
//	{
//		whitePaint.setColor(Color.WHITE);
//	}
//
//	public BoundedTileOverlay(MapTileProviderBase aTileProvider,
//			Context aContext, BoundingBoxE6 bb) {
//		this(aTileProvider, new ResourceProxyImpl(aContext), bb);
//	}
//
//	public BoundedTileOverlay(MapTileProviderBase aTileProvider,
//			ResourceProxy pResourceProxy, BoundingBoxE6 bb) {
//		super(aTileProvider, pResourceProxy);
//
//		this.bb = bb;
//		this.topleft = new Point();
//		this.bottomr = new Point();
//	}
//
//	private void log(String str){
//		if(bdebug)
//			System.out.println(str);
//	}
//	private void logg(String str){
//		if(bdebug)
//		System.out.print(str);
//	}
//	
//	// same as TilesOverlay with Log
//	@Override
//	protected void onDraw(final Canvas c, final MapView osmv) {	
//
//		// Calculate the half-world size
//		final Projection pj = osmv.getProjection();
//		final int zoomLevel = pj.getZoomLevel();
//		final int tileZoom = pj.getTileMapZoom();
//		log(TAG + " zoomLevel: " + zoomLevel + "/ tileZoom: " + tileZoom);
//		
//		mWorldSize_2 = 1 << (zoomLevel + tileZoom - 1);
//		
//		log(TAG + "mWorldSize_2: " + mWorldSize_2);
//		// Get the area we are drawing to
//		c.getClipBounds(mViewPort);
//		log(TAG + " ViewPort b4offset: " + mViewPort.toShortString());
//		
//		// Translate the Canvas coordinates into Mercator coordinates
//		mViewPort.offset(mWorldSize_2, mWorldSize_2);
//		
//		log(TAG + " ViewPort w/offset: " + mViewPort.toShortString());
//		// Draw the tiles!
//		drawTiles(c, pj.getZoomLevel(), pj.getTileSizePixels(), mViewPort);
//	}
//
//	
//	
//	
//	
//	// should precise an extent OR a tile.
//	// Rect target may be specify or null. If null we will use a shared Rect
//	private Rect extent2rect(int zoom, int tileSizePx, Rect target) {
//		topleft = Mercator.projectGeoPoint(bb.getLatNorthE6(), bb
//				.getLonWestE6(), zoom, topleft);
//		bottomr = Mercator.projectGeoPoint(bb.getLatSouthE6(), bb
//				.getLonEastE6(), zoom, bottomr);		
//
//		
//		Rect result = target != null ? target : tmpRect;
//		
//		topleft.x += 1; bottomr.x += 1; 
//		topleft.y += 1;
//		// topleft.y -= 1;  bottomr.y -= 1;
//		// topleft.x -= 1; topleft.y -= 1; bottomr.x -= 1; bottomr.y -= 1;
//		// topleft.x -= 1; topleft.y -= 1; bottomr.x += 1; bottomr.y += 1;
//		
//		log("\t\t>> MAX RECT EXTENT <<");		
//		logg("\t\t In TilePixel: ");
//		logg("[" + topleft.x + ", " + topleft.y + "]");
//		logg("[" + bottomr.x + ", " + bottomr.y + "]");
//
//		result.set(topleft.x * tileSizePx, topleft.y * tileSizePx, bottomr.x
//				* tileSizePx, bottomr.y * tileSizePx);
//		
//		log("\t\t" + "In Pixel: " + result.toShortString());
//
//		return result;
//	}
//
//	public void drawTiles(final Canvas c, final int zoomLevel,
//			final int tileSizePx, final Rect viewPort) {
//
//		int intersection = 0;
//		final int tileZoom = MapView.getMapTileZoom(tileSizePx);
//		/*
//		 * Calculate the amount of tiles needed for each side around the center
//		 * one.
//		 */
//		
//		/* ViewPort is in full pixel*/
//		
//		final int tileNeededToLeftOfCenter = (viewPort.left >> tileZoom) - 1; // /256
//		final int tileNeededToRightOfCenter = viewPort.right >> tileZoom;
//		final int tileNeededToTopOfCenter = (viewPort.top >> tileZoom) - 1;
//		final int tileNeededToBottomOfCenter = viewPort.bottom >> tileZoom;
//		
//		// Left:32471/Right:32473/Top:22992/Bottom:22995
//		
//		// =>
//		/*
//		final int tileNeededToLeftOfCenter = viewPort.left >> tileZoom; // /256
//		final int tileNeededToRightOfCenter = (viewPort.right >> tileZoom) + 1;
//		final int tileNeededToTopOfCenter = viewPort.top >> tileZoom;
//		final int tileNeededToBottomOfCenter = (viewPort.bottom >> tileZoom) + 1;
//		*/
//		
//		log(TAG + "Left:" + tileNeededToLeftOfCenter + "/"
//				+ "Right:" + tileNeededToRightOfCenter + "/" + "Top:"
//				+ tileNeededToTopOfCenter + "/" + "Bottom:"
//				+ tileNeededToBottomOfCenter);
//
//		// extent
//		Rect maxRect = extent2rect(zoomLevel, tileSizePx, null);
//
//		// nombre de tuile par cote en fonction du niveau de zoom
//		// permet de récupérer le numero de la tuile: x/y
//		final int mapTileUpperBound = (1 << zoomLevel); // -1; //?! >_<
//		log(TAG + "mapTileUpperBound: " + mapTileUpperBound);
//
//		// make sure the cache is big enough for all the tiles
//		final int numNeeded = (tileNeededToBottomOfCenter
//				- tileNeededToTopOfCenter + 1)
//				* (tileNeededToRightOfCenter - tileNeededToLeftOfCenter + 1);
//		mTileProvider.ensureCapacity(numNeeded);
//		log(TAG + "numNeeded: " + numNeeded);
//
//		List<Pair<MapTile, Rect>> tiles2draw = new ArrayList<Pair<MapTile, Rect>>();
//
//		
//		if (! viewPort.contains(maxRect.centerX(), maxRect.centerY())){
////			System.out.println("BEFORE ViewPort: " + viewPort.toShortString());
////			System.out.println("BEFORE center: " + LgMap.getLgMap().getMapCenter());
////			System.out.println("OUT OF ZONE: " + viewPort.toShortString() + "| Center X/Y: " + maxRect.centerX() + "/" + maxRect.centerY());
//			// LgMap.getLgMap().scrollTo(maxRect.centerX() >> tileZoom, maxRect.centerY() >> tileZoom);
//			
//			int centerx = maxRect.centerX() - mWorldSize_2;
//			int centery = maxRect.centerY() - mWorldSize_2;
//			// mWorldSize_2 = 1 << (zoomLevel + tileZoom - 1);
//			// LgMap.getLgMap().scrollTo(maxRect.centerX() - mWorldSize_2, maxRect.centerY() - mWorldSize_2);
//			// LgMap.getLgMap().
//			//LgMap.getLgMap().s
//			
//			// LgMap.getLgMap().requestRectangleOnScreen(new Rect(centerx, centery, centerx,  centery));
//		}
//		// LgMap.getLgMap().getController().
////		System.out.println("After ViewPort: " + viewPort.toShortString());
////		System.out.println("After center: " + LgMap.getLgMap().getMapCenter());
//		
//		/* Draw all the MapTiles (from the upper left to the lower right). */
//		for (int y = tileNeededToTopOfCenter; y <= tileNeededToBottomOfCenter; y++) {
//			for (int x = tileNeededToLeftOfCenter; x <= tileNeededToRightOfCenter; x++) {
//
//				mTileRect.set(x * tileSizePx, y * tileSizePx, x * tileSizePx
//						+ tileSizePx, y * tileSizePx + tileSizePx);
//				
//				if (!Rect.intersects(maxRect, mTileRect)) {
//					intersection++;
////					log("NO Intersection: ");
////					log("\t\t" + "Tile chosen rect: " + mTileRect.toShortString());
////					int tileY = MyMath.mod(y, mapTileUpperBound);
////					int tileX = MyMath.mod(x, mapTileUpperBound);
////					log("\t\t" + "Tile chosen x/y : " + tileX + "/" + tileY);
//					// whiteTileReadyToDraw(c, mTileRect); // undo this
//					continue;
//				}
//				logg("Intersection: ");
//				log(TAG + "Tile chosen rect: " + mTileRect.toShortString());
//				
//
//				// Construct a MapTile to request from the tile provider.
//				int tileY = MyMath.mod(y, mapTileUpperBound);
//				int tileX = MyMath.mod(x, mapTileUpperBound);
//				
//				// HACK
//				/*
//				tileX += 1;
//				tileY += 1; 				
//				mTileRect.offset(tileSizePx, tileSizePx);
//				*/
//				
//				log(TAG + "Tile chosen x/y : " + tileX + "/" + tileY);
//				
//				tiles2draw.add(new Pair<MapTile, Rect>(new MapTile(zoomLevel,
//						tileX, tileY), new Rect(mTileRect)));
//			}
//
//			for (Pair<MapTile, Rect> pair : tiles2draw) {
//
//				Drawable currentMapTile = mTileProvider.getMapTile(pair.first);
//				if (currentMapTile == null) {
//					// currentMapTile = getLoadingTile();
//					// log(TAG + "loadingTile...");
//				}
//
//				if (currentMapTile != null) {
//					log("DrawingTile: " + pair.first);
//					onTileReadyToDraw(c, currentMapTile, pair.second);
//				}
//
////				if (DEBUGMODE) { // DEBUGMODE;
////					c.drawText(pair.first.toString(), pair.second.left + 1,
////							pair.second.top + mPaint.getTextSize(), mPaint);
////					c.drawLine(pair.second.left, pair.second.top, pair.second.right,
////							pair.second.top, mPaint);
////					c.drawLine(pair.second.left, pair.second.top, pair.second.left,
////							pair.second.bottom, mPaint);
////				}
//			}
//		}
//
//		// draw a cross at center in debug mode
//		if (DEBUGMODE) {
//			// final GeoPoint center = osmv.getMapCenter();
//			final Point centerPoint = new Point(viewPort.centerX()
//					- mWorldSize_2, viewPort.centerY() - mWorldSize_2);
//			c.drawLine(centerPoint.x, centerPoint.y - 9, centerPoint.x,
//					centerPoint.y + 9, mPaint);
//			c.drawLine(centerPoint.x - 9, centerPoint.y, centerPoint.x + 9,
//					centerPoint.y, mPaint);
//		}
//
//		log("NO INTERSECTION COUNT: " + intersection +"/" + numNeeded);
//	}
//
//	// onTileReadyToDraw copy
//	protected void whiteTileReadyToDraw(final Canvas c, final Rect tileRect) {
//		tileRect.offset(-mWorldSize_2, -mWorldSize_2);
//		c.drawRect(tileRect, whitePaint);
//	}
//
//	
//	// http://developer.android.com/reference/android/view/GestureDetector.OnGestureListener.html
//	// http://developer.android.com/reference/android/view/MotionEvent.html
//	// http://developer.android.com/reference/android/view/View.html
//	// http://developer.android.com/reference/android/widget/Scroller.html
//	
//	@Override
//	public boolean onScroll(MotionEvent pEvent1, MotionEvent pEvent2, float pDistanceX, float pDistanceY, MapView mv) {
//		
//
//		final Projection pj = mv.getProjection();
//		final int zoomLevel = pj.getZoomLevel();
//		final int tileZoom = pj.getTileMapZoom();
//		int _mWorldSize_2 = 1 << (zoomLevel + tileZoom - 1);
//		
//		Rect maxRect = extent2rect(mv.getZoomLevel(), pj.getTileSizePixels(), new Rect());	
//		maxRect.offset(- _mWorldSize_2, - _mWorldSize_2);
//		
//		// System.out.println("X/Y: " + pEvent2.getX() + "/" + pEvent2.getY());
//		// System.out.println("distance X/Y: " + pDistanceX + "/" + pDistanceY);
//		
//		// pEvent2.getPointerCount()
//					
//		Point p = new Point((int) (mv.getScrollX() + pDistanceX), (int) (mv.getScrollY() + pDistanceY));
//		// Point p = new Point((int) (pEvent2.getX() + pDistanceX), (int) (pEvent2.getY() + pDistanceY));
//		// Point p = new Point((int) pEvent2.getX(), (int) pEvent2.getY());
//		
//		if (maxRect.contains(p.x, p.y))
//			return false;
//
//		// mv.scrollBy((int) pDistanceX, (int) pDistanceY);
//		return true;
//	}
//	
//	@Override
//	public boolean onFling(MotionEvent pEvent1, MotionEvent pEvent2, float velocityX, float velocityY, MapView mv) {
//		
//		
//		final Projection pj = mv.getProjection();
//		final int zoomLevel = pj.getZoomLevel();
//		final int tileZoom = pj.getTileMapZoom();
//		int _mWorldSize_2 = 1 << (zoomLevel + tileZoom - 1);
//		
//		Rect maxRect = extent2rect(mv.getZoomLevel(), pj.getTileSizePixels(), new Rect());	
//		
//		
//		// viewPort.contains(maxRect.centerX(), maxRect.centerY()))
//		int left = maxRect.left - _mWorldSize_2;
//		int right = maxRect.right - _mWorldSize_2;
//		int top = maxRect.top - _mWorldSize_2;
//		int bottom = maxRect.bottom - _mWorldSize_2;
//		
////		final int worldSize = mv.getWorldSizePx();
////		mScroller.fling(getScrollX(), getScrollY(), (int) -velocityX, (int) -velocityY,
////				-worldSize, worldSize, -worldSize, worldSize);
//		mv.getScroller().fling(
//				mv.getScrollX(), mv.getScrollY(),
//				(int) -velocityX, (int) -velocityY,
//				// -worldSize, worldSize, -worldSize, worldSize
//				// int minX, int maxX, int minY, int maxY
//				left, right, top, bottom
//		);
//		
//		return true;
//	}
//	
}

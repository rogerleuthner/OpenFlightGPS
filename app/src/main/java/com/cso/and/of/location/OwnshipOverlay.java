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

import java.util.LinkedList;
import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.OpenStreetMapViewController;
import org.andnav.osm.views.overlay.MyLocationOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay.Snappable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.MotionEvent;

import com.cso.and.of.OpenFlight;
import com.cso.and.of.config.Utils;

/**
 *
 * @author Manuel Stahl
 * @author Roger Leuthner
 */
public class OwnshipOverlay extends OpenStreetMapViewOverlay implements SensorEventListener, LocationListener, Snappable {

	private static final Logger logger = LoggerFactory.getLogger(MyLocationOverlay.class);

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final Paint mPaint = new Paint();
	protected final Paint mCirclePaint = new Paint();

	protected final Bitmap LOCATION_ICON;  // not moving, we are here
	protected final Bitmap OWNSHIP_ICON;  // points with the device, not the aircraft heading

	protected final OpenStreetMapView mMapView;

	private final OpenStreetMapViewController mMapController;

	private boolean mMyLocationEnabled = false;
	private LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
	private final Point mMapCoords = new Point();

	protected Location mLocation;
	private long mLocationUpdateMinTime = 0;
	private float mLocationUpdateMinDistance = 0.0f;
	protected boolean mFollow = false;	// follow location updates

	private final Matrix directionRotater = new Matrix();

	/** Coordinates the feet of the person are located. */
	protected final android.graphics.Point PERSON_HOTSPOT = new android.graphics.Point(24,39);

	private final float OWNSHIP_ICON_CENTER_X;
	private final float OWNSHIP_ICON_CENTER_Y;

	// Compass values
	private boolean mCompassEnabled = false;
	private final boolean mOrientationSensorAvailable;

	protected final Picture mCompassFrame = new Picture();
	protected final Picture mCompassRose = new Picture();
	private final Matrix mCompassMatrix = new Matrix();

	// actual compass value. Note: this one is only changed when an actual compass value
	// is being read, so a check >= 0 is valid
	private float mAzimuth = -1.0f;

	private float mCompassCenterX = 35.0f;
	private float mCompassCenterY = 35.0f;
	private float mCompassRadius = 20.0f;

	private final float COMPASS_FRAME_CENTER_X;
	private final float COMPASS_FRAME_CENTER_Y;
	private final float COMPASS_ROSE_CENTER_X;
	private final float COMPASS_ROSE_CENTER_Y;

	private float mScale = 1.0f;
	private float textHeight = 30f;

	// to avoid allocations during onDraw
	private final float[] mMatrixValues = new float[9];
	private final GeoPoint mGeoPoint = new GeoPoint(0, 0);
	private final Matrix mMatrix = new Matrix();	
	private final GeoPoint mCenterPoint = new GeoPoint(0, 0);
	
	// not a true overlay for efficiency + it depends upon location updates from here
	private final DataOverlay dataOverlay = new DataOverlay();
	private boolean mDataEnabled = true;	
	
	// ===========================================================
	// Constructors
	// ===========================================================

	public OwnshipOverlay( final Context ctx, final OpenStreetMapView mapView ) {
		super( ctx );
		mMapView = mapView;		
		mMapController = mapView.getController();
		mCirclePaint.setARGB(0, 100, 100, 255);
		mCirclePaint.setAntiAlias(true);
		mPaint.setAntiAlias( true );
		mPaint.setStyle(Style.FILL);
		mPaint.setStrokeWidth(1.0f);
		mPaint.setColor( Color.WHITE );
		mPaint.setTextSize( textHeight );
		
		
		// TODO define these file names somewhere, or do it with resources!!
		LOCATION_ICON = Utils.getUtils().getBitmapAsset( "location_icon.png" ); 
		OWNSHIP_ICON = Utils.getUtils().getBitmapAsset( "ownship_icon.png" );

		OWNSHIP_ICON_CENTER_X = OWNSHIP_ICON.getWidth() / 2 - 0.5f;
		OWNSHIP_ICON_CENTER_Y = OWNSHIP_ICON.getHeight() / 2 - 0.5f;

		mScale = ctx.getResources().getDisplayMetrics().density; 
		
		createCompassFramePicture();
		createCompassRosePicture();

		COMPASS_FRAME_CENTER_X = mCompassFrame.getWidth() / 2 - 0.5f;
		COMPASS_FRAME_CENTER_Y = mCompassFrame.getHeight() / 2 - 0.5f;
		COMPASS_ROSE_CENTER_X = mCompassRose.getWidth() / 2 - 0.5f;
		COMPASS_ROSE_CENTER_Y = mCompassRose.getHeight() / 2 - 0.5f;

		final List<Sensor> mOrientationSensors = Utils.getUtils().getSensorManager().getSensorList(Sensor.TYPE_ORIENTATION);
		mOrientationSensorAvailable = !mOrientationSensors.isEmpty();
		
	}
	
	/**
	 * This is needed elsewhere so we can detect clicks in the box and avoid putting waypoints there
	 * @return
	 */
	public DataOverlay getDataOverlay() {
		return this.dataOverlay;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public final Location getLastFix() {
		return mLocation;
	}

	/**
	 * Return a GeoPoint of the last known location, or null if not known.
	 */
	public final GeoPoint getMyLocation() {
		if (mLocation == null) {
			return null;
		} else {
			// avoid allocation, used to return new GeoPoint(location)
			mCenterPoint.setCoordsE6((int)(mLocation.getLatitude()* 1E6), (int)(mLocation.getLongitude()* 1E6));
			return mCenterPoint;
		}
	}

	public final boolean isMyLocationEnabled() {
		return mMyLocationEnabled;
	}

	public final boolean isCompassEnabled() {
		return mCompassEnabled;
	}

	public final boolean isLocationFollowEnabled() {
		return mFollow;
	}

	public final void followLocation(boolean enable) {
		mFollow = enable;
	}

	public final long getLocationUpdateMinTime() {
		return mLocationUpdateMinTime;
	}

	/**
	 * Set the minimum interval for location updates.
	 * See {@link LocationManager.requestLocationUpdates(String, long, float, LocationListener)}.
	 * Note that you should call this before calling {@link enableMyLocation()}.
	 * @param milliSeconds
	 */
	public final void setLocationUpdateMinTime(final long milliSeconds) {
		mLocationUpdateMinTime = milliSeconds;
	}

	public final float getLocationUpdateMinDistance() {
		return mLocationUpdateMinDistance;
	}

	/**
	 * Set the minimum distance for location updates.
	 * See {@link LocationManager.requestLocationUpdates}.
	 * Note that you should call this before calling {@link enableMyLocation()}.
	 * @param meters
	 */
	public final void setLocationUpdateMinDistance(final float meters) {
		mLocationUpdateMinDistance = meters;
	}

	public final void setCompassCenter(float x, float y) {
		mCompassCenterX = x;
		mCompassCenterY = y;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected final void onDrawFinished(Canvas c, OpenStreetMapView osmv) {}

	@Override
	public void onDraw(final Canvas c, final OpenStreetMapView osmv) {

		if(this.mLocation != null) {
			final OpenStreetMapViewProjection pj = osmv.getProjection();
			mGeoPoint.setCoordsE6((int)(mLocation.getLatitude()* 1E6), (int)(mLocation.getLongitude()* 1E6));
			pj.toMapPixels(mGeoPoint, mMapCoords);

			c.getMatrix(mMatrix);
			mMatrix.getValues(mMatrixValues);

			float bearing = -1.0f;
			if (mLocation.getProvider().equals(LocationManager.GPS_PROVIDER) && mAzimuth >= 0.0f) {
				// if GPS and compass is available, use compass value
				bearing = mAzimuth;
			} else if (mLocation.hasSpeed() && ( mLocation.getSpeed() > OpenFlight.MIN_SPEED_METERS_SEC ) && mLocation.hasBearing()) {
				// use bearing if available and if we're actually moving
				bearing = this.mLocation.getBearing();
			}

			if (bearing >= 0.0f) {
				/* Rotate the direction-Arrow according to the bearing we are driving. And draw it to the canvas. */
				this.directionRotater.setRotate(bearing, OWNSHIP_ICON_CENTER_X , OWNSHIP_ICON_CENTER_Y);
				this.directionRotater.postTranslate(-OWNSHIP_ICON_CENTER_X, -OWNSHIP_ICON_CENTER_Y);
				this.directionRotater.postScale(1/mMatrixValues[Matrix.MSCALE_X], 1/mMatrixValues[Matrix.MSCALE_Y]);
				this.directionRotater.postTranslate(mMapCoords.x, mMapCoords.y);
				c.drawBitmap(OWNSHIP_ICON, this.directionRotater, this.mPaint);
			} else {
				this.directionRotater.setTranslate(-PERSON_HOTSPOT.x, -PERSON_HOTSPOT.y);
				this.directionRotater.postScale(1/mMatrixValues[Matrix.MSCALE_X], 1/mMatrixValues[Matrix.MSCALE_Y]);
				this.directionRotater.postTranslate(mMapCoords.x, mMapCoords.y);
				c.drawBitmap(LOCATION_ICON, this.directionRotater, this.mPaint);
			}
			
			if ( mLocation != null && isDataEnabled() ) {
				dataOverlay.drawData( c, 
						mLocation.getLatitude(),
						mLocation.getLongitude(),
						mLocation.getAltitude(),
						mLocation.getSpeed(),
						mLocation.getBearing(),
						mLocation.getAccuracy() );
			}
		}

		if (mAzimuth >= 0.0f) {
			final float centerX = mCompassCenterX * mScale;
			final float centerY = mCompassCenterY * mScale + (c.getHeight() - mMapView.getHeight());

			this.mCompassMatrix.setTranslate(-COMPASS_FRAME_CENTER_X, -COMPASS_FRAME_CENTER_Y);
			this.mCompassMatrix.postTranslate(centerX, centerY);

			c.save();
			c.setMatrix(mCompassMatrix);
			c.drawPicture(mCompassFrame);

			this.mCompassMatrix.setRotate(-mAzimuth, COMPASS_ROSE_CENTER_X, COMPASS_ROSE_CENTER_Y);
			this.mCompassMatrix.postTranslate(-COMPASS_ROSE_CENTER_X, -COMPASS_ROSE_CENTER_Y);
			this.mCompassMatrix.postTranslate(centerX, centerY);

			c.setMatrix(mCompassMatrix);
			c.drawPicture(mCompassRose);
			c.restore();
		}
				
	}

	

	public final boolean isDataEnabled() {
		return mDataEnabled;
	}
	
	public final void setDataEnabled( boolean which ) {
		mDataEnabled = which;
	}
	
	@Override
	public void onLocationChanged( Location location) {

		// ONLY LISTEN FOR GPS, SO NOT NEEDED UNLESS NETWORK LOC IS ENABLED
//		// for this purpose, ignore anything but GPS
//		if ( location.getProvider().equals( LocationManager.GPS_PROVIDER ) ) {
				
		try {
		
			mLocation = location;
	
			if (mFollow) {
				mMapController.animateTo( new GeoPoint( mLocation ) ); // animate and redraw
			} else {
				mMapView.invalidate(); // just redraw
			}
			
			// getting NPE's here occasionally, find out why
			// and keep running
		} catch ( Exception e ) {
			e.printStackTrace();
			logger.info( "NPE: location: " + (mLocation != null ? mLocation.toString() : " NULL ") + e.getMessage() );
		}
	}

	@Override
	public final void onProviderDisabled(String provider) {
	}

	@Override
	public final void onProviderEnabled(String provider) {
	}

	@Override
	public final void onStatusChanged(String provider, int status, Bundle extras) {
		if(status == LocationProvider.AVAILABLE) {
			final Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					for(Runnable runnable: mRunOnFirstFix) {
						runnable.run();
					}
					mRunOnFirstFix.clear();
				}
			});
			t.run();
		}
	}

	@Override
	public final boolean onSnapToItem(int x, int y, Point snapPoint, OpenStreetMapView mapView) {
		if(this.mLocation != null) {
			final OpenStreetMapViewProjection pj = mapView.getProjection();
			pj.toMapPixels(new GeoPoint(mLocation), mMapCoords);
			snapPoint.x = mMapCoords.x;
			snapPoint.y = mMapCoords.y;
			final double xDiff = (x - mMapCoords.x);
			final double yDiff = (y - mMapCoords.y);
			final boolean snap = xDiff *xDiff + yDiff *yDiff < 64;
			if (DEBUGMODE) {
				logger.debug("snap=" + snap);
			}
			return snap;
		} else {
			return false;
		}
	}

	@Override
	public final boolean onTouchEvent(MotionEvent event, OpenStreetMapView mapView) {
		
		// if touch is in the data area, magnify the data and mark touch handled
		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
			
			if ( dataOverlay != null ) {
				if ( dataOverlay.handleMotionEvent( event ) ) {
					mMapView.invalidate();
					return false;
				}
			}		
			
		}
		// avoid shutting off location following at touches
		
//		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//			mFollow = false;
//		}

		return super.onTouchEvent(event, mapView);
	}

	@Override
	public final void onAccuracyChanged(Sensor arg0, int arg1) {
		// This is not interesting for us at the moment
	}

	@Override
	public final void onSensorChanged(SensorEvent event) {
		// It's not necessary to check for mCompassEnabled here, because the event will
		// only fire, if the sensor has been enabled ...
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION)
		{
			if (event.values != null)
			{
				mAzimuth = (float) event.values[0];
				mMapView.invalidate();
			}
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public final void disableMyLocation() {
		Utils.getUtils().getLocationManager().removeUpdates(this);
		mMyLocationEnabled = false;
	}

	/**
	 * Enable location updates so that the map follows the current location.
	 * By default this will request location updates as frequently as possible,
	 * but you can change the frequency and/or distance by calling
	 * {@link setLocationUpdateMinTime(long)} and/or {@link setLocationUpdateMinDistance(float)}
	 * before calling this method.
	 */
	public final boolean enableMyLocation() {
		if ( !mMyLocationEnabled ) {
			Utils.setupLocationListener( this );

//			if ( Utils.getUtils().isCellLocationEnabled() )
//				mLocationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, mLocationUpdateMinTime, mLocationUpdateMinDistance, this );
		}
		return mMyLocationEnabled = true;
	}

	public final boolean toggleMyLocation() {
		if (mMyLocationEnabled) {
			disableMyLocation();
		} else {
			enableMyLocation();
		}
		return mMyLocationEnabled;
	}

	public final boolean enableCompass() {
		if (mOrientationSensorAvailable) {
			if (!mCompassEnabled) {
				final Sensor sensorOrientation = Utils.getUtils().getSensorManager().getDefaultSensor(Sensor.TYPE_ORIENTATION);
				Utils.getUtils().getSensorManager().registerListener(this, sensorOrientation, SensorManager.SENSOR_DELAY_NORMAL );
			}
			return mCompassEnabled = true;
		} else {
			return mCompassEnabled = false;
		}
	}

	public final boolean disableCompass() {
		if (mCompassEnabled) {
			Utils.getUtils().getSensorManager().unregisterListener(this);
			mCompassEnabled = false;
			// Reset azimuth value
			mAzimuth = -1.0f;
		}
		return mCompassEnabled;
	}

	public final boolean toggleCompass() {
		return (mCompassEnabled) ? disableCompass() : enableCompass();
	}

	public final boolean runOnFirstFix(Runnable runnable) {
		if(mMyLocationEnabled && mLocation != null) {
			runnable.run();
			return true;
		} else {
			mRunOnFirstFix.addLast(runnable);
			return false;
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	private Point calculatePointOnCircle(float centerX, float centerY, float radius, float degrees) {
		// for trigonometry, 0 is pointing east, so subtract 90
		// compass degrees are the wrong way round
		final double dblRadians = Math.toRadians(-degrees + 90);

		final int intX = (int) (radius * Math.cos(dblRadians));
		final int intY = (int) (radius * Math.sin(dblRadians));

		return new Point((int) centerX + intX, (int) centerY - intY);
	}

	private void drawTriangle(Canvas canvas, float x, float y, float radius, float degrees, Paint paint) {
		canvas.save();
		final Point point = this.calculatePointOnCircle(x, y, radius, degrees);
		canvas.rotate(degrees, point.x, point.y);
		final Path p = new Path();
		p.moveTo(point.x - 2 * mScale, point.y);
		p.lineTo(point.x + 2 * mScale, point.y);
		p.lineTo(point.x, point.y - 5 * mScale);
		p.close();
		canvas.drawPath(p, paint);
		canvas.restore();
	}

	private void createCompassFramePicture() {
		// The inside of the compass is white and transparent
		final Paint innerPaint = new Paint();
		innerPaint.setColor(Color.WHITE);
		innerPaint.setAntiAlias(true);
		innerPaint.setStyle(Style.FILL);
		innerPaint.setAlpha(200);

		// The outer part (circle and little triangles) is gray and transparent
		final Paint outerPaint = new Paint();
		outerPaint.setColor(Color.GRAY);
		outerPaint.setAntiAlias(true);
		outerPaint.setStyle(Style.STROKE);
		outerPaint.setStrokeWidth(2.0f);
		outerPaint.setAlpha(200);

		final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2);
		final int center = picBorderWidthAndHeight / 2;

		final Canvas canvas = mCompassFrame.beginRecording(picBorderWidthAndHeight, picBorderWidthAndHeight);

		// draw compass inner circle and border
		canvas.drawCircle(center, center, mCompassRadius * mScale, innerPaint);
		canvas.drawCircle(center, center, mCompassRadius * mScale, outerPaint);

		// Draw little triangles north, south, west and east (don't move)
		// to make those move use "-bearing + 0" etc. (Note: that would mean to draw the triangles in the onDraw() method)
		drawTriangle(canvas, center, center, mCompassRadius * mScale, 0, outerPaint);
		drawTriangle(canvas, center, center, mCompassRadius * mScale, 90, outerPaint);
		drawTriangle(canvas, center, center, mCompassRadius * mScale, 180, outerPaint);
		drawTriangle(canvas, center, center, mCompassRadius * mScale, 270, outerPaint);

		mCompassFrame.endRecording();
	}

	private void createCompassRosePicture() {
		// Paint design of north triangle (it's common to paint north in red color)
		final Paint northPaint = new Paint();
		northPaint.setColor(0xFFA00000);
		northPaint.setAntiAlias(true);
		northPaint.setStyle(Style.FILL);
		northPaint.setAlpha(220);

		// Paint design of south triangle (black)
		final Paint southPaint = new Paint();
		southPaint.setColor(Color.BLACK);
		southPaint.setAntiAlias(true);
		southPaint.setStyle(Style.FILL);
		southPaint.setAlpha(220);

		// Create a little white dot in the middle of the compass rose
		final Paint centerPaint = new Paint();
		centerPaint.setColor(Color.WHITE);
		centerPaint.setAntiAlias(true);
		centerPaint.setStyle(Style.FILL);
		centerPaint.setAlpha(220);

		// final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2 * mScale);
		final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2);
		final int center = picBorderWidthAndHeight / 2;

		final Canvas canvas = mCompassRose.beginRecording(picBorderWidthAndHeight, picBorderWidthAndHeight);

		// Blue triangle pointing north
		final Path pathNorth = new Path();
		pathNorth.moveTo(center, center - (mCompassRadius - 3) * mScale);
		pathNorth.lineTo(center + 4 * mScale, center);
		pathNorth.lineTo(center - 4 * mScale, center);
		pathNorth.lineTo(center, center - (mCompassRadius - 3) * mScale);
		pathNorth.close();
		canvas.drawPath(pathNorth, northPaint);

		// Red triangle pointing south
		final Path pathSouth = new Path();
		pathSouth.moveTo(center, center + (mCompassRadius - 3) * mScale);
		pathSouth.lineTo(center + 4 * mScale, center);
		pathSouth.lineTo(center - 4 * mScale, center);
		pathSouth.lineTo(center, center + (mCompassRadius - 3) * mScale);
		pathSouth.close();
		canvas.drawPath(pathSouth, southPaint);

		// Draw a little white dot in the middle
		canvas.drawCircle(center, center, 2, centerPaint);

		mCompassRose.endRecording();
	}
}

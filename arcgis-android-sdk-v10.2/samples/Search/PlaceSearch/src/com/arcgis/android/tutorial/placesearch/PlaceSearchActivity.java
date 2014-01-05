/* Copyright 2013 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the Sample code usage restrictions document for further information.
 *
 */

package com.arcgis.android.tutorial.placesearch;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;

/**
 * The Place Search app is the most basic address location app for the ArcGIS
 * Runtime SDK for Android. It shows how to create a {@link #mMapView} object
 * and populate it with a {@link #basemap} and show the layer on the map.
 * 
 * @author EsriAndroidTeam
 * @version 10.1.1
 * 
 */

public class PlaceSearchActivity extends Activity {

	// create ArcGIS objects
	MapView mMapView;
	ArcGISTiledMapServiceLayer basemap;
	GraphicsLayer locationLayer;
	Locator locator;
	LocatorGeocodeResult geocodeResult;
	Callout locationCallout;
	GeocoderTask mWorker;

	Point mLocation = null;
	// Spatial references used for projecting points
	final SpatialReference wm = SpatialReference.create(102100);
	final SpatialReference egs = SpatialReference.create(4326);

	// create UI components
	static ProgressDialog dialog;
	static Handler handler;

	// Label instructing input for EditText
	TextView geocodeLabel;
	// Text box for entering address
	EditText addressText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mWorker = (GeocoderTask) getLastNonConfigurationInstance();
		if (mWorker != null) {
			mWorker.mActivity = new WeakReference<PlaceSearchActivity>(this);
		}

		// create handler to update the UI
		handler = new Handler();

		// Set the geocodeLabel with instructions
		geocodeLabel = (TextView) findViewById(R.id.geocodeLabel);
		geocodeLabel.setText(getString(R.string.geocode_label));

		// Get the addressText component
		addressText = (EditText) findViewById(R.id.addressText);

		// Retrieve the map and initial extent from XML layout
		mMapView = (MapView) findViewById(R.id.map);
		/* create a @ArcGISTiledMapServiceLayer */
		basemap = new ArcGISTiledMapServiceLayer(this.getResources().getString(
				R.string.basemap_url));
		// Add tiled layer to MapView
		mMapView.addLayer(basemap);
		// Add location layer
		locationLayer = new GraphicsLayer();
		mMapView.addLayer(locationLayer);

		// get the location service and start reading location
		LocationService locationSrv = mMapView.getLocationService();
		locationSrv.setLocationListener(new MyLocationListener());
		locationSrv.start();
		locationSrv.setAutoPan(false);

		// attribute ESRI logo to map
		mMapView.setEsriLogoVisible(true);
		mMapView.enableWrapAround(true);

		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			public void onSingleTap(float x, float y) {
				if (!mMapView.isLoaded())
					return;

				int[] uids = locationLayer.getGraphicIDs(x, y, 10);
				if (uids != null && uids.length > 0) {
					// int targetId = uids[0];
					// Graphic selection = locationLayer.getGraphic(targetId);
					locationCallout = mMapView.getCallout();

					// set callout style
					locationCallout.setStyle(R.xml.calloutstyle);
					String place = geocodeResult.getAddress();
					locationCallout.setContent(loadView(place));
					locationCallout.show(mMapView.toMapPoint(new Point(x, y)));
				} else {
					if (locationCallout != null && locationCallout.isShowing()) {
						locationCallout.hide();
					}
				}

			}
		});
	}

	public Object onRetainNonConfigurationInstance() {
		return mWorker;
	}

	/*
	 * Submit address for place search
	 */
	public void locate(View view) {
		// hide virtual keyboard
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(
				getCurrentFocus().getWindowToken(), 0);
		// remove any previous graphics and callouts
		locationLayer.removeAll();
		if (locationCallout != null && locationCallout.isShowing())
			locationCallout.hide();
		// obtain address from text box
		String address = addressText.getText().toString();
		// set parameters to support the find operation for a geocoding service
		setSearchParams(address);
	}

	private void setSearchParams(String address) {
		try {
			// create Locator parameters from single line address string
			LocatorFindParameters findParams = new LocatorFindParameters(
					address);
			// set the search extent to extent of map
			// SpatialReference inSR = mMapView.getSpatialReference();
			// Envelope searchExtent = mMapView.getMapBoundaryExtent();
			// findParams.setSearchExtent(searchExtent, inSR);
			// limit the results to 2
			findParams.setMaxLocations(2);
			// set address spatial reference to match map
			findParams.setOutSR(mMapView.getSpatialReference());
			// execute async task to geocode address
			// new Geocoder().execute(findParams);
			mWorker = new GeocoderTask(this);
			mWorker.execute(findParams);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Creates custom content view with 'Graphic' attributes
	private View loadView(String address) {
		View view = LayoutInflater.from(PlaceSearchActivity.this).inflate(
				R.layout.callout, null);

		final TextView addressText = (TextView) view.findViewById(R.id.address);
		addressText.setText(address);

		final ImageView photo = (ImageView) view
				.findViewById(R.id.selection_icon);
		photo.setImageDrawable(PlaceSearchActivity.this.getResources()
				.getDrawable(R.drawable.selection));

		return view;

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mWorker != null) {
			mWorker.mActivity = null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.unpause();
	}

	/*
	 * Dismiss dialog when geocode task completes
	 */
	static public class MyRunnable implements Runnable {
		public void run() {
			dialog.dismiss();
		}
	}

	/*
	 * AsyncTask to geocode an address to a point location Draw resulting point
	 * location on the map with matching address
	 */
	private class GeocoderTask extends
			AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {

		WeakReference<PlaceSearchActivity> mActivity;

		GeocoderTask(PlaceSearchActivity activity) {
			mActivity = new WeakReference<PlaceSearchActivity>(activity);
		}

		// The result of geocode task is passed as a parameter to map the
		// results
		protected void onPostExecute(List<LocatorGeocodeResult> result) {
			if (result == null || result.size() == 0) {
				// update UI with notice that no results were found
				Toast toast = Toast.makeText(PlaceSearchActivity.this,
						"No result found.", Toast.LENGTH_LONG);
				toast.show();
			} else {
				// update global result
				geocodeResult = result.get(0);
				// show progress dialog while geocoding address
				dialog = ProgressDialog.show(mMapView.getContext(), "Geocoder",
						"Searching for address ...");
				// get return geometry from geocode result
				Geometry resultLocGeom = geocodeResult.getLocation();
				// create marker symbol to represent location
				SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(
						Color.BLUE, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
				// create graphic object for resulting location
				Graphic resultLocation = new Graphic(resultLocGeom,
						resultSymbol);
				// add graphic to location layer
				locationLayer.addGraphic(resultLocation);

				// // create callout for return address
				// locationCallout = mMapView.getCallout();
				// String place = geocodeResult.getAddress();
				// locationCallout.setContent(loadView(place));
				// locationCallout.show();

				// create text symbol for return address
				TextSymbol resultAddress = new TextSymbol(12,
						geocodeResult.getAddress(), Color.BLACK);
				// create offset for text
				resultAddress.setOffsetX(10);
				resultAddress.setOffsetY(50);
				// create a graphic object for address text
				Graphic resultText = new Graphic(resultLocGeom, resultAddress);
				// add address text graphic to location graphics layer
				locationLayer.addGraphic(resultText);
				// zoom to geocode result

				mMapView.zoomToResolution(geocodeResult.getLocation(), 2);
				// create a runnable to be added to message queue
				handler.post(new MyRunnable());
			}
		}

		// invoke background thread to perform geocode task
		@Override
		protected List<LocatorGeocodeResult> doInBackground(
				LocatorFindParameters... params) {

			// create results object and set to null
			List<LocatorGeocodeResult> results = null;
			// set the geocode service
			locator = Locator.createOnlineLocator(getResources()
					.getString(R.string.geocode_url));
			try {

				// pass address to find method to return point representing
				// address
				results = locator.find(params[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// return the resulting point(s)
			return results;
		}

	}

	private class MyLocationListener implements LocationListener {

		public MyLocationListener() {
			super();
		}

		/**
		 * If location changes, update our current location. If being found for
		 * the first time, zoom to our current position with a resolution of 20
		 */
		public void onLocationChanged(Location loc) {
			if (loc == null)
				return;
			boolean zoomToMe = (mLocation == null) ? true : false;
			mLocation = new Point(loc.getLongitude(), loc.getLatitude());
			if (zoomToMe) {
				Point p = (Point) GeometryEngine.project(mLocation, egs, wm);
				mMapView.zoomToResolution(p, 20.0);
			}
		}

		public void onProviderDisabled(String provider) {
			Toast.makeText(getApplicationContext(), "GPS Disabled",
					Toast.LENGTH_SHORT).show();
		}

		public void onProviderEnabled(String provider) {
			Toast.makeText(getApplicationContext(), "GPS Enabled",
					Toast.LENGTH_SHORT).show();
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	}

}
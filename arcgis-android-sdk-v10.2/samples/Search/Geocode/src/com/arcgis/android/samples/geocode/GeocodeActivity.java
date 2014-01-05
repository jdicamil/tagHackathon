/* Copyright 2012 ESRI
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

package com.arcgis.android.samples.geocode;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.tasks.geocode.Locator;
import com.esri.core.tasks.geocode.LocatorFindParameters;
import com.esri.core.tasks.geocode.LocatorGeocodeResult;
import com.esri.core.tasks.geocode.LocatorReverseGeocodeResult;

/**
 * The Geocode app is the most basic address location app for the ArcGIS Runtime
 * SDK for Android. It shows how to create a {@link #mMapView} object and
 * populate it with a {@link #basemap} and show the layer on the map.
 * 
 * @author EsriAndroidTeam
 * @version 10.1.1
 * 
 */

public class GeocodeActivity extends Activity {
	// create arcgis objects
	MapView mMapView;
	ArcGISTiledMapServiceLayer basemap;
	GraphicsLayer locationLayer;
	Locator locator;
	// create UI components
	static ProgressDialog dialog;
	static Handler handler;

	// Label instructing input for EditText
	TextView geocodeLabel;
	// Text box for entering address
	EditText addressText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

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

		// initialize arcgis locator
		locator = Locator.createOnlineLocator();

		// attribute ESRI logo to map
		mMapView.setEsriLogoVisible(true);

		// perform reverse geocode on single tap.
		mMapView.setOnSingleTapListener(new OnSingleTapListener() {
			private static final long serialVersionUID = 1L;

			public void onSingleTap(final float x, final float y) {

				// retrieve the user clicked location
				final Point loc = mMapView.toMapPoint(x, y);
				try {

					if (loc != null) {
						new ReverseGeocode().execute(loc);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/*
	 * Submit address for geocoding
	 */
	public void locate(View view) {
		// remove any previous graphics
		locationLayer.removeAll();
		// obtain address from text box
		String address = addressText.getText().toString();
		// send address to conversion method
		address2Pnt(address);

	}

	/*
	 * Convert input address into geocoded point
	 */
	private void address2Pnt(String address) {
		try {
			// create Locator parameters from single line address string
			LocatorFindParameters findParams = new LocatorFindParameters(
					address);
			// set the search country to USA
			findParams.setSourceCountry("USA");
			// limit the results to 2
			findParams.setMaxLocations(2);
			// set address spatial reference to match map
			findParams.setOutSR(mMapView.getSpatialReference());
			// execute async task to geocode address
			new Geocoder().execute(findParams);
		} catch (Exception e) {
			e.printStackTrace();
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
	 * Cutomize the map Callout text
	 */
	private TextView message(String text) {

		final TextView msg = new TextView(this);
		msg.setText(text);
		msg.setTextSize(12);
		msg.setTextColor(Color.BLACK);
		return msg;

	}

	/*
	 * AsyncTask to geocode an address to a point location Draw resulting point
	 * location on the map with matching address
	 */
	private class Geocoder extends
			AsyncTask<LocatorFindParameters, Void, List<LocatorGeocodeResult>> {

		protected void onPreExecute() {
			// show progress dialog while geocoding address
			dialog = ProgressDialog.show(mMapView.getContext(), "Geocoder",
					"Searching for address ...");
		}

		// The result of geocode task is passed as a parameter to map the
		// results
		protected void onPostExecute(List<LocatorGeocodeResult> result) {
			// dismiss dialog
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			
			if (result == null || result.size() == 0) {
				// update UI with notice that no results were found
				Toast toast = Toast.makeText(GeocodeActivity.this,
						"No result found.", Toast.LENGTH_LONG);
				toast.show();
			} else {
				// get return geometry from geocode result
				Geometry resultLocGeom = result.get(0).getLocation();
				// create marker symbol to represent location
				SimpleMarkerSymbol resultSymbol = new SimpleMarkerSymbol(
						Color.BLUE, 20, SimpleMarkerSymbol.STYLE.CIRCLE);
				// create graphic object for resulting location
				Graphic resultLocation = new Graphic(resultLocGeom,
						resultSymbol);
				// add graphic to location layer
				locationLayer.addGraphic(resultLocation);
				// create text symbol for return address
				TextSymbol resultAddress = new TextSymbol(12, result.get(0)
						.getAddress(), Color.BLACK);
				// create offset for text
				resultAddress.setOffsetX(10);
				resultAddress.setOffsetY(50);
				// create a graphic object for address text
				Graphic resultText = new Graphic(resultLocGeom, resultAddress);
				// add address text graphic to location graphics layer
				locationLayer.addGraphic(resultText);
				// zoom to geocode result
				mMapView.zoomToResolution(result.get(0).getLocation(), 2);

			}
		}

		// invoke background thread to perform geocode task
		@Override
		protected List<LocatorGeocodeResult> doInBackground(
				LocatorFindParameters... params) {
			// get spatial reference from map
			// SpatialReference sr = mMapView.getSpatialReference();
			// create results object and set to null
			List<LocatorGeocodeResult> results = null;

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

	/*
	 * Execute reverse geocode task asynchronously.
	 */
	class ReverseGeocode extends
			AsyncTask<Point, Void, LocatorReverseGeocodeResult> {

		protected void onPreExecute() {
			// show progress dialog while geocoding address
			dialog = ProgressDialog.show(mMapView.getContext(),
					"Reverse Geocoder", "Searching for address ...");
		}

		@Override
		protected void onPostExecute(LocatorReverseGeocodeResult result) {
			// dismiss dialog
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			
			locationLayer.removeAll();

			// Check for an empty result
			Map<String, String> fieldResults;
			fieldResults = result.getAddressFields();

			if (fieldResults == null || fieldResults.isEmpty()) {

				mMapView.getCallout().show(result.getLocation(),
						message("No Address Found."));
			} else {
				// display the result in map callout
				String msg = "Address:" + fieldResults.get("Address") + "\n"
						+ "City:" + fieldResults.get("City") + "\n" + "State:"
						+ fieldResults.get("Region") + "\n" + "Zip:"
						+ fieldResults.get("Postal");
				mMapView.getCallout().show(result.getLocation(), message(msg));
			}
		}

		@Override
		protected LocatorReverseGeocodeResult doInBackground(Point... params) {

			Point location = params[0];
			SpatialReference sr = mMapView.getSpatialReference();
			double distance = 100.0;
			LocatorReverseGeocodeResult result = null;

			// perform reverse geocode operation
			try {
				result = locator.reverseGeocode(location, distance, sr, sr);
			} catch (Exception e) {
				e.getMessage();
			}
			return result;
		}

	}

}

/* Copyright 2013 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the use restrictions 
 * http://help.arcgis.com/en/sdk/10.0/usageRestrictions.htm.
 */

package com.esri.arcgis.android.samples.servicearea;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.tasks.na.NAFeaturesAsFeature;
import com.esri.core.tasks.na.ServiceAreaParameters;
import com.esri.core.tasks.na.ServiceAreaResult;
import com.esri.core.tasks.na.ServiceAreaTask;

public class ServiceAreaSample extends Activity {
	MapView mMapView = null;
	ArcGISTiledMapServiceLayer baseMap;
	// Graphics layer for displaying the service area polygons
	GraphicsLayer serviceAreaLayer;
	// Spatial references used for projecting points
	final SpatialReference wm = SpatialReference.create(102100);
	final SpatialReference egs = SpatialReference.create(4326);
	// Three text boxes for specifying the break values
	EditText break1, break2, break3;
	
	static ProgressDialog dialog;
	static AlertDialog.Builder alertDialogBuilder;
	static AlertDialog alertDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Retrieve the map and initial extent from XML layout
		mMapView = (MapView) findViewById(R.id.map);
		// Add tiled layer basemap to MapView
		baseMap = new ArcGISTiledMapServiceLayer(
				this.getResources().getString(R.string.basemap_url));
		mMapView.addLayer(baseMap);

		// Add the service area graphic layer (shows the SA polygons)
		serviceAreaLayer = new GraphicsLayer();
		mMapView.addLayer(serviceAreaLayer);

		break1 = (EditText) findViewById(R.id.break1);
		break2 = (EditText) findViewById(R.id.break2);
		break3 = (EditText) findViewById(R.id.break3);

		/**
		 * On single tapping the map, calculate the three service area polygons
		 * defined by our three break value EditTexts, using the location
		 * clicked as the source facility
		 */
		mMapView.setOnSingleTapListener(new OnSingleTapListener() {
			private static final long serialVersionUID = 1L;

			public void onSingleTap(float x, float y) {
				// retrieve the user clicked location
				final Point location = mMapView.toMapPoint(x, y);
				
				SolveServiceArea solveArea = new SolveServiceArea();
				solveArea.execute(location);

			}

		});

		/**
		 * On pressing the clear button, clear the service area graphics
		 * 
		 */
		Button b = (Button) findViewById(R.id.button1);
		b.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// Clear the graphics
				serviceAreaLayer.removeAll();
			}

		});
	}

	/**
	 * Create a TextView containing the message
	 * 
	 * @param text
	 *            The text view's content
	 * @return The TextView
	 */
	TextView message(String text) {

		final TextView msg = new TextView(this);
		msg.setText(text);
		msg.setTextSize(12);
		msg.setTextColor(Color.BLACK);
		return msg;

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
	
	class SolveServiceArea extends AsyncTask<Point, Void, ServiceAreaResult> {
		
		protected void onPreExecute(){
			dialog = ProgressDialog.show(ServiceAreaSample.this, "", "Solving Service Area Task");
		}
		

		@Override
		protected ServiceAreaResult doInBackground(Point... params) {
			Point startLocation = params[0];
			
			try {
				// Start building up service area parameters
				ServiceAreaParameters sap = new ServiceAreaParameters();
				NAFeaturesAsFeature nfaf = new NAFeaturesAsFeature();
				// Convert point to EGS (decimal degrees)
				Point p = (Point) GeometryEngine.project(startLocation, wm, egs);
				nfaf.addFeature(new Graphic(p, null));
				sap.setFacilities(nfaf);
				// Set the service area output SR to our map service's SR
				sap.setOutSpatialReference(wm);

				// Set the default break values with our entered values
				sap.setDefaultBreaks(new Double[] {
						Double.valueOf(break1.getText().toString()),
						Double.valueOf(break2.getText().toString()),
						Double.valueOf(break3.getText().toString()) });

				// Create a new service area task pointing to an NAService
				// (null credentials -> free service)
				ServiceAreaTask sat = new ServiceAreaTask(
						ServiceAreaSample.this.getResources().getString(R.string.naservice_url),
						null);

				// Solve the service area and retrieve the result.
				ServiceAreaResult saResult = sat.solve(sap);
				return saResult;

			} catch (Exception e) {

				e.printStackTrace();
				mMapView.getCallout().show(startLocation,
						message("Exception occurred"));
				return null;
			}			
			
			
		}

		protected void onPostExecute(ServiceAreaResult result) {
			dialog.dismiss();
			
			ServiceAreaResult saResult = result;
			
			if(saResult != null){
				// Symbol for the smallest service area polygon
				SimpleFillSymbol smallSymbol = new SimpleFillSymbol(
						Color.GREEN);
				smallSymbol.setAlpha(128);
				// Symbol for the medium service area polygon
				SimpleFillSymbol mediumSymbol = new SimpleFillSymbol(
						Color.YELLOW);
				mediumSymbol.setAlpha(128);
				// Symbol for the largest service area polygon
				SimpleFillSymbol largeSymbol = new SimpleFillSymbol(
						Color.RED);
				largeSymbol.setAlpha(128);

				// Create and add the service area graphics to the service
				// area Layer
				Graphic smallGraphic = new Graphic(saResult
						.getServiceAreaPolygons().getGraphics()[2]
						.getGeometry(), smallSymbol);
				Graphic mediumGraphic = new Graphic(saResult
						.getServiceAreaPolygons().getGraphics()[1]
						.getGeometry(), mediumSymbol);
				Graphic largeGraphic = new Graphic(saResult
						.getServiceAreaPolygons().getGraphics()[0]
						.getGeometry(), largeSymbol);
				serviceAreaLayer.addGraphics(new Graphic[] { smallGraphic,
						mediumGraphic, largeGraphic });
				// Zoom to the extent of the service area polygon with a
				// padding
				mMapView.setExtent(largeGraphic.getGeometry(), 50);
			}else{
				// send response to user
				alertDialogBuilder = new AlertDialog.Builder(ServiceAreaSample.this);
				alertDialogBuilder.setTitle("Query Response");
				alertDialogBuilder.setMessage(ServiceAreaSample.this.getResources().getString(R.string.sa_null_response));
				alertDialogBuilder.setCancelable(true);
				// create alert dialog
				alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
			

		}




	}	
	

}
/* Copyright 2012 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the �Sample code usage restrictions� document for further information.
 *
 */

package com.esri.arcgis.android.samples.helloworld;

import android.app.Activity;
import android.os.Bundle;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;

/**
 * The HelloWorld app is the most basic Map app
 * for the ArcGIS Runtime SDK for Android.
 * It shows how to create a {@link #mMapView} object
 * and populate it with a {@link #tileLayer} and 
 * show the layer on the map.  
 * 
 * @author EsriAndroidTeam
 * @version 2.0
 *
 */

public class HelloWorld extends Activity {
	MapView mMapView = null;
	ArcGISTiledMapServiceLayer tileLayer;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Retrieve the map and initial extent from XML layout
		mMapView = (MapView)findViewById(R.id.map);
		/* create a @ArcGISTiledMapServiceLayer */
		tileLayer = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
		// Add tiled layer to MapView
		mMapView.addLayer(tileLayer);
		
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
	
}
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
package com.esri.arcgis.android.samples.addlayer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.event.OnLongPressListener;

/**
 * Adds a layer statically and dynamically and toggles the visibility of top layer 
 * with a single tap 
 *
 */
public class AddLayer extends Activity {
	
	private MapView map = null;
	//Dynamic layer URL from ArcGIS online
	String dynamicMapURL = 
			"http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/Specialty/ESRI_StateCityHighway_USA/MapServer";

	@Override
	@SuppressWarnings("serial")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Retrieve the map and initial extent from XML layout
		map = (MapView)findViewById(R.id.map);
		map.addLayer(new ArcGISDynamicMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"));
		
		//Creates a dynamic layer using service URL 
		ArcGISDynamicMapServiceLayer dynamicLayer = new ArcGISDynamicMapServiceLayer(dynamicMapURL);
		//Adds layer into the 'MapView'
		map.addLayer(dynamicLayer);
		
		Toast.makeText(this, "Long Press to add or remove layer",
				Toast.LENGTH_SHORT).show();		
		
		//Sets 'setOnLongPressListener' to 'MapView'
		map.setOnLongPressListener(new OnLongPressListener() {
			
			@Override
			public boolean onLongPress(float x, float y) {
				//Determines if the map is loaded
				if (map.isLoaded()) {

					// Retrieves the maps layers
					Layer[] layers = map.getLayers();
					// Toggles the dynamic layer's visibility
					if(layers[1].isVisible()){
						layers[1].setVisible(false);
					}else{
						layers[1].setVisible(true);
					}
				}
				return true;
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		map.pause();
 }
	
	@Override
	protected void onResume() {
		super.onResume(); 
		map.unpause();
	}	

}
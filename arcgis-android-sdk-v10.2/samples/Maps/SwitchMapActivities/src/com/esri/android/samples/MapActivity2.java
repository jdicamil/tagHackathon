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

package com.esri.android.samples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Polygon;

public class MapActivity2 extends Activity {

	MapView map = null;
	ArcGISTiledMapServiceLayer tiledMapService;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map2);
		map = (MapView) findViewById(R.id.map);

		tiledMapService = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");

		map.addLayer(tiledMapService);

		// Retrieve extent from Map 1 in Activity 1
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Polygon extent = (Polygon) extras.getSerializable("ExtentMap1");
			map.setExtent(extent);
		}

		((Button) findViewById(R.id.testButton))
				.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						// Get the current extent
						Polygon currExtent = map.getExtent();

						Intent intent = new Intent(MapActivity2.this,
								MapActivity1.class);
						// send current extent to Map 1
						intent.putExtra("ExtentMap2", currExtent);
						startActivity(intent);
					}
				});
	}

}

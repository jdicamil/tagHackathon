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
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Polygon;

public class MapActivity1 extends Activity {

	MapView map = null;
	ArcGISTiledMapServiceLayer tiledMapService;
	private Toast toast;
	private long lastBackPressTime = 0;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map1);
		map = (MapView) findViewById(R.id.map);

		tiledMapService = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");

		map.addLayer(tiledMapService);

		// Retrieve extent from Map 2 in Activity 2
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Polygon extent = (Polygon) extras.getSerializable("ExtentMap2");
			map.setExtent(extent);
		}

		((Button) findViewById(R.id.testButton))
				.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						// Get the current extent
						Polygon currExtent = map.getExtent();
						Intent intent = new Intent(MapActivity1.this,
								MapActivity2.class);
						// send current extent to Map 2
						intent.putExtra("ExtentMap1", currExtent);
						startActivity(intent);
					}
				});
	}

	@Override
	public void onBackPressed() {
		if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
			toast = Toast.makeText(this,
					"Press Back button agian to close the app", 4000);
			toast.show();
			this.lastBackPressTime = System.currentTimeMillis();
		} else {
			if (toast != null) {
				toast.cancel();
			}
			super.onBackPressed();
		}
	}

	protected void onPause() {
		super.onPause();
		map.pause();
	}

	protected void onResume() {
		super.onResume();
		map.unpause();
	}

}

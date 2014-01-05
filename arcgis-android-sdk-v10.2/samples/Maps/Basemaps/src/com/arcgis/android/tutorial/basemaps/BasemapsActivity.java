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

package com.arcgis.android.tutorial.basemaps;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;

public class BasemapsActivity extends Activity {

	MapView mMapView = null;
	ArcGISTiledMapServiceLayer basemapStreet;
	ArcGISTiledMapServiceLayer basemapTopo;
	ArcGISTiledMapServiceLayer basemapNatGeo;
	ArcGISTiledMapServiceLayer basemapOcean;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Retrieve the map and initial extent from XML layout
		mMapView = (MapView) findViewById(R.id.map);
		/* create an initial basemap */
		basemapStreet = new ArcGISTiledMapServiceLayer(this.getResources()
				.getString(R.string.WORLD_STREET_MAP));
		basemapTopo = new ArcGISTiledMapServiceLayer(this.getResources()
				.getString(R.string.WORLD_TOPO_MAP));
		basemapNatGeo = new ArcGISTiledMapServiceLayer(this.getResources()
				.getString(R.string.WORLD_NATGEO_MAP));
		basemapOcean = new ArcGISTiledMapServiceLayer(this.getResources()
				.getString(R.string.OCEAN_BASEMAP));
		// Add basemap to MapView
		mMapView.addLayer(basemapStreet);
		mMapView.addLayer(basemapTopo);
		mMapView.addLayer(basemapNatGeo);
		mMapView.addLayer(basemapOcean);
		// set visibility
		basemapTopo.setVisible(false);
		basemapNatGeo.setVisible(false);
		basemapOcean.setVisible(false);
		// attribute ESRI logo to map
		mMapView.setEsriLogoVisible(true);
		// enable map to wrap around date line
		mMapView.enableWrapAround(true);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.basemap_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
		case R.id.World_Street_Map:
			basemapStreet.setVisible(true);
			basemapTopo.setVisible(false);
			basemapNatGeo.setVisible(false);
			basemapOcean.setVisible(false);
			return true;
		case R.id.World_Topo:
			basemapStreet.setVisible(false);
			basemapNatGeo.setVisible(false);
			basemapOcean.setVisible(false);
			basemapTopo.setVisible(true);
			return true;
		case R.id.NatGeo:
			basemapStreet.setVisible(false);
			basemapTopo.setVisible(false);
			basemapOcean.setVisible(false);
			basemapNatGeo.setVisible(true);
			return true;
		case R.id.Ocean_Basemap:
			basemapStreet.setVisible(false);
			basemapTopo.setVisible(false);
			basemapNatGeo.setVisible(false);
			basemapOcean.setVisible(true);
		default:
			return super.onOptionsItemSelected(item);
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

}
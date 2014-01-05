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

package com.esri.android.tutorial.cljf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.tutorial.cljf.Sketcher.SKETCHMODE;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureSet;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;

/**
 * The main activity for CreateLocalJsonFeatures app. It uses a Tiled Map
 * Service Layer from ArcGIS Online and Feature Layer from Esri's mobile demo
 * server. App allows users to create a sketch polygon to query underlying
 * features. Selected features are persisted device and shown as json feature
 * layer over a local tile map package.
 * 
 * json features are persisted to the following device dir:
 * /<storage_dir>/ArcGIS/samples/cljf/OfflineData/ where <storage-dir> is
 * relative to device naming.
 * 
 * Please be aware that the Feature Layer service is not guaranteed to be
 * running.
 * 
 * @author EsriAndroidTeam
 * @version 2.0
 * 
 */

public class OnlineMapActivity extends Activity {

	private static File demoDataFile;
	private static String offlineDataSDCardDirName;
	private static String filename;

	static ProgressDialog dialog;
	static Handler handler;

	protected static String OFFLINE_FILE_EXTENSION = ".json";

	public final static String EXTRA_JSON_PATH = "com.esri.android.tutorial.JSONPATH";
	public final static String CURRENT_MAP_EXTENT = "com.esri.android.tutorial.EXTENT";

	// Mapview and Layers
	MapView mMapView;
	ArcGISTiledMapServiceLayer basemap;
	ArcGISFeatureLayer windTurbine;
	// drawable layers
	GraphicsLayer gLayer;
	GraphicsLayer sketchLayer;
	Sketcher sketcher;
	Geometry selection;

	Context context;
	String path;

	// buttons
	Button start;
	Button save;
	Button submit;
	Button send;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// create handler to update the UI
		handler = new Handler();

		// get sdcard resource names
		demoDataFile = Environment.getExternalStorageDirectory();
		offlineDataSDCardDirName = this.getResources().getString(
				R.string.config_data_sdcard_offline_dir);
		filename = this.getResources().getString(
				R.string.config_windturbine_name);

		// Retrieve the map and initial extent from XML layout
		mMapView = (MapView) findViewById(R.id.map);
		// create layers
		basemap = new ArcGISTiledMapServiceLayer(this.getResources().getString(
				R.string.basemap_url));
		windTurbine = new ArcGISFeatureLayer(this.getResources().getString(
				R.string.featurelayer_url), MODE.SNAPSHOT);
		// add basemap layer
		mMapView.addLayer(basemap);
		// add feature layer
		mMapView.addLayer(windTurbine);

		// add graphics layer to draw on
		sketchLayer = new GraphicsLayer();
		mMapView.addLayer(sketchLayer);

		// add graphics layer to show selection
		gLayer = new GraphicsLayer();
		SimpleRenderer selected = new SimpleRenderer(new SimpleLineSymbol(
				Color.RED, 2));
		gLayer.setRenderer(selected);
		mMapView.addLayer(gLayer);
		// attribute Esri logo on map
		mMapView.setEsriLogoVisible(true);

		// set button initial visibility
		start = (Button) findViewById(R.id.start);
		start.setEnabled(true);
		save = (Button) findViewById(R.id.save);
		save.setEnabled(false);
		submit = (Button) findViewById(R.id.submit);
		submit.setEnabled(false);
		send = (Button) findViewById(R.id.send);
		send.setEnabled(false);

	}

	/*
	 * Create a polygon graphic representing the area to query for features
	 */
	public void startSketch(View view) {
		sketcher = new Sketcher(mMapView, sketchLayer, gLayer,
				SKETCHMODE.POLYGON);
		sketcher.start();
		// set button state
		start.setEnabled(false);
		save.setEnabled(true);
		submit.setEnabled(false);
		send.setEnabled(false);
	}

	/*
	 * Save the area to query for features
	 */
	public void saveSketch(View view) {
		selection = sketcher.save();
		sketcher.stop();
		// set button state
		start.setEnabled(false);
		save.setEnabled(false);
		submit.setEnabled(true);
		send.setEnabled(false);
	}

	/*
	 * Submit query based on saved polygon representing area to query
	 */
	public void submit(View view) {
		gLayer.removeAll();
		queryFeatureLayer();
		// set button state
		start.setEnabled(false);
		save.setEnabled(false);
		submit.setEnabled(false);
		send.setEnabled(true);
	}

	/*
	 * Send the current extent and path to local json features to
	 * LocalMapActivity
	 */
	public void sendMessage(View view) {

		if (path != null) {
			Intent intent = new Intent(this, LocalMapActivity.class);
			Polygon currExtent = mMapView.getExtent();
			intent.putExtra(CURRENT_MAP_EXTENT, currExtent);
			intent.putExtra(EXTRA_JSON_PATH, path);
			startActivity(intent);
		} else {
			Toast toast = Toast.makeText(this,
					"Features did not save to disk!  Please try again.",
					Toast.LENGTH_LONG);
			toast.show();
		}
		// set button state
		start.setEnabled(true);
		save.setEnabled(false);
		submit.setEnabled(false);
		send.setEnabled(false);
	}

	/*
	 * Create the json file location and name structure
	 */
	public String createJsonFile() {
		StringBuilder sb = new StringBuilder();
		sb.append(demoDataFile.getAbsolutePath());
		sb.append(File.separator);
		sb.append(offlineDataSDCardDirName);
		sb.append(File.separator);
		sb.append(filename);
		sb.append(OFFLINE_FILE_EXTENSION);
		return sb.toString();
	}

	/*
	 * Query features from the feature layer
	 */
	private void queryFeatureLayer() {
		// show progress dialog while querying feature layer
		dialog = ProgressDialog.show(mMapView.getContext(),
				"Query FeatureLayer", "Creating local JSON FeatureLayer");
		// create a Query
		Query query = new Query();
		// set spatial reference
		SpatialReference sr = SpatialReference.create(102100);
		// set the geometry to the sketch polygon
		query.setGeometry(selection);
		// query features that are completely contained by selection
		query.setSpatialRelationship(SpatialRelationship.CONTAINS);
		// set query in/out spatial ref
		query.setInSpatialReference(sr);
		query.setOutSpatialReference(sr);
		// return all features
		query.setOutFields(new String[] { "*" });
		// include geometry in result set
		query.setReturnGeometry(true);
		// run query on FeatureLayer off UI thread
		windTurbine.queryFeatures(query, new CallbackListener<FeatureSet>() {

			// an error occurred, log exception
			@Override
			public void onError(Throwable e) {
				Log.e("Test", "Unable to perform query", e);
				dialog.dismiss();
			}

			// create json from resulting FeatureSet
			@Override
			public void onCallback(FeatureSet result) {
				if (result != null) {
					FileOutputStream outstream = null;
					try {
						// create feature set as json string
						String fsstring = FeatureSet.toJson(result);
						// create fully qualified path for json file
						path = createJsonFile();
						// create a File from json fully qualified path
						File outfile = new File(path);
						// create output stream to write to json file
						outstream = new FileOutputStream(outfile);
						outstream.write(fsstring.getBytes());
						// close output stream
						outstream.close();
						// create new Runnable to be added to message queue
						handler.post(new MyRunnable());
					} catch (Exception e) {
						e.printStackTrace();
						if (outstream != null) {
							try {
								outstream.close();
								handler.post(new MyRunnable());
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}

				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.unpause();
	}

	/*
	 * Dismiss dialog if activity is destroyed(non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
	}

	/*
	 * Dismiss progress dialog when query thread completes
	 */
	static public class MyRunnable implements Runnable {
		@Override
		public void run() {
			dialog.dismiss();
		}

	}

}

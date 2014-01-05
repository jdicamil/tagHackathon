/* Copyright 2010 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 *
 * See the sample code usage restrictions document for further information.
 *
 */

package com.esri.arcgis.android.samples.querytask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

public class AttributeQuery extends Activity {

	/** Called when the activity is first created. */
	MapView mMapView;
	GraphicsLayer graphicsLayer;
	Graphic graphic;
	Graphic fillGraphic;
	Button queryButton;
	String targetServerURL = "http://services.arcgisonline.com/ArcGIS/rest/services/Demographics/USA_Average_Household_Size/MapServer";
	boolean boolQuery = true;
	ProgressDialog progress;

	final static int HAS_RESULTS = 1;
	final static int NO_RESULT = 2;
	final static int CLEAR_RESULT = 3;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mMapView = (MapView) findViewById(R.id.map);

		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {

			private static final long serialVersionUID = 1L;

			public void onStatusChanged(Object source, STATUS status) {
				if (source == mMapView && status == STATUS.INITIALIZED) {
					graphicsLayer = new GraphicsLayer();
					SimpleRenderer sr = new SimpleRenderer(
							new SimpleFillSymbol(Color.RED));
					graphicsLayer.setRenderer(sr);
					mMapView.addLayer(graphicsLayer);
					boolean doQuery = false;
					for (Layer layer : mMapView.getLayers()) {
						if (layer instanceof ArcGISTiledMapServiceLayer) {
							ArcGISTiledMapServiceLayer tiledLayer = (ArcGISTiledMapServiceLayer) layer;

							if (tiledLayer.getUrl().equals(targetServerURL)) {
								doQuery = true;
								break;
							}
						}
					}
					if (!doQuery) {
						Toast toast = Toast.makeText(AttributeQuery.this,
								"URL for query does not exist any more",
								Toast.LENGTH_LONG);
						toast.show();
					} else {
						queryButton.setEnabled(true);
					}
				}
			}
		});

		queryButton = (Button) findViewById(R.id.queryButton);
		queryButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				if (boolQuery) {
					String targetLayer = targetServerURL.concat("/3");
					String[] queryParams = { targetLayer, "AVGHHSZ_CY>3.5" };
					AsyncQueryTask ayncQuery = new AsyncQueryTask();
					ayncQuery.execute(queryParams);
				} else {
					graphicsLayer.removeAll();
					boolQuery = true;
					queryButton.setText("Average Household > 3.5");

				}
			}
		});
	}

	/**
	 * 
	 * Query Task executes asynchronously.
	 * 
	 */
	private class AsyncQueryTask extends AsyncTask<String, Void, FeatureSet> {

		protected void onPreExecute() {
			progress = ProgressDialog.show(AttributeQuery.this, "",
					"Please wait....query task is executing");

		}

		/**
		 * First member in parameter array is the query URL; second member is
		 * the where clause.
		 */
		protected FeatureSet doInBackground(String... queryParams) {
			if (queryParams == null || queryParams.length <= 1)
				return null;

			String url = queryParams[0];
			Query query = new Query();
			String whereClause = queryParams[1];
			SpatialReference sr = SpatialReference.create(102100);
			query.setGeometry(new Envelope(-20147112.9593773, 557305.257274575,
					-6569564.7196889, 11753184.6153385));
			query.setOutSpatialReference(sr);
			query.setReturnGeometry(true);
			query.setWhere(whereClause);

			QueryTask qTask = new QueryTask(url);
			FeatureSet featureSet = null;

			try {
				featureSet = qTask.execute(query);
			} catch (Exception e) {
				e.printStackTrace();
				return featureSet;
			}
			return featureSet;

		}

		protected void onPostExecute(FeatureSet result) {

			String message = "No result comes back";
			if (result != null) {
				Graphic[] grs = result.getGraphics();

				if (grs.length > 0) {
					graphicsLayer.addGraphics(grs);
					message = (grs.length == 1 ? "1 result has " : Integer
							.toString(grs.length) + " results have ")
							+ "come back";
				}

			}
			progress.dismiss();

			Toast toast = Toast.makeText(AttributeQuery.this, message,
					Toast.LENGTH_LONG);
			toast.show();
			queryButton.setText("Clear graphics");
			boolQuery = false;

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
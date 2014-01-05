package com.esri.arcgis.android.samples.identifytask;

/* Copyright 2010 ESRI
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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.esri.android.action.IdentifyResultSpinner;
import com.esri.android.action.IdentifyResultSpinnerAdapter;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.tasks.ags.identify.IdentifyParameters;
import com.esri.core.tasks.ags.identify.IdentifyResult;
import com.esri.core.tasks.ags.identify.IdentifyTask;

/**
 * This sample allows the user to identify data based on single tap and view the
 * results in a callout window which has a spinner in its layout. Also the user
 * can select any of the results displayed and view its details. The details are
 * the attribute values.
 * 
 * The output value shown in the spinner is the display field.
 * 
 */

public class Identify extends Activity {

	// create ArcGIS objects
	MapView mMapView;
	IdentifyParameters params;

	// create UI objects
	static ProgressDialog dialog;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Retrieve the map and initial extent from XML layout
		mMapView = (MapView) findViewById(R.id.map);
		/* create a @ArcGISTiledMapServiceLayer */
		ArcGISTiledMapServiceLayer basemap = new ArcGISTiledMapServiceLayer(
				this.getResources().getString(R.string.basemap_url));
		// Add tiled layer to MapView
		mMapView.addLayer(basemap);
		// create a demographic layer to identify on
		ArcGISTiledMapServiceLayer demographicLayer = new ArcGISTiledMapServiceLayer(
				this.getResources().getString(R.string.identify_task_url));
		// add demographic layer to the map
		mMapView.addLayer(demographicLayer);

		// set Identify Parameters
		params = new IdentifyParameters();
		params.setTolerance(20);
		params.setDPI(98);
		params.setLayers(new int[] { 4 });
		params.setLayerMode(IdentifyParameters.ALL_LAYERS);

		// Identify on single tap of map
		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			public void onSingleTap(final float x, final float y) {

				if (!mMapView.isLoaded()) {
					return;
				}
				// Add to Identify Parameters based on tapped location
				Point identifyPoint = mMapView.toMapPoint(x, y);
				params.setGeometry(identifyPoint);
				params.setSpatialReference(mMapView.getSpatialReference());
				params.setMapHeight(mMapView.getHeight());
				params.setMapWidth(mMapView.getWidth());
				// add the area of extent to identify parameters
				Envelope env = new Envelope();
				mMapView.getExtent().queryEnvelope(env);
				params.setMapExtent(env);
				// execute the identify task off UI thread
				MyIdentifyTask mTask = new MyIdentifyTask(identifyPoint);
				mTask.execute(params);
			}

		});

	}

	private ViewGroup createIdentifyContent(final List<IdentifyResult> results) {
		// create a new LinearLayout in application context
		LinearLayout layout = new LinearLayout(this);
		// view height and widthwrap content
		layout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		// default orientation
		layout.setOrientation(LinearLayout.HORIZONTAL);
		// Spinner to hold the results of an identify operation
		IdentifyResultSpinner spinner = new IdentifyResultSpinner(this,
				(List<IdentifyResult>) results);
		// make view clickable
		spinner.setClickable(true);
		// MyIdentifyAdapter creates a bridge between spinner and it's data
		MyIdentifyAdapter adapter = new MyIdentifyAdapter(this, results);
		spinner.setAdapter(adapter);
		spinner.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		layout.addView(spinner);

		return layout;
	}

	/**
	 * This class allows the user to customize the string shown in the callout.
	 * By default its the display field name.
	 * 
	 * A spinner adapter defines two different views; one that shows the data in
	 * the spinner itself and one that shows the data in the drop down list when
	 * spinner is pressed.
	 * 
	 */
	public class MyIdentifyAdapter extends IdentifyResultSpinnerAdapter {
		String m_show = null;
		List<IdentifyResult> resultList;
		int currentDataViewed = -1;
		Context m_context;

		public MyIdentifyAdapter(Context context, List<IdentifyResult> results) {
			super(context, results);
			this.resultList = results;
			this.m_context = context;

		}

		// Get a TextView that displays identify results in the callout.
		public View getView(int position, View convertView, ViewGroup parent) {
			String outputVal = null;

			// Get Name attribute from identify results
			IdentifyResult curResult = this.resultList.get(position);
			if (curResult.getAttributes().containsKey("Name")) {
				outputVal = curResult.getAttributes().get("Name").toString();
			}

			// Create a TextView to write identify results
			TextView txtView;
			txtView = new TextView(this.m_context);
			txtView.setText(outputVal);
			txtView.setTextColor(Color.BLACK);
			txtView.setLayoutParams(new ListView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			txtView.setGravity(Gravity.CENTER_VERTICAL);

			return txtView;
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

	private class MyIdentifyTask extends
			AsyncTask<IdentifyParameters, Void, IdentifyResult[]> {

		IdentifyTask mIdentifyTask;
		Point mAnchor;

		MyIdentifyTask(Point anchorPoint) {
			mAnchor = anchorPoint;
		}

		@Override
		protected void onPreExecute() {
			// create dialog while working off UI thread
			dialog = ProgressDialog.show(Identify.this, "Identify Task",
					"Identify query ...");
			//
			mIdentifyTask = new IdentifyTask(Identify.this.getResources()
					.getString(R.string.identify_task_url));
		}

		@Override
		protected IdentifyResult[] doInBackground(IdentifyParameters... params) {
			IdentifyResult[] mResult = null;
			// check that you have the identify parameters
			if (params != null && params.length > 0) {
				IdentifyParameters mParams = params[0];
				try {
					// Run IdentifyTask with Identify Parameters
					mResult = mIdentifyTask.execute(mParams);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			return mResult;
		}

		@Override
		protected void onPostExecute(IdentifyResult[] results) {

			// dismiss dialog
			if (dialog.isShowing()) {
				dialog.dismiss();
			}

			ArrayList<IdentifyResult> resultList = new ArrayList<IdentifyResult>();
			for (int index = 0; index < results.length; index++) {

				if (results[index].getAttributes().get(
						results[index].getDisplayFieldName()) != null)
					resultList.add(results[index]);
			}

			mMapView.getCallout().show(mAnchor,
					createIdentifyContent(resultList));
		}

	}

}
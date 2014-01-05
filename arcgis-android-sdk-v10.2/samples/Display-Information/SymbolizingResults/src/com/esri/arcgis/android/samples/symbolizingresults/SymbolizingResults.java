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

package com.esri.arcgis.android.samples.symbolizingresults;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Point;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.ClassBreak;
import com.esri.core.renderer.ClassBreaksRenderer;
import com.esri.core.renderer.Renderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;

public class SymbolizingResults extends Activity {

	MapView map;
	Button queryBtn;
	GraphicsLayer gl;
	Callout callout;

	/** Called when the activity is first created. */

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		map = (MapView) findViewById(R.id.map);
		map.addLayer(new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer"));

		gl = new GraphicsLayer();
		gl.setRenderer(createClassBreaksRenderer());
		map.addLayer(gl);

		queryBtn = (Button) findViewById(R.id.querybtn);

		queryBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// Sets query parameter
				Query query = new Query();
				query.setWhere("STATE_NAME='Kansas'");

				query.setReturnGeometry(true);
				String[] outfields = new String[] { "NAME", "STATE_NAME",
						"POP07_SQMI" };
				query.setOutFields(outfields);
				query.setOutSpatialReference(map.getSpatialReference());

				Query[] queryParams = { query };

				AsyncQueryTask qt = new AsyncQueryTask();

				qt.execute(queryParams);

			}
		});

		// Sets 'OnSingleTapListener' so that when single tap
		// happens, Callout would show 'SQMI' information associated
		// with tapped 'Graphic'
		map.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			public void onSingleTap(float x, float y) {

				if (!map.isLoaded())
					return;
				int[] uids = gl.getGraphicIDs(x, y, 2);
				if (uids != null && uids.length > 0) {

					int targetId = uids[0];
					Graphic gr = gl.getGraphic(targetId);
					callout = map.getCallout();

					// Sets Callout style
					callout.setStyle(R.xml.countypop);
					String countyName = (String) gr.getAttributeValue("NAME");
					String countyPop = gr.getAttributeValue("POP07_SQMI")
							.toString();
					// Sets custom content view to Callout
					callout.setContent(loadView(countyName, countyPop));
					callout.show(map.toMapPoint(new Point(x, y)));
				} else {
					if (callout != null && callout.isShowing()) {
						callout.hide();
					}
				}

			}
		});

	}

	// Creates custom content view with 'Graphic' attributes
	private View loadView(String countyName, String pop07) {
		View view = LayoutInflater.from(SymbolizingResults.this).inflate(
				R.layout.sqmi, null);

		final TextView name = (TextView) view.findViewById(R.id.county_name);
		name.setText(countyName + "'s SQMI");

		final TextView number = (TextView) view.findViewById(R.id.pop_sqmi);
		number.setText(pop07);

		final ImageView photo = (ImageView) view
				.findViewById(R.id.family_photo);
		photo.setImageDrawable(SymbolizingResults.this.getResources()
				.getDrawable(R.drawable.family));

		return view;

	}

	// Creates 'ClassBreaksRenderer' based on 'SQMI'
	private Renderer createClassBreaksRenderer() {
		ClassBreaksRenderer renderer = new ClassBreaksRenderer();
		renderer.setMinValue(0.0);
		renderer.setField("POP07_SQMI");
		ClassBreak cb1 = new ClassBreak();
		cb1.setClassMaxValue(25);
		cb1.setSymbol(new SimpleFillSymbol(Color.argb(128, 56, 168, 0)));
		cb1.setLabel("First class");

		ClassBreak cb2 = new ClassBreak();
		cb2.setClassMaxValue(75);
		cb2.setSymbol(new SimpleFillSymbol(Color.argb(128, 139, 209, 0)));
		cb2.setLabel("Second class");

		ClassBreak cb3 = new ClassBreak();
		cb3.setClassMaxValue(175);
		cb3.setSymbol(new SimpleFillSymbol(Color.argb(128, 255, 255, 0)));
		cb3.setLabel("Third class");

		ClassBreak cb4 = new ClassBreak();
		cb4.setClassMaxValue(400);
		cb4.setSymbol(new SimpleFillSymbol(Color.argb(128, 255, 128, 0)));
		cb4.setLabel("Fourth class");

		ClassBreak cb5 = new ClassBreak();
		cb5.setClassMaxValue(Double.MAX_VALUE);
		cb5.setSymbol(new SimpleFillSymbol(Color.argb(128, 255, 0, 0)));

		renderer.addClassBreak(cb1);
		renderer.addClassBreak(cb2);
		renderer.addClassBreak(cb3);
		renderer.addClassBreak(cb4);
		renderer.addClassBreak(cb5);
		return renderer;
	}

	private class AsyncQueryTask extends AsyncTask<Query, Void, FeatureSet> {

		protected FeatureSet doInBackground(Query... params) {

			if (params.length > 0) {
				Query query = params[0];
				QueryTask queryTask = new QueryTask(
						"http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/Demographics/ESRI_Census_USA/MapServer/3");
				try {
					FeatureSet fs = queryTask.execute(query);
					return fs;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			return null;
		}

		protected void onPostExecute(FeatureSet fs) {
			gl.addGraphics(fs.getGraphics());
			// gl.recycle();
			queryBtn.setEnabled(false);
			Toast toast = Toast.makeText(SymbolizingResults.this,
					"Tap on county for SQMI data", Toast.LENGTH_LONG);
			toast.show();
		}

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
/*
 * Copyright 2010 ESRI
 * All rights reserved under the copyright laws of the United States and applicable international laws, treaties, and conventions.
 * You may freely redistribute and use this sample code, with or without modification, provided you include the original copyright notice and use restrictions.
 * Disclaimer: THE SAMPLE CODE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL ESRI OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) SUSTAINED BY YOU OR A THIRD PARTY, HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT ARISING IN ANY WAY OUT OF THE USE OF THIS SAMPLE CODE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * For additional information, contact:
 * Environmental Systems Research Institute, Inc.
 * Attn: Contracts and Legal Services Department
 * 380 New York Street Redlands, California, 92373
 * USA
 * email: contracts@esri.com 
 */
package com.esri.arcgis.android.samples.highlightfeatures;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.tasks.ags.identify.IdentifyParameters;
import com.esri.core.tasks.ags.identify.IdentifyResult;
import com.esri.core.tasks.ags.identify.IdentifyTask;

public class HighlightFeatures extends Activity {

	// ArcGIS Android elements
	MapView mapView;
	ArcGISTiledMapServiceLayer tiledMapServiceLayer;
	GraphicsLayer graphicsLayer;
	Graphic[] highlightGraphics;
	ArrayList<IdentifyResult> identifyResults;

	// Android UI elements
	Button clearButton;
	Button layerButton;
	TextView label;
	TextView idRes;

	String mapURL = "http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/PublicSafety/PublicSafetyBasemap/MapServer";

	final String[] layerNames = new String[] { "Interstates", "US Highways",
			"Major and Minor Streets", "WaterBodies",
			"Cemetaries, Parks, Golf Courses" };

	final int[] layerIndexes = new int[] { 6, 7, 8, 28, 37 };

	int selectedLayerIndex = -1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		
		// Initialize ArcGIS Android MapView
		mapView = (MapView) findViewById(R.id.map);

		
		// Long Press listener for map view
		mapView.setOnLongPressListener(new OnLongPressListener() {

			private static final long serialVersionUID = 1L;

			/*
			 * Invoked when user does a Long Press on map. This fires an
			 * identify query for features covered by user's finder, on selected
			 * layer.
			 */
			public boolean onLongPress(float x, float y) {
				try {
					if (tiledMapServiceLayer.isInitialized()
							&& selectedLayerIndex >= 0) {

						graphicsLayer.removeAll();

						// Get the point user clicked on
						Point pointClicked = mapView.toMapPoint(x, y);

						// Set parameters for identify task
						IdentifyParameters inputParameters = new IdentifyParameters();
						inputParameters.setGeometry(pointClicked);
						inputParameters
								.setLayers(new int[] { layerIndexes[selectedLayerIndex] });
						Envelope env = new Envelope();
						mapView.getExtent().queryEnvelope(env);
						inputParameters.setSpatialReference(mapView
								.getSpatialReference());
						inputParameters.setMapExtent(env);
						inputParameters.setDPI(96);
						inputParameters.setMapHeight(mapView.getHeight());
						inputParameters.setMapWidth(mapView.getWidth());
						inputParameters.setTolerance(10);

						// Execute identify task
						MyIdentifyTask mIdenitfy = new MyIdentifyTask();
						mIdenitfy.execute(inputParameters);

					} else {
						Toast toast = Toast
								.makeText(
										getApplicationContext(),
										"Please select a layer to identify features from.",
										Toast.LENGTH_SHORT);
						toast.show();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return true;
			}
		});

		// Initialize LayerButton, Clear button, and Label
		layerButton = (Button) findViewById(R.id.layerbutton);
		layerButton.setEnabled(false);
		layerButton.setOnClickListener(new View.OnClickListener() {
			/*
			 * This displays an AlertDilaog as defined in onCreateDialog()
			 * method. Invocation of show() causes onCreateDialog() to be called
			 * internally.
			 */
			public void onClick(View v) {
				showDialog(0);
			}
		});

		label = (TextView) findViewById(R.id.label);

		clearButton = (Button) findViewById(R.id.clearbutton);
		clearButton.setEnabled(false);
		clearButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				graphicsLayer.removeAll();
				clearButton.setEnabled(false);
			}
		});

		/*
		 * Retrieve application state after device is flipped. The
		 * onRetainNonConfigurationInstance() method allows us to persist states
		 * of elements of the application, including ArcGIS Android entitied,
		 * and Android UI element. When device is flipped and onCreate() gets
		 * invoked, the getLastNonConfigurationInstance() method will provide
		 * access to state of above mentioned elements, which can be used to
		 * restore the app to its previous state.
		 */
		Object[] init = (Object[]) getLastNonConfigurationInstance();
		if (init != null) {
			// Retrieve map view state
			mapView.restoreState((String) init[0]);

			int index = ((Integer) init[1]).intValue();
			if (index != -1) {
				label.setText(layerNames[index]);
				selectedLayerIndex = index;
			}
		} else {
			/*
			 * Initialize MapView, TiledMapServiceLayer and GraphicsLayer. This
			 * block will be executed when app is started the first time.
			 */
			mapView.setExtent(new Envelope(-85.61828847183895,
					38.19242311866144, -85.53589100936443, 38.31361605305102));

		}

		tiledMapServiceLayer = new ArcGISTiledMapServiceLayer(mapURL);
		graphicsLayer = new GraphicsLayer();

		/*
		 * Use TiledMapServiceLayer's OnStatusChangedListener to listen to
		 * events such as change of status. This event allows developers to
		 * check if layer is indeed initialized and ready for use, and take
		 * appropriate action. In this case, we are modifying state of other UI
		 * elements if and when the layer is loaded.
		 */
		tiledMapServiceLayer
				.setOnStatusChangedListener(new OnStatusChangedListener() {
					private static final long serialVersionUID = 1L;

					public void onStatusChanged(Object arg0, STATUS status) {
						/*
						 * Check if layer's new status = INITIALIZED. If it is,
						 * initialize UI elements
						 */
						if (status
								.equals(OnStatusChangedListener.STATUS.INITIALIZED)) {
							layerButton.setEnabled(true);
						}
					}
				});

		// Add TiledMapServiceLayer and GraphicsLayer to map
		mapView.addLayer(tiledMapServiceLayer);
		mapView.addLayer(graphicsLayer);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		Object[] objs = new Object[2];
		objs[0] = mapView.retainState();
		objs[1] = Integer.valueOf(selectedLayerIndex);
		return objs;
	}

	/**
	 * Returns an AlertDialog that includes names of all layers in the map
	 * service
	 */
	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(HighlightFeatures.this)
				.setTitle("Select a Layer")
				.setItems(layerNames, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						label.setText(layerNames[which] + " selected.");
						selectedLayerIndex = which;

						Toast toast = Toast
								.makeText(
										getApplicationContext(),
										"Identify features by pressing for 2-3 seconds.",
										Toast.LENGTH_LONG);
						toast.setGravity(Gravity.BOTTOM, 0, 0);
						toast.show();
					}
				}).create();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.unpause();
	}

	private class MyIdentifyTask extends
			AsyncTask<IdentifyParameters, Void, IdentifyResult[]> {

		IdentifyTask mIdentifyTask;

		@Override
		protected IdentifyResult[] doInBackground(IdentifyParameters... params) {
			IdentifyResult[] mResult = null;
			if (params != null && params.length > 0) {
				IdentifyParameters mParams = params[0];
				try {
					mResult = mIdentifyTask.execute(mParams);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			return mResult;
		}

		@Override
		protected void onPostExecute(IdentifyResult[] results) {
			if (results != null && results.length > 0) {

				highlightGraphics = new Graphic[results.length];

				Toast toast = Toast.makeText(getApplicationContext(),
						results.length + " features identified\n",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.BOTTOM, 0, 0);
				toast.show();

				// Highlight all features that match with results
				for (int i = 0; i < results.length; i++) {
					Geometry geom = results[i].getGeometry();
					String typeName = geom.getType().name();

					Random r = new Random();
					int color = Color.rgb(r.nextInt(255), r.nextInt(255),
							r.nextInt(255));

					// Create appropriate symbol, based on geometry type
					if (typeName.equalsIgnoreCase("point")) {
						SimpleMarkerSymbol sms = new SimpleMarkerSymbol(color,
								20, STYLE.SQUARE);
						highlightGraphics[i] = new Graphic(geom, sms);
					} else if (typeName.equalsIgnoreCase("polyline")) {
						SimpleLineSymbol sls = new SimpleLineSymbol(color, 5);
						highlightGraphics[i] = new Graphic(geom, sls);
					} else if (typeName.equalsIgnoreCase("polygon")) {
						SimpleFillSymbol sfs = new SimpleFillSymbol(color);
						sfs.setAlpha(75);
						highlightGraphics[i] = new Graphic(geom, sfs);
					}

					// set the Graphic's geometry, add it to GraphicLayer and
					// refresh the Graphic Layer
					graphicsLayer.addGraphic(highlightGraphics[i]);
					clearButton.setEnabled(true);
				}
			} else {
				Toast toast = Toast.makeText(getApplicationContext(),
						"No features identified.", Toast.LENGTH_SHORT);
				toast.show();
			}

		}

		@Override
		protected void onPreExecute() {
			mIdentifyTask = new IdentifyTask(mapURL);
		}

	}

}

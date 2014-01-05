package com.esri.arcgis.android.samples.offlineeditor;

/* Copyright 2013 ESRI
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.gdb.GdbFeature;
import com.esri.core.gdb.GdbFeatureTable;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.io.OnSelfSignedCertificateListener;
import com.esri.core.io.SelfSignedCertificateHandle;
import com.esri.core.map.FeatureTemplate;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.symbol.Symbol;
import com.esri.core.table.TableException;

public class OfflineEditorActivity extends Activity {

	protected static final String TAG = "OfflineEditorActivity";
	private static final int POINT = 0;
	private static final int POLYLINE = 1;
	private static final int POLYGON = 2;

	private PopupForEditOffline popup;

	private MapView mapView;
	GraphicsLayer graphicsLayer;
	GraphicsLayer graphicsLayerEditing;
	GraphicsLayer highlightGraphics;

	boolean featureUpdate = false;
	long featureUpdateId;
	int addedGraphicId;

	MyTouchListener myListener;
	private TemplatePicker tp;

	Button editButton;
	Button removeButton;
	Button clearButton;
	Button cancelButton;
	Button undoButton;
	Button saveButton;
	Button openButton;
	ToggleButton switchBasemapbutton;
	TabHost mTabHost;

	ArrayList<Point> points = new ArrayList<Point>();
	ArrayList<Point> mpoints = new ArrayList<Point>();
	boolean midpointselected = false;
	boolean vertexselected = false;
	int insertingindex;

	int editingmode;

	ArrayList<EditingStates> editingstates = new ArrayList<EditingStates>();
	FeatureTemplate template;
	Activity activityForButton;
	volatile int numUnInitedViews = 5; // uninitialized views including mapView,
										// graphics layer and feature layers
										// only enable edit button after all
										// above views have been initialized

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(false);
		setContentView(R.layout.offliner);

		mTabHost = (TabHost) findViewById(R.id.tabHost);
		mTabHost.setup();

		TabHost.TabSpec downloadSpec = mTabHost.newTabSpec("Download");
		downloadSpec.setContent(R.id.offlinerdownloadlayout);
		downloadSpec.setIndicator("Download");
		mTabHost.addTab(downloadSpec);

		TabHost.TabSpec editSpec = mTabHost.newTabSpec("Edit");
		editSpec.setContent(R.id.offlinereditlayout);
		editSpec.setIndicator("Edit");
		mTabHost.addTab(editSpec);

		TabHost.TabSpec syncSpec = mTabHost.newTabSpec("Sync");
		syncSpec.setContent(R.id.offlinersynclayout);
		syncSpec.setIndicator("Sync");
		mTabHost.addTab(syncSpec);

		SelfSignedCertificateHandle
				.setOnSelfSignedCertificateListener(new OnSelfSignedCertificateListener() {
					public boolean checkServerTrusted(X509Certificate[] chain,
							String authType) {
						try {
							chain[0].checkValidity();
						} catch (Exception e) {
							return true;
						}

						return true;
					}
				});

		/*
		 * Initialize ArcGIS Android MapView, tiledMapServiceLayer, and Graphics
		 * Layer
		 */
		mapView = ((MapView) findViewById(R.id.map));
		activityForButton = this;
		removeButton = (Button) findViewById(R.id.removebutton);

		editButton = (Button) findViewById(R.id.editbutton);

		cancelButton = (Button) findViewById(R.id.cancelbutton);

		clearButton = (Button) findViewById(R.id.clearbutton);

		saveButton = (Button) findViewById(R.id.savebutton);

		undoButton = (Button) findViewById(R.id.undobutton);
		openButton = (Button) findViewById(R.id.openbutton);
		switchBasemapbutton = (ToggleButton) findViewById(R.id.switchBasemap);

		mapView.addLayer(new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/ESRI_StreetMap_World_2D/MapServer"));
		GDBUtil.loadPreferences(OfflineEditorActivity.this);
		for (int i : GDBUtil.getLayerIds()) {

			mapView.addLayer(new ArcGISFeatureLayer(GDBUtil.getFsUrl() + "/"
					+ i, ArcGISFeatureLayer.MODE.ONDEMAND));
		}

		// mapView.addLayer(new
		// ArcGISFeatureLayer(GDBUtil.DEFAULT_FEATURE_SERVICE_URL +
		// "/0",ArcGISFeatureLayer.MODE.ONDEMAND));

		/**
		 * When the basemap is initialized the status will be true.
		 */
		mapView.setOnStatusChangedListener(new OnStatusChangedListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void onStatusChanged(final Object source, final STATUS status) {
				// editButton.setEnabled(true);
				// graphicsLayer = new GraphicsLayer();

				if (STATUS.INITIALIZED == status) {

					if (source instanceof MapView) {
						Log.d("GeometryEditor Status Change",
								"source is MapView, numUninitViews: "
										+ numUnInitedViews);
						numUnInitedViews--;
						graphicsLayer = new GraphicsLayer();
						highlightGraphics = new GraphicsLayer();
						mapView.addLayer(graphicsLayer);
						mapView.addLayer(highlightGraphics);
					}
				}
				if (STATUS.LAYER_LOADED == status) {
					if (source instanceof ArcGISFeatureLayer) {
						GDBUtil.showProgress(OfflineEditorActivity.this, false);
					}
				}
			}

		});

	}// oncreate

	public void loadLayers(View view) {
		if (mTabHost.getCurrentTabTag().equals("Download")) {
			GDBUtil.showProgress(OfflineEditorActivity.this, true);
			for (Layer layer : mapView.getLayers()) {
				if (layer instanceof ArcGISFeatureLayer
						|| layer instanceof FeatureLayer)
					mapView.removeLayer(layer);
			}
			if (tp != null) {
				tp.clearSelection();
				clear();
			}
			GDBUtil.loadPreferences(OfflineEditorActivity.this);

			for (int i : GDBUtil.getLayerIds()) {
				mapView.addLayer(new ArcGISFeatureLayer(GDBUtil.getFsUrl()
						+ "/" + i, ArcGISFeatureLayer.MODE.ONDEMAND));
			}

		}

	}

	public void switchLocalBasemap(View view) {
		GDBUtil.loadPreferences(OfflineEditorActivity.this);
		if (switchBasemapbutton.isChecked()) {

			File file = new File(GDBUtil.getBasemapFileName());

			if (file.exists()) {
				mapView.removeLayer(0);
				try {
					mapView.addLayer(
							new ArcGISLocalTiledLayer(GDBUtil
									.getBasemapFileName()), 0);
					GDBUtil.showMessage(OfflineEditorActivity.this,
							"Local Basemap is On");
				} catch (Exception e) {
					// TODO: handle exception
					switchBasemapbutton.setChecked(false);
					GDBUtil.showMessage(OfflineEditorActivity.this,
							"Invalid Basemap Tpk File");
				}

			} else {
				switchBasemapbutton.setChecked(false);
				GDBUtil.showMessage(OfflineEditorActivity.this,
						"Local Basemap tpk doesn't exist");
			}

		} else {
			mapView.removeLayer(0);
			mapView.addLayer(
					new ArcGISTiledMapServiceLayer(
							"http://services.arcgisonline.com/ArcGIS/rest/services/ESRI_StreetMap_World_2D/MapServer"),
					0);
			GDBUtil.showMessage(OfflineEditorActivity.this,
					"Local Basemap is Off. Switched to ArcGIS Online Basemap");
		}

	}

	public void downloadGdb(View view) {
		GDBUtil.loadPreferences(OfflineEditorActivity.this);
		if (getTemplatePicker() != null) {
			getTemplatePicker().clearSelection();
			clear();
		}
		new MyAsyncTask().execute("downloadGdb");
	}

	public void syncGdb(View view) {
		new MyAsyncTask().execute("syncGdb");
	}

	public void saveButton(View view) {
		save();

	}

	public void removeButton(View view) {
		if (!vertexselected)
			points.remove(points.size() - 1); // remove last vertex
		else
			points.remove(insertingindex);
		midpointselected = false;
		vertexselected = false;
		editingstates.add(new EditingStates(points, midpointselected,
				vertexselected, insertingindex));
		refresh();

	}

	public void editButton(View view) {

		GDBUtil.showProgress(OfflineEditorActivity.this, true);
		clear();
		int layerCount = 0;
		for (Layer layer : mapView.getLayers()) {
			if (layer instanceof FeatureLayer) {
				layerCount++;
			}

		}
		if (layerCount > 0) {
			if (myListener == null) {
				myListener = new MyTouchListener(OfflineEditorActivity.this,
						mapView);
				mapView.setOnTouchListener(myListener);
			}
			if (getTemplatePicker() != null) {
				getTemplatePicker().showAtLocation(editButton, Gravity.BOTTOM,
						0, 0);
			} else {
				new TemplatePickerTask().execute();
			}
		} else {
			GDBUtil.showMessage(OfflineEditorActivity.this,
					"No Editable Local Feature Layers.");

		}
		GDBUtil.showProgress(OfflineEditorActivity.this, false);

	}

	public void cancelButton(View view) {
		midpointselected = false;
		vertexselected = false;
		refresh();

	}

	public void clearButton(View view) {
		clear();
	}

	public void undoButton(View view) {
		editingstates.remove(editingstates.size() - 1);
		EditingStates state = editingstates.get(editingstates.size() - 1);
		points.clear();
		points.addAll(state.points1);
		Log.d(TAG, "# of points = " + points.size());
		midpointselected = state.midpointselected1;
		vertexselected = state.vertexselected1;
		insertingindex = state.insertingindex1;
		refresh();

	}

	/**
	 * An instance of this class is created when a new point is to be
	 * added/moved/deleted. Hence we can describe this class as a container of
	 * points selected. Points, vertexes, or mid points.
	 */
	class EditingStates {
		ArrayList<Point> points1 = new ArrayList<Point>();
		boolean midpointselected1 = false;
		boolean vertexselected1 = false;
		int insertingindex1;

		public EditingStates(ArrayList<Point> points, boolean midpointselected,
				boolean vertexselected, int insertingindex) {
			this.points1.addAll(points);
			this.midpointselected1 = midpointselected;
			this.vertexselected1 = vertexselected;
			this.insertingindex1 = insertingindex;
		}
	}

	/*
	 * MapView's touch listener
	 */
	class MyTouchListener extends MapOnTouchListener {
		MapView map;
		Context context;
		Bitmap snapshot = null;
		boolean redrawCache = true;
		boolean showmag = false;

		public MyTouchListener(Context context, MapView view) {
			super(context, view);
			this.context = context;
			map = view;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			if (tp != null) {

				// if (vertexselected || midpointselected) {
				if (getTemplatePicker().getselectedTemplate() != null) {
					setEditingMode();
				}

				if (getTemplatePicker().getSelectedLayer() != null) {
					if (editingmode == POINT || editingmode == POLYLINE
							|| editingmode == POLYGON) {

						highlightGraphics.removeAll();
						long[] featureIds = ((FeatureLayer) mapView
								.getLayerByID(getTemplatePicker()
										.getSelectedLayer().getID()))
								.getFeatureIDs(e.getX(), e.getY(), 25);

						if (featureIds.length > 0) {
							final long gdbFeatureSelectedId = featureIds[0];
							GdbFeature gdbFeatureSelected = (GdbFeature) ((FeatureLayer) mapView
									.getLayerByID(getTemplatePicker()
											.getSelectedLayer().getID()))
									.getFeature(gdbFeatureSelectedId);
							if (gdbFeatureSelected.getGeometry().getType()
									.equals(Geometry.Type.POINT)) {
								Point pt = (Point) gdbFeatureSelected
										.getGeometry();

								Graphic g = new Graphic(pt,
										new SimpleMarkerSymbol(Color.CYAN, 10,
												STYLE.DIAMOND));
								highlightGraphics.addGraphic(g);
								popup = new PopupForEditOffline(mapView,
										OfflineEditorActivity.this);
								popup.showPopup(e.getX(), e.getY(), 25);
							} else if (gdbFeatureSelected.getGeometry()
									.getType().equals(Geometry.Type.POLYLINE)) {
								Polyline poly = (Polyline) gdbFeatureSelected
										.getGeometry();
								Graphic g = new Graphic(poly,
										new SimpleLineSymbol(Color.CYAN, 5));
								highlightGraphics.addGraphic(g);
								popup = new PopupForEditOffline(mapView,
										OfflineEditorActivity.this);
								popup.showPopup(e.getX(), e.getY(), 25);

							} else if (gdbFeatureSelected.getGeometry()
									.getType().equals(Geometry.Type.POLYGON)) {
								Polygon polygon = (Polygon) gdbFeatureSelected
										.getGeometry();
								Graphic g = new Graphic(
										polygon,
										new SimpleFillSymbol(
												Color.CYAN,
												com.esri.core.symbol.SimpleFillSymbol.STYLE.SOLID));
								highlightGraphics.addGraphic(g);
								popup = new PopupForEditOffline(mapView,
										OfflineEditorActivity.this);
								popup.showPopup(e.getX(), e.getY(), 25);

							}
						}

					}
				}
				// }

			}
		}

		@Override
		public boolean onDragPointerMove(MotionEvent from, final MotionEvent to) {
			if (tp != null) {
				if (getTemplatePicker().getselectedTemplate() != null) {
					setEditingMode();
				}
			}
			return super.onDragPointerMove(from, to);
		}

		@Override
		public boolean onDragPointerUp(MotionEvent from, final MotionEvent to) {
			if (tp != null) {
				if (getTemplatePicker().getselectedTemplate() != null) {
					setEditingMode();
				}
			}

			return super.onDragPointerUp(from, to);
		}

		/**
		 * In this method we check if the point clicked on the map denotes a new
		 * point or means an existing vertex must be moved.
		 */
		@Override
		public boolean onSingleTap(final MotionEvent e) {
			if (tp != null) {

				Point point = map.toMapPoint(new Point(e.getX(), e.getY()));
				if (getTemplatePicker().getselectedTemplate() != null) {
					setEditingMode();

				}
				if (getTemplatePicker().getSelectedLayer() != null) {
					long[] featureIds = ((FeatureLayer) mapView
							.getLayerByID(getTemplatePicker()
									.getSelectedLayer().getID()))
							.getFeatureIDs(e.getX(), e.getY(), 25);
					if (featureIds.length > 0 && (!featureUpdate)) {
						featureUpdateId = featureIds[0];
						GdbFeature gdbFeatureSelected = (GdbFeature) ((FeatureLayer) mapView
								.getLayerByID(getTemplatePicker()
										.getSelectedLayer().getID()))
								.getFeature(featureIds[0]);
						if (editingmode == POLYLINE || editingmode == POLYGON) {
							if (gdbFeatureSelected.getGeometry().getType()
									.equals(Geometry.Type.POLYLINE)) {
								Polyline polyline = (Polyline) gdbFeatureSelected
										.getGeometry();
								for (int i = 0; i < polyline.getPointCount(); i++) {
									points.add(polyline.getPoint(i));
								}
								/*
								 * drawVertices(); drawMidPoints();
								 * drawPolyline();
								 */
								refresh();

								editingstates.add(new EditingStates(points,
										midpointselected, vertexselected,
										insertingindex));

							} else if (gdbFeatureSelected.getGeometry()
									.getType().equals(Geometry.Type.POLYGON)) {
								Polygon polygon = (Polygon) gdbFeatureSelected
										.getGeometry();
								for (int i = 0; i < polygon.getPointCount(); i++) {
									points.add(polygon.getPoint(i));
								}
								/*
								 * drawVertices(); drawMidPoints();
								 * drawPolyline();
								 */
								refresh();
								editingstates.add(new EditingStates(points,
										midpointselected, vertexselected,
										insertingindex));

							}
							featureUpdate = true;
						}
						// points.clear();
					} else {
						if (editingmode == POINT) {

							GdbFeature g;
							try {
								graphicsLayer.removeAll();
								// this needs to tbe created from FeatureLayer
								// by
								// passing template
								g = ((GdbFeatureTable) ((FeatureLayer) mapView
										.getLayerByID(getTemplatePicker()
												.getSelectedLayer().getID()))
										.getFeatureTable())
										.createFeatureWithTemplate(
												getTemplatePicker()
														.getselectedTemplate(),
												point);
								Symbol symbol = ((FeatureLayer) mapView
										.getLayerByID(getTemplatePicker()
												.getSelectedLayer().getID()))
										.getRenderer().getSymbol(g);

								Graphic gr = new Graphic(g.getGeometry(),
										symbol, g.getAttributes());

								addedGraphicId = graphicsLayer.addGraphic(gr);
							} catch (TableException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

							points.clear();
						}
						if (!midpointselected && !vertexselected) {
							// check if user tries to select an existing point.

							int idx1 = getSelectedIndex(e.getX(), e.getY(),
									mpoints, map);
							if (idx1 != -1) {
								midpointselected = true;
								insertingindex = idx1;
							}

							if (!midpointselected) { // check vertices
								int idx2 = getSelectedIndex(e.getX(), e.getY(),
										points, map);
								if (idx2 != -1) {
									vertexselected = true;
									insertingindex = idx2;
								}

							}
							if (!midpointselected && !vertexselected) {
								// no match, add new vertex at the location
								points.add(point);
								editingstates.add(new EditingStates(points,
										midpointselected, vertexselected,
										insertingindex));
							}

						} else if (midpointselected || vertexselected) {
							int idx1 = getSelectedIndex(e.getX(), e.getY(),
									mpoints, map);
							int idx2 = getSelectedIndex(e.getX(), e.getY(),
									points, map);
							if (idx1 == -1 && idx2 == -1) {
								movePoint(point);
								editingstates.add(new EditingStates(points,
										midpointselected, vertexselected,
										insertingindex));
							} else {

								if (idx1 != -1) {
									insertingindex = idx1;
								}
								if (idx2 != -1) {
									insertingindex = idx2;
								}

								editingstates.add(new EditingStates(points,
										midpointselected, vertexselected,
										insertingindex));

							}
						} else { // an existing point has been selected
									// previously.

							movePoint(point);

						}
						refresh();
						redrawCache = true;
						return true;
					}
				}

			}
			return true;
		}

	}

	/**
	 * The edits made are applied and hence saved on the server.
	 */
	private void save() {
		saveButton.setEnabled(false);
		Graphic addedGraphic;
		MultiPath multipath;

		if (editingmode == POINT)
			try {
				addedGraphic = graphicsLayer.getGraphic(addedGraphicId);
				((FeatureLayer) mapView.getLayerByID(getTemplatePicker()
						.getSelectedLayer().getID())).getFeatureTable()
						.addFeature(addedGraphic);
				graphicsLayer.removeAll();
			} catch (TableException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		else {
			if (editingmode == POLYLINE)
				multipath = new Polyline();
			else if (editingmode == POLYGON)
				multipath = new Polygon();
			else
				return;
			multipath.startPath(points.get(0));
			for (int i = 1; i < points.size(); i++) {
				multipath.lineTo(points.get(i));
			}

			// Simplify the geometry that is to be set on the graphics.
			// Note this call is local not made to the server.
			Geometry geom = GeometryEngine.simplify(multipath,
					mapView.getSpatialReference());
			if (featureUpdate) {
				try {
					GdbFeature g = ((GdbFeatureTable) ((FeatureLayer) mapView
							.getLayerByID(getTemplatePicker()
									.getSelectedLayer().getID()))
							.getFeatureTable()).createFeatureWithTemplate(
							getTemplatePicker().getselectedTemplate(), geom);
					Symbol symbol = ((FeatureLayer) mapView
							.getLayerByID(getTemplatePicker()
									.getSelectedLayer().getID())).getRenderer()
							.getSymbol(g);
					addedGraphic = new Graphic(geom, symbol, g.getAttributes());
					((FeatureLayer) mapView.getLayerByID(getTemplatePicker()
							.getSelectedLayer().getID())).getFeatureTable()
							.updateFeature(featureUpdateId, addedGraphic);
				} catch (TableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					GdbFeature g = ((GdbFeatureTable) ((FeatureLayer) mapView
							.getLayerByID(getTemplatePicker()
									.getSelectedLayer().getID()))
							.getFeatureTable()).createFeatureWithTemplate(
							getTemplatePicker().getselectedTemplate(), geom);
					Symbol symbol = ((FeatureLayer) mapView
							.getLayerByID(getTemplatePicker()
									.getSelectedLayer().getID())).getRenderer()
							.getSymbol(g);
					addedGraphic = new Graphic(geom, symbol, g.getAttributes());
					((FeatureLayer) mapView.getLayerByID(getTemplatePicker()
							.getSelectedLayer().getID())).getFeatureTable()
							.addFeature(addedGraphic);
				} catch (TableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	void movePoint(Point point) {

		if (midpointselected) {// Move mid-point to the new location and make it
			// a vertex.
			points.add(insertingindex + 1, point);
			editingstates.add(new EditingStates(points, midpointselected,
					vertexselected, insertingindex));
		} else if (vertexselected) {
			ArrayList<Point> temp = new ArrayList<Point>();
			for (int i = 0; i < points.size(); i++) {
				if (i == insertingindex)
					temp.add(point);
				else
					temp.add(points.get(i));
			}
			points.clear();
			points.addAll(temp);
			editingstates.add(new EditingStates(points, midpointselected,
					vertexselected, insertingindex));
		}
		midpointselected = false; // back to the normal drawing mode.
		vertexselected = false;

	}

	void refresh() {

		if (editingmode != POINT) {
			if (graphicsLayerEditing != null && graphicsLayer != null) {
				graphicsLayerEditing.removeAll();
				graphicsLayer.removeAll();
			}

			drawPolyline();
			drawMidPoints();
			drawVertices();

			undoButton.setEnabled(editingstates.size() > 1);
		}

		clearButton.setEnabled(true);
		removeButton.setEnabled(points.size() > 1 && !midpointselected);
		cancelButton.setEnabled(midpointselected || vertexselected);

		saveButton.setEnabled((editingmode == POINT && points.size() > 0)
				|| (editingmode == POLYLINE && points.size() > 1)
				|| (editingmode == POLYGON && points.size() > 2));
	}

	private void drawMidPoints() {
		int index;
		Graphic graphic;
		// GraphicsLayer gll = null;
		if (graphicsLayerEditing == null) {
			graphicsLayerEditing = new GraphicsLayer();
			mapView.addLayer(graphicsLayerEditing);
		}
		// draw mid-point
		if (points.size() > 1) {
			mpoints.clear();
			for (int i = 1; i < points.size(); i++) {
				Point p1 = points.get(i - 1);
				Point p2 = points.get(i);
				mpoints.add(new Point((p1.getX() + p2.getX()) / 2,
						(p1.getY() + p2.getY()) / 2));
			}
			if (editingmode == POLYGON) { // complete the circle
				Point p1 = points.get(0);
				Point p2 = points.get(points.size() - 1);
				mpoints.add(new Point((p1.getX() + p2.getX()) / 2,
						(p1.getY() + p2.getY()) / 2));
			}
			index = 0;
			for (Point pt : mpoints) {

				if (midpointselected && insertingindex == index)
					graphic = new Graphic(pt, new SimpleMarkerSymbol(Color.RED,
							20, SimpleMarkerSymbol.STYLE.CIRCLE));
				else
					graphic = new Graphic(pt, new SimpleMarkerSymbol(
							Color.GREEN, 15, SimpleMarkerSymbol.STYLE.CIRCLE));
				graphicsLayerEditing.addGraphic(graphic);
				index++;
			}
		}
	}

	private void drawVertices() {
		int index;
		// draw vertices
		index = 0;

		if (graphicsLayerEditing == null) {
			graphicsLayerEditing = new GraphicsLayer();
			mapView.addLayer(graphicsLayerEditing);
		}

		for (Point pt : points) {
			if (vertexselected && index == insertingindex) {
				Graphic graphic = new Graphic(pt, new SimpleMarkerSymbol(
						Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE));
				Log.d(TAG, "Add Graphic vertex");
				graphicsLayerEditing.addGraphic(graphic);
			} else if (index == points.size() - 1 && !midpointselected
					&& !vertexselected) {
				Graphic graphic = new Graphic(pt, new SimpleMarkerSymbol(
						Color.RED, 20, SimpleMarkerSymbol.STYLE.CIRCLE));

				int id = graphicsLayer.addGraphic(graphic);

				Log.d(TAG,
						"Add Graphic mid point" + pt.getX() + " " + pt.getY()
								+ " id = " + id);

			} else {
				Graphic graphic = new Graphic(pt, new SimpleMarkerSymbol(
						Color.BLACK, 20, SimpleMarkerSymbol.STYLE.CIRCLE));
				Log.d(TAG, "Add Graphic point");
				graphicsLayerEditing.addGraphic(graphic);
			}

			index++;
		}
	}

	private void drawPolyline() {

		if (graphicsLayerEditing == null) {
			graphicsLayerEditing = new GraphicsLayer();
			mapView.addLayer(graphicsLayerEditing);
		}
		if (points.size() <= 1)
			return;
		Graphic graphic;
		MultiPath multipath;
		if (editingmode == POLYLINE)
			multipath = new Polyline();
		else
			multipath = new Polygon();
		multipath.startPath(points.get(0));
		for (int i = 1; i < points.size(); i++) {
			multipath.lineTo(points.get(i));
		}
		Log.d(TAG, "DrawPolyline: Array coutn = " + points.size());
		if (editingmode == POLYLINE)
			graphic = new Graphic(multipath, new SimpleLineSymbol(Color.BLACK,
					4));
		else {
			SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(
					Color.YELLOW);
			simpleFillSymbol.setAlpha(100);
			simpleFillSymbol.setOutline(new SimpleLineSymbol(Color.BLACK, 4));
			graphic = new Graphic(multipath, (simpleFillSymbol));
		}
		Log.d(TAG, "Add Graphic Line in DrawPolyline");
		graphicsLayerEditing.addGraphic(graphic);
	}

	void clear() {
		if (graphicsLayer != null) {
			graphicsLayer.removeAll();
		}

		if (graphicsLayerEditing != null) {
			graphicsLayerEditing.removeAll();
		}
		if (highlightGraphics != null) {
			highlightGraphics.removeAll();
			mapView.getCallout().hide();

		}

		featureUpdate = false;
		points.clear();
		mpoints.clear();
		midpointselected = false;
		vertexselected = false;
		insertingindex = 0;
		clearButton.setEnabled(false);
		removeButton.setEnabled(false);
		cancelButton.setEnabled(false);
		undoButton.setEnabled(false);
		saveButton.setEnabled(false);
		editingstates.clear();

	}

	/**
	 * return index of point in array whose distance to touch point is minimum
	 * and less than 40.
	 * */
	int getSelectedIndex(double x, double y, ArrayList<Point> points1,
			MapView map) {

		if (points1 == null || points1.size() == 0)
			return -1;

		int index = -1;
		double distSQ_Small = Double.MAX_VALUE;
		for (int i = 0; i < points1.size(); i++) {
			Point p = map.toScreenPoint(points1.get(i));
			double diffx = p.getX() - x;
			double diffy = p.getY() - y;
			double distSQ = diffx * diffx + diffy * diffy;
			if (distSQ < distSQ_Small) {
				index = i;
				distSQ_Small = distSQ;
			}
		}

		if (distSQ_Small < (40 * 40)) {
			return index;
		}
		return -1;

	}// end of method

	@Override
	protected void onDestroy() {
		super.onDestroy();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.offlinepreferences, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.preferences:
			Intent intent = new Intent(OfflineEditorActivity.this,
					PreferencesActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void setEditingMode() {
		if (getTemplatePicker() != null) {
			if (getTemplatePicker().getSelectedLayer().getGeometryType()
					.equals(Geometry.Type.POINT)
					|| getTemplatePicker().getSelectedLayer().getGeometryType()
							.equals(Geometry.Type.MULTIPOINT)) {
				editingmode = POINT;
				template = getTemplatePicker().getselectedTemplate();
			} else if (getTemplatePicker().getSelectedLayer().getGeometryType()
					.equals(Geometry.Type.POLYLINE)) {
				editingmode = POLYLINE;
				template = getTemplatePicker().getselectedTemplate();
			} else if (getTemplatePicker().getSelectedLayer().getGeometryType()
					.equals(Geometry.Type.POLYGON)) {
				editingmode = POLYGON;
				template = getTemplatePicker().getselectedTemplate();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == 1) {
			if (resultCode == Activity.RESULT_OK && data != null
					&& popup != null) {
				// Add the selected media as attachment.
				Uri selectedImage = data.getData();
				popup.addAttachment(selectedImage);
			}
		}
		if (requestCode == 3) {
			if (resultCode == RESULT_OK) {
				String fileName = getFileName(data.getData());
				String contentType = getContentType(data.getData());
				String selectedImage = getPath(data.getData());
				File attachment = new File(selectedImage);
				GdbFeatureTable gdbTable = (GdbFeatureTable) (popup
						.getSelectedGdbFeature().getTable());
				try {
					popup.addAttachment(data.getData());
					gdbTable.addAttachment(popup.getSelectedGdbFeature()
							.getId(), attachment, contentType, fileName);

					Toast.makeText(OfflineEditorActivity.this,
							"Attachment Added ! Click on feature again !",
							Toast.LENGTH_LONG).show();

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					Toast.makeText(OfflineEditorActivity.this,
							"Failed to Add Attachment !" + e.getMessage(),
							Toast.LENGTH_LONG).show();
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Toast.makeText(OfflineEditorActivity.this,
							"Failed to Add Attachment !" + e.getMessage(),
							Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public MapView getMapView() {
		return mapView;
	}

	private class MyAsyncTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			if (params[0].equals("syncGdb")) {
				GDBUtil.synchronize(OfflineEditorActivity.this);
			} else if (params[0].equals("downloadGdb")) {
				GDBUtil.downloadData(OfflineEditorActivity.this);
			}
			return null;
		}

	}

	private class TemplatePickerTask extends AsyncTask<Void, Void, Void> {

		ProgressDialog progressDialog;

		@Override
		protected Void doInBackground(Void... params) {

			// TODO Auto-generated method stub
			setTemplatePicker(new TemplatePicker(OfflineEditorActivity.this,
					mapView));
			return null;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			progressDialog = ProgressDialog
					.show(OfflineEditorActivity.this,
							"Loading Edit Templates",
							"Might take more time for layers with many templates",
							true);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			progressDialog.dismiss();
			getTemplatePicker()
					.showAtLocation(editButton, Gravity.BOTTOM, 0, 0);

			super.onPostExecute(result);
		}

	}

	private String getContentType(Uri uri) {
		// TODO Auto-generated method stub
		ContentResolver cR = getContentResolver();
		// MimeTypeMap mime = MimeTypeMap.getSingleton();
		// String type = mime.getExtensionFromMimeType(cR.getType(uri));
		String type = cR.getType(uri);
		return type;
	}

	private String getFileName(Uri uri) {
		String fileName = "";
		String scheme = uri.getScheme();
		if (scheme.equals("file")) {
			fileName = uri.getLastPathSegment();
		} else if (scheme.equals("content")) {
			String[] proj = { MediaStore.Images.Media.TITLE };
			Cursor cursor = getContentResolver().query(uri, proj, null, null,
					null);
			if (cursor != null && cursor.getCount() != 0) {
				int columnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
				cursor.moveToFirst();
				fileName = cursor.getString(columnIndex);
			}
		}
		return fileName;
	}

	private String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		startManagingCursor(cursor);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public TemplatePicker getTemplatePicker() {
		return tp;
	}

	public void setTemplatePicker(TemplatePicker tp) {
		this.tp = tp;
	}

}

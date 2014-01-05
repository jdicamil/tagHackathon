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

package com.esri.arcgis.android.samples.selectfeatures;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.GraphicsLayer.RenderingMode;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.ags.ArcGISFeatureLayer.Options;
import com.esri.android.map.ags.ArcGISFeatureLayer.SELECTION_METHOD;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;


public class SelectFeatures extends Activity {

	
	MapView map = null;
	ArcGISFeatureLayer fLayer = null;
	GraphicsLayer gLayer = null;

	SimpleFillSymbol sfs;
	CallbackListener<FeatureSet> callback = new CallbackListener<FeatureSet>() {

		public void onCallback(FeatureSet fSet) {			

		}

		public void onError(Throwable arg0) {
			gLayer.removeAll();
		}
	};

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		map = new MapView(this);
		map.setExtent(new Envelope(-10868502.895856911, 4470034.144641369,
				-10837928.084542884, 4492965.25312689), 0);
		ArcGISTiledMapServiceLayer tms = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
		map.addLayer(tms);

		Options o = new Options();
		o.mode = MODE.ONDEMAND;
		o.outFields = new String[] { "FIELD_KID", "APPROXACRE", "FIELD_NAME",
				"STATUS", "PROD_GAS", "PROD_OIL", "ACTIVEPROD", "CUMM_OIL",
				"MAXOILWELL", "LASTOILPRO", "LASTOILWEL", "LASTODATE",
				"CUMM_GAS", "MAXGASWELL", "LASTGASPRO", "LASTGASWEL",
				"LASTGDATE", "AVGDEPTH", "AVGDEPTHSL", "FIELD_TYPE",
				"FIELD_KIDN" };
		fLayer = new ArcGISFeatureLayer(
				"http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Petroleum/KSPetro/MapServer/1",
				o);
		SimpleFillSymbol fiedldsSelectionSymbol = new SimpleFillSymbol(
				Color.MAGENTA);
		fiedldsSelectionSymbol
				.setOutline(new SimpleLineSymbol(Color.YELLOW, 2));
		fLayer.setSelectionSymbol(fiedldsSelectionSymbol);		

		map.addLayer(fLayer);

		// selection box
		// Use DYNAMIC rendering mode for drawing graphics(Sketching)
		gLayer = new GraphicsLayer(RenderingMode.DYNAMIC);
		sfs = new SimpleFillSymbol(Color.BLACK);
		sfs.setOutline(new SimpleLineSymbol(Color.RED, 2));
		sfs.setAlpha(100);

		map.addLayer(gLayer);
	 		
		setContentView(map);

		MyTouchListener touchListener = new MyTouchListener(this, map);
		map.setOnTouchListener(touchListener);

		Toast.makeText(this, "Press down to start let go the stop",
				Toast.LENGTH_SHORT).show();

	}

	class MyTouchListener extends MapOnTouchListener {

		Graphic g;
		// first point clicked on the map
		Point p0 = null;
		int uid = -1;

		public MyTouchListener(Context arg0, MapView arg1) {
			super(arg0, arg1);
		}

		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
			if (uid == -1) { // first time
				g = new Graphic(null, sfs);
				p0 = map.toMapPoint(from.getX(), from.getY());
				uid = gLayer.addGraphic(g);

			} else {

				Point p2 = map.toMapPoint(new Point(to.getX(), to.getY()));
				Envelope envelope = new Envelope();				
				envelope.merge(p0);
				envelope.merge(p2);
				gLayer.updateGraphic(uid, envelope);
				
			}

			return true;

		}		
		public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {

			if (uid != -1) {
				g = gLayer.getGraphic(uid);
				if (g!= null && g.getGeometry() != null) {
					fLayer.clearSelection();
					Query q = new Query();
					// optional
					q.setWhere("PROD_GAS='Yes'");
					q.setReturnGeometry(true);
					q.setInSpatialReference(map.getSpatialReference());
					q.setGeometry(g.getGeometry());
					q.setSpatialRelationship(SpatialRelationship.INTERSECTS);

					fLayer.selectFeatures(q, SELECTION_METHOD.NEW, callback);
				}
				gLayer.removeAll();

			}

			p0 = null;
			// Resets it
			uid = -1;
			return true;

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
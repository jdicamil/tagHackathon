/* Copyright 2012 ESRI
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

package com.esri.android.tutorial.cljf;

import java.util.ArrayList;

import android.graphics.Color;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.symbol.Symbol;

/**
 * Class to allow interaction with a maps
 * graphic layer to create a geometric shape.
 * 
 * @author EsriAndroidTeam
 * @version 2.0
 *
 */
public class Sketcher {
	
	public enum SKETCHMODE {
		POINT, POLYLINE, POLYGON, ENVELOPE
	}
	
	/**
	 * Sketcher constructor which creates all the tools to draw on the
	 * provided map, layers, and geometry types.
	 * 
	 * @param map MapView The MapView which contains the layers
	 * @param sketchlayer GraphicsLayer The layer to draw on
	 * @param storagelayer GraphicsLayer
	 * @param mode SKETCHMODE The Geometry type to be drawn
	 */
	public Sketcher(MapView map, GraphicsLayer sketchlayer,
			GraphicsLayer storagelayer, SKETCHMODE mode) {
		this.map = map;
		this.sketchlayer = sketchlayer;
		this.storagelayer = storagelayer;
		this.mode = mode;
		this.pointList = new ArrayList<Point>();

		sketchFillSymbol = new SimpleFillSymbol(Color.MAGENTA);
		sketchFillSymbol.setAlpha(100);
		sketchFillSymbol.setOutline(new SimpleLineSymbol(Color.GRAY, 4));

		sketchLineSymbol = new SimpleLineSymbol(Color.MAGENTA, 4);

		sketchMarkerSymbol = new SimpleMarkerSymbol(Color.GREEN, 10,
				STYLE.CIRCLE);

		storageFillSymbol = new SimpleFillSymbol(Color.argb(100, 204, 204,
				204));
		storageFillSymbol.setAlpha(100);

		storageLineSymbol = new SimpleLineSymbol(Color.BLACK, 4);
		storageLineSymbol.setAlpha(200);

		storageMarkerSymbol = new SimpleMarkerSymbol(Color.BLACK, 8,
				SimpleMarkerSymbol.STYLE.CIRCLE);
	}
	/**
	 * This method is called when the sending activity wants to start
	 * drawing a geometry type on a graphics layer
	 */
	public void start() {
		map.setOnSingleTapListener(new SketchingListener());
	}

	public void stop() {
		map.setOnSingleTapListener(null);
	}

	/**
	 * This method is called when the sending activity has finished
	 * and requests the return geometry.
	 * @return Geometry The 
	 */
	public Geometry save() {
		Symbol symbol;
		if (editedGeometry instanceof Polygon
				|| editedGeometry instanceof Envelope) {
			symbol = storageFillSymbol;
		} else if (editedGeometry instanceof Polyline) {
			symbol = storageLineSymbol;
		} else {
			symbol = storageMarkerSymbol;
		}
		
		Graphic gr = new Graphic(editedGeometry, symbol);
		if (storagelayer != null) {
		   storagelayer.addGraphic(gr);
		}
		sketchlayer.removeAll();
		pointList.clear();
		return editedGeometry;

	}

	void rendertoSketchlayer() {
		sketchlayer.removeAll();
		Symbol symbol;
		if ((mode == SKETCHMODE.POLYGON || mode == SKETCHMODE.POLYLINE) && pointList.size() > 2) {
			MultiPath multiPath;

			if (mode == SKETCHMODE.POLYGON) {
				multiPath = new Polygon();
				symbol = sketchFillSymbol;
			} else {
				multiPath = new Polyline();
				symbol = sketchLineSymbol;
			}
			multiPath.startPath(pointList.get(0));
			for (int i = 1; i < pointList.size(); i++) {
				multiPath.lineTo(pointList.get(i));
			}
			editedGeometry = multiPath;

		} else if (mode == SKETCHMODE.POINT) {
			editedGeometry = pointList.get(0);
			symbol = sketchMarkerSymbol;
		} else { // SKETCHMODE : Envelope
			Envelope envelope = new Envelope();
			for (Point point : pointList) {
				envelope.merge(point);
			}
			editedGeometry = envelope;
			symbol = sketchFillSymbol;

		}

		Graphic gr = new Graphic(editedGeometry, symbol);
		sketchlayer.addGraphic(gr);
		for (Point point : pointList) {
			gr = new Graphic(point, sketchMarkerSymbol);
			sketchlayer.addGraphic(gr);
		}

	}

	/**
	 * Method getStorageFillSymbol.
	 * @return SimpleFillSymbol
	 */
	public SimpleFillSymbol getStorageFillSymbol() {
		return storageFillSymbol;
	}

	/**
	 * Method getStorageLineSymbol.
	 * @return SimpleLineSymbol
	 */
	public SimpleLineSymbol getStorageLineSymbol() {
		return storageLineSymbol;
	}

	/**
	 * Method getStorageMarkerSymbol.
	 * @return SimpleMarkerSymbol
	 */
	public SimpleMarkerSymbol getStorageMarkerSymbol() {
		return storageMarkerSymbol;
	}
	
	/**
	 * Using a single tap interaction this class allows for 
	 * drawing a geometry type, e.g. POINT, LINE, POlYGON, on 
	 * a graphics layer.
	 */
	private class SketchingListener implements OnSingleTapListener {

		private static final long serialVersionUID = 1L;

		public SketchingListener() {
		}

		/**
		 * This method uses a single tap interaction with device to draw
		 * on a graphics layer.  
		 * @param x float The x screen coordinate
		 * @param y float The y screen coordinate
		 * @see com.esri.android.map.event.OnSingleTapListener#onSingleTap(float, float)
		 */
		@Override
		public void onSingleTap(float x, float y) {
			Point curPoint = map.toMapPoint(x, y);
			if (mode == SKETCHMODE.POINT) {
				if (pointList.size() == 1) {
					pointList.set(0, curPoint);
				} else {
					pointList.add(curPoint);
				}
			} else if (mode == SKETCHMODE.POLYLINE
					|| mode == SKETCHMODE.POLYGON) {
				pointList.add(curPoint);
			} else if (mode == SKETCHMODE.ENVELOPE) {
				if (pointList.size() == 2) {
					pointList.set(1, curPoint);
				} else {
					pointList.add(curPoint);
				}
			}
			rendertoSketchlayer();
		}
	}
	
	private GraphicsLayer sketchlayer;
	private GraphicsLayer storagelayer;

	private SimpleFillSymbol sketchFillSymbol;
	private SimpleLineSymbol sketchLineSymbol;
	private SimpleMarkerSymbol sketchMarkerSymbol;

	private SimpleLineSymbol storageLineSymbol;
	private SimpleMarkerSymbol storageMarkerSymbol;

	private Geometry editedGeometry;
	
	private SimpleFillSymbol storageFillSymbol;
	
	SKETCHMODE mode;
	MapView map;
	ArrayList<Point> pointList;
}

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

package com.esri.android.sample;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.advanced.Message;
import com.esri.core.symbol.advanced.MessageGroupLayer;
import com.esri.core.symbol.advanced.MessageHelper;
import com.esri.core.symbol.advanced.MessageProcessor;
import com.esri.core.symbol.advanced.SymbolDictionary.DictionaryType;

/**
 * ##################################################################### NOTE:
 * For this Advanced Symbology sample you need a Symbol Dictionary resource
 * (.dat file) and default Message Type templates (.json files) on your device.
 * More information can be found here:
 * http://developers.arcgis.com/en/android/guide/advanced-symbology.htm
 * #####################################################################
 * 
 * This sample illustrates the use of MessageProcessor to process Advanced
 * Symbology features whose definitions are stored locally on the device. The
 * MessageProcessor class provides the capability to take a message received
 * from an external source and convert this into a symbol which is displayed on
 * a map in a graphics layer. The message processor class requires a group layer
 * which has been added to an initialized map. The MessageProcessor constructor
 * also requires a DictionaryType which indicates the type of symbol dictionary
 * being used. The Message class encapsulates information from an incoming or
 * outgoing message. Apart from a message ID all other properties in the message
 * are held as name-value pairs.
 * 
 **/

public class AdvancedSymbologySampleActivity extends Activity {

	MapView mMapView;
	MessageGroupLayer messageGroupLayer;
	MessageProcessor processor;
	ArcGISTiledMapServiceLayer tBaseMap;
	private int symbolCount = 10;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// get map from xml
		mMapView = (MapView) findViewById(R.id.map);
		// create world top map basemap
		tBaseMap = new ArcGISTiledMapServiceLayer(
				"http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");
		// add basemap layer
		mMapView.addLayer(tBaseMap);
		// enable map to pan around date line
		mMapView.enableWrapAround(true);
		// attribute map
		mMapView.setEsriLogoVisible(true);

		// Create a new MessageGroupLayer class based on the
		// m2525c dictionary type
		// NOTE: assumes you have mil2525c.dat installed in
		// default directory of
		// /{device-externalstoragepath}/ArcGIS/SymbolDictionary
		try {
			// create a new MessageGroupLayer for MIL 2525C symbol type
			messageGroupLayer = new MessageGroupLayer(DictionaryType.MIL2525C);
			// Add layer to the map
			mMapView.addLayer(messageGroupLayer);
			// Get the message processor from the GroupLayer
			processor = messageGroupLayer.getMessageProcessor();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
			private static final long serialVersionUID = 1L;

			// Once map is loaded add points
			@Override
			public void onStatusChanged(Object source, STATUS status) {
				if (source == mMapView && status == STATUS.INITIALIZED) {

					// Center the map and set its scale
					Point center = new Point(-567329.8, 4878732);
					mMapView.centerAt(center, false);
					mMapView.setScale(720000.0);

					// add some symbols to the map
					addPoints();

				}
			}
		});

		// Setup long press listener
		mMapView.setOnLongPressListener(new OnLongPressListener() {

			private static final long serialVersionUID = 1L;

			public boolean onLongPress(float x, float y) {
				Layer[] layers = messageGroupLayer.getLayers();
				for (Layer layer : layers) {
					if (layer instanceof GraphicsLayer) {
						GraphicsLayer glayer = (GraphicsLayer) layer;
						int[] graphics = glayer.getGraphicIDs(x, y, 10);
						if (graphics != null && graphics.length > 0) {
							Log.d("Test", "Graphic is found");
							Graphic gr = glayer.getGraphic(graphics[0]);
							Geometry geom = gr.getGeometry();
							Point targetcontrolpt = null;
							if (geom instanceof Point) {
								Point pt = (Point) geom;
								Point scrnpt = mMapView.toScreenPoint(pt);
								scrnpt = new Point(scrnpt.getX() + 50, scrnpt
										.getY() - 50);
								targetcontrolpt = mMapView.toMapPoint(scrnpt);
								Log.d("Test",
										"x: " + pt.getX() + "; y: " + pt.getY());
							}
							Message message = processor.createMessageFrom(gr);
							String controlPoints = (String) message
									.getProperty(MessageHelper.MESSAGE_2525C_CONTROL_POINTS_PROPERTY_NAME);
							Log.i("Test", "control point:" + controlPoints);
							if (targetcontrolpt != null) {
								message.setProperty(
										MessageHelper.MESSAGE_2525C_CONTROL_POINTS_PROPERTY_NAME,
										targetcontrolpt.getX() + ","
												+ targetcontrolpt.getY());
							}
							message.setProperty("_Action", "update");
							processor.processMessage(message);
						}
					}
				}
				return true;
			}
		});
	}

	/*
	 * Adds two messages, one using the MessageHelper and one doing it from
	 * scratch
	 */
	private void addTwoMessages(double x1, double y1, double x2, double y2) {

		// ##### Create message using message helper

		// create a list of control points for input into message
		Point point1 = new Point(x1, x2);
		ArrayList<Point> controlPoints = new ArrayList<Point>(1);
		controlPoints.add(point1);
		// create the Map of properties
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put("sic", "SFGPEVC--------");
		properties.put("uniquedesignation", "Tex");

		Message msg1 = MessageHelper.create2525CUpdateMessage(UUID.randomUUID()
				.toString(), "position_report", controlPoints, properties);
		// The "position_report" string matches one of the default message types
		// You can specify your own message types as .json files and place these
		// into the /ArcGIS/SymbolDictionary/MessageTypes/ folder , each message
		// type will be placed in its own layer within the message group layer
		// automatically for more info see:
		// http://developers.arcgis.com/en/android/guide/advanced-symbology.htm

		// process message and add point
		processor.processMessage(msg1);

		// ##### create multipoint message from scratch

		Message msg2 = new Message();
		msg2.setID(UUID.randomUUID().toString());
		msg2.setProperty("_Type", "position_report");
		msg2.setProperty("_Action", "update");
		String controlpoints = getMultiPointString(x2, y2);
		msg2.setProperty("_Control_Points", controlpoints);
		msg2.setProperty("sic", "GFMPNC----****X");
		msg2.setProperty("_WKID", "3857"); // same as map, but can use any
											// reference here and will be
											// re-projected
		msg2.setProperty("uniquedesignation", "Mad dog");

		// process message and add point
		processor.processMessage(msg2);

	}

	/*
	 * calculates some points from the field of view, then calls
	 * addTwoMessages() (the important method)
	 */
	private void addPoints() {

		Polygon extent = mMapView.getExtent();
		Envelope env = new Envelope();
		extent.queryEnvelope(env);

		// create coordinates in the field of view
		double resolution = mMapView.getResolution();
		double middley = env.getCenterY();
		double leftx = env.getLowerLeft().getX();
		double strideX = resolution * (mMapView.getWidth() / symbolCount);
		double strideXOffset = resolution
				* (mMapView.getWidth() / (symbolCount * 2));
		double strideYOffset = resolution * (mMapView.getHeight() / 16);
		double strideY8 = resolution * (mMapView.getHeight() / 8);

		double y1 = middley - strideYOffset;
		double y5 = middley + strideYOffset + strideY8 * 3;

		// Iterate through the symbols to process points on map
		for (int i = 0; i < symbolCount; i++) {
			double x1 = leftx + strideXOffset + strideX * i;

			// call to add messages using these coordinates
			addTwoMessages(x1, y1, x1, y5);

		}
	}

	/*
	 * getMultiPointString helper method
	 */
	private String getMultiPointString(double centerPointX, double centerPointY) {

		double resolution = mMapView.getResolution();

		double cornerXOffset = resolution
				* (mMapView.getWidth() / (symbolCount * 4));
		double cornerYoffset = resolution * (mMapView.getHeight() / 32);

		double leftx = centerPointX - cornerXOffset;
		double rightx = centerPointX + cornerXOffset;
		double topy = centerPointY + cornerYoffset;
		double bottomy = centerPointY - cornerYoffset;
		StringBuilder builder = new StringBuilder();
		builder.append(leftx).append(",").append(topy).append(";");
		builder.append(rightx).append(",").append(topy).append(";");
		builder.append(rightx).append(",").append(bottomy).append(";");
		builder.append(leftx).append(",").append(bottomy);

		return builder.toString();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
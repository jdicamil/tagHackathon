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

package com.esri.arcgis.android.samples.maprotation;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnPinchListener;
import com.esri.android.map.event.OnSingleTapListener;



/**
 * The sample application shows how to get the angle of rotation from the map.
 * A view with a compass has been added and will rotate with the map.
 * To go back to the original state, single tap on the map.
 * 
 *
 */
public class MapRotation extends Activity {

	MapView map = null;
	public static String TAG = "TAG";
	Compass compass;
	int m_progress;
	ImageButton test;
	

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		map = (MapView) findViewById(R.id.map);
	
		ArcGISTiledMapServiceLayer url=new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
		map.addLayer(url);
		
		map.setAllowRotationByPinch(true);
		
		compass = new Compass(this, null);
		map.addView(compass);
		test = (ImageButton)findViewById(R.id.test);
		
		
		
		
		
		
		
		
		//here we reset the map to its original state
		map.setOnSingleTapListener(new OnSingleTapListener() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public void onSingleTap(float x, float y) {
				
				map.setRotationAngle(0);
				compass.setRotationAngle(map.getRotationAngle());				
				compass.postInvalidate();
				
				
			}
		});
		
	
		
		/**
		 * Once the user has rotated the map, get the angle of rotation from the map.
		 * Pass it to the compass view and rotate this view with the same angle.
		 * The angle of rotation is in degrees.
		 */
		map.setOnPinchListener(new OnPinchListener() {
	
			private static final long serialVersionUID = 1L;

			@Override
			public void prePointersUp(float arg0, float arg1, float arg2, float arg3,
					double arg4) {
				
				map.getRotationAngle();				
				compass.setRotationAngle(map.getRotationAngle());					
				compass.postInvalidate();
				
			}
			
			@Override
			public void prePointersMove(float arg0, float arg1, float arg2, float arg3,
					double arg4) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void prePointersDown(float arg0, float arg1, float arg2, float arg3,
					double arg4) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void postPointersUp(float arg0, float arg1, float arg2, float arg3,
					double arg4) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void postPointersMove(float arg0, float arg1, float arg2,
					float arg3, double arg4) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void postPointersDown(float arg0, float arg1, float arg2,
					float arg3, double arg4) {
				// TODO Auto-generated method stub
				
			}
		});
	}		
		
		
	
	
}


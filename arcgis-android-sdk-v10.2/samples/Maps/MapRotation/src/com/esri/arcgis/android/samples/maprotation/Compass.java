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


package com.esri.arcgis.android.samples.maprotation;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;



/**
 * This class is the view that contains the compass and rotates
 * with the map once the pinch action on the map is done.
 */
public class Compass extends View {

	float m_angle = 0;
	Paint paint;
	Context m_Context;
	ImageButton north ;
	Bitmap bitmap ;
	
	
	public Compass(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		m_Context = context;
		north = new ImageButton(m_Context);
		
	}
	
	
	
	/**
	 * 
	 * @param angle - map.getRotationAngle() in degrees
	 */
	public void setRotationAngle(double angle)
	{
		this.m_angle = (float)angle;
	}

	
	/**
	 * Create the bitmap from the resource.
	 * Give the angle of rotation to the matrix.
	 * Set the center of rotation as the center of the bitmap.
	 * 
	 */
	@Override
	protected void onDraw(Canvas canvas) {		
		
		bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.compass);
		
		Matrix matrix = new Matrix();		
			 
		matrix.postRotate(-this.m_angle,bitmap.getHeight()/2,bitmap.getWidth()/2);	
		
		canvas.drawBitmap(bitmap,matrix, paint);
		super.onDraw(canvas);
		
		
		
		
		}

	
}

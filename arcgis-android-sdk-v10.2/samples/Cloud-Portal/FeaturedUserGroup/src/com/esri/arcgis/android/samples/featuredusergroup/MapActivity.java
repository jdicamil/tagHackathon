package com.esri.arcgis.android.samples.featuredusergroup;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.map.CallbackListener;
import com.esri.core.portal.WebMap;


/**
 * 
 * In this activity the map is loaded. First we get the item that was selected
 * by the user. Since the list of items is available we pass it the position of
 * the item selected
 * 
 * Using this item, a webmap is created added to the mapview. Each time a new
 * item is selected, this intent is called and the mapview is refreshed.
 * 
 */
public class MapActivity extends Activity {
	MapView map;
	// private String TAG = "TAG";
	LinearLayout emptyMapViewGroup;
	String appId = "AssFrEsFVlvF7XSZ7MyIwFX0BZOkcUof2Qly0BtXtml70r1D7NiK8_4KGh5qIxf7";
	private String TAG = "TAG";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// get the position of the item in the itemlist
		String itemId = getIntent().getExtras().getString("itemid");
		Log.i(TAG, "itemid = " + getIntent().getExtras().getString("itemid"));
		Log.i(TAG, "itemid = " + itemId);

		// create a new instance of the webmap from item
		// the webmap will be created in the callback
		WebMap.newInstance(itemId, FeaturedGroupListActivity.myPortal,
				new CallbackListener<WebMap>() {

					@Override
					public void onError(Throwable e) {

						e.printStackTrace();
					}

					@Override
					public void onCallback(final WebMap webmap) {
						
						// Add the mapview in the ui thread.
						MapActivity.this.runOnUiThread(new Runnable() {

							@Override
							public void run() {

								if (webmap != null){
									map = new MapView(MapActivity.this, webmap,
											appId, null);
									
									map.setOnStatusChangedListener(new OnStatusChangedListener() {
		
										private static final long serialVersionUID = 1L;

										@Override
										public void onStatusChanged(Object source, STATUS status) {
											if(status.getValue() == EsriStatusException.INIT_FAILED_WEBMAP_UNSUPPORTED_LAYER)
											{
												
												Toast.makeText(MapActivity.this,
														"Webmap failed to load",
														Toast.LENGTH_SHORT).show();
											}
											
										}
									});
									setContentView(map);
								} 

							}
						});

					}
				});

	}
}

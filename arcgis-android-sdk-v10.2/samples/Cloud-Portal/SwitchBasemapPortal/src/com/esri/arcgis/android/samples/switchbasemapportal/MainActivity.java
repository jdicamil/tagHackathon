package com.esri.arcgis.android.samples.switchbasemapportal;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Polygon;
import com.esri.core.portal.BaseMap;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalGroup;
import com.esri.core.portal.PortalInfo;
import com.esri.core.portal.PortalItem;
import com.esri.core.portal.PortalItemType;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryParams.PortalQuerySortOrder;
import com.esri.core.portal.PortalQueryResultSet;
import com.esri.core.portal.WebMap;


public class MainActivity extends Activity {
	
	MapView mMapView;
	BaseMap baseMap = null;
	WebMap webMap = null;
	// create a portal object with null credentials
	Portal portal;
	// create portal query parameters
	List<PortalItem> itemResults;
	String appId = "AldHFwpgVaIuaMmVR56F-_t2YMnmXZmykn4MwYu5mpkNBNCS7-H5h7aJyjpw7Xyc";
	
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // allow networking on UI thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        try {
        	mMapView = createWebMap();
			setContentView(mMapView);
			List<PortalGroup> basemapGroups = fetchBasemapGroup(portal);
			itemResults = setParams(basemapGroups);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		mMapView.setOnSingleTapListener(new OnSingleTapListener() {
			
			private static final long serialVersionUID = 1L;

			public void onSingleTap(float arg0, float arg1) {
				Polygon extent = mMapView.getExtent();
				try {
					if(itemResults != null && itemResults.size() > 0){
						mMapView = switchBaseMap(itemResults, portal, webMap, extent);
						setContentView(mMapView);
					}else{
						Log.d("Test", "query result empty");
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});   


    }

    public MapView createWebMap() throws Exception{
    	String itemId = "6e03e8c26aad4b9c92a87c1063ddb0e3";
    	// create a new instance of portal
    	portal = new Portal("http://www.arcgis.com", null);
		webMap = WebMap.newInstance(itemId, portal);
		mMapView = new MapView(this, webMap, appId, null);
		return mMapView;
    }
    
    public List<PortalGroup> fetchBasemapGroup(Portal portal) throws Exception{
    	// get the information provided by portal
		PortalInfo portalInfo = portal.fetchPortalInfo();
		// get query to determine which basemap gallery group should be used in client
		String basemapGalleryGroupQuery = portalInfo.getBasemapGalleryGroupQuery();
		// create a PortalQueryParams from the basemap query
		PortalQueryParams portalQueryParams = new PortalQueryParams(basemapGalleryGroupQuery);
		// allow public search for basemaps
		portalQueryParams.setCanSearchPublic(true);
		// find groups for basemaps
		PortalQueryResultSet<PortalGroup> results = portal.findGroups(portalQueryParams);
		// get the basemap results
		List<PortalGroup> groupResults = results.getResults();
		// return the results
		return groupResults;
    }
    
    public List<PortalItem> setParams(List<PortalGroup> portalGroup) throws Exception{
    	List<PortalItem> queryResults = null;
    	
		if(portalGroup != null && portalGroup.size() > 0){
			PortalQueryParams queryParams = new PortalQueryParams();
			queryParams.setCanSearchPublic(true);
			queryParams.setLimit(15);
			String groupID = portalGroup.get(0).getGroupId();
			queryParams.setQuery(PortalItemType.WEBMAP, groupID, null);
			queryParams.setSortField("name").setSortOrder(PortalQuerySortOrder.ASCENDING);
			PortalQueryResultSet<PortalItem> queryResultSet = portal.findItems(queryParams);
			queryResults = queryResultSet.getResults();
		}else{
			Log.d("Test", "portal group empty");
		}
    	
    	return queryResults;
    }
    
    public MapView switchBaseMap(List<PortalItem> portalItem, Portal portal, WebMap mWebMap, Geometry extent) throws Exception{
    	String basemapID = portalItem.get(0).getItemId();
    	Log.d("Test", "basemap id = " + basemapID);
    	// create a new Webmap
    	WebMap uWebMap = WebMap.newInstance(basemapID, portal);
    	// create MapView with new basemap
    	mMapView = new MapView(MainActivity.this, mWebMap, uWebMap.getBaseMap(), null, null);
    	// extent not being honored
    	mMapView.setExtent(extent);

    	// return MapView
    	return mMapView;
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
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

package com.esri.android.tutorial.cljf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.FeatureSet;

/**
 * The local map activity for CreateLocalJsonFeatures app.  
 * The MapView uses a local tile package (*.tpk) as a 
 * baselayer and sets it's extent from OnlineMapActivity.
 * It renders features from local json file persisted on disk
 * and created by OnlineMapActivity. 
 * 
 * @author EsriAndroidTeam
 * @version 2.0
 *
 */

public class LocalMapActivity extends Activity {

  TextView pathLabel;
  
  private static File demoDataFile;
  private static String offlineDataSDCardDirName;
  private static String filename;

  MapView mMapView;
  ArcGISLocalTiledLayer baseLayer;
  ArcGISFeatureLayer windTurbinesFeatureLayer;
  FeatureSet windTurbineFeatureSet;
  Polygon extent;
  
  protected static String windTurbineLayerDefinition;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_localmap);
    
    demoDataFile = Environment.getExternalStorageDirectory();
    offlineDataSDCardDirName = this.getResources().getString(R.string.config_data_sdcard_offline_dir);
    filename = this.getResources().getString(R.string.config_local_basemap);
    String basemap = demoDataFile + File.separator + offlineDataSDCardDirName + File.separator + filename;
    String basemapurl = "file://" + basemap;
    
    mMapView = (MapView) findViewById(R.id.localMap);
    baseLayer = new ArcGISLocalTiledLayer(basemapurl);

    // Get the json path from the intent
    Intent intent = getIntent();
    String jsonPath = intent.getStringExtra(OnlineMapActivity.EXTRA_JSON_PATH);
    // Get current extent from intent
    Bundle extras = intent.getExtras();
    extent = (Polygon)extras.getSerializable(OnlineMapActivity.CURRENT_MAP_EXTENT);
    
    // Display the path in the TextView 
    pathLabel = (TextView) findViewById(R.id.pathLable);
    pathLabel.setText(jsonPath);

    // Create wind turbine featurelayer from json
    windTurbineFeatureSet = createWindTurbinesFeatureSet(jsonPath);
    windTurbineLayerDefinition = this.getResources().getString(R.string.config_windturbine_layer_definition);
    ArcGISFeatureLayer.Options layerOptions = new ArcGISFeatureLayer.Options();
    layerOptions.mode = ArcGISFeatureLayer.MODE.SNAPSHOT;
    windTurbinesFeatureLayer = new ArcGISFeatureLayer(windTurbineLayerDefinition, windTurbineFeatureSet, layerOptions);
    
    // add layers to map
    mMapView.addLayer(baseLayer);
    mMapView.addLayer(windTurbinesFeatureLayer);
    
    mMapView.setOnStatusChangedListener(new OnStatusChangedListener(){
      private static final long serialVersionUID = 1L;

      @Override
      public void onStatusChanged(Object source, STATUS status) {
          if(source == mMapView && status == STATUS.INITIALIZED){
            mMapView.setExtent(extent);
          }
      }
    });
    
  }

  public FeatureSet createWindTurbinesFeatureSet(String path) {
    FeatureSet fs = null;
    
    try {
      JsonFactory factory = new JsonFactory();
      JsonParser parser = factory.createJsonParser(new FileInputStream(path));
      parser.nextToken();
      fs = FeatureSet.fromJson(parser);
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return fs;
  } 

}

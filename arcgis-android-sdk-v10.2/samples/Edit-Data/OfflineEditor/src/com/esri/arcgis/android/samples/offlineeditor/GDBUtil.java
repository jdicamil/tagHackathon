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
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.arcgis.android.samples.offlineeditor.OfflineEditorActivity;
import com.esri.core.ags.FeatureServiceInfo;
import com.esri.core.gdb.GdbFeatureTable;
import com.esri.core.gdb.Geodatabase;
import com.esri.core.map.CallbackListener;
import com.esri.core.tasks.gdb.GenerateGeodatabaseParameters;
import com.esri.core.tasks.gdb.GeodatabaseStatusInfo;
import com.esri.core.tasks.gdb.GeodatabaseStatusCallback;
import com.esri.core.tasks.gdb.GeodatabaseTask;
import com.esri.core.tasks.gdb.SyncGeodatabaseParameters;
import com.esri.core.tasks.gdb.SyncModel;
import com.esri.core.tasks.tilecache.GenerateTileCacheParameters;
import com.esri.core.tasks.tilecache.GenerateTileCacheParameters.ExportBy;
import com.esri.core.tasks.tilecache.TileCacheStatus;
import com.esri.core.tasks.tilecache.TileCacheTask;

public class GDBUtil {
	static final String DEFAULT_FEATURE_SERVICE_URL = "http://services.arcgis.com/P3ePLMYs2RVChkJx/arcgis/rest/services/Wildfire/FeatureServer";
	static final String DEFAULT_GDB_PATH = "/ArcGIS/samples/OfflineEditor/DamageAssess.geodatabase";
	static final String DEFAULT_BASEMAP_FILENAME = "/ArcGIS/samples/OfflineEditor/SanFrancisco.tpk";
	static final String DEFAULT_LAYERIDS = "0";
	static final String DEFAULT_RETURN_ATTACHMENTS = "false";
	static final String DEFAULT_SYNC_MODEL = "perLayer";
	protected static final String TAG = "GDBUtil";
	private static GeodatabaseTask gdbTask;
	private static String fsUrl = DEFAULT_FEATURE_SERVICE_URL;
	private static String gdbFileName = Environment
			.getExternalStorageDirectory().getPath() + DEFAULT_GDB_PATH;
	private static String basemapFileName = Environment
			.getExternalStorageDirectory().getPath() + DEFAULT_BASEMAP_FILENAME;
	private static int[] layerIds = { 0 };
	private static boolean returnAttachments = false;
	private static SyncModel syncModel = SyncModel.REPLICA;

	public static void loadPreferences(final OfflineEditorActivity activity) {

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(activity);
		setFsUrl(settings.getString("fsurl", DEFAULT_FEATURE_SERVICE_URL));		
		setGdbPath(Environment.getExternalStorageDirectory().getPath()
				+ settings.getString("gdbfilename", DEFAULT_GDB_PATH));		
		setBasemapFileName(Environment.getExternalStorageDirectory().getPath()
				+ settings.getString("tpkfilename", DEFAULT_BASEMAP_FILENAME));
		setLayerIds(settings.getString("layerIds", DEFAULT_LAYERIDS));
		setReturnAttachments(settings.getString("returnAttachments",
				DEFAULT_RETURN_ATTACHMENTS));
		setSyncModel(settings.getString("syncModel", DEFAULT_SYNC_MODEL));
	}

	// request and download basemap and geodatabase from the server
	public static void downloadData(final OfflineEditorActivity activity) {
		Log.i(TAG, "downloadData");
		showProgress(activity, true);
		final MapView mapView = activity.getMapView();
		downloadGeodatabase(activity, mapView);
	}

	public static void downloadBasemapData(final OfflineEditorActivity activity) {
		Log.i(TAG, "downloadBasemapData");
		showProgress(activity, true);
		final MapView mapView = activity.getMapView();
		downloadBasemap(activity, mapView);

	}

	private static void downloadBasemap(final OfflineEditorActivity activity,
			final MapView mapView) {
		double[] levels = { 4622324.434309, 2311162.217155, 1155581.108577 };

		CallbackListener<TileCacheStatus> callback = new CallbackListener<TileCacheStatus>() {

			@Override
			public void onCallback(TileCacheStatus objs) {
				showMessage(activity, "TileRequest accepted");
				Log.i(TAG, "TileRequest accepted");
			}

			@Override
			public void onError(Throwable e) {
				Log.e(TAG, "onError in callback: " + e.getMessage());
				showMessage(activity, e.getMessage());
				showProgress(activity, false);
			}
		};

		CallbackListener<String> downloadCallback = new CallbackListener<String>() {

			@Override
			public void onCallback(String objs) {
				showMessage(activity, "Local Tile Package Download Complete");
				Log.i(TAG, "Local Tile Package Download Complete");
				showProgress(activity, false);
				mapView.removeLayer(0);
				ArcGISLocalTiledLayer localTiledLayer = new ArcGISLocalTiledLayer(
						objs);
				mapView.addLayer(localTiledLayer, 0);
			}

			@Override
			public void onError(Throwable e) {
				Log.e(TAG, "onError in downloadCallback: " + e.getMessage());
				showMessage(activity, e.getMessage());
				showProgress(activity, false);
			}
		};

		TileCacheTask tileCacheTask = new TileCacheTask(mapView.getLayer(0)
				.getUrl(), null);

		GenerateTileCacheParameters tileCacheParams = new GenerateTileCacheParameters(
				true, levels, ExportBy.ID, mapView.getExtent(),
				mapView.getSpatialReference());
		tileCacheTask.submitTileCacheJobAndDownload(tileCacheParams, callback,
				downloadCallback, Environment.getExternalStorageDirectory().getAbsolutePath()+"/ArcGIS");

	}

	private static void downloadGeodatabase(
			final OfflineEditorActivity activity, final MapView mapView) {

		gdbTask = new GeodatabaseTask(fsUrl, null,
				new CallbackListener<FeatureServiceInfo>() {

					@Override
					public void onError(Throwable e) {
						// TODO Auto-generated method stub
						Log.e(TAG, "", e);
						showMessage(activity, e.getMessage());
						showProgress(activity, false);
					}

					@Override
					public void onCallback(FeatureServiceInfo objs) {
						// TODO Auto-generated method stub
						if (objs.isSyncEnabled()) {
							requestGdbInOneMethod(gdbTask, activity, mapView);
						}
					}
				});
	}

	/**
	 * 'All-in-one' method.
	 */
	private static void requestGdbInOneMethod(GeodatabaseTask gdbTask,
			final OfflineEditorActivity activity, final MapView mapView) {

		GenerateGeodatabaseParameters params = new GenerateGeodatabaseParameters(
				GDBUtil.layerIds, mapView.getExtent(),
				mapView.getSpatialReference(), GDBUtil.returnAttachments,
				GDBUtil.syncModel, mapView.getSpatialReference());

		showProgress(activity, true);

		// gdb complete callback
		CallbackListener<Geodatabase> gdbResponseCallback = new CallbackListener<Geodatabase>() {

			@Override
			public void onCallback(Geodatabase obj) {
				// update UI
				showMessage(activity, "Geodatabase downloaded!");
				Log.i(TAG, "geodatabase is: " + obj.getPath());

				showProgress(activity, false);

				// remove all the feature layers from map and add a feature
				// layer from the downloaded geodatabase
				for (Layer layer : mapView.getLayers()) {
					if (layer instanceof ArcGISFeatureLayer)
						mapView.removeLayer(layer);
				}
				for (GdbFeatureTable gdbFeatureTable : obj.getGdbTables()) {
					if (gdbFeatureTable.hasGeometry())
						mapView.addLayer(new FeatureLayer(gdbFeatureTable));
				}
				activity.setTemplatePicker(null);
			}

			@Override
			public void onError(Throwable e) {
				Log.e(TAG, "", e);
				showMessage(activity, e.getMessage());
				showProgress(activity, false);
			}

		};

		GeodatabaseStatusCallback statusCallback = new GeodatabaseStatusCallback() {

			@Override
			public void statusUpdated(GeodatabaseStatusInfo status) {
				// TODO Auto-generated method stub
				showMessage(activity, status.getStatus().toString());
			}
		};

		// single method does it all!
		gdbTask.submitGenerateGeodatabaseJobAndDownload(params, gdbFileName,
				statusCallback, gdbResponseCallback);
		showMessage(activity, "Submitting gdb job...");
	}

	// upload and synchronize local geodatabase to the server
	static void synchronize(final OfflineEditorActivity activity) {
		showProgress(activity, true);

		gdbTask = new GeodatabaseTask(fsUrl, null,
				new CallbackListener<FeatureServiceInfo>() {

					@Override
					public void onError(Throwable e) {
						// TODO Auto-generated method stub
						Log.e(TAG, "", e);
						showMessage(activity, e.getMessage());
						showProgress(activity, false);
					}

					@Override
					public void onCallback(FeatureServiceInfo objs) {
						// TODO Auto-generated method stub
						if (objs.isSyncEnabled()) {
							doSyncAllInOne(activity);
						}
					}
				});
	}

	/**
	 * All-in-one method used...
	 * 
	 * @throws Exception
	 */
	private static void doSyncAllInOne(final OfflineEditorActivity activity) {

		try {
			// create local geodatabase
			Geodatabase gdb = new Geodatabase(gdbFileName);

			// get sync parameters from geodatabase
			final SyncGeodatabaseParameters syncParams = gdb
					.getSyncParameters();

			CallbackListener<Geodatabase> syncResponseCallback = new CallbackListener<Geodatabase>() {

				@Override
				public void onCallback(Geodatabase objs) {
					showMessage(activity, "Sync Completed");
					showProgress(activity, false);
					Log.e(TAG, "Geodatabase: " + objs.getPath());
				}

				@Override
				public void onError(Throwable e) {
					Log.e(TAG, "", e);
					showMessage(activity, e.getMessage());
					showProgress(activity, false);
				}

			};
			GeodatabaseStatusCallback statusCallback = new GeodatabaseStatusCallback() {

				@Override
				public void statusUpdated(GeodatabaseStatusInfo status) {
					// TODO Auto-generated method stub
					showMessage(activity, status.getStatus().toString());
				}
			};

			// start sync...
			gdbTask.submitSyncJobAndApplyResults(syncParams, gdb,
					statusCallback, syncResponseCallback);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setPreferences() {

	}

	public static String getGdbPath() {
		return gdbFileName;
	}

	public static void setGdbPath(String gdbPath) {
		GDBUtil.gdbFileName = gdbPath;
	}

	public static boolean isBasemapLocal() {
		File file = new File(basemapFileName);
		return file.exists();
	}

	public static boolean isGeoDatabaseLocal() {
		File file = new File(gdbFileName);
		return file.exists();
	}

	static void showProgress(final OfflineEditorActivity activity,
			final boolean b) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				activity.setProgressBarIndeterminateVisibility(b);
			}
		});
	}

	static void showMessage(final OfflineEditorActivity activity,
			final String message) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public static String getFsUrl() {
		return fsUrl;
	}

	public static void setFsUrl(String fsUrl) {
		GDBUtil.fsUrl = fsUrl;
	}

	public static String getBasemapFileName() {
		return basemapFileName;
	}

	public static void setBasemapFileName(String basemapFileName) {
		GDBUtil.basemapFileName = basemapFileName;
	}

	public static void setLayerIds(String layerIds) {
		// TODO Auto-generated method stub
		String[] strArray = layerIds.split(",");
		int[] intArray = new int[strArray.length];
		for (int i = 0; i < strArray.length; i++) {
			intArray[i] = Integer.parseInt(strArray[i]);
		}
		GDBUtil.layerIds = intArray;
	}

	public static int[] getLayerIds() {
		return GDBUtil.layerIds;
	}

	public static void setReturnAttachments(String returnAttachment) {
		// TODO Auto-generated method stub
		if (returnAttachment.equalsIgnoreCase("true")) {
			GDBUtil.returnAttachments = true;
		} else {
			GDBUtil.returnAttachments = false;
		}
	}

	public static void setSyncModel(String syncModel) {
		// TODO Auto-generated method stub
		if (syncModel.equalsIgnoreCase("perLayer")) {
			GDBUtil.syncModel = SyncModel.LAYER;
		} else {
			GDBUtil.syncModel = SyncModel.REPLICA;
		}
	}

}

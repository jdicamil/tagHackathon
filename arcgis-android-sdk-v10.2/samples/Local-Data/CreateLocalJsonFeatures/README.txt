==========================
ArcGIS Runtime SDK for Android Sample
CreateLocalJsonFeatures 
build date 2012-08-20
==========================

This sample depends on your Eclipse environment being setup to work with the ArcGIS Runtime SDK for Android.  Please refer to the installation docs located at http://resources.arcgis.com/en/help/android-sdk/concepts/index.html#//01190000002m000000 if you need assistance.

===============
Deploy Basemap Data
===============
The CreateLocalJsonFeatures sample is an example of working with data in an offline setting.  The sample depends on basemap data to be located on the device. This includes installing a local tile map cache (tpk) to device as described below:

NOTE: <storage> is used to represent your device external storage location. 
More information about this location at https://developer.android.com/guide/topics/data/data-storage.html#filesExternal.  

1. Download Basemap data from http://www.arcgis.com/home/item.html?id=4497b7bb42e543b691027840d1b9092a.
2. Create the the sample data folder at the root <storage> folder on your device, /<storage>/ArcGIS/samples/cljf/OfflineData.  
3. Push the downloaded basemap from step 1, ImageryTPK.tpk to your device.
    /<storage>/ArcGIS/samples/cljf/OfflineData/ImageryTPK.tpk
    
This sample will create json file in the directory set up above which represents the features selected in the app for offline usage.  

===============
How to use the Sample
===============
This sample has 2 map activities, one which accesses services from ArcGIS Online and another which uses local data.  The local data is a basemap deployed above and a json file representing features selected in the online map activity.  

Import into Eclipse
1. In Eclipse, select File > Import to open the import dialog. 
2. Under General select Import Existing Projects into Workspace. 
3. Browse to the location of the project on disk and ensure that the project is selected and click Finish. 
4. The project will be recognized as an ArcGIS Android Project and the ArcGIS for Android classpath library will be added. You need to use the SDK feature to add the native libs. 
5. Right click on the project in the package explorer and select ArcGIS Tools > Fix Project Properties.

Your project is now a valid ArcGIS Android Project.

App usage
1. Pan the map to center on the wind turbine features. 
2. Zoom into the a subsection of the wind turbine features you would like to persist locally to your device. 
3. Click the Start Sketch button.
4. Single tap the map to create a polygon over the features you want to persist.  
5. Once your polygon is created, click the Save sketch to save the geometry representation of the area you want to select.
6. Click the Submit button to query the underlying features and create a local json feature set.  A progess dialog will take focus.
7. Once the dialog is dismissed, click the Send button to switch activities and see your json feature set in the local map activity.  

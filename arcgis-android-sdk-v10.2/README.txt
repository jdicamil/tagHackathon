Welcome to the ArcGIS Runtime SDK for Android!

The ArcGIS Runtime SDK for Android provides you with the API libraries and developer tools required to build apps for ArcGIS Android.  

Previous releases of the ArcGIS Runtime SDK for Android have been in the form of an Eclipse plugin exclusively.  New at 10.2 we have expanded the SDK to include the API libraries and developer tools required to build apps for ArcGIS Android. 

SDK Contents
The ArcGIS Runtime SDK for Android contains everything you need to develop ArcGIS Android apps. 
The contents of the SDK are provided below:
 
 + docs > API reference doc for arcgis android and arcgis android app framework api's
 + libs > API jar libraries for arcgis-android and arcgis-android-app-framework, arcgis-android core native libaries, and third party dependency jar libraries.  
+ samples > Sample projects in Eclipse project format
+ tools > Eclipse plugin

Installing the Eclipse Plugin
Developing ArcGIS applications for Android is simplified by a group of Eclipse plugins provided with the SDK, ArcGIS for Android Core and ArcGIS for Android Doc and Samples. The ArcGIS for Android Eclipse plugins provide a rich set of tools, documentation, and samples to help developers create applications using the ArcGIS API for Android.

Add ArcGIS Android Plugin repository
The ArcGIS Runtime SDK for Android includes a local copy of the Eclipse plugin.  We also provide a public Ecilpse updatesite which hosts the Eclipse plugin.  Choose either step 1 or 2 depending on whether you install via local plugin or public updatesite URL then continue with step 3 to completion:  

1. Download the ArcGIS Android Eclipse plugin

  1. Start Eclipse, then select Help > Install New Software
  2. Click Add, in the top right corner
  3. In the Add Repository dialog that appears, enter 'ArcGIS Android Public updatesite' for the Name and the following URL for the Location: http://developers.arcgis.com/en/android/eclipse/updatesite/
  4. Click OK.


2. Install the local ArcGIS Android Eclipse plugin

  1. Start Eclipse, then select Help > Install New Software
  2. Click Add, in the top right corner
  3. In the Add Repository dialog that appears, enter 'ArcGIS Android Local updatesite' for the Name and click on the Archive button.
  4. Navigate to the plugin jar file location in <arcgis-install-dir>/tools/eclipse-plugin/arcgis-android-eclipse-plugin.jar.  
  5. Click OK.

3. In the Available Software dialog, select the checkbox next to 'ArcGIS for Android' and click Next.
4. In the next window, you'll see a list fo tools to be downloaded.  Click Next. 
5. Read and accept the license agreement, then click Finish.  
6. When your installation completes, restart Eclipse.

Integrating with IntelliJ IDE
The ArcGIS Runtime SDK for Android allows easier integration into other developer environments without the support of a developer IDE plugin.  Please refer to <arcgis-install-dir>/tools/INTELLJ.README file for recommended instructions for setting up ArcGIS Runtime SDK for Android wtih IntelliJ IDE. 




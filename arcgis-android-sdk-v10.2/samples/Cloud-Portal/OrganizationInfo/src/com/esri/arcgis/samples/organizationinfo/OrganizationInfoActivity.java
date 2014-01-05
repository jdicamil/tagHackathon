package com.esri.arcgis.samples.organizationinfo;

import java.io.ByteArrayInputStream;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.esri.core.geometry.Envelope;
import com.esri.core.io.UserCredentials;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalGroup;
import com.esri.core.portal.PortalInfo;

/**
 * This samples shows how to use the PortalInfo to get information about the
 * portal and organization.
 * 
 * 1) First step create a portal using the constructor that may/maynot have
 * credentials. 2) Using the portal returned in the callback, get the portalInfo
 * object 3) Now using the portalInfo object get the organization information
 * using methods provided on the portal info. 4) In order to get the
 * portal/organization thumbnail the callback should be used.
 * 
 * An important aspect is that since the callback is its own thread separate
 * from the UI thread, while updating the UI with the information from the
 * portal, the UI thread must be used.
 */
public class OrganizationInfoActivity extends Activity {

	private final String url = "http://arcgis.com";

	private final String username = "democsf";

	private final String password = "devdemo";

	protected String TAG = "OrganizationInfoActivity";

	private TextView orgNameTxtView;

	private ImageView organizationThumbnail;

	private TextView descriptionTxtView, id;
	private PortalInfo portalInfo = null;
	ProgressDialog dialog;

	ListView list;

	public static Portal myPortal;
	protected List<PortalGroup> groupsFromPortal;

	UserCredentials credentials;

	private TextView extentTxtView;

	private TextView isSharedTxtView;

	private TextView thumbnailNameTxtView;

	private TextView basemapURLTxtView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.portalorganization);
		organizationThumbnail = (ImageView) findViewById(R.id.orgImage);
		id = (TextView) findViewById(R.id.org_id);
		descriptionTxtView = (TextView) findViewById(R.id.orgdescription);
		orgNameTxtView = (TextView) findViewById(R.id.orgNameText);
		extentTxtView = (TextView) findViewById(R.id.extent);
		isSharedTxtView = (TextView) findViewById(R.id.isshared);
		thumbnailNameTxtView = (TextView) findViewById(R.id.thumbnailName);
		basemapURLTxtView = (TextView) findViewById(R.id.basemapURL);
		new OrgTask().execute();

	}// oncreate

	/**
	 * This class provides an Async way to fetch information from the server.
	 * The ui is populated within the UI thread.
	 */
	private class OrgTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// Create UserCredentials with login and password, anonymous login
			// is also possible
			UserCredentials credentials = new UserCredentials();
			credentials.setUserAccount(username, password);

			// Create the portal, the callback will return with a valid portal
			// object
			// It is possible to create a portal without user credentials,
			// however
			// once the user credential object is created both login and
			// password
			// must be populated.
			final Portal portal = new Portal(url, credentials);

			try {
				portalInfo = portal.fetchPortalInfo();

				if (portalInfo.getOrganizationName() != null) {
					byte[] data = portalInfo.fetchOrganizationThumbnail();

					ByteArrayInputStream bytes = new ByteArrayInputStream(data);
					final BitmapDrawable bmd = new BitmapDrawable(bytes);
					if (bmd != null) {
						OrganizationInfoActivity.this
								.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										descriptionTxtView.setText(portalInfo
												.getOrganizationDescription());
										id.setText(portalInfo
												.getOrganizationId());
										orgNameTxtView.setText(portalInfo
												.getOrganizationName());
										isSharedTxtView.setText(String
												.valueOf(portalInfo
														.isCanSharePublic()));

										Envelope e = new Envelope();
										portalInfo.getDefaultExtent()
												.getGeometry().queryEnvelope(e);
										extentTxtView.setText(e.getXMin() + " "
												+ e.getYMin() + " "
												+ e.getXMax() + " "
												+ e.getYMax());

										basemapURLTxtView
												.setText(portalInfo
														.getDefaultBaseMap()
														.getTitle());
										organizationThumbnail
												.setImageBitmap(bmd.getBitmap());

										try {
											thumbnailNameTxtView
													.setText(portal
															.fetchPortalInfo()
															.getOrganizationThumbnailFileName());
										} catch (Exception e1) {
											e1.printStackTrace();
										}

									}
								});
					}

				}// if
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return null;
		}

	}
}

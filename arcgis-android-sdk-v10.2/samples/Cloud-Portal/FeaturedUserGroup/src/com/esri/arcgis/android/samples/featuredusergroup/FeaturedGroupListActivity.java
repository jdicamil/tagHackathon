package com.esri.arcgis.android.samples.featuredusergroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.esri.core.io.UserCredentials;
import com.esri.core.portal.Portal;
import com.esri.core.portal.PortalGroup;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryResultSet;

/**
 * The sample essentially loads the featured groups from the portal 1) Create
 * the portal with credentials and portal url
 * 
 * 2) Fetch the featured group query list from portalInfo
 * 
 * 3) Loop through the list of queries and retrieve each group
 * 
 * 4) For each group fetch the title and also thumbnail
 * 
 * 5) In order to add the thumbnail to the list adapter, use the uithread
 * provided in the activity class
 * 
 * 6) Update the adapter using notifydatasetchanged method, each time the
 * portalg group is returned
 * 
 */
public class FeaturedGroupListActivity extends Activity {
	protected static final String TAG = "TAG";
	ListView list;
	MyAdapter adapter;
	HashMap<String, Bitmap> portalGroupImage;
	private String url;
	ArrayList<MyGroup> portalData;
	public static Portal myPortal;
	protected List<PortalGroup> groupsFromPortal;
	protected static final int CLOSE_LOADING_WINDOW = 0;
	private ProgressDialog dialog;
	UserCredentials credentials;
	MyGroup mygroup;
	
	
	final Handler uihandler = new Handler() {

		public void handleMessage(Message msg) {
			switch ((msg.what)) {
			case CLOSE_LOADING_WINDOW:

				if (dialog != null)
					dialog.dismiss();
				break;

			default:
				break;
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		findViewById(R.id.lyt_btn_container).setVisibility(View.GONE);
		list = (ListView) findViewById(R.id.list_view);
		portalData = new ArrayList<FeaturedGroupListActivity.MyGroup>();
		adapter = new MyAdapter(portalData);
		groupsFromPortal = new ArrayList<PortalGroup>();
		list.setAdapter(adapter);
		dialog = ProgressDialog.show(this,
		 "Featured Groups from arcgis.com ",
		 "Getting groups from portal ........");
		new GroupTask().execute();

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {
				Intent intent = new Intent(FeaturedGroupListActivity.this,
						ItemListGeneral.class);
				intent.putExtra("portalgroup", portalData.get(position).group
						.getGroupId());
				startActivity(intent);

			}

		});

		// set up portal
		
	}// oncreate

	public class GroupTask extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params) {
			try {
				fetchPortal();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}
		
	}
	public void fetchPortal() throws Exception {
		url = "https://arcgis.com/";
		credentials = new UserCredentials();
		credentials.setUserAccount("democsf", "devdemo");

		Portal portal = new Portal(url, credentials);
		// create a new instance of portal

		myPortal = portal;

		// get the list of all the queries for getting all the featured groups
		// from the portal
		List<String> querys = portal.fetchPortalInfo()
				.getFeaturedGroupsQueries();

		// loop through query list to find each featured group in your portal
		for (String query : querys) {
			Log.d(TAG, "[query in callback ]" + query);
			PortalQueryResultSet<PortalGroup> result = portal
					.findGroups(new PortalQueryParams(query));
			for (final PortalGroup group : result.getResults()) {
				byte[] data = group.fetchThumbnail();
				Log.d(TAG, "[query in callback ] group title " + group.getTitle());
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
						data.length);
				ImageView image = new ImageView(FeaturedGroupListActivity.this);
				image.setImageBitmap(bitmap);
				mygroup = new MyGroup(group, bitmap);
				portalData.add(mygroup);
				FeaturedGroupListActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {

						
						adapter.notifyDataSetChanged();
						Log.d(TAG, "[uithread ]");

					}
				});
				uihandler.sendEmptyMessage(CLOSE_LOADING_WINDOW);
			}
		}

	}

	/**
	 * This class is the listadapter It contains the thumbnail and title of a
	 * group The adapter is called each time the callback returns with
	 * additional group or thumbnail information.
	 */
	public class MyAdapter extends BaseAdapter {
		 
		List<MyGroup> groups;

		public MyAdapter(ArrayList<MyGroup> groupinfo) {
			groups = groupinfo;
			

		}

		
		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

		@Override
		public int getCount() {

			return groups.size();
		}

		@Override
		public Object getItem(int position) {

			return groups.get(position);
		}

		@Override
		public long getItemId(int arg0) {

			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.groupitemwithimage,
						null);
			}

			ImageView image = (ImageView) convertView
					.findViewById(R.id.listimageView);
			image.setImageBitmap(groups.get(position).groupThumbnail);

			TextView text = (TextView) convertView
					.findViewById(R.id.listtextView);
			text.setText(groups.get(position).group.getTitle());
			return convertView;
		}

	}
	
	private class MyGroup{
		private PortalGroup group;
		private Bitmap groupThumbnail;
		
		public MyGroup(PortalGroup pg, Bitmap bt)
		{
			this.group = pg;
			this.groupThumbnail = bt;
		}
	}
}

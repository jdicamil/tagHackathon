package com.esri.arcgis.android.samples.featuredusergroup;

import java.util.ArrayList;
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

import com.esri.core.portal.PortalItem;
import com.esri.core.portal.PortalItemType;
import com.esri.core.portal.PortalQueryParams;
import com.esri.core.portal.PortalQueryResultSet;

/**
 * The portal group id passed via the intent to this class Using the id, we get
 * the items in that group Once we have the item, we get the title and thumbnail
 * for the item and display it in a list view.
 * 
 * The title and thumbnail are stored in a arraylist of MyItem class. This class
 * contains the thumbnail and title.
 * 
 * The entire process of fetching data from the server is executed via an async
 * task. For updating UI a uithread is launched inside the async task.
 * 
 */
public class ItemListGeneral extends Activity {
	protected static final int CLOSE_LOADING_WINDOW = 0;
	private String TAG = "TAG";
	ListView list;
	MyAdapter adapter;
	String sort;
	String username, password;
	private ProgressDialog dialog;
	String groupId;

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
	protected List<String> itemURL;
	private ArrayList<MyItem> itemDataList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		findViewById(R.id.lyt_btn_container).setVisibility(View.GONE);

		dialog = ProgressDialog.show(this, "Items",
				"Loading item list ........");
		// setup listview and add the adapter to it
		list = (ListView) findViewById(R.id.list_view);
		itemURL = new ArrayList<String>();
		itemDataList = new ArrayList<ItemListGeneral.MyItem>();
		adapter = new MyAdapter(itemDataList);
		list.setAdapter(adapter);

		// get the group id from the activity
		// use this group id to get the items in that group
		groupId = (String) getIntent().getExtras().get("portalgroup");

		// execute async task
		new ItemTask().execute();

		/**
		 * When the user selects one of the items presented in the list another
		 * activity is created and the position of the item selected is passed
		 * to it.
		 * 
		 * The list of items retrieved is a public static variable that can be
		 * accessed from the next activity.
		 * 
		 */
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {

				Intent intent = new Intent(ItemListGeneral.this,
						MapActivity.class);
				intent.putExtra("itemid",
						itemDataList.get(position).item.getItemId());
				Log.i(TAG, "[item]position = "
						+ itemDataList.get(position).item.getItemId());
				startActivity(intent);

			}
		});

	}// oncreate

	public class ItemTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {

				// Set up the query with the groupId and select only those items
				// that are web maps
				PortalQueryParams queryParams = new PortalQueryParams();
				queryParams.setQuery(PortalItemType.WEBMAP, groupId, null);
				
				PortalQueryResultSet<PortalItem> results = FeaturedGroupListActivity.myPortal
						.findItems(queryParams);
				for (PortalItem item : results.getResults()) {
					byte[] data = item.fetchThumbnail();
					if (data != null) {
						Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
								data.length);
						MyItem portalItemData = new MyItem(item, bitmap);
						Log.i(TAG, "Item id = " + item.getTitle());
						itemDataList.add(portalItemData);
					}

				}
				ItemListGeneral.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						adapter.notifyDataSetChanged();

					}
				});
				uihandler.sendEmptyMessage(CLOSE_LOADING_WINDOW);

			} catch (Exception e1) {
				e1.printStackTrace();
				uihandler.sendEmptyMessage(CLOSE_LOADING_WINDOW);
			}
			return null;
		}

	}

	/**
	 * 
	 * List adapter constructor takes a list of MyItem class objects
	 * 
	 */

	public class MyAdapter extends BaseAdapter {

		List<MyItem> items;

		public MyAdapter(ArrayList<MyItem> portalitems) {
			this.items = portalitems;

		}

		@Override
		public void notifyDataSetChanged() {
			super.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {

			return items.get(position);
		}

		@Override
		public long getItemId(int arg0) {

			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.itemwithimage, null);
			}

			ImageView image = (ImageView) convertView
					.findViewById(R.id.listimageView);
			image.setImageBitmap(items.get((position)).itemThumbnail);

			TextView text = (TextView) convertView
					.findViewById(R.id.listtextView);
			text.setText(items.get((position)).item.getTitle());
			return convertView;
		}

	}

	/**
	 * This class is used to store the portalgroup and its corresponding
	 * thumbnail
	 */
	private class MyItem {
		private PortalItem item;
		private Bitmap itemThumbnail;

		public MyItem(PortalItem item, Bitmap bt) {
			this.item = item;
			this.itemThumbnail = bt;
		}
	}
}

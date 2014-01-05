package com.att.m2x.testapp;

import java.util.Locale;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

import com.att.m2x.helpers.DateHelper;
import com.att.m2x.*;

public class FeedDetailActivity extends Activity {

	public static final String INPUT_SELECTED_FEED = "com.att.m2x.Feed.SelectedFeed";
	
	private Feed feed;
	private TextView name;
	private TextView description;
	private TextView visibility;
	private TextView status;
	private TextView type;
	private TextView created;
	private TextView updated;	
	private TextView location;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_feed_detail);

		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			feed = bundle.getParcelable(INPUT_SELECTED_FEED);
			
			name = (TextView) findViewById(R.id.name);
			description = (TextView) findViewById(R.id.description);
			visibility = (TextView) findViewById(R.id.visibility);
			status = (TextView) findViewById(R.id.status);
			type = (TextView) findViewById(R.id.type);
			created = (TextView) findViewById(R.id.created);
			updated = (TextView) findViewById(R.id.updated);
			location = (TextView) findViewById(R.id.location);
			
			name.setText(feed.getName());
			description.setText(feed.getDescription());
			visibility.setText(feed.getVisibility());
			status.setText(feed.getStatus());
			type.setText(feed.getType());
			created.setText(DateHelper.dateToString(feed.getCreated()));
			updated.setText(DateHelper.dateToString(feed.getUpdated()));

			Location loc = feed.getLocation();
			location.setText((loc != null) ? String.format(Locale.US, "%.5f, %.5f", loc.getLatitude(), loc.getLongitude()) : "Unknown");

		} else {
			finish();			
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.feed_detail, menu);
		return true;
	}
	
}

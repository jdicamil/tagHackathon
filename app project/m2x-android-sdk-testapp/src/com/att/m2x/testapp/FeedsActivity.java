package com.att.m2x.testapp;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.att.m2x.*;

public class FeedsActivity extends Activity implements OnItemClickListener {

	private ListView feedList;
	private FeedArrayAdapter adapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);         

        feedList = (ListView) findViewById(R.id.feedView);
    	feedList.setOnItemClickListener(this); 

        M2X.getInstance().setMasterKey(getString(R.string.m2x_master_key));
        this.loadFeeds();
                
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private void loadFeeds() {
    	
    	Feed.getFeeds(this, null, new Feed.FeedsListener() {
			
			@Override
			public void onSuccess(ArrayList<Feed> feeds) {
		    	adapter = new FeedArrayAdapter(FeedsActivity.this, feeds);
		    	feedList.setAdapter(adapter);    	
			}
			
			@Override
			public void onError(String errorMessage) {
				Log.d("m2x-test", "Failed to load feeds! " + errorMessage);
			}
		});
    	
    }
    
    private class FeedArrayAdapter extends ArrayAdapter<Feed> {
    	
    	private Context context;
    	private static final int LAYOUT_RESOURCE = R.layout.activity_main_listitem;
    	private List<Feed> objects;
    	
    	public FeedArrayAdapter(Context context, List<Feed> objects) {
    		super(context, LAYOUT_RESOURCE, objects);
    		this.objects = objects;
    		this.context = context;
    	}
    	
    	public List<Feed> getObjects() {
    		return objects;
    	}
    	
    	public View getView(int position, View convertView, ViewGroup parent) {
    		View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(LAYOUT_RESOURCE, null);
            }
            
            Feed feed = getItem(position);
            if (feed != null) {
                TextView title = (TextView) view.findViewById(R.id.title);
                title.setText(feed.getName());

                TextView subtitle = (TextView) view.findViewById(R.id.subtitle);
                subtitle.setText(feed.getDescription());
                
                TextView symbol = (TextView) view.findViewById(R.id.symbol);
                String type = feed.getType();
                
                if (type.equals("datasource")) {
                	symbol.setText(R.string.datasource_symbol);
                	symbol.setBackgroundResource(R.color.datasource_symbol_background);
                	
                } else if (type.equals("batch")) {
                	symbol.setText(R.string.batch_symbol);
                	symbol.setBackgroundResource(R.color.batch_symbol_background);
                	
                } else if (type.equals("blueprint")) {
                	symbol.setText(R.string.blueprint_symbol);
                	symbol.setBackgroundResource(R.color.blueprint_symbol_background);
                }
                
             }
            
            return view;
    	}
   }

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		
		  Intent feedDetail = new Intent(this, FeedDetailActivity.class);		
		  List<Feed> objects = adapter.getObjects();
		  Feed selectedFeed = objects.get(position);
		  feedDetail.putExtra(FeedDetailActivity.INPUT_SELECTED_FEED, selectedFeed);
		  startActivity(feedDetail);		
	}
    
}

package com.att.m2x.testapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Player extends Activity implements OnClickListener {

	TextView whosIt, Points;
	Button Maps, Stats;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		whosIt = (TextView) findViewById(R.id.tvWhosIt);
		Points = (TextView) findViewById(R.id.tvPoints);
		Maps = (Button) findViewById(R.id.bMaps);
		Stats = (Button) findViewById(R.id.bStats);
		Maps.setOnClickListener(this);
		Stats.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.player, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.bMaps:
			Intent i = new Intent(getApplicationContext(), Maps.class);
			startActivity(i);
			break;
		case R.id.bStats:
			Intent i2 = new Intent(getApplicationContext(), Stats.class);
			startActivity(i2);
			break;
		}
	}

}

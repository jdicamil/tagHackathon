package com.att.m2x.testapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Tts extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tts);
		Button speakButton = (Button)findViewById(R.id.speak);
		speakButton.setOnClickListener(this);
		
	}
	
	public void onClick(View v) {
		//handle user clicks here
		EditText enteredText = (EditText)findViewById(R.id.enter);
		String words = enteredText.getText().toString();
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tts, menu);
		return true;
	}

}

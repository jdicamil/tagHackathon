package com.att.m2x.testapp;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.att.m2x.Feed;
import com.att.m2x.M2X;
import com.att.m2x.Stream;
import com.att.m2x.StreamValue;

public class Player extends Activity implements OnClickListener {
	
	public final static String TAG = "Player";

	TextView whosIt, Points;
	Button Maps, Stats;
	private BluetoothAdapter mBluetoothAdapter;
	private Feed myFeed;
	private Stream tagStream;

	private BluetoothChatService mChatService;
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		
        M2X.getInstance().setMasterKey(getString(R.string.m2x_master_key));
        this.loadFeeds();

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	}
    	private void loadFeeds() {

    		Feed.getFeed(this, null, "1809ad358b8d17350bc8d95bb8d5b519",new Feed.FeedListener() {
				public void onSuccess(Feed feed) {
					myFeed = feed;
		    		Stream.getStream(Player.this, null, myFeed.getId(), "tags",new Stream.StreamListener() {
    					private Stream tagStream;

    					public void onSuccess(Stream stream) {
    						tagStream = stream;
    					}

    					public void onError(String errorMessage) {
    					}
    				});
					
				}

				public void onError(String errorMessage) {
Log.e(TAG,"loadFeed:"+errorMessage);
				}
			});

    		
    }
    
        @Override
        public void onStart() {
            super.onStart();
                if (mChatService == null) {
                	setupChat();
                }
        }

        @Override
        public synchronized void onResume() {
            super.onResume();

            // Performing this check in onResume() covers the case in which BT was
            // not enabled during onStart(), so we were paused to enable it...
            // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
            if (mChatService != null) {
                // Only if the state is STATE_NONE, do we know that we haven't started already
                if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                  // Start the Bluetooth chat services
                  mChatService.start();
                }
            }
        }

        private void setupChat() {
            Log.d(TAG, "setupChat()");

            // Initialize the array adapter for the conversation thread
//            mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
//            mConversationView = (ListView) findViewById(R.id.in);
//            mConversationView.setAdapter(mConversationArrayAdapter);

            // Initialize the compose field with a listener for the return key
//            mOutEditText = (EditText) findViewById(R.id.edit_text_out);
//            mOutEditText.setOnEditorActionListener(mWriteListener);

            // Initialize the send button with a listener that for click events
//            mSendButton = (Button) findViewById(R.id.button_send);

            // Initialize the BluetoothChatService to perform bluetooth connections
            mChatService = new BluetoothChatService(this, mHandler);
        }

        @Override
        public synchronized void onPause() {
            super.onPause();
            if(D) Log.e(TAG, "- ON PAUSE -");
        }

        @Override
        public void onStop() {
            super.onStop();
            if(D) Log.e(TAG, "-- ON STOP --");
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            // Stop the Bluetooth chat services
            if (mChatService != null) mChatService.stop();
            if(D) Log.e(TAG, "--- ON DESTROY ---");
        }

        // The Handler that gets information back from the BluetoothChatService
        private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                    case BluetoothChatService.STATE_CONNECTED:

                        break;
                    case BluetoothChatService.STATE_CONNECTING:
//                        setStatus(R.string.title_connecting);
                        break;
                    case BluetoothChatService.STATE_LISTEN:
                    case BluetoothChatService.STATE_NONE:
//                        setStatus(R.string.title_not_connected);
                        break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                   
                    Log.e("DEBUG>>>>>>",readMessage);

                    sendToM2X(readMessage);
                    
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
//                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
//                    Toast.makeText(getApplicationContext(), "Connected to "
 //                                  + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                                   Toast.LENGTH_SHORT).show();
                    break;
                }
            }

        };

		private void sendToM2X(String readMessage) {
			
			ArrayList<StreamValue> readings = new ArrayList<StreamValue>();
			StreamValue value = new StreamValue();
		    value.setValue(1.0);
		    readings.add(value);
		    tagStream.setValues(this, null, myFeed.getId(), readings, new Stream.BasicListener() {
		        public void onSuccess() {
		        	Log.e(TAG,"yes!");
		        }

		        public void onError(String errorMessage) {
		        	Log.e(TAG,"send to M2X:"+errorMessage);
		        }
		    });
			
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

package com.example.accesspoints;
import java.util.Collections;
import java.util.List;

import android.R.bool;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	private Button mConnect;
	private Switch mSwitch;
	private TextView mText, mCurrent;
	private WifiManager mWifiManager;
	private AccessPointComparer mComparer;
	private WifiInfo mInfo;
	private boolean getAllPoints = true;
	
	private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context c, Intent intent) {
	        if (intent.getAction() == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
	        	List<ScanResult> mScanResults = mWifiManager.getScanResults();
	        	Collections.sort(mScanResults, mComparer);
	        	int currentResult = 0;
	            for(ScanResult result : mScanResults){
	            	if(currentResult == 10){
	            		break;
	            	}
	            	if(!getAllPoints){
	            		if(result.SSID.equals("WLAN-00")){
	            			currentResult++;
		            		mText.append(result.SSID + ": " + result.BSSID + ", " + result.level+"\n");
		            	}
		            	
	            	}
	            	else{
	            		if(result.SSID.equals("")){
		            		mText.append("Hidden SSID Name: " + result.BSSID + ", " + result.level+"\n");
		            	}
		            	else{
		            		mText.append(result.SSID + ": " + result.BSSID + ", " + result.level+"\n");
		            	}
		            	currentResult++;
	            	}
	            	
	            }
	        }
	    }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mConnect = (Button) findViewById(R.id.button);
		mSwitch = (Switch) findViewById(R.id.switchButton);
		mText = (TextView) findViewById(R.id.text);
		mCurrent = (TextView) findViewById(R.id.top);
		mText.setMovementMethod(new ScrollingMovementMethod());
		mComparer = new AccessPointComparer();
		mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		mInfo = mWifiManager.getConnectionInfo();
		mCurrent.setText("Current Connection: " + mInfo.getSSID() + " and " + mInfo.getBSSID());
		
		mConnect.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mText.setText("");
				mInfo = mWifiManager.getConnectionInfo();
				mCurrent.setText("Current Connection: " + mInfo.getSSID() + " and " + mInfo.getBSSID());
				//start new context to get connection info?
				Log.d("JLK", mWifiManager.toString());
				mWifiManager.startScan();
				registerReceiver(mWifiScanReceiver, new IntentFilter(
					      WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			}
		});
		
		mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		            // The toggle is enabled
		        	getAllPoints = false;
		        } else {
		            // The toggle is disabled
		        	getAllPoints = true;
		        }
		    }
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}

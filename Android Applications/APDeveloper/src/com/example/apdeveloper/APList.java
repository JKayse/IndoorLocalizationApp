package com.example.apdeveloper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.apdeveloper.AccessPointComparer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class APList extends ActionBarActivity {

	private Button mSave;
	private ExpandableListView mList;
	private Context mContext;
	private ConnectionTestsAdapter mAdapter;
	private ArrayList<ConnectionTest> mConnections;
	private float mOriginalImageOffsetX = 0;
	private float mOriginalImageOffsetY = 0;
	private float mCurrentDegree = 0;
	private TextView mDirection;
	private TextView mCoordinates;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		mConnections = MainActivity.mConnections;
		mOriginalImageOffsetX = MainActivity.mOriginalImageOffsetX;
		mOriginalImageOffsetY = MainActivity.mOriginalImageOffsetY;
		mCurrentDegree = MainActivity.mCurrentDegree;
		mDirection = (TextView) findViewById(R.id.finalDirection);
		String currentDirection="";
		if((mCurrentDegree <= 0 && mCurrentDegree >=-45) || (mCurrentDegree <= -315 && mCurrentDegree >=-360)){
			currentDirection = "North";
		}
		else if(mCurrentDegree <= -45 && mCurrentDegree >=-135){
			currentDirection = "East";
		}
		else if(mCurrentDegree <= -135 && mCurrentDegree >=-225){
			currentDirection = "South";
		}
		else if(mCurrentDegree <= -225 && mCurrentDegree >=-315){
			currentDirection = "West";
		}
        mDirection.setText("Current Direction: " + currentDirection);
		mCoordinates = (TextView) findViewById(R.id.finalCoordinates);
		mCoordinates.setText("Current location: (" + mOriginalImageOffsetX + "," + mOriginalImageOffsetY + ")");
		mSave = (Button) findViewById(R.id.saveButton);
		mContext = this;
		mList = (ExpandableListView) findViewById(R.id.scrollConnections);
		if(mConnections!= null){
			mAdapter = new ConnectionTestsAdapter(mContext, mConnections);
			mList.setAdapter(mAdapter);
		}
		
		mSave.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String currentDirection="";
				if((mCurrentDegree <= 0 && mCurrentDegree >=-45) || (mCurrentDegree <= -315 && mCurrentDegree >=-360)){
					currentDirection = "N";
				}
				else if(mCurrentDegree <= -45 && mCurrentDegree >=-135){
					currentDirection = "E";
				}
				else if(mCurrentDegree <= -135 && mCurrentDegree >=-225){
					currentDirection = "S";
				}
				else if(mCurrentDegree <= -225 && mCurrentDegree >=-315){
					currentDirection = "W";
				}			
							   
			   	JSONArray testsArray = new JSONArray();
			   	for(int i=0; i< mConnections.size(); ++i) {
			   	    JSONArray testResults = new JSONArray();
			   	    ArrayList<APStrength> listAP = mConnections.get(i).getAPs();
			   	    for(int j = 0; j < listAP.size(); j++){
			   	    	JSONObject accessPointPair = new JSONObject();
			   	    	try{
			   	    		accessPointPair.put("AccessPoint", listAP.get(j).getName());
			   	    		accessPointPair.put("SignalStrength", listAP.get(j).getStrength());
			   	    		testResults.put(accessPointPair);
			   	    	}
			   	    	catch (JSONException e) {
				   		    // TODO Auto-generated catch block
				   		    e.printStackTrace();
				   		}	
			   	    }
			   	    testsArray.put(testResults);
			   	}	
			   	
			   	JSONObject scanResult = new JSONObject();
		   	    try {
		   	    	JSONArray coordinates = new JSONArray();
		   	    	coordinates.put(mOriginalImageOffsetX);
		   	    	coordinates.put(mOriginalImageOffsetY);

		   	    	scanResult.put("Tests", testsArray);
		   	    	scanResult.put("Direction", currentDirection);
		   	    	scanResult.put("Coordinates", coordinates);
					String stringScanResult = scanResult.toString();

					Log.d("JLK", stringScanResult);		
					new scanRequest(mContext).execute(stringScanResult);
					//move to new location
					finish();
					
				} 
		   	    catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}});
	}
		
		
	class scanRequest extends AsyncTask<String, Void, String>{
    	Context context;
        private scanRequest(Context context) {
            this.context = context.getApplicationContext();
        }
    	
		@Override
		protected String doInBackground(String... params) {
			HttpResponse response = null;
			HttpClient client= new DefaultHttpClient();
			HttpPost post = new HttpPost("http://54.187.74.117/AccessPoints/api/index.php/AddScanResults");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("scanResults", params[0]));
			
			try {
				post.setEntity(new UrlEncodedFormEntity(pairs));
			} 
			catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				response = client.execute(post);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
			String responseString="";
			HttpEntity temp = response.getEntity();
			try 
			{
				responseString = EntityUtils.toString(temp);
			} 
			catch (Exception e) {
				e.printStackTrace();
			} 
			
			return responseString;	
		}

		@Override
		protected void onPostExecute(String response) {
			CharSequence text = "";
			Log.d("JLK", response);
			return;
	     }
    }

}


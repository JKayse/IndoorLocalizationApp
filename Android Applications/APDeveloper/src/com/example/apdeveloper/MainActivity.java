package com.example.apdeveloper;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends ActionBarActivity implements SensorEventListener {

	private Button mButton;
	private Context mContext;
	private ImageView mImage;
	private ImageView mPin;
	private TextView mDirection;
	private TextView mCoordinates;
	private Spinner mSpinner;
	private int mCurrentX=0;
	private int mCurrentY=0;
	public static float mOriginalImageOffsetX = 0;
	public static float mOriginalImageOffsetY = 0;
	public static float mCurrentDegree = 0;
	private SensorManager mSensorManager;
	private String mainURL = "http://54.187.74.117/AccessPoints/api/index.php/Vertices";
	private DownloadJson task = new DownloadJson(this);

	private float intrinsicHeight;
    private float intrinsicWidth;
    private float scaledHeight;
    private float scaledWidth;
    private float heightRatio;
    private float widthRatio;
    private float scaledImageOffsetX;
    private float scaledImageOffsetY;
	
	
	private WifiManager mWifiManager;
	private AccessPointComparer mComparer;
	private WifiInfo mInfo;
	public static ArrayList<ConnectionTest> mConnections;
	private int mTestNumber = 0;
	private int mAPNumber = 0;
	//private String[] mAccessPoints = {"00:22:bd:9a:c4:9","00:22:bd:9a:d4:e","00:22:bd:9a:d4:6","00:22:bd:9a:c5:3","00:22:bd:9a:d5:4","00:22:bd:9a:d3:f"};

	private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context c, Intent intent) {
	        if (intent.getAction() == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
	        	List<ScanResult> mScanResults = mWifiManager.getScanResults();
	        	Collections.sort(mScanResults, mComparer);
	        	ArrayList<APStrength> mStrengths = new ArrayList<APStrength>();
	        	mTestNumber++;
	        	Log.d("JLK", Integer.toString(mTestNumber));
	        	ConnectionTest test = new ConnectionTest();
	        	test.setTestName("Test " + Integer.toString(mTestNumber));
	        	int count =0;
	            for(ScanResult result : mScanResults){
	            	if(count <10){
	    				APStrength ap = new APStrength();
	    				ap.setName(result.BSSID);
	    				ap.setStrength(Integer.toString(result.level));
	    				mStrengths.add(ap);
    				}
	            	else{
	            		break;
	            	}
    				count++;
	            }
            	test.setAPs(mStrengths);
	        	mConnections.add(test);
	            if(mTestNumber == 10){
					Intent toList = new Intent(mContext, APList.class);
					startActivity(toList);
	            	
	            }
	            else{
	            	mWifiManager.startScan();
					registerReceiver(mWifiScanReceiver, new IntentFilter(
					      WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	            }
	        }
	    }
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		mContext = this;
		mComparer = new AccessPointComparer();
		mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		mButton = (Button) findViewById(R.id.button);
		mImage = (ImageView) findViewById(R.id.image);
		mPin = (ImageView) findViewById(R.id.arrow);
		mDirection = (TextView) findViewById(R.id.top);
		mCoordinates = (TextView) findViewById(R.id.second);
		mSpinner = (Spinner) findViewById(R.id.spinner);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		task.execute(mainURL);
		
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				String coordinates = mSpinner.getSelectedItem().toString();
				List<String> xyCoordinates = Arrays.asList(coordinates.split(","));
				mOriginalImageOffsetX = Float.parseFloat(xyCoordinates.get(0));
				mOriginalImageOffsetY = Float.parseFloat(xyCoordinates.get(1));
				
				
				
	            scaledImageOffsetX = mOriginalImageOffsetX / widthRatio;
	            scaledImageOffsetY = mOriginalImageOffsetY / heightRatio;
	            int x = (int) (scaledImageOffsetX + mImage.getLeft());
	            int y = (int) (scaledImageOffsetY + mImage.getTop());
	            Log.d("JLK",widthRatio + "," + heightRatio);
	            mCoordinates.setText("Current location: (" + mOriginalImageOffsetX + "," + mOriginalImageOffsetY + ")");
	            mPin.layout(x, y, x+mPin.getMeasuredWidth(), y+mPin.getMeasuredHeight());
	            mPin.setVisibility(View.VISIBLE);
            
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}

		});
		
		mImage.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				mCurrentY = (int)event.getY();
	            mCurrentX = (int)event.getX();

	            Drawable drawable = mImage.getDrawable();
	            Rect imageBounds = drawable.getBounds();

	            //original height and width of the bitmap
	            intrinsicHeight = drawable.getIntrinsicHeight();
	            intrinsicWidth = drawable.getIntrinsicWidth();

	            //height and width of the visible (scaled) image
	            scaledHeight = imageBounds.height();
	            scaledWidth = imageBounds.width();

	            //Find the ratio of the original image to the scaled image
	            //Should normally be equal unless a disproportionate scaling
	            //(e.g. fitXY) is used.
	            heightRatio = intrinsicHeight / scaledHeight;
	            widthRatio = intrinsicWidth / scaledWidth;

	            //do whatever magic to get your touch point
	            //MotionEvent event;

	            int[] locations = new int[2];
	            mImage.getLocationOnScreen(locations);
	            
	            //get the distance from the left and top of the image bounds
	            scaledImageOffsetX = event.getX() - mImage.getLeft();
	            scaledImageOffsetY = event.getY() - mImage.getTop();

	            //scale these distances according to the ratio of your scaling
	            //For example, if the original image is 1.5x the size of the scaled
	            //image, and your offset is (10, 20), your original image offset
	            //values should be (15, 30). 
	            mOriginalImageOffsetX = scaledImageOffsetX * widthRatio;
	            mOriginalImageOffsetY = scaledImageOffsetY * heightRatio;
	            
	            Log.v("JLK",scaledImageOffsetX+","+scaledImageOffsetY+","+mOriginalImageOffsetX+","+mOriginalImageOffsetY+","+mImage.getLeft()+","+mImage.getTop());
	            
	            if(scaledImageOffsetX < 0 || scaledImageOffsetY <0 || scaledImageOffsetX > scaledWidth || scaledImageOffsetY > scaledHeight){
	            	return true;
	            }
	            
	            mCoordinates.setText("Current location: (" + mOriginalImageOffsetX + "," + mOriginalImageOffsetY + ")");
	            mPin.layout(mCurrentX, mCurrentY, mCurrentX+mPin.getMeasuredWidth(), mCurrentY+mPin.getMeasuredHeight());
	            mPin.setVisibility(View.VISIBLE);
	            
	            return true;
			}
		});
		
		mButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mConnections = new ArrayList<ConnectionTest>();
            	mTestNumber = 0;
				mWifiManager.startScan();
				registerReceiver(mWifiScanReceiver, new IntentFilter(
				      WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			}
		});

       

	}
	
	 @Override
	 protected void onResume() {
         super.onResume();
         mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_GAME);
	 }
	 
     @Override
     protected void onPause() {
         super.onPause();
         mSensorManager.unregisterListener(this);
     }



	@Override
	public void onSensorChanged(SensorEvent event) {
		float degree = Math.round(event.values[0]);
        mCurrentDegree = -degree;
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
	}


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public class DownloadJson extends AsyncTask<String, Void, String[]>{

		//The list of variables used for this class.
		HttpURLConnection connection;
		URL requestURL;
		InputStream stream;
		Scanner scanner;
		Context context;
		String jsonresults;

		//Constructor for the subclass to take in the proper context.
		public DownloadJson(Context context){
			this.context = context;	
		}

		protected String[] doInBackground(String... urls){

			String[] verticesArray = null;
			//Opens up a connection.
			try {
				requestURL = new URL(urls[0]);
				connection = (HttpURLConnection) requestURL.openConnection();
				connection.setReadTimeout(10000);
				connection.setConnectTimeout(15000);
				int status;
				status = connection.getResponseCode();

				if(status == HttpURLConnection.HTTP_UNAUTHORIZED){
					Log.d("JLK", "No authorization");
				}
				else if(status != HttpURLConnection.HTTP_OK){
					Log.d("JLK", "Status code: " + status);
				}
				else{
					//Gets all the data from the connection and saves it in a string.
					stream = connection.getInputStream();
					scanner = new Scanner(stream);

					jsonresults = scanner.useDelimiter("\\A").next();
					scanner.close();

					JSONObject json = new JSONObject(jsonresults);
					JSONArray values = json.getJSONArray("Vertices");
					verticesArray = new String[values.length()];
					for(int i = 0; i < values.length(); i++){
						JSONObject vertex = values.getJSONObject(i);
						String x = vertex.getString("X");
						String y = vertex.getString("Y");
						verticesArray[i] = x + "," + y;
					}
				}
			} catch (IOException | JSONException e) {
				Log.e("JLK", "URL is bad");
				e.printStackTrace();
			}

			return verticesArray;
		}

		//Sets the list view once it gets all the information.
		protected void onPostExecute(String[] result){
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, result);
		    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    mSpinner.setAdapter(adapter);
		}

	}
	
}

package com.example.apuser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;


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
	private String mainURL = "http://54.187.74.117/AccessPoints/api/index.php/DataAverage";
	private DownloadJson task = new DownloadJson(this);
	private ArrayList<Fingerprint> mFingerprintsNorth;
	private ArrayList<Fingerprint> mFingerprintsEast;
	private ArrayList<Fingerprint> mFingerprintsSouth;
	private ArrayList<Fingerprint> mFingerprintsWest;
	private Fingerprint mTestFingerprint;
	private String currentDirection;

	private float intrinsicHeight;
    private float intrinsicWidth;
    private float scaledHeight;
    private float scaledWidth;
    private float heightRatio;
    private float widthRatio;
    private float scaledImageOffsetX;
    private float scaledImageOffsetY;
	
    private Bitmap mOriginalBitmap;
	private boolean mScan;
	private String mRoomName;
	private WifiManager mWifiManager;
	private AccessPointComparer mComparer;
	private WifiInfo mInfo;
	private int mTestNumber = 0;
	private int mAPNumber = 0;

	private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context c, Intent intent) {
	        if (intent.getAction() == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
	        	List<ScanResult> mScanResults = mWifiManager.getScanResults();
	        	Collections.sort(mScanResults, mComparer);
	        	HashMap<String, Double> mStrengths = new HashMap<String, Double>();
	        	mTestNumber++;
	        	Log.d("JLK", Integer.toString(mTestNumber));
	        	int count =0;
	            for(ScanResult result : mScanResults){
	            	if(count <10){
	            		//Add to hash map
	            		String name = result.BSSID;
	            		Double strength = (double) result.level;
	            		mStrengths.put(name, strength);
    				}
	            	else{
	            		break;
	            	}
    				count++;
	            }
	            //Add direction
	            Fingerprint test = new Fingerprint(1, -1, -1, mStrengths);
	            Fingerprint resultTest = null;
	            
	            switch(currentDirection)
	    		{
	    			case "North":
	    				resultTest = test.getClosestMatchPenalty(mFingerprintsNorth);
	    			case "East":
	    				resultTest = test.getClosestMatchPenalty(mFingerprintsEast);
	    			case "South":
	    				resultTest = test.getClosestMatchPenalty(mFingerprintsSouth);
	    			case "West":
	    				resultTest = test.getClosestMatchPenalty(mFingerprintsWest);
	    		}
	            
				double x = resultTest.getX();
				double y = resultTest.getY();
				Log.d("JLK", Double.toString(x));
				Log.d("JLK", Double.toString(y));
				mOriginalImageOffsetX = (float) x;
				mOriginalImageOffsetY = (float) y;
				
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
				
	            scaledImageOffsetX = mOriginalImageOffsetX / widthRatio;
	            scaledImageOffsetY = mOriginalImageOffsetY / heightRatio;
	            int xImage = (int) (scaledImageOffsetX + mImage.getLeft());
	            int yImage = (int) (scaledImageOffsetY + mImage.getTop());	            
	            mPin.layout(xImage, yImage, xImage+mPin.getMeasuredWidth(), yImage+mPin.getMeasuredHeight());
	            mPin.setVisibility(View.VISIBLE);
	            try{
		            MainPath mainPath = new MainPath();
		    		List<Room> rooms = Resources.GetRooms();
		    		Room randomRoom = Resources.GetRoomByName(rooms, mRoomName);
		    		Point startingPoint = new Point((double)(int) x, (double)(int) y);	
		    		Line startingLine = mainPath.FindNearestLine(startingPoint, 0);
		    		Point connectingPoint = startingLine.GetPointOnLineCosestToPoint(startingPoint);
		    		Stack<Vector> answer = mainPath.Navigate(connectingPoint, startingLine, randomRoom, new Stack<Vector>());
		    		List<Vector> path = Resources.GetPath(answer, startingPoint, connectingPoint, startingLine);
		    				    		
		    		mImage.setImageBitmap(mOriginalBitmap);
		    		Bitmap myBitmap = ((BitmapDrawable) mImage.getDrawable()).getBitmap();
		    		Paint myPaint = new Paint();
		    		Bitmap tempBitmap = Bitmap.createBitmap(myBitmap.getWidth(), myBitmap.getHeight(), Bitmap.Config.RGB_565);
		    		Canvas tempCanvas = new Canvas(tempBitmap);
		    		tempCanvas.drawBitmap(myBitmap, 0, 0, null);
		    		myPaint.setStrokeWidth(12);
		    		myPaint.setColor(Color.BLUE);
		    		myPaint.setStyle(Paint.Style.STROKE);
		    		Path _path = new Path();
		    		int counter = 0;
		    		_path.moveTo(mOriginalImageOffsetX, mOriginalImageOffsetY);	    		
		    		for( Vector item : path)
		    		{
		    			if(counter==0){
		    				_path.moveTo((float)item.StartPoint.XCoord, (float)item.StartPoint.YCoord);
		    				counter++;
		    			}
		    			Log.d("JLK", item.EndPoint.XCoord + " " + item.EndPoint.YCoord);
			    		_path.lineTo((float)item.EndPoint.XCoord, (float)item.EndPoint.YCoord);
		    		}
		    		tempCanvas.drawPath(_path, myPaint);
		    		mImage.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
		    		if(!mScan){
		    			mImage.setImageBitmap(mOriginalBitmap);
		    		}
	    		}
	            catch (Exception e){
	            	;
	            }
	            
	            if(mScan){
	            	mWifiManager.startScan();
					registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
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
		mOriginalBitmap = ((BitmapDrawable) mImage.getDrawable()).getBitmap();
		mPin = (ImageView) findViewById(R.id.arrow);
		mDirection = (TextView) findViewById(R.id.top);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		task.execute(mainURL);
		
		mButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(!mScan){
					final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
					LayoutInflater inflater = getLayoutInflater();
					View inflateThis = inflater.inflate(R.layout.get_destination,null);
					dialog.setView(inflateThis);
					
					final Spinner destinations = (Spinner) inflateThis.findViewById(R.id.destinations);
					final Button startScan = (Button) inflateThis.findViewById(R.id.startScan);
					final Button cancelScan = (Button) inflateThis.findViewById(R.id.cancelScan);
					final AlertDialog dialogFinal = dialog.create();
					dialogFinal.show();
					
					cancelScan.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							dialogFinal.dismiss();
						}
					});
					
					startScan.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) {
							String destination =destinations.getSelectedItem().toString();
							mRoomName = destination;
							Log.d("JLK", destination);
							dialogFinal.dismiss();
							mButton.setText("Cancel Scan");
							mScan = true;
							mWifiManager.startScan();
							registerReceiver(mWifiScanReceiver, new IntentFilter(
							      WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
						}
					});
				}
				else{
					mButton.setText("Start Scan");
					mImage.setImageBitmap(mOriginalBitmap);
					mScan = false;
				}
				
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

	public class DownloadJson extends AsyncTask<String, Void, ArrayList<Fingerprint>>{

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

		protected ArrayList<Fingerprint> doInBackground(String... urls){
			mFingerprintsNorth = new ArrayList<Fingerprint>();
			mFingerprintsEast = new ArrayList<Fingerprint>();
			mFingerprintsSouth = new ArrayList<Fingerprint>();
			mFingerprintsWest = new ArrayList<Fingerprint>();
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
					for(int i = 0; i < values.length(); i++){
						JSONObject vertex = values.getJSONObject(i);
						double x = vertex.getDouble("X");
						double y = vertex.getDouble("Y");
						JSONObject directions = vertex.getJSONObject("Directions");
						JSONArray north = directions.getJSONArray("North");
						JSONArray east = directions.getJSONArray("East");
						JSONArray south = directions.getJSONArray("South");
						JSONArray west = directions.getJSONArray("West");
						HashMap<String, Double> macIdStrength = new HashMap<String, Double>();
						for(int j = 0; j < north.length(); j++){
							JSONObject ap = north.getJSONObject(j);
							String apName = ap.getString("AccessPoint");
							Double apStrength = ap.getDouble("AverageStrength");
							macIdStrength.put(apName, apStrength);
						}
						Fingerprint print = new Fingerprint(1, x, y, macIdStrength);
						mFingerprintsNorth.add(print);
						
						macIdStrength = new HashMap<String, Double>();
						for(int j = 0; j < east.length(); j++){
							JSONObject ap = east.getJSONObject(j);
							String apName = ap.getString("AccessPoint");
							Double apStrength = ap.getDouble("AverageStrength");
							macIdStrength.put(apName, apStrength);
						}
						print = new Fingerprint(1, x, y, macIdStrength);
						mFingerprintsEast.add(print);
						
						macIdStrength = new HashMap<String, Double>();
						for(int j = 0; j < south.length(); j++){
							JSONObject ap = south.getJSONObject(j);
							String apName = ap.getString("AccessPoint");
							Double apStrength = ap.getDouble("AverageStrength");
							macIdStrength.put(apName, apStrength);
						}
						print = new Fingerprint(1, x, y, macIdStrength);
						mFingerprintsSouth.add(print);
						
						macIdStrength = new HashMap<String, Double>();
						for(int j = 0; j < west.length(); j++){
							JSONObject ap = west.getJSONObject(j);
							String apName = ap.getString("AccessPoint");
							Double apStrength = ap.getDouble("AverageStrength");
							macIdStrength.put(apName, apStrength);
						}
						print = new Fingerprint(1, x, y, macIdStrength);
						mFingerprintsWest.add(print);
						
					}
				}
			} catch (IOException | JSONException e) {
				Log.e("JLK", "URL is bad");
				e.printStackTrace();
			}

			return mFingerprintsNorth;
		}

		protected void onPostExecute(ArrayList<Fingerprint> result){
			return;
		}

	}
	
}
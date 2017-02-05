package com.bignerdranch.android.runtracker.fragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.activity.RunMapActivity;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.LocationDataCursor;
import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.domain.Run;
import com.bignerdranch.android.runtracker.loader.LastLocationDataLoader;
import com.bignerdranch.android.runtracker.loader.LocationDataListLoader;
import com.bignerdranch.android.runtracker.loader.RunLoader;
import com.bignerdranch.android.runtracker.manager.RunManager;
import com.bignerdranch.android.runtracker.receiver.LocationReceiver;

public class RunFragment extends Fragment {
	
	private static final String TAG = "RunFragment";

	private static final String ARG_RUN_ID = "RUN_ID";
	
	private static final int LOADER_LOAD_RUN = 1;

	private static final int LOADER_LOAD_LOCATION_DATA_LIST = 2;
	
    private Button mStartButton, mStopButton,mButtonShowMap;
    private TextView mTVCurrentRunStatus,mStartedTextView, mLatitudeTextView, 
        mLongitudeTextView,mAccuracyTextView, mAltitudeTextView, mDurationTextView
        ,mTotalMetreTextView;
    
    private RunManager mRunManager;
    
    private Run mRun;
    
    private boolean isTrackingCurrentRun;
    
    private LocationData mLastLocationData;

	private LocationDataCursor mLocationDataCursor;
    
    private LocationReceiver locationReceiver = new LocationReceiver(){
    	
    	protected void onLocationReceived(Context context, Location location) {
    		Log.i(TAG, "onLocationReceived");
    		//如果没有记录当前的旅程，则不要显示地理信息
    		if(!isTrackingCurrentRun){
    			return;
    		}
    		mLastLocationData = LocationData.parseLocation(location);
    		if(RunFragment.this.isVisible()){
        		updateButtonUI();
        		updateUI();
    		}
    	};
    	
    	protected void onProviderEnabledChange(boolean providerEnabled) {
    		Log.i(TAG, "onProviderEnabledChange");
    		int toastTextId = providerEnabled ? R.string.gps_enabled:R.string.gps_disabled;
    		Activity activity = getActivity();
    		if(activity != null){
    			Toast.makeText(activity, toastTextId, Toast.LENGTH_SHORT).show();
    		}
    	};
    };    

	private LoaderCallbacks<Cursor> mLocationDataListLoaderCallbacks = 
			new LoaderCallbacks<Cursor>() {

				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args) {
					
					long runId = args.getLong(ARG_RUN_ID, -1);
					return new LocationDataListLoader(getActivity(), runId);
				}

				@Override
				public void onLoadFinished(Loader<Cursor> loader,
						Cursor cursor) {
					mLocationDataCursor = (LocationDataCursor) cursor;
					
					if(mLocationDataCursor.moveToLast()){
						mLastLocationData = mLocationDataCursor.getLocationData();
					}else{
						mLastLocationData = null;
					}
					updateButtonUI();
					updateUI();
					
					showTotalTripMetre();
				}

				@Override
				public void onLoaderReset(Loader<Cursor> loader) {
					mLocationDataCursor.close();
					mLocationDataCursor = null;
				}
			};
    
    private LoaderCallbacks<Run> mRunLoaderCallbacks = 
    		new LoaderCallbacks<Run>() {

				@Override
				public Loader<Run> onCreateLoader(int id, Bundle args) {
					
					long runId = args.getLong(ARG_RUN_ID, -1);
					return new RunLoader(getActivity(), runId);
				}

				@Override
				public void onLoadFinished(Loader<Run> loader, Run run) {

					mRun = run;
					checkIsTrackingCurrentRun();
					updateButtonUI();
					updateUI();
				}

				@Override
				public void onLoaderReset(Loader<Run> loader) {

					//do nothing
				}
			};
				
			
	public static RunFragment newInstance(long runId){
		
		Bundle args = new Bundle();
		args.putLong(ARG_RUN_ID, runId);
		
		RunFragment runFragment = new RunFragment();
		runFragment.setArguments(args);
		return runFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getActivity().getApplicationContext());
		setRetainInstance(true);
		
		mRunManager = RunManager.getInstance(getActivity());
		Bundle args = getArguments();
		if(args != null && args.containsKey(ARG_RUN_ID)){
			long runId = args.getLong(ARG_RUN_ID,-1);
			Log.i(TAG, "get runId:"+runId);
			
			if(runId != -1){
				
				getLoaderManager().initLoader(LOADER_LOAD_RUN, args, mRunLoaderCallbacks);
				getLoaderManager().initLoader(LOADER_LOAD_LOCATION_DATA_LIST
						, args, mLocationDataListLoaderCallbacks);
			}
		}
		
		checkIsTrackingCurrentRun();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_run, container, false);
        
		mTVCurrentRunStatus = (TextView) view.findViewById(R.id.tv_current_run_status);
        mStartedTextView = (TextView)view.findViewById(R.id.run_startedTextView);
        mLatitudeTextView = (TextView)view.findViewById(R.id.run_latitudeTextView);
        mLongitudeTextView = (TextView)view.findViewById(R.id.run_longitudeTextView);
        mAccuracyTextView = (TextView) view.findViewById(R.id.run_accuracyTextView);
        mAltitudeTextView = (TextView)view.findViewById(R.id.run_altitudeTextView);
        mDurationTextView = (TextView)view.findViewById(R.id.run_durationTextView);
        mTotalMetreTextView = (TextView) view.findViewById(R.id.run_totalMetreTextView);
                
        mStartButton = (Button)view.findViewById(R.id.run_startButton);
        mStartButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Log.i(TAG, "press start button");
				if(mRun != null){
					mRunManager.startCurrentRun(mRun);
				}else{
					mRun = mRunManager.startNewRun();
				}
				checkIsTrackingCurrentRun();
				updateButtonUI();
				updateUI();
			}
		});
        
        mStopButton = (Button)view.findViewById(R.id.run_stopButton);
        mStopButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Log.i(TAG, "press stop button");
				mRunManager.stopRun();
				checkIsTrackingCurrentRun();
				updateButtonUI();
				updateUI();
				
				mLocationDataCursor.requery();
			}
		});
        
        mButtonShowMap = (Button) view.findViewById(R.id.button_show_map);
        mButtonShowMap.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(mRun != null && mLastLocationData != null){
					long runId = mRun.getRunId();
					if(-1 == runId){
						Log.e(TAG, "runId have no value");
						return;
					}
					Intent intent = new Intent(getActivity(),RunMapActivity.class);
					intent.putExtra(RunMapActivity.ARG_RUN_ID, runId);
					startActivity(intent);
				}
			}
		});
        
        updateButtonUI();
        updateUI();
        
        return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		IntentFilter filter = new IntentFilter(RunManager.ACTION_LOCATION);
		getActivity().registerReceiver(locationReceiver, filter);
	}
	
	@Override
	public void onStop() {
		
		getActivity().unregisterReceiver(locationReceiver);
		
		super.onStop();		
	}
	
	private void checkIsTrackingCurrentRun(){
		
		if(mRun != null && mRunManager.isTrackingRun()){
			long currentTrackingRunId = mRunManager.getCurrentTrackingRunId();
			if(currentTrackingRunId == mRun.getRunId()){
				isTrackingCurrentRun = true;
			}else{
				isTrackingCurrentRun = false;
			}
		}else{
			isTrackingCurrentRun = false;
		}
	}
	
	private void updateUI(){

        if(isTrackingCurrentRun){
        	mTVCurrentRunStatus.setText(R.string.string_tracking_current_run);
        }else{
        	mTVCurrentRunStatus.setText(R.string.string_not_tracking_current_run);
        }
		
		Activity activity = getActivity();
		if(mRun != null && activity != null){
			Date startDate = mRun.getStartDate();
			String startDateStr = DateFormat.getMediumDateFormat(activity).format(startDate);
			mStartedTextView.setText(startDateStr);
		}else{
			mStartedTextView.setText("");
		}
		
		if(mRun != null && mLastLocationData != null){
			mLatitudeTextView.setText(String.valueOf(mLastLocationData.getLatitude()));
			mLongitudeTextView.setText(String.valueOf(mLastLocationData.getLongitude()));
			mAccuracyTextView.setText(String.valueOf(mLastLocationData.getAccuracy()));
			mAltitudeTextView.setText(String.valueOf(mLastLocationData.getAltitude()));
			
			int durationSeconds = mRun.getDurationSeconds(
					mLastLocationData.getTimestamp().getTime());
			String durationStr = Run.formatDuration(durationSeconds);
			mDurationTextView.setText(durationStr);
		}else{
			mLatitudeTextView.setText("");
			mLongitudeTextView.setText("");
			mAccuracyTextView.setText("");
			mAltitudeTextView.setText("");
			mDurationTextView.setText("");
		}
	}
	
	private void updateButtonUI(){
		
		boolean running = mRunManager.isTrackingRun();
		if(running){
			mStartButton.setEnabled(false);
			//如果正在跟踪当前旅程，那么可以停止，否则不可以停止
			if(isTrackingCurrentRun){
				mStopButton.setEnabled(true);
			}else{
				mStopButton.setEnabled(false);
			}
		}else{
			mStartButton.setEnabled(true);
			mStopButton.setEnabled(false);
		}

		if(mRun != null && mLastLocationData != null){
			mButtonShowMap.setEnabled(true);
		}else{
			mButtonShowMap.setEnabled(false);
		}
	}
	
	public void showTotalTripMetre(){
		
		List<LatLng> pointList = new ArrayList<LatLng>();
		
		mLocationDataCursor.moveToFirst();
		while(!mLocationDataCursor.isAfterLast()){
			LocationData locationData = mLocationDataCursor.getLocationData();
			
			LatLng sourceLatLng = new LatLng(locationData.getLatitude()
					, locationData.getLongitude());
			
			// 将GPS设备采集的原始GPS坐标转换成百度坐标  
			CoordinateConverter converter  = new CoordinateConverter();  
			converter.from(CoordType.GPS);  
			// sourceLatLng待转换坐标  
			converter.coord(sourceLatLng);  
			LatLng desLatLng = converter.convert();
			
			pointList.add(desLatLng);

			mLocationDataCursor.moveToNext();
		}
		
		
		if(pointList.size() == 0){
			Toast.makeText(getActivity(),R.string.cant_show_total_metre_no_location_data
					,Toast.LENGTH_LONG).show();
			return;
		}else
			if(pointList.size() == 1){

				Toast.makeText(getActivity(),R.string.cant_show_total_metre_need_more_location_data
						,Toast.LENGTH_LONG).show();
				return;
			}

		//去除重复的节点得到的最终旅程点
		//但长期停留在一个位置时，就会产生很多重复的节点，去掉这些重复的节点
		List<LatLng> finalPointList = new ArrayList<LatLng>();
		LatLng lastLL = pointList.get(0);
		finalPointList.add(lastLL);
		for(int i=1;i<pointList.size();i++){
			LatLng pointLL = pointList.get(i);
			double distance = DistanceUtil.getDistance(lastLL, pointLL);
			Log.i(TAG, "i:"+i+"-- distance:"+distance);
						
			if(distance > RunMapFragment.MIN_TRIP_DISTANCE){
				lastLL = pointLL;
				finalPointList.add(lastLL);
			}else{
				//如果小于最小间距，那就忽略当前节点，说明有可能停留在一个地方
				if(i == pointList.size()-1){
					lastLL = pointLL;
					finalPointList.add(lastLL);
				}
			}
		}
		
		//开始统计总里程
		double totalDistance = 0;
		
		for(int i=1;i<finalPointList.size();i++){
			LatLng previousLL = finalPointList.get(i-1);
			LatLng pointLL = finalPointList.get(i);
			double distance = DistanceUtil.getDistance(previousLL, pointLL);
			Log.i(TAG, "i:"+i+"-- distance:"+distance);
			totalDistance += distance;
		}
		Log.i(TAG, "total distance:"+totalDistance);
		
		DecimalFormat decFormat = new DecimalFormat("#");
		String totalDistStr = decFormat.format(totalDistance);
		mTotalMetreTextView.setText(totalDistStr);
	}
	
}

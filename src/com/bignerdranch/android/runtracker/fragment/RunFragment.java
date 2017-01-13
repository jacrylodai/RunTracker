package com.bignerdranch.android.runtracker.fragment;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.activity.RunMapActivity;
import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.domain.Run;
import com.bignerdranch.android.runtracker.loader.LastLocationDataLoader;
import com.bignerdranch.android.runtracker.loader.RunLoader;
import com.bignerdranch.android.runtracker.manager.RunManager;
import com.bignerdranch.android.runtracker.receiver.LocationReceiver;

public class RunFragment extends Fragment {
	
	private static final String TAG = "RunFragment";

	private static final String ARG_RUN_ID = "RUN_ID";
	
	private static final int LOADER_LOAD_RUN = 1;
	
	private static final int LOADER_LOAD_LOAST_LOCATION_DATA = 2;

    private Button mStartButton, mStopButton,mButtonShowMap;
    private TextView mTVCurrentRunStatus,mStartedTextView, mLatitudeTextView, 
        mLongitudeTextView, mAltitudeTextView, mDurationTextView;
    
    private RunManager mRunManager;
    
    private Run mRun;
    
    private boolean isTrackingCurrentRun;
    
    private LocationData mLastLocationData;
    
    private LocationReceiver locationReceiver = new LocationReceiver(){
    	
    	protected void onLocationReceived(Context context, Location location) {
    		Log.i(TAG, "onLocationReceived");
    		//如果没有记录当前的旅程，则不要显示地理信息
    		if(!isTrackingCurrentRun){
    			return;
    		}
    		mLastLocationData = LocationData.parseLocation(location);
    		if(RunFragment.this.isVisible()){
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
			
	private LoaderCallbacks<LocationData> mLastLocationDataLoaderCallbacks = 
			new LoaderCallbacks<LocationData>() {

				@Override
				public Loader<LocationData> onCreateLoader(int id, Bundle args) {
					
					long runId = args.getLong(ARG_RUN_ID, -1);
					return new LastLocationDataLoader(getActivity(), runId);
				}

				@Override
				public void onLoadFinished(Loader<LocationData> loader,
						LocationData data) {
					
					mLastLocationData = data;
					checkIsTrackingCurrentRun();
					updateButtonUI();
					updateUI();
				}

				@Override
				public void onLoaderReset(Loader<LocationData> arg0) {

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
		setRetainInstance(true);
		
		mRunManager = RunManager.getInstance(getActivity());
		Bundle args = getArguments();
		if(args != null && args.containsKey(ARG_RUN_ID)){
			long runId = args.getLong(ARG_RUN_ID,-1);
			Log.i(TAG, "get runId:"+runId);
			
			if(runId != -1){
				
				getLoaderManager().initLoader(LOADER_LOAD_RUN, args, mRunLoaderCallbacks);
				
				getLoaderManager().initLoader(LOADER_LOAD_LOAST_LOCATION_DATA, args
						, mLastLocationDataLoaderCallbacks);
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
        mAltitudeTextView = (TextView)view.findViewById(R.id.run_altitudeTextView);
        mDurationTextView = (TextView)view.findViewById(R.id.run_durationTextView);
                
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
			mAltitudeTextView.setText(String.valueOf(mLastLocationData.getAltitude()));
			
			int durationSeconds = mRun.getDurationSeconds(
					mLastLocationData.getTimestamp().getTime());
			String durationStr = Run.formatDuration(durationSeconds);
			mDurationTextView.setText(durationStr);
		}else{
			mLatitudeTextView.setText("");
			mLongitudeTextView.setText("");
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
	
}

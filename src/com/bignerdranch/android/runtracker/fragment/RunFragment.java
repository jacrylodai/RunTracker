package com.bignerdranch.android.runtracker.fragment;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.domain.Run;
import com.bignerdranch.android.runtracker.manager.RunManager;
import com.bignerdranch.android.runtracker.receiver.LocationReceiver;

public class RunFragment extends Fragment {
	
	private static final String TAG = "RunFragment";

    private Button mStartButton, mStopButton;
    private TextView mStartedTextView, mLatitudeTextView, 
        mLongitudeTextView, mAltitudeTextView, mDurationTextView;
    
    private RunManager mRunManager;
    
    private Run mRun;
    
    private Location mLastLocation;
    
    private LocationReceiver locationReceiver = new LocationReceiver(){
    	
    	protected void onLocationReceived(Context context, Location location) {
    		Log.i(TAG, "onLocationReceived");
    		mLastLocation = location;
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
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
		mRunManager = RunManager.getInstance(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_run, container, false);
        
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
				mRun = mRunManager.startNewRun();
				mLastLocation = null;
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
				updateButtonUI();
				updateUI();
			}
		});
        
        mRun = null;
        mLastLocation = null;
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
	
	private void updateUI(){
		
		Activity activity = getActivity();
		if(mRun != null && activity != null){
			Date startDate = mRun.getStartDate();
			String startDateStr = DateFormat.getMediumDateFormat(activity).format(startDate);
			mStartedTextView.setText(startDateStr);
		}else{
			mStartedTextView.setText("");
		}
		
		if(mRun != null && mLastLocation != null){
			mLatitudeTextView.setText(String.valueOf(mLastLocation.getLatitude()));
			mLongitudeTextView.setText(String.valueOf(mLastLocation.getLongitude()));
			mAltitudeTextView.setText(String.valueOf(mLastLocation.getAltitude()));
			
			int durationSeconds = mRun.getDurationSeconds(mLastLocation.getTime());
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
			mStopButton.setEnabled(true);
		}else{
			mStartButton.setEnabled(true);
			mStopButton.setEnabled(false);
		}
	}
	
}

package com.bignerdranch.android.runtracker.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bignerdranch.android.runtracker.R;

public class RunFragment extends Fragment {

    private Button mStartButton, mStopButton;
    private TextView mStartedTextView, mLatitudeTextView, 
        mLongitudeTextView, mAltitudeTextView, mDurationTextView;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
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
        
        mStopButton = (Button)view.findViewById(R.id.run_stopButton);
        
        return view;
	}
	
}

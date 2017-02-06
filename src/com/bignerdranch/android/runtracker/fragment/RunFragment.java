package com.bignerdranch.android.runtracker.fragment;

import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.activity.RunMapActivity;
import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.domain.Run;
import com.bignerdranch.android.runtracker.loader.RunLoader;
import com.bignerdranch.android.runtracker.util.DateUtils;

public class RunFragment extends Fragment {
	
	private static final String TAG = "RunFragment";

	private static final String ARG_RUN_ID = "RUN_ID";
	
	private static final int LOADER_LOAD_RUN = 1;
	
    private Button mButtonShowMap;
    private TextView mTVRunName,mTotalMetreTextView,mDurationTextView,mTVTotalTripPoint
    	,mStartedTextView;
    
    private Run mRun;
    
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
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_run, container, false);
        
		mTVRunName = (TextView) view.findViewById(R.id.tv_run_name);
        mStartedTextView = (TextView)view.findViewById(R.id.run_startedTextView);
        mDurationTextView = (TextView)view.findViewById(R.id.run_durationTextView);
        mTotalMetreTextView = (TextView) view.findViewById(R.id.run_totalMetreTextView);
        mTVTotalTripPoint = (TextView) view.findViewById(R.id.tv_total_trip_point);
                
        mButtonShowMap = (Button) view.findViewById(R.id.button_show_map);
        mButtonShowMap.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intent = new Intent(getActivity(),RunMapActivity.class);
				intent.putExtra(RunMapActivity.ARG_RUN_ID, mRun.getRunId());
				startActivity(intent);
			}
		});
        
        Bundle args = getArguments();
		if(args != null && args.containsKey(ARG_RUN_ID)){
			long runId = args.getLong(ARG_RUN_ID,-1);
			Log.i(TAG, "get runId:"+runId);
			
			if(runId != -1){
				
				getLoaderManager().initLoader(LOADER_LOAD_RUN, args, mRunLoaderCallbacks);
			}
		}
		
        return view;
	}
	
	private void updateUI(){
        
		mTVRunName.setText(mRun.getRunName());
		
		Date startDate = mRun.getStartDate();
		String startDateStr = DateUtils.formatFullDate(getActivity(),startDate);
		mStartedTextView.setText(startDateStr);
	
		int durationSeconds = (int) (mRun.getElapsedTime()/1000);
		String durationStr = Run.formatDuration(durationSeconds);
		mDurationTextView.setText(durationStr);
		
		mTotalMetreTextView.setText( String.valueOf(mRun.getTotalMetre()) );
		
		mTVTotalTripPoint.setText( String.valueOf(mRun.getTotalTripPoint()) );
	}
		
}

package com.bignerdranch.android.runtracker.fragment;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.activity.ConfigActivity;
import com.bignerdranch.android.runtracker.activity.RunActivity;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.RunCursor;
import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.domain.Run;
import com.bignerdranch.android.runtracker.loader.RunListCursorLoader;
import com.bignerdranch.android.runtracker.manager.RunManager;
import com.bignerdranch.android.runtracker.receiver.LocationReceiver;
import com.bignerdranch.android.runtracker.util.LocationUtils;

public class RunListFragment extends Fragment {
	
	private static final String TAG = "RunListFragment";
	
	private static final int HANDLER_MESSAGE_UPDATE_CLOCK = 1;
	
	private static final int REQUEST_CODE_NEW_RUN = 1;
	
	private static final int LOADER_LOAD_RUN_LIST = 1;
	
	private ListView mLVRunList;
	
	private Button mButtonStart,mButtonStop;
	
	private TextView mTVElapsedTime,mTVTripPoint,mTVTotalMetre;
	
	private RunManager mRunManager;
	
	//当前正在被记录的旅程
	private Run mRun;

	//记录时间
	private long mElapsedTime;
	
	//记录节点数
	private int mTripPoint;
	
	//总路程
	private long mTotalMetre;
	
	//上一个最终统计旅程节点，最终统计旅程节点之间间距大于25米，可以统计总路程的节点
	private LatLng mLastFinalTripPoint;
	
	//是否正在跟踪旅程
	private boolean mIsTracking;
	
	private Handler handler = new Handler(){
		
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case HANDLER_MESSAGE_UPDATE_CLOCK:
				
				updateClockUI();
				break;

			default:
				break;
			}
		};
	};
	
	private Runnable mTimeClock = new Runnable() {
		
		@Override
		public void run() {

			while(mIsTracking){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Log.e(TAG, "",e);
				}
				
				handler.sendEmptyMessage(HANDLER_MESSAGE_UPDATE_CLOCK);
			}
		}
	};
	
    private LocationReceiver locationReceiver = new LocationReceiver(){
    	
    	protected void onLocationReceived(Context context, Location location) {
    		
    		Log.i(TAG, "onLocationReceived");
    		LocationData locationData = LocationData.parseLocation(location);
    		
			//更新旅程节点
			mTripPoint ++;
			
			LatLng sourceLatLng = new LatLng(locationData.getLatitude()
					, locationData.getLongitude());			
			LatLng destLatLng = LocationUtils.convertGPSToBaiduPoint(sourceLatLng);
			//计算总旅程 及 更新上一个最终统计旅程节点
			if(mLastFinalTripPoint == null){
				mLastFinalTripPoint = destLatLng;
			}else{
				double distance = DistanceUtil.getDistance(mLastFinalTripPoint, destLatLng);
				//间距是否大于20米，大于说明在移动，可以统计，小于说明停在原地，不计入统计
				if(distance > RunManager.MIN_TRIP_DISTANCE){
					mTotalMetre += distance;
					mLastFinalTripPoint = destLatLng;
				}else{
					//不计入统计
				}
			}
			
			updateRunInfoUI();
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
	
	private LoaderCallbacks<Cursor> mRunListLoaderCallbacks = new LoaderCallbacks<Cursor>() {

				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args) {

					return new RunListCursorLoader(getActivity());
				}

				@Override
				public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

					RunCursorAdapter adapter = 
							new RunCursorAdapter(getActivity(), (RunCursor)cursor);
					mLVRunList.setAdapter(adapter);
				}

				@Override
				public void onLoaderReset(Loader<Cursor> loader) {

					mLVRunList.setAdapter(null);
				}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);		
		setHasOptionsMenu(true);
		
		mRunManager = RunManager.getInstance(getActivity());
		mIsTracking = mRunManager.isTrackingRun();
		
		//注册监听器
		IntentFilter intentFilter = new IntentFilter(RunManager.ACTION_LOCATION);
		getActivity().registerReceiver(locationReceiver, intentFilter);
		
		getLoaderManager().initLoader(LOADER_LOAD_RUN_LIST, null, mRunListLoaderCallbacks);		
	}
	
	@Override
	public void onDestroy() {
		
		getActivity().unregisterReceiver(locationReceiver);
		
		super.onDestroy();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_run_list, container, false);
		
		mLVRunList = (ListView) view.findViewById(R.id.lv_run_list);
		mTVElapsedTime = (TextView) view.findViewById(R.id.tv_elapsed_time);
		mTVTripPoint = (TextView) view.findViewById(R.id.tv_trip_point);
		mTVTotalMetre = (TextView) view.findViewById(R.id.tv_total_metre);
		mButtonStart = (Button) view.findViewById(R.id.button_start);
		mButtonStop = (Button) view.findViewById(R.id.button_stop);
		
		mLVRunList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Log.i(TAG, "onListItemClick");
				Log.i(TAG,"runId:"+id);
				
				Intent intent = new Intent(getActivity(),RunActivity.class);
				intent.putExtra(RunActivity.EXTRA_RUN_ID, id);
				startActivity(intent);
			}
		});
		
		mButtonStart.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Log.i(TAG, "press start button");
				if(mIsTracking){
					
					Log.e(TAG, "current is already tracking run.can't execute two task");
				}else{

					mRun = mRunManager.startNewRun();	
					mIsTracking = true;
					updateButtonUI();
					
					//启动定时器，每隔1秒更新记录时间UI
					new Thread(mTimeClock).start();
				}
			}
		});
		
		mButtonStop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Log.i(TAG, "press stop button");
				if(mIsTracking){
					
					long runId = mRunManager.getCurrentTrackingRunId();
					boolean isSuccess = mRunManager.stopRun();
					if(isSuccess){
						getLoaderManager().restartLoader(LOADER_LOAD_RUN_LIST, null
								, mRunListLoaderCallbacks);						
					}
					
					mIsTracking = false;
					updateButtonUI();
					
					resetCurrentRunInfo();
					updateRunInfoUI();
					
					updateClockUI();
					
				}else{

					Log.e(TAG, "can't stop.There is no tracking run");
				}
			}
		});
		
		updateButtonUI();
		if(mIsTracking){
			
		}else{
			resetCurrentRunInfo();
			updateRunInfoUI();
			updateClockUI();
		}
		
		return view;
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.run_list_options, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
			
		case R.id.menu_item_config:
			
			Intent configIntent = new Intent(getActivity(),ConfigActivity.class);
			startActivity(configIntent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void updateButtonUI(){
		
		if(mIsTracking){
			mButtonStart.setEnabled(false);
			mButtonStop.setEnabled(true);
		}else{
			mButtonStart.setEnabled(true);
			mButtonStop.setEnabled(false);
		}
	}
	
	private void resetCurrentRunInfo(){
		
		mRun = null;
		mElapsedTime = 0;
		mTripPoint = 0;
		mTotalMetre = 0;
		mLastFinalTripPoint = null;
	}
	
	private void updateRunInfoUI(){

		mTVTripPoint.setText( String.valueOf(mTripPoint) );
		mTVTotalMetre.setText( String.valueOf(mTotalMetre) );
	}
	
	private void updateClockUI(){
		
		if(mIsTracking){
			if(mRun != null){
				int durationSeconds = mRun.getDurationSeconds(new Date().getTime());
				String durationStr = Run.formatDuration(durationSeconds);
				mTVElapsedTime.setText(durationStr);
			}
		}else{
			mTVElapsedTime.setText("00:00:00");
		}
	}
	
	private class RunCursorAdapter extends CursorAdapter{

		private RunCursor mCursor;
		
		public RunCursorAdapter(Context context, RunCursor runCursor) {
			super(context, runCursor,0);
			mCursor = runCursor;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			Run run = mCursor.getRun();
			
			TextView tvStartDate = (TextView)view;
			tvStartDate.setText(run.getRunName());
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			
			LayoutInflater layoutInflater = 
					(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = 
					layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
			return view;
		}
		
	}
	
}

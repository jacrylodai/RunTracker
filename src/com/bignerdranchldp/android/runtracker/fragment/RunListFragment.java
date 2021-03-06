package com.bignerdranchldp.android.runtracker.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.bignerdranchldp.android.runtracker.R;
import com.bignerdranchldp.android.runtracker.activity.ConfigActivity;
import com.bignerdranchldp.android.runtracker.activity.DeleteRunItemsActivity;
import com.bignerdranchldp.android.runtracker.activity.RunPagerActivity;
import com.bignerdranchldp.android.runtracker.db.RunDatabaseHelper;
import com.bignerdranchldp.android.runtracker.db.RunDatabaseHelper.LocationDataCursor;
import com.bignerdranchldp.android.runtracker.db.RunDatabaseHelper.RunCursor;
import com.bignerdranchldp.android.runtracker.domain.LocationData;
import com.bignerdranchldp.android.runtracker.domain.Run;
import com.bignerdranchldp.android.runtracker.loader.LocationDataListLoader;
import com.bignerdranchldp.android.runtracker.loader.RunListCursorLoader;
import com.bignerdranchldp.android.runtracker.manager.RunManager;
import com.bignerdranchldp.android.runtracker.receiver.LocationReceiver;
import com.bignerdranchldp.android.runtracker.utils.location.LocationUtils;

public class RunListFragment extends Fragment {
	
	private static final String TAG = RunListFragment.class.getSimpleName();

	private static final String ARG_RUN_ID = "RUN_ID";
	
	private static final int HANDLER_MESSAGE_UPDATE_CLOCK = 1;
	
	private static final int LOADER_LOAD_RUN_LIST = 1;

	private static final int LOADER_LOAD_LOCATION_DATA_LIST = 2;
	
	private static final int REQUEST_CODE_UPDATE_RUN_NAME = 1;
	
	private static final int REQUEST_CODE_DELETE_RUN_ITEMS = 2;
	
	private static final String DIALOG_UPDATE_RUN_NAME = "dialogUpdateRunName";

	private static final String DIALOG_FIRST_USE_GPS_GUIDE = "dialogFirstUseGPSGuide";
	
	//第一次使用GPS记录时的指南
	public static final String PREF_FIRST_USE_GPS_GUIDE_SHOW = "firstUseGPSGuideShow";
	
	private SharedPreferences mPref;
	
	private ListView mLVRunList;
	
	private Button mButtonStart,mButtonStop;
	
	private TextView mTVElapsedTime,mTVTripPoint,mTVTotalMetre;
	
	private RunManager mRunManager;
	
	private ArrayList<Long> mRunIdList;
	
	//当前正在被记录的旅程
	private Run mRun;
	
	//记录节点数
	private int mTripPoint;
	
	//总路程
	private long mTotalMetre;
	
	//上一个最终统计旅程节点，最终统计旅程节点之间间距大于25米，可以统计总路程的节点
	private LatLng mLastFinalTripPoint;
	
	//是否正在跟踪旅程
	private boolean mIsTracking;
	
	//是否还可以运行计时器，只有在界面在显示时才运行计时器
	private boolean mIsRunningTimeClock;
	
	private Handler handler = new Handler(){
		
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case HANDLER_MESSAGE_UPDATE_CLOCK:
				
				if(isVisible()){
					updateClockUI();
				}
				break;

			default:
				break;
			}
		};
	};
	
	private Runnable mTimeClock = new Runnable() {
		
		@Override
		public void run() {

			while(mIsTracking && mIsRunningTimeClock){
				
				Log.i(TAG, "time clock running");
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
			
			if(isVisible()){
				updateRunInfoUI();
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
	
	private LoaderCallbacks<Cursor> mRunListLoaderCallbacks = new LoaderCallbacks<Cursor>() {

				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args) {

					return new RunListCursorLoader(getActivity());
				}

				@Override
				public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

					mRunIdList = new ArrayList<Long>();
					RunCursor runCursor = (RunCursor)cursor;
					
					if(cursor.moveToFirst()){
						do{
							long runId = 
									runCursor.getLong(runCursor.getColumnIndex(
											RunDatabaseHelper.COLUMN_RUN_RUN_ID));
							mRunIdList.add(runId);
						}while(cursor.moveToNext());
					}
					
					runCursor.moveToFirst();
					
					RunCursorAdapter adapter = 
							new RunCursorAdapter(getActivity(), runCursor);
					mLVRunList.setAdapter(adapter);
				}

				@Override
				public void onLoaderReset(Loader<Cursor> loader) {

					mLVRunList.setAdapter(null);
				}
	};
	
	private LoaderCallbacks<Cursor> mLocationDataListLoaderCallbacks = new LoaderCallbacks<Cursor>() {

				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args) {

					Log.i(TAG, "mLocationDataListLoaderCallbacks--onCreateLoader");
					long runId = args.getLong(ARG_RUN_ID, -1);
					return new LocationDataListLoader(getActivity(), runId);
				}

				@Override
				public void onLoadFinished(Loader<Cursor> loader,
						Cursor cursor) {
					
					Log.i(TAG, "mLocationDataListLoaderCallbacks--onLoadFinished");
					LocationDataCursor locationDataCursor = (LocationDataCursor) cursor;
					
					List<LatLng> pointList = new ArrayList<LatLng>();
					
					locationDataCursor.moveToFirst();
					while(!locationDataCursor.isAfterLast()){
						LocationData locationData = locationDataCursor.getLocationData();
						
						LatLng sourceLatLng = new LatLng(locationData.getLatitude()
								, locationData.getLongitude());
						
						LatLng destLatLng = LocationUtils.convertGPSToBaiduPoint(sourceLatLng);
						
						pointList.add(destLatLng);

						locationDataCursor.moveToNext();
					}
					
					//如果记录的节点为0，或为1
					if(pointList.size() == 0){
						
						mTripPoint = 0;
						mTotalMetre = 0;
						mLastFinalTripPoint = null;
						
						updateRunInfoUI();
						return;
					}else
						if(pointList.size() == 1){

							mTripPoint = 1;
							mTotalMetre = 0;
							mLastFinalTripPoint = pointList.get(0);

							updateRunInfoUI();
							return;
						}
					
					mTripPoint = pointList.size();
					
					List<LatLng> finalPointList = LocationUtils.getFinalTripPoint(pointList);
					int size = finalPointList.size();
					mLastFinalTripPoint = finalPointList.get(size-1);
					
					double totalDistance = LocationUtils.caculateTotalDistance(finalPointList);
					mTotalMetre = (long) totalDistance;
					
					updateRunInfoUI();
				}

				@Override
				public void onLoaderReset(Loader<Cursor> loader) {

					Log.i(TAG, "mLocationDataListLoaderCallbacks--onLoaderReset");
					//do nothing
				}
	};
    
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);		
		
		Log.i(TAG, "onCreate");
		
		setHasOptionsMenu(true);
		
		mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		mRunManager = RunManager.getInstance(getActivity());
		mIsTracking = mRunManager.isTrackingRun();
		if(mIsTracking){
			long runId = mRunManager.getCurrentTrackingRunId();
			mRun = mRunManager.queryRunById(runId);			
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();

		Log.i(TAG, "onStart");

		//注册监听器
		IntentFilter intentFilter = new IntentFilter(RunManager.ACTION_LOCATION);
		getActivity().registerReceiver(locationReceiver, intentFilter);
		
		mIsRunningTimeClock = true;
		updateClockUI();
		
		if(mIsTracking){

			new Thread(mTimeClock).start();
			
			Bundle args = new Bundle();
			args.putLong(ARG_RUN_ID, mRun.getRunId());
			getLoaderManager().restartLoader(LOADER_LOAD_LOCATION_DATA_LIST, args
					, mLocationDataListLoaderCallbacks);
			
		}
	}
	
	@Override
	public void onStop() {

		Log.i(TAG, "onStop");
		
		getActivity().unregisterReceiver(locationReceiver);
		mIsRunningTimeClock = false;
		
		super.onStop();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		Log.i(TAG, "onCreateView");
		
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
				Log.i(TAG,"position:"+position);
				Log.i(TAG,"runId:"+id);
				
				Intent intent = new Intent(getActivity(),RunPagerActivity.class);
				intent.putExtra(RunPagerActivity.EXTRA_RUN_ID_POSITION, position);
				intent.putExtra(RunPagerActivity.EXTRA_RUN_ID_LIST, mRunIdList);
				
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

					boolean isProviderEnabled = mRunManager.checkIsProviderEnabled(
							mRunManager.getLocationProvider());
					if(isProviderEnabled == false){
						Toast.makeText(getActivity(), R.string.cant_start_tracking_gps_not_enabled
								, Toast.LENGTH_LONG).show();
						Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				        startActivity(intent); //此为设置完成
						return;
					}
					
					mRun = mRunManager.startNewRun();	
					mIsTracking = true;
					updateButtonUI();
					
					//启动定时器，每隔1秒更新记录时间UI
					new Thread(mTimeClock).start();
					
					showFirstUseGPSDialog();
				}
			}
		});
		
		mButtonStop.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Log.i(TAG, "press stop button");
				if(mIsTracking){
					
					boolean isSuccess = mRunManager.stopRun();
					if(isSuccess){						
						showUpdateRunNameDialog();					
					}
					
					Log.i(TAG, "stop still running the next");
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

		getLoaderManager().initLoader(LOADER_LOAD_RUN_LIST, null
				, mRunListLoaderCallbacks);
		
		updateButtonUI();
		
		if(mIsTracking){

			//启动计时器
			new Thread(mTimeClock).start();
		}else{
			resetCurrentRunInfo();
			updateRunInfoUI();
			updateClockUI();
		}
		
		return view;
	}
	
	@Override
	public void onDestroy() {

		Log.i(TAG, "onDestroy");
		
		super.onDestroy();
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

		case R.id.menu_item_delete_run_items:
			
			Intent deleteRunItemsIntent = new Intent(getActivity(),DeleteRunItemsActivity.class);
			startActivityForResult(deleteRunItemsIntent, REQUEST_CODE_DELETE_RUN_ITEMS);
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {
		case REQUEST_CODE_UPDATE_RUN_NAME:
			if(resultCode == Activity.RESULT_OK){
				long runId = data.getLongExtra(UpdateRunNameFragment.EXTRA_RUN_ID,-1);
				String runName = data.getStringExtra(UpdateRunNameFragment.EXTRA_RUN_NAME);
				
				Run run = mRunManager.queryRunById(runId);
				run.setRunName(runName);
				mRunManager.updateRun(run);
			}else
				if(resultCode == Activity.RESULT_CANCELED){
					//cancel does not modify the run name
				}

			getLoaderManager().restartLoader(LOADER_LOAD_RUN_LIST, null
					, mRunListLoaderCallbacks);	
			break;

		case REQUEST_CODE_DELETE_RUN_ITEMS:
			
			Log.i(TAG, "onActivityResult--REQUEST_CODE_DELETE_RUN_ITEMS");
			Log.i(TAG, "resultCode:"+resultCode);
			
			if(resultCode == Activity.RESULT_OK){
				getLoaderManager().restartLoader(LOADER_LOAD_RUN_LIST, null
						, mRunListLoaderCallbacks);	
			}
			break;
			
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
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
	
	private void showUpdateRunNameDialog(){
			
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		
		UpdateRunNameFragment updateRunNameFragment = 
				UpdateRunNameFragment.newInstance(mRun.getRunId(),mRun.getRunName());
		updateRunNameFragment.setTargetFragment(this, REQUEST_CODE_UPDATE_RUN_NAME);
		
		updateRunNameFragment.show(fragmentManager, DIALOG_UPDATE_RUN_NAME);
	}
	
	private void showFirstUseGPSDialog(){
		
		boolean isShowFirstUseGPS = mPref.getBoolean(PREF_FIRST_USE_GPS_GUIDE_SHOW, true);
		if(isShowFirstUseGPS == false){
			return;
		}
		
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		
		FirstUseGPSGuideFragment firstUseGPSGuideFragment = 
				new FirstUseGPSGuideFragment();
		firstUseGPSGuideFragment.show(fragmentManager, DIALOG_FIRST_USE_GPS_GUIDE);
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
			
			TextView tvRunName = (TextView) view.findViewById(R.id.tv_run_name);
			TextView tvDurationTime = (TextView) view.findViewById(R.id.tv_duration_time);
			TextView tvTotalMetre = (TextView) view.findViewById(R.id.tv_total_metre);
			
			tvRunName.setText(run.getRunName());
			
			int durationSeconds = (int) (run.getElapsedTime()/1000);
			String durationStr = Run.formatDuration(durationSeconds);
			tvDurationTime.setText(durationStr);
			
			tvTotalMetre.setText( String.valueOf(run.getTotalMetre()) );
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			
			LayoutInflater layoutInflater = 
					(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = 
					layoutInflater.inflate(R.layout.view_run_list_item, parent, false);
			return view;
		}
		
	}
	
}

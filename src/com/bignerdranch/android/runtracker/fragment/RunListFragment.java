package com.bignerdranch.android.runtracker.fragment;

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
import android.os.Handler;
import android.os.Message;
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
import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.activity.ConfigActivity;
import com.bignerdranch.android.runtracker.activity.RunActivity;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.LocationDataCursor;
import com.bignerdranch.android.runtracker.db.RunDatabaseHelper.RunCursor;
import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.domain.Run;
import com.bignerdranch.android.runtracker.loader.LocationDataListLoader;
import com.bignerdranch.android.runtracker.loader.RunListCursorLoader;
import com.bignerdranch.android.runtracker.manager.RunManager;
import com.bignerdranch.android.runtracker.receiver.LocationReceiver;
import com.bignerdranch.android.runtracker.util.LocationUtils;

public class RunListFragment extends Fragment {
	
	private static final String TAG = "RunListFragment";

	private static final String ARG_RUN_ID = "RUN_ID";
	
	private static final int HANDLER_MESSAGE_UPDATE_CLOCK = 1;
	
	private static final int LOADER_LOAD_RUN_LIST = 1;

	private static final int LOADER_LOAD_LOCATION_DATA_LIST = 2;
	
	private static final int REQUEST_CODE_UPDATE_RUN_NAME = 1;
	
	private static final String DIALOG_UPDATE_RUN_NAME = "dialogUpdateRunName";
	
	private ListView mLVRunList;
	
	private Button mButtonStart,mButtonStop;
	
	private TextView mTVElapsedTime,mTVTripPoint,mTVTotalMetre;
	
	private RunManager mRunManager;
	
	//��ǰ���ڱ���¼���ó�
	private Run mRun;
	
	//��¼�ڵ���
	private int mTripPoint;
	
	//��·��
	private long mTotalMetre;
	
	//��һ������ͳ���ó̽ڵ㣬����ͳ���ó̽ڵ�֮�������25�ף�����ͳ����·�̵Ľڵ�
	private LatLng mLastFinalTripPoint;
	
	//�Ƿ����ڸ����ó�
	private boolean mIsTracking;
	
	//�Ƿ񻹿������м�ʱ����ֻ���ڽ�������ʾʱ�����м�ʱ��
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
    		
			//�����ó̽ڵ�
			mTripPoint ++;
			
			LatLng sourceLatLng = new LatLng(locationData.getLatitude()
					, locationData.getLongitude());			
			LatLng destLatLng = LocationUtils.convertGPSToBaiduPoint(sourceLatLng);
			//�������ó� �� ������һ������ͳ���ó̽ڵ�
			if(mLastFinalTripPoint == null){
				mLastFinalTripPoint = destLatLng;
			}else{
				double distance = DistanceUtil.getDistance(mLastFinalTripPoint, destLatLng);
				//����Ƿ����20�ף�����˵�����ƶ�������ͳ�ƣ�С��˵��ͣ��ԭ�أ�������ͳ��
				if(distance > RunManager.MIN_TRIP_DISTANCE){
					mTotalMetre += distance;
					mLastFinalTripPoint = destLatLng;
				}else{
					//������ͳ��
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

					RunCursorAdapter adapter = 
							new RunCursorAdapter(getActivity(), (RunCursor)cursor);
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
					
					//�����¼�Ľڵ�Ϊ0����Ϊ1
					if(pointList.size() == 0){
						
						mTripPoint = 0;
						mTotalMetre = 0;
						mLastFinalTripPoint = null;
						
						if(isVisible()){
							updateRunInfoUI();
						}
						return;
					}else
						if(pointList.size() == 1){

							mTripPoint = 1;
							mTotalMetre = 0;
							mLastFinalTripPoint = pointList.get(0);

							if(isVisible()){
								updateRunInfoUI();
							}
							return;
						}
					
					mTripPoint = pointList.size();
					
					List<LatLng> finalPointList = LocationUtils.getFinalTripPoint(pointList);
					int size = finalPointList.size();
					mLastFinalTripPoint = finalPointList.get(size-1);
					
					double totalDistance = LocationUtils.caculateTotalDistance(finalPointList);
					mTotalMetre = (long) totalDistance;
					
					if(isVisible()){
						updateRunInfoUI();
					}
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
		setHasOptionsMenu(true);
		
		mRunManager = RunManager.getInstance(getActivity());
		mIsTracking = mRunManager.isTrackingRun();
		if(mIsTracking){
			long runId = mRunManager.getCurrentTrackingRunId();
			mRun = mRunManager.queryRunById(runId);			
		}
		
		getLoaderManager().initLoader(LOADER_LOAD_RUN_LIST, null, mRunListLoaderCallbacks);
	}
	
	@Override
	public void onStart() {
		super.onStart();

		//ע�������
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

		getActivity().unregisterReceiver(locationReceiver);
		mIsRunningTimeClock = false;
		
		super.onStop();
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
					
					//������ʱ����ÿ��1����¼�¼ʱ��UI
					new Thread(mTimeClock).start();
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
		
		updateButtonUI();
		
		if(mIsTracking){

			//������ʱ��
			new Thread(mTimeClock).start();
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
	
	private class RunCursorAdapter extends CursorAdapter{

		private RunCursor mCursor;
		
		public RunCursorAdapter(Context context, RunCursor runCursor) {
			super(context, runCursor,0);
			mCursor = runCursor;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			Run run = mCursor.getRun();
			
			TextView tvRunName = (TextView)view;
			tvRunName.setText(run.getRunName());
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

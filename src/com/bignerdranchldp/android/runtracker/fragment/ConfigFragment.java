package com.bignerdranchldp.android.runtracker.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranchldp.android.runtracker.R;
import com.bignerdranchldp.android.runtracker.activity.UseGuideActivity;
import com.bignerdranchldp.android.runtracker.ui.ConfigItemSelectView;

public class ConfigFragment extends Fragment {
	
	private static final String TAG = ConfigFragment.class.getSimpleName();	
	
	private static final String DIALOG_UPDATE_RECORD_TIME = "dialogUpdateRecordTime";
	
	private static final int REQUEST_CODE_UPDATE_RECORD_TIME = 1;
	
	//记录间隔时间
	public static final String PREF_RECORD_TIME = "recordTime";
	
	//默认的记录间隔时间（秒）
	public static final int DEFAULT_RECORD_TIME = 90;
	
	private SharedPreferences mPref;
	
	private ConfigItemSelectView cisvConfigRecordTime;
	
	private ConfigItemSelectView cisvConfigLocationService;
	
	private ConfigItemSelectView cisvConfigUseGuide;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		
		mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_config, container, false);
		
		cisvConfigRecordTime = 
				(ConfigItemSelectView) view.findViewById(R.id.cisv_config_record_time);
		cisvConfigLocationService = 
				(ConfigItemSelectView) view.findViewById(R.id.cisv_config_location_service);
		cisvConfigUseGuide = 
				(ConfigItemSelectView) view.findViewById(R.id.cisv_config_use_guide);
		
		cisvConfigRecordTime.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				showUpdateRecordTimeDialog();
			}
		});
		
		cisvConfigLocationService.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		});
		cisvConfigLocationService.setValue("");
		
		cisvConfigUseGuide.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(getActivity(),UseGuideActivity.class);
				startActivity(intent);
			}
		});
		cisvConfigUseGuide.setValue("");
		
		updateConfigUI();
		
		return view;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.public_delete_options, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.menu_item_back:
			
			getActivity().finish();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void updateConfigUI(){
	
		int recordTime = mPref.getInt(PREF_RECORD_TIME, DEFAULT_RECORD_TIME);;
		String recordTimeDesc = String.format(getString(R.string.record_time_desc), recordTime);
		cisvConfigRecordTime.setDesc(recordTimeDesc);

		cisvConfigRecordTime.setValue(String.valueOf(recordTime));
	}
	
	private void showUpdateRecordTimeDialog(){
		
		int recordTime = mPref.getInt(PREF_RECORD_TIME,DEFAULT_RECORD_TIME);;
		UpdateRecordTimeFragment updateRecoTimeFrag = 
				UpdateRecordTimeFragment.newInstance(recordTime);
		updateRecoTimeFrag.setTargetFragment(ConfigFragment.this, REQUEST_CODE_UPDATE_RECORD_TIME);
		
		FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
		updateRecoTimeFrag.show(fragmentManager, DIALOG_UPDATE_RECORD_TIME);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		Log.i(TAG, "onActivityResult");
		Log.i(TAG, "requestCode:"+requestCode+"---resultCode:"+resultCode);
		
		switch (requestCode) {
		case REQUEST_CODE_UPDATE_RECORD_TIME:
			if(resultCode == Activity.RESULT_OK){
				int recordTime = data.getIntExtra(
						UpdateRecordTimeFragment.EXTRA_RECORD_TIME, DEFAULT_RECORD_TIME);
				Log.i(TAG, "recordTime:"+recordTime);
				
				mPref.edit()
					.putInt(PREF_RECORD_TIME, recordTime)
					.commit();
				updateConfigUI();
			}
			break;

		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}
	
}

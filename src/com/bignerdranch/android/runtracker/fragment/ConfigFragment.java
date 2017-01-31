package com.bignerdranch.android.runtracker.fragment;

import java.util.Arrays;
import java.util.List;

import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.ui.ConfigItemSelectView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

public class ConfigFragment extends Fragment {
	
	private static final String TAG = ConfigFragment.class.getSimpleName();
	
	public static final String PREF_RECORD_TIME = "recordTime";
	
	private static final Integer[] RECORD_TIME_ARRAY = new Integer[]{30,60,90,150,240,390};
	
	//默认的记录间隔时间（秒）
	public static final int DEFAULT_RECORD_TIME = RECORD_TIME_ARRAY[2];
	
	private SharedPreferences mPref;
	
	private ConfigItemSelectView cisvConfigRecordTime;
	
	private AlertDialog mRecordTimeDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_config, container, false);
		
		cisvConfigRecordTime = (ConfigItemSelectView) view.findViewById(R.id.cisv_config_record_time);
		cisvConfigRecordTime.setTitleId(R.string.record_time_title);
		
		cisvConfigRecordTime.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				showUpdateRecordTimeDialog();
			}
		});
		
		updateConfigUI();
		
		return view;
	}
	
	private void updateConfigUI(){
	
		int recordTime = mPref.getInt(PREF_RECORD_TIME, DEFAULT_RECORD_TIME);;
		String recordTimeDesc = String.format(getString(R.string.record_time_desc), recordTime);
		cisvConfigRecordTime.setDesc(recordTimeDesc);

		cisvConfigRecordTime.setValue(String.valueOf(recordTime));
	}
	
	private void showUpdateRecordTimeDialog(){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.record_time_title)
			.setNegativeButton(android.R.string.cancel,null);
		builder.setCancelable(true);
		
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_record_time, null);
		ListView lvTimeList = (ListView) view.findViewById(R.id.lv_time_list);
		
		final ArrayAdapter<Integer> adapter = 
				new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_list_item_single_choice
						, RECORD_TIME_ARRAY);
		
		lvTimeList.setAdapter(adapter);
		lvTimeList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		int recordTime = mPref.getInt(PREF_RECORD_TIME, DEFAULT_RECORD_TIME);;
		int index = Arrays.binarySearch(RECORD_TIME_ARRAY, recordTime);
		lvTimeList.setItemChecked(index, true);
		
		lvTimeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				int selectedRecordTime = adapter.getItem(position);
				Log.i(TAG, "you have select :"+selectedRecordTime);
				
				mPref.edit()
					.putInt(PREF_RECORD_TIME, selectedRecordTime)
					.commit();
				updateConfigUI();
				mRecordTimeDialog.dismiss();
			}
		});
		
		builder.setView(view);
		
		mRecordTimeDialog = builder.create();
		mRecordTimeDialog.show();
	}
	
}

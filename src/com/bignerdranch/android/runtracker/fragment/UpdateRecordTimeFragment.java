package com.bignerdranch.android.runtracker.fragment;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bignerdranch.android.runtracker.R;

public class UpdateRecordTimeFragment extends DialogFragment {
	
	private static final String TAG = UpdateRecordTimeFragment.class.getSimpleName();
	
	public static final String EXTRA_RECORD_TIME = "recordTime";
	
	private static final Integer[] RECORD_TIME_ARRAY = new Integer[]{2,5,20,40,60,90,150,240,390};
	
	public static UpdateRecordTimeFragment newInstance(int recordTime){
		
		Bundle args = new Bundle();
		args.putInt(EXTRA_RECORD_TIME, recordTime);
		
		UpdateRecordTimeFragment updateRecoTimeFrag = new UpdateRecordTimeFragment();
		updateRecoTimeFrag.setArguments(args);
		
		return updateRecoTimeFrag;
	}
	
	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.dialog_update_record_time, null);
		ListView lvTimeList = (ListView) view.findViewById(R.id.lv_time_list);
		
		final ArrayAdapter<Integer> adapter = 
				new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_list_item_single_choice
						, RECORD_TIME_ARRAY);
		
		lvTimeList.setAdapter(adapter);
		lvTimeList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		Bundle args = getArguments();
		int recordTime = args.getInt(EXTRA_RECORD_TIME, ConfigFragment.DEFAULT_RECORD_TIME);
		
		int index = Arrays.binarySearch(RECORD_TIME_ARRAY, recordTime);
		lvTimeList.setItemChecked(index, true);
		lvTimeList.setSelection(index);
		
		lvTimeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				int selectedRecordTime = adapter.getItem(position);
				Log.i(TAG, "you have select :"+selectedRecordTime);
				
				setResult(Activity.RESULT_OK,selectedRecordTime);
				UpdateRecordTimeFragment.this.dismiss();
			}
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.record_time_title)
			.setNegativeButton(android.R.string.cancel,null);
		builder.setCancelable(true);
		builder.setView(view);
		
		return builder.create();
	}
	
	private void setResult(int resultCode,int recordTime){
		
		Fragment targetFragment = getTargetFragment();
		if(targetFragment == null){
			return;
		}
		
		Intent data = new Intent();
		data.putExtra(EXTRA_RECORD_TIME, recordTime);
		
		targetFragment.onActivityResult(getTargetRequestCode(), resultCode, data);
	}
	
}

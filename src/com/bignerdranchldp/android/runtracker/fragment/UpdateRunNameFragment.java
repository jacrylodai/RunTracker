package com.bignerdranchldp.android.runtracker.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bignerdranchldp.android.runtracker.R;

public class UpdateRunNameFragment extends DialogFragment {
	
	private static final String TAG = UpdateRunNameFragment.class.getSimpleName();

	public static final String EXTRA_RUN_ID = "extraRunId";
	
	public static final String EXTRA_RUN_NAME = "extraRunName";
	
	private EditText mETRunName;
	
	private long mRunId;
	
	public static UpdateRunNameFragment newInstance(long runId,String runName){
		
		Bundle args = new Bundle();
		args.putLong(EXTRA_RUN_ID, runId);
		args.putString(EXTRA_RUN_NAME, runName);
		
		UpdateRunNameFragment updateRunNameFragment = new UpdateRunNameFragment();
		updateRunNameFragment.setArguments(args);
		return updateRunNameFragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "onCreate");
		mRunId = getArguments().getLong(EXTRA_RUN_ID);
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		Log.d(TAG, "onCreateDialog");
		
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.dialog_update_run_name, null);
		mETRunName = (EditText) view.findViewById(R.id.et_run_name);
		mETRunName.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				Log.i(TAG, "onTextChanged");
				
				//保存runName的值，以免设备旋转丢失值
				String runName = s.toString();
				getArguments().putString(EXTRA_RUN_NAME, runName);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		String runName = getArguments().getString(EXTRA_RUN_NAME);
		mETRunName.setText(runName);
		mETRunName.setSelection(runName.length());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.update_run_name)
			.setCancelable(true);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {

					String runName = mETRunName.getText().toString().trim();
					if(runName.length() == 0){
						Toast.makeText(getActivity(), R.string.does_not_input_run_name
								, Toast.LENGTH_LONG).show();
						setResult(Activity.RESULT_CANCELED, "");
					}else{
						setResult(Activity.RESULT_OK,runName);
					}
				}
		});
		
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {

				setResult(Activity.RESULT_CANCELED, "");
			}
		});
		
		return builder.create();
	}
	
	private void setResult(int resultCode,String runName){
		
		Fragment fragment = getTargetFragment();
		if(fragment == null){
			return;
		}
		
		int requestCode = getTargetRequestCode();
		
		Intent data = new Intent();
		data.putExtra(EXTRA_RUN_ID, mRunId);
		data.putExtra(EXTRA_RUN_NAME, runName);
		
		fragment.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		Log.i(TAG, "onCancel");
		setResult(Activity.RESULT_CANCELED, "");
	}
	
}

package com.bignerdranchldp.android.runtracker.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.bignerdranchldp.android.runtracker.R;

public class FirstUseGuideFragment extends DialogFragment {
	
	private SharedPreferences mPref;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.dialog_first_use_guide, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.first_use_guide_title);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
		});
		
		return builder.create();
	}
	
}

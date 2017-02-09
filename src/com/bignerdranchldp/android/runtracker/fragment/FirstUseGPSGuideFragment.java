package com.bignerdranchldp.android.runtracker.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CheckBox;

import com.bignerdranchldp.android.runtracker.R;
import com.bignerdranchldp.android.runtracker.activity.RunListActivity;
import com.bignerdranchldp.android.runtracker.activity.SplashActivity;

public class FirstUseGPSGuideFragment extends DialogFragment {
	
	private SharedPreferences mPref;
	
	private CheckBox mCBDoNotWarn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		View view = getActivity().getLayoutInflater().inflate(
				R.layout.dialog_first_use_gps_guide, null);
		mCBDoNotWarn = (CheckBox) view.findViewById(R.id.cb_do_not_warn);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.first_use_gps_guide_title);
		builder.setView(view);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if(mCBDoNotWarn.isChecked()){
					mPref.edit()
						.putBoolean(RunListFragment.PREF_FIRST_USE_GPS_GUIDE_SHOW, false)
						.commit();
				}
			}
		});
		
		return builder.create();
	}
	
}

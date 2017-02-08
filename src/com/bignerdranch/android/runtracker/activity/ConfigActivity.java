package com.bignerdranch.android.runtracker.activity;

import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.fragment.ConfigFragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class ConfigActivity extends SingleFragmentActivity {

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			getActionBar().setSubtitle(R.string.config_config);
		}
	}
	
	@Override
	protected Fragment createFragment() {
		return new ConfigFragment();
	}

}

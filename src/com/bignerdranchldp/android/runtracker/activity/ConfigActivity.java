package com.bignerdranchldp.android.runtracker.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.bignerdranchldp.android.runtracker.R;
import com.bignerdranchldp.android.runtracker.fragment.ConfigFragment;

public class ConfigActivity extends SingleFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setSubtitle(R.string.config_config);
	}
	
	@Override
	protected Fragment createFragment() {
		return new ConfigFragment();
	}

}

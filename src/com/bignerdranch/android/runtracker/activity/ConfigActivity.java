package com.bignerdranch.android.runtracker.activity;

import com.bignerdranch.android.runtracker.fragment.ConfigFragment;

import android.support.v4.app.Fragment;

public class ConfigActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new ConfigFragment();
	}

}

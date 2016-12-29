package com.bignerdranch.android.runtracker.activity;

import android.support.v4.app.Fragment;

import com.bignerdranch.android.runtracker.fragment.RunFragment;

public class RunActivity extends SingleFragmentActivity {

	@Override
	public Fragment createFragment() {
		return new RunFragment();
	}

}

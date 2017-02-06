package com.bignerdranch.android.runtracker.activity;

import com.bignerdranch.android.runtracker.fragment.DeleteRunItemsFragment;

import android.support.v4.app.Fragment;

public class DeleteRunItemsActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		return new DeleteRunItemsFragment();
	}

}

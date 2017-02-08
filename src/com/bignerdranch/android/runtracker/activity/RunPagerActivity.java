package com.bignerdranch.android.runtracker.activity;

import java.util.ArrayList;
import java.util.List;

import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.fragment.RunFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class RunPagerActivity extends FragmentActivity {
	
	private static final String TAG = RunPagerActivity.class.getSimpleName();

	public static final String EXTRA_RUN_ID_POSITION = 
			"com.bignerdranch.android.runtracker.runIdPosition";
	
	public static final String EXTRA_RUN_ID_LIST = 
			"com.bignerdranch.android.runtracker.runIdList";

	private ViewPager mVPRunFragmentContainer;
	
	private ArrayList<Long> mRunIdList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_run_pager);
		mVPRunFragmentContainer = (ViewPager) findViewById(R.id.vp_run_fragment_container);
		
		int runIdPosition = getIntent().getIntExtra(EXTRA_RUN_ID_POSITION, -1);
		mRunIdList = (ArrayList<Long>) getIntent().getSerializableExtra(EXTRA_RUN_ID_LIST);
		
		Log.i(TAG, "runIdPosition:"+runIdPosition);
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		mVPRunFragmentContainer.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
			
			@Override
			public int getCount() {
				Log.i(TAG, "getCount()"+mRunIdList.size());
				return mRunIdList.size();
			}
			
			@Override
			public Fragment getItem(int pos) {
				return RunFragment.newInstance(mRunIdList.get(pos));
			}
		});
		
		Log.i(TAG, "setCurrentItem");
		mVPRunFragmentContainer.setCurrentItem(runIdPosition);
	}
	
}

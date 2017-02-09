package com.bignerdranchldp.android.runtracker.activity;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.bignerdranchldp.android.runtracker.R;
import com.bignerdranchldp.android.runtracker.fragment.RunFragment;

public class RunPagerActivity extends ActionBarActivity {
	
	private static final String TAG = RunPagerActivity.class.getSimpleName();

	public static final String EXTRA_RUN_ID_POSITION = 
			"com.bignerdranchldp.android.runtracker.runIdPosition";
	
	public static final String EXTRA_RUN_ID_LIST = 
			"com.bignerdranchldp.android.runtracker.runIdList";

	private ViewPager mVPRunFragmentContainer;
	
	private ArrayList<Long> mRunIdList;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_run_pager);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			getActionBar().setSubtitle(R.string.run_detail);
		}
		
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

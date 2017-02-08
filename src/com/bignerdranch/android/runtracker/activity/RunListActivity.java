package com.bignerdranch.android.runtracker.activity;

import com.bignerdranch.android.runtracker.R;
import com.bignerdranch.android.runtracker.fragment.RunListFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

public class RunListActivity extends SingleFragmentActivity {
	
	private long mPressBackTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPressBackTime = 0;
	}

	@Override
	protected Fragment createFragment() {
		return new RunListFragment();
	}
	
	@Override
	public void onBackPressed() {
			
		long currentTime = System.currentTimeMillis();
		long intervalTime = currentTime - mPressBackTime;
		if(intervalTime<3000){
			super.onBackPressed();
		}else{
			mPressBackTime = currentTime;
			Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
		}
	}

}

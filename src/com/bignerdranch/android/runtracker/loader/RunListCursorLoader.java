package com.bignerdranch.android.runtracker.loader;

import android.content.Context;
import android.database.Cursor;

import com.bignerdranch.android.runtracker.manager.RunManager;

public class RunListCursorLoader extends SQLiteCursorLoader{

	public RunListCursorLoader(Context context) {
		super(context);
	}

	@Override
	protected Cursor loadCursor() {
		return RunManager.getInstance(getContext()).queryRunList();
	}
	
}

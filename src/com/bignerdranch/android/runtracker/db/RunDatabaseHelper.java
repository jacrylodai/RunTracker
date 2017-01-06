package com.bignerdranch.android.runtracker.db;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bignerdranch.android.runtracker.domain.LocationData;
import com.bignerdranch.android.runtracker.domain.Run;

public class RunDatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME="RunTracker.db";
	
	private static final int VERSION = 1;
	
	private static final String CREATE_TABLE_RUN = 
			"create table t_run("+
				"_id integer primary key autoincrement,"+
				"start_date integer"+
			")";
	
	private static final String TABLE_RUN_NAME = "t_run";
	
	private static final String COLUMN_RUN_RUN_ID = "_id";
	
	private static final String COLUMN_RUN_START_DATE = "start_date";
	
	private static final String CREATE_TABLE_LOCATION_DATA = 
			"create table t_location_data("+
				"_id integer primary key autoincrement,"+
				"fk_run_id integer references t_run(run_id),"+
				"timestamp integer,"+
				"latitude real,"+
				"longitude real,"+
				"altitude real,"+
				"provider varchar(100)"+
			")";
	
	private static final String TABLE_LOCATION_DATA_NAME = "t_location_data";
	
	private static final String COLUMN_LOCATION_DATA_LOCATION_DATA_ID = "_id";
	
	private static final String COLUMN_LOCATION_DATA_FK_RUN_ID = "fk_run_id";
	
	private static final String COLUMN_LOCATION_DATA_TIMESTAMP = "timestamp";
	
	private static final String COLUMN_LOCATION_DATA_LATITUDE = "latitude";
	
	private static final String COLUMN_LOCATION_DATA_LONGITUDE = "longitude";
	
	private static final String COLUMN_LOCATION_DATA_ALTITUDE = "altitude";
	
	private static final String COLUMN_LOCATION_DATA_PROVIDER = "provider";
	
	public RunDatabaseHelper(Context context){
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL(CREATE_TABLE_RUN);
		db.execSQL(CREATE_TABLE_LOCATION_DATA);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	
	public long insertRun(Run run){
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(COLUMN_RUN_START_DATE, run.getStartDate().getTime());
		return getWritableDatabase().insert(TABLE_RUN_NAME, null, contentValues);
	}
	
	public long insertLocation(LocationData locationData){
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(COLUMN_LOCATION_DATA_FK_RUN_ID, locationData.getFKRunId());
		contentValues.put(COLUMN_LOCATION_DATA_TIMESTAMP
				, locationData.getTimestamp().getTime());
		contentValues.put(COLUMN_LOCATION_DATA_LATITUDE, locationData.getLatitude());
		contentValues.put(COLUMN_LOCATION_DATA_LONGITUDE, locationData.getLongitude());
		contentValues.put(COLUMN_LOCATION_DATA_ALTITUDE, locationData.getAltitude());
		contentValues.put(COLUMN_LOCATION_DATA_PROVIDER, locationData.getProvider());
		return getReadableDatabase().insert(TABLE_LOCATION_DATA_NAME, null, contentValues);
	}
	
	public RunCursor queryRunList(){
		Cursor wrapped = getReadableDatabase()
				.query(TABLE_RUN_NAME, null, null, null, null, null
						, COLUMN_RUN_START_DATE + " desc");
		return new RunCursor(wrapped);
	}
	
	public RunCursor queryRunById(long runId){
		Cursor wrapped = getReadableDatabase()
				.query(
						TABLE_RUN_NAME
						, null
						, COLUMN_RUN_RUN_ID + "=?"
						, new String[]{String.valueOf(runId)}
						, null
						, null
						, COLUMN_RUN_START_DATE + " desc"
						,"1");
		return new RunCursor(wrapped);
	}

	public static class RunCursor extends CursorWrapper{

		public RunCursor(Cursor cursor) {
			super(cursor);
		}
		
		public Run getRun(){
			
			if(isBeforeFirst() || isAfterLast()){
				return null;
			}
			Run run = new Run();
			long runId = getLong(getColumnIndex(COLUMN_RUN_RUN_ID));
			Date startDate = new Date(getLong(getColumnIndex(COLUMN_RUN_START_DATE)));
			
			run.setRunId(runId);
			run.setStartDate(startDate);
			return run;
		}
		
	}
	
}

package com.bignerdranchldp.android.runtracker.db;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bignerdranchldp.android.runtracker.domain.LocationData;
import com.bignerdranchldp.android.runtracker.domain.Run;

public class RunDatabaseHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME="RunTracker.db";
	
	private static final int VERSION = 1;
	
	private static final String CREATE_TABLE_RUN = 
			"create table t_run("+
				"_id integer primary key autoincrement,"+
				"run_name varchar(100),"+
				"run_state integer,"+
				"total_metre integer,"+
				"elapsed_time integer,"+
				"total_trip_point integer,"+
				"start_date integer"+
			")";
	
	private static final String TABLE_RUN_NAME = "t_run";
	
	public static final String COLUMN_RUN_RUN_ID = "_id";

	private static final String COLUMN_RUN_RUN_STATE= "run_state";
	
	private static final String COLUMN_RUN_RUN_NAME = "run_name";

	private static final String COLUMN_RUN_TOTAL_METRE = "total_metre";

	private static final String COLUMN_RUN_ELAPSED_TIME = "elapsed_time";

	private static final String COLUMN_RUN_TOTAL_TRIP_POINT = "total_trip_point";

	private static final String COLUMN_RUN_START_DATE = "start_date";
	
	private static final String CREATE_TABLE_LOCATION_DATA = 
			"create table t_location_data("+
				"_id integer primary key autoincrement,"+
				"fk_run_id integer references t_run(run_id),"+
				"timestamp integer,"+
				"latitude real,"+
				"longitude real,"+
				"accuracy real,"+
				"altitude real,"+
				"provider varchar(100)"+
			")";
	
	private static final String TABLE_LOCATION_DATA_NAME = "t_location_data";
	
	private static final String COLUMN_LOCATION_DATA_LOCATION_DATA_ID = "_id";
	
	private static final String COLUMN_LOCATION_DATA_FK_RUN_ID = "fk_run_id";
	
	private static final String COLUMN_LOCATION_DATA_TIMESTAMP = "timestamp";
	
	private static final String COLUMN_LOCATION_DATA_LATITUDE = "latitude";
	
	private static final String COLUMN_LOCATION_DATA_LONGITUDE = "longitude";
	
	private static final String COLUMN_LOCATION_DATA_ACCURACY = "accuracy";
	
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
	
	public synchronized long insertRun(Run run){
		
		ContentValues contentValues = new ContentValues();
		
		contentValues.put(COLUMN_RUN_RUN_STATE, run.getRunState());
		contentValues.put(COLUMN_RUN_RUN_NAME, run.getRunName());
		contentValues.put(COLUMN_RUN_TOTAL_METRE, run.getTotalMetre());
		contentValues.put(COLUMN_RUN_ELAPSED_TIME, run.getElapsedTime());
		contentValues.put(COLUMN_RUN_TOTAL_TRIP_POINT, run.getTotalTripPoint());
		contentValues.put(COLUMN_RUN_START_DATE, run.getStartDate().getTime());
		return getWritableDatabase().insert(TABLE_RUN_NAME, null, contentValues);
	}

	public synchronized long updateRun(Run run){
		
		ContentValues contentValues = new ContentValues();
		
		contentValues.put(COLUMN_RUN_RUN_STATE, run.getRunState());
		contentValues.put(COLUMN_RUN_RUN_NAME, run.getRunName());
		contentValues.put(COLUMN_RUN_TOTAL_METRE, run.getTotalMetre());
		contentValues.put(COLUMN_RUN_ELAPSED_TIME, run.getElapsedTime());
		contentValues.put(COLUMN_RUN_TOTAL_TRIP_POINT, run.getTotalTripPoint());
		contentValues.put(COLUMN_RUN_START_DATE, run.getStartDate().getTime());
		return getWritableDatabase()
				.update(
						TABLE_RUN_NAME
						, contentValues
						, COLUMN_RUN_RUN_ID + "=?"
						, new String[]{String.valueOf(run.getRunId())}
						);
	}
	
	public synchronized int deleteRunById(long runId){
		
		return getWritableDatabase()
			.delete(
					TABLE_RUN_NAME
					, COLUMN_RUN_RUN_ID + "=?"
					, new String[]{ String.valueOf(runId) }
					);
	}
	
	public synchronized long insertLocationData(LocationData locationData){
		
		ContentValues contentValues = new ContentValues();
		contentValues.put(COLUMN_LOCATION_DATA_FK_RUN_ID, locationData.getFKRunId());
		contentValues.put(COLUMN_LOCATION_DATA_TIMESTAMP
				, locationData.getTimestamp().getTime());
		contentValues.put(COLUMN_LOCATION_DATA_LATITUDE, locationData.getLatitude());
		contentValues.put(COLUMN_LOCATION_DATA_LONGITUDE, locationData.getLongitude());
		contentValues.put(COLUMN_LOCATION_DATA_ACCURACY, locationData.getAccuracy());
		contentValues.put(COLUMN_LOCATION_DATA_ALTITUDE, locationData.getAltitude());
		contentValues.put(COLUMN_LOCATION_DATA_PROVIDER, locationData.getProvider());
		return getReadableDatabase().insert(TABLE_LOCATION_DATA_NAME, null, contentValues);
	}
	
	public synchronized long deleteLocationDataByRunId(long runId){
		
		return getWritableDatabase()
				.delete(
						TABLE_LOCATION_DATA_NAME
						, COLUMN_LOCATION_DATA_FK_RUN_ID + "=?"
						, new String[]{ String.valueOf(runId) }
						);
	}
	
	public RunCursor queryRunList(){
		Cursor wrapped = getReadableDatabase()
				.query(TABLE_RUN_NAME
						, null
						, COLUMN_RUN_RUN_STATE + "=?"
						, new String[]{ String.valueOf(Run.STATE_NORMAL) }
						, null
						, null
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
	
	public LocationDataCursor queryLatestLocationDataByRunId(long runId){
		
		Cursor cursor = getReadableDatabase().query(
				TABLE_LOCATION_DATA_NAME
				, null
				, COLUMN_LOCATION_DATA_FK_RUN_ID + "=?"
				, new String[]{String.valueOf(runId)}
				, null
				, null
				, COLUMN_LOCATION_DATA_TIMESTAMP + " desc"
				, "1");
		
		return new LocationDataCursor(cursor);
	}
	
	public LocationDataCursor queryLocationDataListByRunId(long runId){
		
		Cursor cursor = getReadableDatabase().query(
				TABLE_LOCATION_DATA_NAME
				, null
				, COLUMN_LOCATION_DATA_FK_RUN_ID + "=?"
				, new String[]{ String.valueOf(runId) }
				, null
				, null
				, COLUMN_LOCATION_DATA_TIMESTAMP + " asc");
		
		return new LocationDataCursor(cursor);
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
			int runState = getInt(getColumnIndex(COLUMN_RUN_RUN_STATE));
			String runName = getString(getColumnIndex(COLUMN_RUN_RUN_NAME));
			long totalMetre = getLong(getColumnIndex(COLUMN_RUN_TOTAL_METRE));
			long elapsedTime = getLong(getColumnIndex(COLUMN_RUN_ELAPSED_TIME));
			int totalTripPoint = getInt(getColumnIndex(COLUMN_RUN_TOTAL_TRIP_POINT));
			Date startDate = new Date(getLong(getColumnIndex(COLUMN_RUN_START_DATE)));
			
			run.setRunId(runId);
			run.setRunState(runState);
			run.setRunName(runName);
			run.setTotalMetre(totalMetre);
			run.setElapsedTime(elapsedTime);
			run.setTotalTripPoint(totalTripPoint);
			run.setStartDate(startDate);
			return run;
		}
		
	}
	
	public static class LocationDataCursor extends CursorWrapper{

		public LocationDataCursor(Cursor cursor) {
			super(cursor);
		}
		
		public LocationData getLocationData(){
			
			if(isBeforeFirst() || isAfterLast()){
				return null;
			}
			
			LocationData locaData = new LocationData();
			locaData.setLocationDataId(getLong(getColumnIndex(COLUMN_LOCATION_DATA_LOCATION_DATA_ID)));
			locaData.setFKRunId(getLong(getColumnIndex(COLUMN_LOCATION_DATA_FK_RUN_ID)));
			
			Date timestamp = new Date(getLong(getColumnIndex(COLUMN_LOCATION_DATA_TIMESTAMP)));
			locaData.setTimestamp(timestamp);
			
			locaData.setLatitude(getDouble(getColumnIndex(COLUMN_LOCATION_DATA_LATITUDE)));
			locaData.setLongitude(getDouble(getColumnIndex(COLUMN_LOCATION_DATA_LONGITUDE)));
			locaData.setAccuracy(getDouble(getColumnIndex(COLUMN_LOCATION_DATA_ACCURACY)));
			locaData.setAltitude(getDouble(getColumnIndex(COLUMN_LOCATION_DATA_ALTITUDE)));
			locaData.setProvider(getString(getColumnIndex(COLUMN_LOCATION_DATA_PROVIDER)));
			
			return locaData;
		}
		
	}
	
}

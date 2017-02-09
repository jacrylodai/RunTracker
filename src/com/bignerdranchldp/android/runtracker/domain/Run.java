package com.bignerdranchldp.android.runtracker.domain;

import java.util.Date;

/**
 * 旅程类
 * @author jacrylodai
 *
 */
public class Run {
	
	/**
	 * 正在被记录的旅程
	 */
	public static final int STATE_CURRENT_TRACKING = 1;
	
	/**
	 * 正常旅程
	 */
	public static final int STATE_NORMAL = 2;
	
	private long mRunId;
	
	//旅程状态
	private int mRunState;
	
	//旅程名称
	private String mRunName;

	//总路程 米
	private long mTotalMetre;

	//记录时间
	private long mElapsedTime;

	//总记录节点
	private int mTotalTripPoint;

	//创建时间
	private Date mStartDate;
	
	public long getRunId() {
		return mRunId;
	}

	public void setRunId(long runId) {
		mRunId = runId;
	}

	public int getRunState() {
		return mRunState;
	}

	public void setRunState(int runState) {
		mRunState = runState;
	}

	public void setStartDate(Date startDate) {
		mStartDate = startDate;
	}
	
	public Date getStartDate() {
		return mStartDate;
	}

	public String getRunName() {
		return mRunName;
	}

	public void setRunName(String runName) {
		mRunName = runName;
	}

	public long getTotalMetre() {
		return mTotalMetre;
	}

	public void setTotalMetre(long totalMetre) {
		mTotalMetre = totalMetre;
	}

	public long getElapsedTime() {
		return mElapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		mElapsedTime = elapsedTime;
	}

	public int getTotalTripPoint() {
		return mTotalTripPoint;
	}

	public void setTotalTripPoint(int totalTripPoint) {
		mTotalTripPoint = totalTripPoint;
	}

	public int getDurationSeconds(long endMils){
		
		return (int)((endMils - mStartDate.getTime())/1000);
	}
	
	public static String formatDuration(int durationSeconds){
		
		int seconds = durationSeconds%60;
		int leftMinutes = durationSeconds/60;
		int minutes = leftMinutes%60;
		int hours = leftMinutes/60;
		
		return String.format("%02d:%02d:%02d", hours,minutes,seconds);
	}
	
}

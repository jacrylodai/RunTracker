package com.bignerdranch.android.runtracker.domain;

import java.util.Date;

public class Run {

	private Date mStartDate;
	
	public Run(){
		mStartDate = new Date();
	}

	public void setStartDate(Date startDate) {
		mStartDate = startDate;
	}
	
	public Date getStartDate() {
		return mStartDate;
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

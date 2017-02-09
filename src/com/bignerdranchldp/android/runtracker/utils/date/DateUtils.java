package com.bignerdranchldp.android.runtracker.utils.date;

import java.util.Date;

import android.content.Context;
import android.text.format.DateFormat;

public class DateUtils {

	public static String formatFullDate(Context context,Date date){
		
		String dateStr = 
				DateFormat.getMediumDateFormat(context).format(date) + " " +
				DateFormat.getTimeFormat(context).format(date);
		return dateStr;
	}
	
}

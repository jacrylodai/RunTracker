package com.bignerdranch.android.runtracker.util;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;

public class LocationUtils {

	public static LatLng convertGPSToBaiduPoint(LatLng sourceLatLng){
		
		// 将GPS设备采集的原始GPS坐标转换成百度坐标  
		CoordinateConverter converter  = new CoordinateConverter();  
		converter.from(CoordType.GPS);  
		// sourceLatLng待转换坐标  
		converter.coord(sourceLatLng);  
		LatLng desLatLng = converter.convert();
		return desLatLng;
	}
	
}

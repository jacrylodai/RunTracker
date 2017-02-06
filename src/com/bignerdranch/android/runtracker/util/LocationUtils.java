package com.bignerdranch.android.runtracker.util;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;

public class LocationUtils {

	public static LatLng convertGPSToBaiduPoint(LatLng sourceLatLng){
		
		// ��GPS�豸�ɼ���ԭʼGPS����ת���ɰٶ�����  
		CoordinateConverter converter  = new CoordinateConverter();  
		converter.from(CoordType.GPS);  
		// sourceLatLng��ת������  
		converter.coord(sourceLatLng);  
		LatLng desLatLng = converter.convert();
		return desLatLng;
	}
	
}

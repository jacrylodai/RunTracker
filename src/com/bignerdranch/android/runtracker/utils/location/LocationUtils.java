package com.bignerdranch.android.runtracker.utils.location;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.baidu.mapapi.utils.DistanceUtil;
import com.bignerdranch.android.runtracker.manager.RunManager;

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
	
	public static List<LatLng> getFinalTripPoint(List<LatLng> pointList){
		
		List<LatLng> finalPointList = new ArrayList<LatLng>();
		if(pointList.size() <= 2){
			for(int i=0;i<pointList.size();i++){
				LatLng point = pointList.get(i);
				finalPointList.add(point);
			}
			return finalPointList;
		}
		
		//ȥ���ظ��Ľڵ�õ��������ó̵�
		//������ͣ����һ��λ��ʱ���ͻ�����ܶ��ظ��Ľڵ㣬ȥ����Щ�ظ��Ľڵ�
		LatLng lastLL = pointList.get(0);
		finalPointList.add(lastLL);
		for(int i=1;i<pointList.size();i++){
			LatLng pointLL = pointList.get(i);
			double distance = DistanceUtil.getDistance(lastLL, pointLL);
						
			if(distance > RunManager.MIN_TRIP_DISTANCE){
				lastLL = pointLL;
				finalPointList.add(lastLL);
			}else{
				//���С����С��࣬�Ǿͺ��Ե�ǰ�ڵ㣬˵���п���ͣ����һ���ط�
				//do nothing
				//������յ㣬�ͼ��뵽�б���
				if(i == pointList.size()-1){
					lastLL = pointLL;
					finalPointList.add(lastLL);
				}
			}
		}
		return finalPointList;
	}
	
	public static double caculateTotalDistance(List<LatLng> finalPointList){

		if(finalPointList.size() <= 1){
			return 0;
		}
		
		double totalDistance = 0;
		
		for(int i=1;i<finalPointList.size();i++){
			LatLng previousLL = finalPointList.get(i-1);
			LatLng pointLL = finalPointList.get(i);
			double distance = DistanceUtil.getDistance(previousLL, pointLL);
			totalDistance += distance;
		}
		
		return totalDistance;
	}
	
}

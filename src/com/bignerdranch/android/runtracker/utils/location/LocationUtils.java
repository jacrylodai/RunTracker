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
		
		// 将GPS设备采集的原始GPS坐标转换成百度坐标  
		CoordinateConverter converter  = new CoordinateConverter();  
		converter.from(CoordType.GPS);  
		// sourceLatLng待转换坐标  
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
		
		//去除重复的节点得到的最终旅程点
		//但长期停留在一个位置时，就会产生很多重复的节点，去掉这些重复的节点
		LatLng lastLL = pointList.get(0);
		finalPointList.add(lastLL);
		for(int i=1;i<pointList.size();i++){
			LatLng pointLL = pointList.get(i);
			double distance = DistanceUtil.getDistance(lastLL, pointLL);
						
			if(distance > RunManager.MIN_TRIP_DISTANCE){
				lastLL = pointLL;
				finalPointList.add(lastLL);
			}else{
				//如果小于最小间距，那就忽略当前节点，说明有可能停留在一个地方
				//do nothing
				//如果是终点，就加入到列表中
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

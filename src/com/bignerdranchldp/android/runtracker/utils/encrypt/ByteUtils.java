package com.bignerdranchldp.android.runtracker.utils.encrypt;

public class ByteUtils {

	/**
	 * �Ѽ��ܺ��byte[]ת��Ϊhex String
	 * @param byteArr
	 * @return
	 */
	public static String byteArrayToHexString(byte[] byteArr){
		
		if(byteArr == null || byteArr.length ==0){
			return "";
		}
		
		StringBuffer sb = new StringBuffer(32);
		for(byte b:byteArr){
			int byteInt = b & 0x00FF;
			String hexStr = Integer.toHexString(byteInt);
			if(hexStr.length() == 1){
				hexStr = "0"+hexStr;
			}
			sb.append(hexStr);
		}
		return sb.toString();
	}
	
}

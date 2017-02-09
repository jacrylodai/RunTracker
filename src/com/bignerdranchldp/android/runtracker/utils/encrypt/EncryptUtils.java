package com.bignerdranchldp.android.runtracker.utils.encrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtils {
	
	public static final String MD5_KEY = "MD5";
	
	public static final String SHA_KEY = "SHA";

	/**
	 * �������ַ�������MD5����
	 * @param input
	 * @return
	 */
	public static byte[] encryptByMD5(byte[] input){
		
		byte[] result = null;
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(MD5_KEY);
			messageDigest.update(input);
			result =  messageDigest.digest();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	public static byte[] encryptBySHA(byte[] input){
		
		byte[] result = null;
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(SHA_KEY);
			messageDigest.update(input);
			result =  messageDigest.digest();
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * ���������ݽ�����ϼ��ܣ�md5->sha
	 * @param input
	 * @return
	 */
	public static byte[] encryptByMD5SHA(byte [] input){

		byte[] result = encryptBySHA(encryptByMD5(input));
		return result;
	}
}

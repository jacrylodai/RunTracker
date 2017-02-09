package com.bignerdranch.android.runtracker.utils.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 流转换工具
 * @author jacrylodai
 *
 */
public class StreamTools {

	/**
	 * 从输入流中读取数据，返回字符串
	 * @param in 输入流
	 * @param encoding 编码方式
	 * @return
	 * @throws IOException
	 */
	public static String readFromStream(InputStream in,String encoding)
		throws IOException {
		
		BufferedInputStream buffIn = new BufferedInputStream(in);
		
		ByteArrayOutputStream byteArrayOut = 
				new ByteArrayOutputStream(300);
		BufferedOutputStream buffOut = new BufferedOutputStream(byteArrayOut);
		
		byte [] buf = new byte [500];
		int length;
		while( (length=buffIn.read(buf)) != -1 ){
			
			buffOut.write(buf, 0, length);
		}
		buffIn.close();
		buffOut.close();
		
		byte[] byteArray = byteArrayOut.toByteArray();
		String result = new String(byteArray,encoding);
		return result;
	}
	
}

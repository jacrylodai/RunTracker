package com.bignerdranch.android.runtracker.utils.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ��ת������
 * @author jacrylodai
 *
 */
public class StreamTools {

	/**
	 * ���������ж�ȡ���ݣ������ַ���
	 * @param in ������
	 * @param encoding ���뷽ʽ
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

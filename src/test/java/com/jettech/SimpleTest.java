package com.jettech;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.jettech.util.DateUtil;

public class SimpleTest {

	public static void main(String[] args) throws ParseException {
		String oldDateStr = "2016-10-15T00:00:00.000+08:00";
		//此格式只有  jdk 1.7才支持  yyyy-MM-dd'T'HH:mm:ss.SSSXXX      
		
		 DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");  //yyyy-MM-dd'T'HH:mm:ss.SSSZ
		 Date date = df.parse(oldDateStr);
		 System.out.println(date);
		 
		 SimpleDateFormat df1 = new SimpleDateFormat ("EEE MMM dd HH:mm:ss Z yyyy", Locale.UK);
		 System.out.println(df1.parse(date.toString()));
		 System.out.println(df1.format(date));
		 
		 DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		 System.out.println(df2.format(date));
		 
		

	}

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jettech.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author gu
 */
public class DateUtil {
	public static final String DATE_PATTERN = "yyyy-MM-dd";
	public static final String TIME_PATTREN = "yyyy-MM-dd HH:mm:ss";
	public static final String TIME_PATTREN_FULL = "yyyy-MM-dd HH:mm:ss.S";
	public static final String TIME_PATTREN_COMPACTED = "yyyyMMddHHmmss";
	public static final String TIME_PATTREN_COMPACTED2 = "yyyy-MM-dd_HH-mm-ss";
	public static final String TIME_PATTREN_SHORT = "MMddHHmmss";// MMDDhhmmss
	public static final String CRON_DATE_FORMAT = "ss mm HH dd MM ? yyyy";

	public static Logger logger = LoggerFactory.getLogger(DateUtil.class);

	public static String getDateStr(Date date, String pattern) {
		SimpleDateFormat df = new SimpleDateFormat(pattern);
		df.setLenient(false);
		return df.format(date);
	}

	public static String getNowStr(String pattern) {
		return getDateStr(getNowFull(), pattern);
	}

	public static Date getDate(String dateStr, String pattern) {
		try {
			SimpleDateFormat df = new SimpleDateFormat(pattern);
			df.setLenient(false);
			return df.parse(dateStr);
		} catch (ParseException ex) {
			logger.error(ex.getMessage());
			return null;

		}
	}

	public static Date addDays(Date date, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, days);
		return c.getTime();
	}

	/**
	 * 获取一个日期某一部分的值
	 * 
	 * @param date
	 * @param fiedNum
	 * @return
	 */
	public static int getValueFromDate(Date date, int fiedNum) {

		Calendar c = Calendar.getInstance();

		c.setTime(date);

		return c.get(fiedNum);
	}

	/**
	 * 把一个标准日期转化成formart格式的日期
	 * 
	 * @param date
	 * @param formart
	 * @return
	 */
	public static Date getFormatDate(Date date, String formart) {

		String desc = DateUtil.getDateStr(date, formart);

		return DateUtil.getDate(desc, formart);

	}

	/**
	 * 对一个日期的某一部分加减操作
	 * 
	 * @param date
	 *            日期
	 * @param partOfDate
	 *            年，月，日对应的整数
	 * @param increment
	 *            增量
	 * @return
	 */
	public static Date operationDate(Date date, int partOfDate, int increment) {

		Calendar calendar = Calendar.getInstance();

		calendar.setTime(date);

		calendar.add(partOfDate, increment);

		Date currentDate = calendar.getTime();

		return currentDate;
	}

	/**
	 * 根据开始日期和结束日期求出时间范围的集合
	 * 
	 * @param startTime
	 *            开始日期
	 * @param endTime
	 *            结束日期
	 * @return List<Date>
	 */
	public static List<Date> getThePeriodOfTwoDates(Date startTime, Date endTime) {

		if (!startTime.before(endTime) && !startTime.equals(endTime)) {

			throw new RuntimeException("开始日期不能大于结束日期");

		}

		Calendar start = Calendar.getInstance();

		start.setTime(startTime);

		Calendar end = Calendar.getInstance();

		end.setTime(endTime);

		int min = start.get(Calendar.DAY_OF_YEAR);

		int max = end.get(Calendar.DAY_OF_YEAR);

		List<Date> dates = new ArrayList<Date>();

		for (int i = 0; i <= max - min; i++) {

			start.add(Calendar.DAY_OF_YEAR, i);

			dates.add(start.getTime());

			start.setTime(startTime);
		}

		return dates;

	}

	/**
	 * 获取系统当前时间
	 * 
	 * @return yyyy-MM-dd HH:mm:ss
	 */
	public static Timestamp getNow() {
		DateFormat format = new SimpleDateFormat(TIME_PATTREN);
		String now = format.format(new Date());
		Timestamp ts = Timestamp.valueOf(now);
		return ts;
	}

	public static Timestamp getNowFull() {
		DateFormat format = new SimpleDateFormat(TIME_PATTREN_FULL);
		String now = format.format(new Date());
		Timestamp ts = Timestamp.valueOf(now);
		return ts;
	}

	static public String getTimeSpin(Date startDate, Date endDate) {
		return getTimeSpin(startDate.getTime(), endDate.getTime());
	}

	public static String getEclapsedTimesStr(long beginTime) {
		return getEclapsedTimesStr(beginTime, System.currentTimeMillis());
	}

	public static String getEclapsedTimesStr(long beginTime, long endTime) {
		long elapsed = endTime - beginTime;
		long hour = elapsed / (60 * 60 * 1000);
		long minute = elapsed / (60 * 1000) - hour * 60;
		long second = elapsed / 1000 - hour * 60 * 60 - minute * 60;
		String elapsedTime = null;
		if (hour > 0) {
			elapsedTime = String.format("%d hour %d minute %d.%d second", hour, minute, second, elapsed % 1000);
		} else if (minute > 0) {
			elapsedTime = String.format("%d minute %d.%d second", minute, second, elapsed % 1000);
		} else {
			elapsedTime = String.format("%d.%d second", second, elapsed % 1000);
		}
		return elapsedTime;
	}

	static public String getTimeSpin(long start, long end) {
		int hours = (int) ((end - start) / (1000 * 60 * 60));
		int minutes = (int) (end - start - hours * 1000 * 60 * 60) / (1000 * 60);
		int seconds = (int) (end - start - hours * 1000 * 60 * 60 - minutes * 1000 * 60) / 1000;
		long millisecond = end - start - hours * 1000 * 60 * 60 - minutes * 1000 * 60 - seconds * 1000;
		if (hours > 0) {
			return hours + " hour " + minutes + " minutes " + seconds + "." + millisecond + " second";
		} else if (minutes > 0) {
			return minutes + " minutes " + seconds + "." + millisecond + " second";
		} else {
			return seconds + "." + millisecond + " second";
		}
	}

	public static void main(String[] args) throws Exception {
		// testFun();
		System.out.println(getNowStr(TIME_PATTREN_COMPACTED2));
//		testgetTimeSpin();
	}

	private static void testgetTimeSpin() throws InterruptedException {
		Date d1 = new Date();
		Thread.currentThread().sleep(12340);
		Date d2 = new Date();
		System.out.println(getTimeSpin(d1, d2));
	}

	private static void testFun() {
		// String defaultValue = "fun(now,'MMddhhmmss')";
		String defaultValue = "fun(now,MMDDhhmmss)";
		if (defaultValue.trim().toLowerCase().startsWith("fun(")) {
			defaultValue = defaultValue.trim();
			String fun = defaultValue.substring(4, defaultValue.indexOf(",")).trim().toLowerCase();
			if (fun.equals("now")) {
				String format = defaultValue.substring(defaultValue.indexOf(",") + 1,
						defaultValue.indexOf("") > 0 ? defaultValue.indexOf("") : defaultValue.length() - 1);
				format = format.replace("'", "");
				String dateStr = DateUtil.getDateStr(new Date(), format);
				System.out.println(dateStr);
				// req.put(element.getAlias(), dateStr);
			} else {
				// logger.error(String.format("%s 报文单元 %s 默认值 %s 格式错误.",
				// message.getName(), element.getAlias(),
				// element.getDefaultValue()));
			}
		}
	}
	public static String getDate(Date date) {
		String dateStr = "";     
        //format的格式可以任意     
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
        try {   
            dateStr = sdf.format(date);   

        } catch (Exception e) {   
            e.printStackTrace();   
        } 
        return dateStr;
	}
	
//	/***
//    * 日期转cron
//    */
//   public static String getCron(Date date){
//       SimpleDateFormat sdf = new SimpleDateFormat(CRON_DATE_FORMAT);
//       String formatTimeStr = "";
//       formatTimeStr = sdf.format(date);
//       return formatTimeStr;
//   }
//
//   /***
//    *cron转日期
//    */
//   public static Date getDate(String cron) {
//       SimpleDateFormat sdf = new SimpleDateFormat(TIME_PATTREN_COMPACTED2);
//       Date date = null;
//       try {
//           date = sdf.parse(cron);
//       } catch (ParseException e) {
//    	   e.printStackTrace();
//       }
//       return date;
//   }
}

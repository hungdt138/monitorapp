package com.fss.util;

import java.text.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: FPT - FSS</p>
 * @author not Thai Hoang Hiep
 * @version 1.0
 */

public class DateUtil
{
	////////////////////////////////////////////////////////
	/**
	 * Check string format with current format locale
	 * @param strSource String to check
	 * @return boolean true if strSource represent a date, otherwise false
	 */
	////////////////////////////////////////////////////////
	public static boolean isDate(String strSource)
	{
		return isDate(strSource,DateFormat.getDateInstance());
	}
	////////////////////////////////////////////////////////
	/**
	 * Check string format
	 * @param strSource String
	 * @param strFormat Format to check
	 * @return boolean true if strSource represent a date, otherwise false
	 */
	////////////////////////////////////////////////////////
	public static boolean isDate(String strSource,String strFormat)
	{
		SimpleDateFormat fmt = new SimpleDateFormat(strFormat);
		fmt.setLenient(false);
		return isDate(strSource,fmt);
	}
	////////////////////////////////////////////////////////
	/**
	 * Check string format
	 * @param strSource String
	 * @param fmt Format to check
	 * @return boolean true if strSource represent a date, otherwise false
	 */
	////////////////////////////////////////////////////////
	public static boolean isDate(String strSource,DateFormat fmt)
	{
		try
		{
			if(fmt.parse(strSource) == null)
				return false;
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert string to date using current format locale
	 * @param strSource String to convert
	 * @return Date converted, null if conversion failure
	 */
	////////////////////////////////////////////////////////
	public static Date toDate(String strSource)
	{
		return toDate(strSource,DateFormat.getDateInstance());
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert string to date
	 * @param strSource String to convert
	 * @param strFormat Format to convert
	 * @return Date converted, null if conversion failure
	 */
	////////////////////////////////////////////////////////
	public static Date toDate(String strSource,String strFormat)
	{
		SimpleDateFormat fmt = new SimpleDateFormat(strFormat);
		fmt.setLenient(false);
		return toDate(strSource,fmt);
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert string to date
	 * @param strSource String to convert
	 * @param fmt Format to convert
	 * @return Date converted, null if conversion failure
	 */
	////////////////////////////////////////////////////////
	public static Date toDate(String strSource,DateFormat fmt)
	{
		try
		{
			return fmt.parse(strSource);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	////////////////////////////////////////////////////////
	/**
	 * Add date value by second
	 * @param dt Date Date to add
	 * @param iValue int value to add
	 * @return Date after add
	 */
	////////////////////////////////////////////////////////
	public static Date addSecond(Date dt,int iValue)
	{
		return add(dt,iValue,Calendar.SECOND);
	}
	////////////////////////////////////////////////////////
	/**
	 * Add date value by minute
	 * @param dt Date Date to add
	 * @param iValue int value to add
	 * @return Date after add
	 */
	////////////////////////////////////////////////////////
	public static Date addMinute(Date dt,int iValue)
	{
		return add(dt,iValue,Calendar.MINUTE);
	}
	////////////////////////////////////////////////////////
	/**
	 * Add date value by hour
	 * @param dt Date Date to add
	 * @param iValue int value to add
	 * @return Date after add
	 */
	////////////////////////////////////////////////////////
	public static Date addHour(Date dt,int iValue)
	{
		return add(dt,iValue,Calendar.HOUR);
	}
	////////////////////////////////////////////////////////
	/**
	 * Add date value by day
	 * @param dt Date Date to add
	 * @param iValue int value to add
	 * @return Date after add
	 */
	////////////////////////////////////////////////////////
	public static Date addDay(Date dt,int iValue)
	{
		return add(dt,iValue,Calendar.DATE);
	}
	////////////////////////////////////////////////////////
	/**
	 * Add date value by month
	 * @param dt Date Date to add
	 * @param iValue int value to add
	 * @return Date after add
	 */
	////////////////////////////////////////////////////////
	public static Date addMonth(Date dt,int iValue)
	{
		return add(dt,iValue,Calendar.MONTH);
	}
	////////////////////////////////////////////////////////
	/**
	 * Add date value by year
	 * @param dt Date Date to add
	 * @param iValue int value to add
	 * @return Date after add
	 */
	////////////////////////////////////////////////////////
	public static Date addYear(Date dt,int iValue)
	{
		return add(dt,iValue,Calendar.YEAR);
	}
	////////////////////////////////////////////////////////
	/**
	 * Add date value
	 * @param dt Date Date to add
	 * @param iValue int value to add
	 * @param iType type of unit
	 * @return Date after add
	 */
	////////////////////////////////////////////////////////
	public static Date add(Date dt,int iValue,int iType)
	{
		Calendar cld = Calendar.getInstance();
		cld.setTime(dt);
		cld.add(iType,iValue);
		return cld.getTime();
	}
}

package com.fss.thread;

import java.io.*;
import java.util.*;

import com.fss.util.*;
import com.fss.dictionary.*;
import com.fss.dictionary.Dictionary;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ScheduleUtil
{
	////////////////////////////////////////////////////////
	public static final int STATUS_READY = 0;
	public static final int STATUS_WAIT = 1;
	public static final int STATUS_NEED_CHANGE = 2;
	////////////////////////////////////////////////////////
	/**
	 * Analyse schedule status
	 * @param dic Dictionary represent schedule script
	 * @return true if can run now, otherwise false
	 */
	////////////////////////////////////////////////////////
	public static int getStatus(Dictionary dic)
	{
		Date dtExpectedDate = getExpectedDate(dic);
		int iExecutionCount = getExecutionCount(dic);
		return getStatus(dic,dtExpectedDate,iExecutionCount);
	}
	////////////////////////////////////////////////////////
	/**
	 * Get execution count
	 * @param dic Dictionary represent schedule script
	 * @return execution count
	 */
	////////////////////////////////////////////////////////
	public static int getExecutionCount(Dictionary dic)
	{
		int iExecutionCount = 0;
		String strExecutionCount = StringUtil.nvl(dic.getString("ExecutionCount"),"");
		if(strExecutionCount != null && strExecutionCount.length() > 0)
			iExecutionCount = Integer.parseInt(strExecutionCount);
		return iExecutionCount;
	}
	////////////////////////////////////////////////////////
	/**
	 * Analyse schedule status
	 * @param dic Dictionary represent schedule script
	 * @return true if can run now, otherwise false
	 */
	////////////////////////////////////////////////////////
	public static int getStatus(Dictionary dic,Date dtExpectedDate,int iExecutionCount)
	{
		// Check date
		final java.text.SimpleDateFormat fmtDate = new java.text.SimpleDateFormat("yyyyMMdd");
		final java.text.SimpleDateFormat fmtTime = new java.text.SimpleDateFormat("HH:mm:ss");
		Date dtNow = new Date();
		int iResult = fmtDate.format(dtExpectedDate).compareTo(fmtDate.format(dtNow));
		if(iResult < 0)
			return STATUS_NEED_CHANGE;
		else if(iResult > 0)
			return STATUS_WAIT;

		// Check time
		String strStartTime = StringUtil.nvl(dic.getString("StartTime"),"");
		if(strStartTime != null && strStartTime.length() > 0)
		{
			if(strStartTime.compareTo(fmtTime.format(dtNow)) > 0)
				return STATUS_WAIT;
		}
		String strEndTime = StringUtil.nvl(dic.getString("EndTime"),"");
		if(strEndTime != null && strEndTime.length() > 0)
		{
			if(strEndTime.compareTo(fmtTime.format(dtNow)) < 0)
				return STATUS_NEED_CHANGE;
		}

		// Check execution time
		String strExecutionTime = StringUtil.nvl(dic.getString("ExecutionTime"),"");
		if(strExecutionTime != null && strExecutionTime.length() > 0 &&
		   !strExecutionTime.equals("0"))
		{
			if(iExecutionCount >= Integer.parseInt(strExecutionTime))
				return STATUS_NEED_CHANGE;
		}

		// Check filter
		String strWeekDay = StringUtil.nvl(dic.getString("WeekDay"),"");
		String strMonthDay = StringUtil.nvl(dic.getString("MonthDay"),"");
		String strYearMonth = StringUtil.nvl(dic.getString("YearMonth"),"");
		Calendar cld = Calendar.getInstance();
		cld.setTime(dtExpectedDate);
		if((strWeekDay.length() == 0 || strWeekDay.indexOf(String.valueOf(cld.get(Calendar.DAY_OF_WEEK)) + ",") >= 0) &&
		   (strMonthDay.length() == 0 || strMonthDay.indexOf(String.valueOf(cld.get(Calendar.DAY_OF_MONTH)) + ",") >= 0) &&
		   (strYearMonth.length() == 0 || strYearMonth.indexOf(String.valueOf(cld.get(Calendar.MONTH)) + ",") >= 0))
			return STATUS_READY; // Ready
		return STATUS_NEED_CHANGE;
	}
	////////////////////////////////////////////////////////
	/**
	 * Change expected date to next execution date from curren schedule script
	 * @param dic Dictionary represent schedule script
	 * @return Date next execution date, null if never come
	 */
	////////////////////////////////////////////////////////
	public static void changeNextDate(Dictionary dic,boolean bForceChange)
	{
		Date dt = calculateNextDate(dic,bForceChange);
		if(dt != null)
		{
			DictionaryNode nd = dic.getNode("ExpectedDate");
			nd.mstrValue = Global.FORMAT_DATE.format(dt);
			setExecutionCount(dic,0);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Set execution count
	 * @param dic Dictionary represent schedule script
	 * @return Date next execution date, null if never come
	 */
	////////////////////////////////////////////////////////
	public static void setExecutionCount(Dictionary dic,int iExecutionCount)
	{
		dic.mndRoot.setChildValue("ExecutionCount",String.valueOf(iExecutionCount));
	}
	////////////////////////////////////////////////////////
	/**
	 * Increase execution count
	 * @param dic Dictionary represent schedule script
	 * @return Date next execution date, null if never come
	 */
	////////////////////////////////////////////////////////
	public static void increaseExecutionCount(Dictionary dic)
	{
		int iExecutionCount = getExecutionCount(dic);
		iExecutionCount++;
		setExecutionCount(dic,iExecutionCount);
	}
	////////////////////////////////////////////////////////
	/**
	 * Get expected date from schedule script
	 * @param dic Dictionary represent schedule script
	 * @return expected date
	 */
	////////////////////////////////////////////////////////
	public static Date getExpectedDate(Dictionary dic)
	{
		Date dtExpectedDate = new Date();
		String strExpectedDate = dic.getString("ExpectedDate");
		if(strExpectedDate != null && strExpectedDate.length() > 0)
			dtExpectedDate = DateUtil.toDate(strExpectedDate,Global.FORMAT_DATE);
		return dtExpectedDate;
	}
	////////////////////////////////////////////////////////
	/**
	 * Calculate next execution date from curren schedule script
	 * @param dic Dictionary represent schedule script
	 * @return Date next execution date, null if never come
	 */
	////////////////////////////////////////////////////////
	public static Date calculateNextDate(Dictionary dic,boolean bForceChange,int iScheduleType)
	{
		String strAdditionValue = dic.getString("AdditionValue");
		if(strAdditionValue == null || strAdditionValue.length() == 0)
			strAdditionValue = "1";
		int iAdditionValue = Integer.parseInt(strAdditionValue);
		String strWeekDay = StringUtil.nvl(dic.getString("WeekDay"),"");
		String strMonthDay = StringUtil.nvl(dic.getString("MonthDay"),"");
		String strYearMonth = StringUtil.nvl(dic.getString("YearMonth"),"");
		Date dtExpectedDate = getExpectedDate(dic);
		Date dtNow = new Date();
		final java.text.SimpleDateFormat fmtDate = new java.text.SimpleDateFormat("yyyyMMdd");
		if(!bForceChange && fmtDate.format(dtExpectedDate).compareTo(fmtDate.format(dtNow)) >= 0)
		{
			Calendar cld = Calendar.getInstance();
			cld.setTime(dtExpectedDate);
			if((strWeekDay.length() == 0 || strWeekDay.indexOf(String.valueOf(cld.get(Calendar.DAY_OF_WEEK)) + ",") >= 0) &&
			   (strMonthDay.length() == 0 || strMonthDay.indexOf(String.valueOf(cld.get(Calendar.DAY_OF_MONTH)) + ",") >= 0) &&
			   (strYearMonth.length() == 0 || strYearMonth.indexOf(String.valueOf(cld.get(Calendar.MONTH)) + ",") >= 0))
				return cld.getTime();
		}
		int iIndex = 0;
		while(iIndex < 10000)
		{
			// Increase expected date
			if(iScheduleType == 0)
				dtExpectedDate = DateUtil.addDay(dtExpectedDate,iAdditionValue);
			else if(iScheduleType == 1)
				dtExpectedDate = DateUtil.addDay(dtExpectedDate,7 * iAdditionValue);
			else if(iScheduleType == 2)
				dtExpectedDate = DateUtil.addMonth(dtExpectedDate,iAdditionValue);
			else
				dtExpectedDate = DateUtil.addYear(dtExpectedDate,iAdditionValue);
			if(fmtDate.format(dtExpectedDate).compareTo(fmtDate.format(dtNow)) < 0)
				continue;
			iIndex++;

			// Validate expected date
			Calendar cld = Calendar.getInstance();
			cld.setTime(dtExpectedDate);
			if(strWeekDay.length() > 0 &&
			   strWeekDay.indexOf("," + String.valueOf(cld.get(Calendar.DAY_OF_WEEK)) + ",") < 0)
				continue;
			if(strMonthDay.length() > 0 &&
			   strMonthDay.indexOf("," + String.valueOf(cld.get(Calendar.DATE)) + ",") < 0)
				continue;
			if(strYearMonth.length() > 0 &&
			   strYearMonth.indexOf("," + String.valueOf(cld.get(Calendar.MONTH)) + ",") < 0)
				continue;
			return cld.getTime();
		}
		return null;
	}
	////////////////////////////////////////////////////////
	/**
	 * Calculate next execution date from curren schedule script
	 * @param dic Dictionary represent schedule script
	 * @return Date next execution date, null if never come
	 */
	////////////////////////////////////////////////////////
	public static Date calculateNextDate(Dictionary dic,boolean bForceChange)
	{
		String strScheduleType = dic.getString("ScheduleType");
		if(strScheduleType == null || strScheduleType.length() == 0)
			strScheduleType = "0";
		int iScheduleType = Integer.parseInt(strScheduleType);
		return calculateNextDate(dic,bForceChange,iScheduleType);
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert schedule script to list of schedule
	 * @param dic Dictionary
	 * @throws Exception
	 * @return Vector
	 */
	////////////////////////////////////////////////////////
	public static Vector scriptToSchedule(Dictionary dic) throws Exception
	{
		// Load schedule from script
		Vector vtSchedule = new Vector();
		if(dic.mndRoot.mvtChild != null)
		{
			for(int iIndex = 0;iIndex < dic.mndRoot.mvtChild.size();iIndex++)
			{
				DictionaryNode nd = (DictionaryNode)dic.mndRoot.mvtChild.elementAt(iIndex);
				vtSchedule.addElement(new Dictionary(new ByteArrayInputStream(nd.getValue().getBytes())));
			}
		}
		return vtSchedule;
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert list of schedule to schedule script
	 * @param vtSchedule Vector
	 * @throws Exception
	 * @return Dictionary
	 */
	////////////////////////////////////////////////////////
	public static Dictionary scheduleToScript(Vector vtSchedule) throws Exception
	{
		// Store schedule to script
		Dictionary dic = new Dictionary();
		dic.mndRoot.mvtChild = new Vector();
		for(int iIndex = 0;iIndex < vtSchedule.size();iIndex++)
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			((Dictionary)vtSchedule.elementAt(iIndex)).store(os);
			DictionaryNode nd = new DictionaryNode();
			nd.mstrName = "Schedule";
			nd.mstrValue = new String(os.toByteArray());
			dic.mndRoot.mvtChild.addElement(nd);
		}
		return dic;
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert list of schedule to schedule script
	 * @param vtSchedule Vector
	 * @throws Exception
	 * @return Dictionary
	 */
	////////////////////////////////////////////////////////
	public static String getScheduleDescription(Dictionary dic)
	{
		String strScheduleType = dic.getString("ScheduleType");
		if(strScheduleType == null || strScheduleType.length() == 0)
			strScheduleType = "0";
		int iScheduleType = Integer.parseInt(strScheduleType);
		String strAdditionValue = dic.getString("AdditionValue");
		if(strAdditionValue == null || strAdditionValue.length() == 0)
			strAdditionValue = "1";
		int iAdditionValue = Integer.parseInt(strAdditionValue);
		Date dtExpectedDate = new Date();
		String strExpectedDate = dic.getString("ExpectedDate");
		if(strExpectedDate != null && strExpectedDate.length() > 0)
			dtExpectedDate = DateUtil.toDate(strExpectedDate,Global.FORMAT_DATE);
		String strDesc = "Run every";
		if(iAdditionValue > 1)
			strDesc += " " + iAdditionValue;
		if(iScheduleType == 0)
			strDesc += " day";
		if(iScheduleType == 1)
			strDesc += " week";
		if(iScheduleType == 2)
			strDesc += " month";
		if(iScheduleType == 3)
			strDesc += " year";
		return strDesc;
	}
}

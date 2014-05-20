package com.fss.util;

import java.text.*;
import java.io.*;
import java.util.*;


/**
 * <p>Title: Global</p>
 * <p>Description: Including global variables</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Nguyen Truong Giang
 * @version 1.0
 */

public class Global
{
	//constants
	public static String CONFIG_DIR = System.getProperty("user.home","") + "/" + "fsslib/";
	public static String FILE_CONFIG = CONFIG_DIR + "config.txt";
	public static String ENCODE = "utf-8";
	public static DecimalFormat FORMAT_NUMBER = new DecimalFormat("#.#");
	public static DecimalFormat FORMAT_CURRENCY = new DecimalFormat("#,##0.#");
	public static SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("dd/MM/yyyy");
	public static SimpleDateFormat FORMAT_MONTH_YEAR = new SimpleDateFormat("MM/yyyy");
	public static SimpleDateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm:ss");
	public static SimpleDateFormat FORMAT_DATE_TIME = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	public static String FORMAT_DB_DATE = "DD/MM/YYYY";
	public static String FORMAT_DB_MONTH_YEAR = "MM/YYYY";
	public static String FORMAT_DB_TIME = "HH24:MI:SS";
	public static String FORMAT_DB_DATE_TIME = "DD/MM/YYYY HH24:MI:SS";

	public static String APP_NAME = "";
	public static String APP_VERSION = "";
	public static final boolean DEBUG = true;
	////////////////////////////////////////////////////////
	// Static initialize
	////////////////////////////////////////////////////////
	static
	{
		FORMAT_DATE.setLenient(false);
		FORMAT_TIME.setLenient(false);
		FORMAT_DATE_TIME.setLenient(false);
		try
		{
			FileUtil.forceFolderExist(CONFIG_DIR);
		}
		catch(Exception e)
		{
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Load data from Properties file in to memory
	 * @param strFileName Path to Hashtable file
	 * @returnHashtable object contain Hashtable data
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static Properties loadProperties(String strFileName) throws Exception
	{
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(strFileName);
			return loadProperties(is);
		}
		finally
		{
			FileUtil.safeClose(is);
		}
	}
	////////////////////////////////////////////////////////
	public static Properties loadProperties(InputStream is) throws Exception
	{
		Properties prtReturn = new Properties();
		prtReturn.load(is);
		return prtReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Store data from memory to Properties file
	 * @param prt Hashtable object contain Hashtable data
	 * @param strFileName Path to Hashtable file
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static void storeProperties(Properties prt,String strFileName) throws Exception
	{
		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(strFileName);
			prt.store(os,new java.util.Date().toString());
		}
		finally
		{
			FileUtil.safeClose(os);
		}
	}
	////////////////////////////////////////////////////////
	public static void storeProperties(Properties prt,OutputStream os) throws Exception
	{
		prt.store(os,new java.util.Date().toString());
	}
	////////////////////////////////////////////////////////
	/**
	 * Load data from Hashtable file in to memory
	 * @param strFileName Path to Hashtable file
	 * @returnHashtable object contain Hashtable data
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static Hashtable loadHashtable(String strFileName) throws Exception
	{
		return loadProperties(strFileName);
	}
	////////////////////////////////////////////////////////
	public static Hashtable loadHashtable(InputStream is) throws Exception
	{
		return loadProperties(is);
	}
	////////////////////////////////////////////////////////
	/**
	 * Store data from memory to Hashtable file
	 * @param prt Hashtable object contain Hashtable data
	 * @param strFileName Path to Hashtable file
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static void storeHashtable(Hashtable prt,String strFileName) throws Exception
	{
		Properties prtStore = new Properties();
		for(Enumeration e = prt.keys(); e.hasMoreElements();)
		{
			Object key = e.nextElement();
			Object val = prt.get(key);
			prtStore.put(key,val);
		}
		storeProperties(prtStore,strFileName);
	}
	////////////////////////////////////////////////////////
	public static void storeHashtable(Hashtable prt,OutputStream os) throws Exception
	{
		Properties prtStore = new Properties();
		for(Enumeration e = prt.keys(); e.hasMoreElements();)
		{
			Object key = e.nextElement();
			Object val = prt.get(key);
			prtStore.put(key,val);
		}
		storeProperties(prtStore,os);
	}
}

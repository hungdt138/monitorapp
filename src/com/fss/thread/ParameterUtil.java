package com.fss.thread;

import java.util.*;

import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ParameterUtil
{
	////////////////////////////////////////////////////////
	// Purpose: Create a parameter defintion whith empty description
	// Inputs: Name, Value, Type, Definition of parameter
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public static Vector createParameterDefinition(String strName,Object objValue,String strType,Object objDefinition)
	{
		return createParameterDefinition(strName,objValue,strType,objDefinition,"");
	}
	////////////////////////////////////////////////////////
	// Purpose: Create a parameter defintion
	// Inputs: Name, Value, Type, Definition, Descriton of parameter
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public static Vector createParameterDefinition(String strName,Object objValue,String strType,Object objDefinition,String strDescription)
	{
		return createParameterDefinition(strName,objValue,strType,objDefinition,strDescription,"");
	}
	////////////////////////////////////////////////////////
	// Purpose: Create a parameter defintion
	// Inputs: Name, Value, Type, Definition, Descriton of parameter
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public static Vector createParameterDefinition(String strName,Object objValue,String strType,Object objDefinition,String strDescription,String strIndex)
	{
		Vector vtReturn = new Vector();
		vtReturn.addElement(strName);
		vtReturn.addElement(objValue);
		vtReturn.addElement(strType);
		vtReturn.addElement(objDefinition);
		vtReturn.addElement(strDescription);
		vtReturn.addElement(strIndex);
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	// Purpose: Load and validate thread parameter from mprtParam
	// Author: HiepTH
	// Date: 29/11/2003
	// Helper function
	// Validate thread paramater
	////////////////////////////////////////////////////////
	public static String loadMandatory(String strParameterName,String strParameterValue) throws AppException
	{
		if(strParameterValue.length() == 0)
			throw new AppException("Value of '" + strParameterName + "' can not be null","ManageableThread.loadMandatory",strParameterName);
		return strParameterValue;
	}
	////////////////////////////////////////////////////////
	public static long loadUnsignedLong(String strParameterName,String strParameterValue) throws AppException
	{
		long lReturn = loadLong(strParameterName,strParameterValue);
		if(lReturn < 0)
			throw new AppException("Value of '" + strParameterName + "' ('" + strParameterValue + "') must be a positive number","ManageableThread.loadUnsignedLong",strParameterName);
		return lReturn;
	}
	////////////////////////////////////////////////////////
	public static long loadLong(String strParameterName,String strParameterValue) throws AppException
	{
		try
		{
			return Long.parseLong(strParameterValue);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new AppException("Value of '" + strParameterName + "' ('" + strParameterValue + "') must be a number","ManageableThread.loadLong",strParameterName);
		}
	}
	////////////////////////////////////////////////////////
	public static int loadInteger(String strParameterName,String strParameterValue) throws AppException
	{
		try
		{
			return Integer.parseInt(strParameterValue);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new AppException("Value of '" + strParameterName + "' ('" + strParameterValue + "') must be a number","ManageableThread.loadInteger",strParameterName);
		}
	}
	////////////////////////////////////////////////////////
	public static double loadUnsignedDouble(String strParameterName,String strParameterValue) throws AppException
	{
		double dblReturn = loadDouble(strParameterName,strParameterValue);
		if(dblReturn < 0)
			throw new AppException("Value of '" + strParameterName + "' ('" + strParameterValue + "') must be a positive number","ManageableThread.loadUnsignedDouble",strParameterName);
		return dblReturn;
	}
	////////////////////////////////////////////////////////
	public static double loadDouble(String strParameterName,String strParameterValue) throws AppException
	{
		try
		{
			return Double.parseDouble(strParameterValue);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new AppException("Value of '" + strParameterName + "' ('" + strParameterValue + "') must be a number","ManageableThread.loadDouble",strParameterName);
		}
	}
	////////////////////////////////////////////////////////
	public static int loadUnsignedInteger(String strParameterName,String strParameterValue) throws AppException
	{
		int iReturn = loadInteger(strParameterName,strParameterValue);
		if(iReturn < 0)
			throw new AppException("Value of '" + strParameterName + "' ('" + strParameterValue + "') must be a positive number","ManageableThread.loadUnsignedInteger",strParameterName);
		return iReturn;
	}
	////////////////////////////////////////////////////////
	public static java.util.Date loadTime(String strParameterName,String strParameterValue) throws AppException
	{
		if(strParameterValue.length() > 0)
		{
			try
			{
				return Global.FORMAT_TIME.parse(strParameterValue);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new AppException("Format of '" + strParameterName + "' ('" + strParameterValue + "') must be '" + Global.FORMAT_TIME.toPattern() + "'","ManageableThread.loadTime",strParameterName);
			}
		}
		else
			return null;
	}
	////////////////////////////////////////////////////////
	public static java.util.Date loadDate(String strParameterName,String strParameterValue) throws AppException
	{
		if(strParameterValue.length() > 0)
		{
			try
			{
				return Global.FORMAT_DATE.parse(strParameterValue);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new AppException("Format of '" + strParameterName + "' ('" + strParameterValue + "') must be '" + Global.FORMAT_DATE.toPattern() + "'","ManageableThread.loadDate",strParameterName);
			}
		}
		else
			return null;
	}
	////////////////////////////////////////////////////////
	public static String loadCustomDate(String strParameterName,String strDateFormat,String strParameterValue) throws AppException
	{
		if(strParameterValue.length() > 0)
		{
			if(!DateUtil.isDate(strParameterValue,strDateFormat))
				throw new AppException("Format of '" + strParameterName + "' must be '" + strDateFormat + "'","DBThread.loadDBDate",strParameterName);
			return strParameterValue;
		}
		else
			return null;
	}
	////////////////////////////////////////////////////////
	public static String loadDirectory(String strParameterName,String strParameterValue,boolean bAutoCreate,boolean bMandatory) throws AppException
	{
		if(strParameterValue.length() > 0)
		{
			if(!strParameterValue.endsWith("/") && !strParameterValue.endsWith("\\"))
				strParameterValue += "/";
			if(bAutoCreate)
			{
				try
				{
					FileUtil.forceFolderExist(strParameterValue);
				}
				catch(Exception e)
				{
					throw new AppException(e.getMessage(),"ManageableThread.loadDirectory",strParameterName);
				}
			}
		}
		else if(bMandatory)
		{
			throw new AppException("Value of '" + strParameterName + "' can not be null","ManageableThread.loadDirectory",strParameterName);
		}
		return strParameterValue;
	}
	////////////////////////////////////////////////////////
	public static Object loadClass(String strParameterName,String strParameterValue) throws AppException
	{
		try
		{
			return Class.forName(strParameterValue).newInstance();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new AppException("Value of '" + strParameterName + "' is invalid, class '" + strParameterValue + "' not found","ManageableThread.loadClass",strParameterName);
		}
	}
	////////////////////////////////////////////////////////
	public static String loadResource(String strParameterName,String strParameterValue) throws AppException
	{
		if(ParameterUtil.class.getResource(strParameterValue) == null)
			throw new AppException("Value of '" + strParameterName + "' invalid, resource '" + strParameterValue + "' not found","ManageableThread.loadResource",strParameterName);
		return strParameterValue;
	}
	////////////////////////////////////////////////////////
	public static String loadYesNo(String strParameterName,String strParameterValue) throws AppException
	{
		if(strParameterValue.length() > 0 &&
		   !strParameterValue.equals("N") &&
		   !strParameterValue.equals("Y"))
			throw new AppException("Value of '" + strParameterName + "' ('" + strParameterValue + "') must be 'Y' or 'N'","ManageableThread.loadYesNo",strParameterName);
		return strParameterValue;
	}
}

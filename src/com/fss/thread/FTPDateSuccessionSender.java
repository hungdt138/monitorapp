package com.fss.thread;

import java.util.*;
import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author not attributable
 * @version 1.0
 */

public class FTPDateSuccessionSender extends FTPSuccessionSender
{
	////////////////////////////////////////////////////////
	protected int miFirstDatePos;
	protected int miLastDatePos;
	protected String mstrSuccessionDateFormat;
	private java.text.SimpleDateFormat mfmt;
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = super.getParameterDefinition();
		vtReturn.insertElementAt(createParameterDefinition("FirstDatePos","",ParameterType.PARAM_TEXTBOX_MASK,"990","First index of date sequence in file name"),17);
		vtReturn.insertElementAt(createParameterDefinition("LastDatePos","",ParameterType.PARAM_TEXTBOX_MASK,"990","Last index of date sequence in file name"),18);
		vtReturn.insertElementAt(createParameterDefinition("SuccessionDateFormat","",ParameterType.PARAM_TEXTBOX_MAX,"256","Format of date sequence in file name"),19);
		return vtReturn;
	}
	///////////////////////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		miFirstDatePos = loadUnsignedInteger("FirstDatePos");
		miLastDatePos = 0;
		if(StringUtil.nvl(mprtParam.get("LastDatePos"),"").length() > 0)
			miLastDatePos = loadUnsignedInteger("LastDatePos");
		mstrSuccessionDateFormat = loadMandatory("SuccessionDateFormat");
		mfmt = new java.text.SimpleDateFormat(mstrSuccessionDateFormat);
		super.fillParameter();
	}
	///////////////////////////////////////////////////////////////////////////
	// Purpose: Compare name of src file with des file
	// Inputs: src file name, des file name
	// Outputs: 0 if equal, 1 if src file name greater than des file name, else -1
	// Authors: HiepTH
	// Notes: Used to filter list of file to receive
	///////////////////////////////////////////////////////////////////////////
	public int compareFileName(String strSrc,String strDes)
	{
		try
		{
			return getFileDate(strSrc).compareTo(getFileDate(strDes));
		}
		catch(Exception e)
		{
			logMonitor("Could not compare date of file '" + strSrc + "' and '" + strDes + "'. Please check your parameter.",mbAlertByMail);
			return -1;
		}
	}
	///////////////////////////////////////////////////////////////////////////
	// Get date from file name
	// Author: HiepTH
	// Modify DateTime: 04/12/2003
	///////////////////////////////////////////////////////////////////////////
	public Date getFileDate(String strFileName) throws Exception
	{
		try
		{
			String strDate = "";
			if(miLastDatePos == 0)
			{
				int iLastDatePos = miFirstDatePos;
				while(strFileName.charAt(iLastDatePos) >= '0' &&
					  strFileName.charAt(iLastDatePos) <= '9' &&
					  iLastDatePos <= strFileName.length())
					iLastDatePos++;
				strDate = strFileName.substring(miFirstDatePos,iLastDatePos);
			}
			else
			{
				int iLastDatePos = miLastDatePos;
				if(iLastDatePos > strFileName.length())
					iLastDatePos = strFileName.length();
				strDate = strFileName.substring(miFirstDatePos,iLastDatePos);
			}
			return mfmt.parse(strDate);
		}
		catch(Exception e)
		{
			throw new Exception("Can not get date of file '" + strFileName + "', please ensure parameter 'FirstDatePos', 'LastDatePos', 'SuccessionDateFormat' and 'Wildcard' are correct");
		}
	}
}

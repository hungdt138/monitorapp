package com.fss.thread;

import java.io.*;
import java.util.*;

import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class FTPSuccessionSender extends FTPSender
{
	////////////////////////////////////////////////////////
	protected String mstrLastProcessFile;
	protected int miFileCount;
	private int miMatchedCount;
	///////////////////////////////////////////////////////////////////////////
	// Purpose: Compare name of src file with des file
	// Inputs: src file name, des file name
	// Outputs: 0 if equal, 1 if src file name greater than des file name, else -1
	// Authors: HiepTH
	// Notes: Used to filter list of file to receive
	///////////////////////////////////////////////////////////////////////////
	public int compareFileName(String strSrc,String strDes)
	{
		return strSrc.compareToIgnoreCase(strDes);
	}
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void beforeListFile() throws Exception
	{
		miMatchedCount = 0;
		super.beforeListFile();
	}
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = super.getParameterDefinition();
		vtReturn.insertElementAt(createParameterDefinition("LastProcessFile","",ParameterType.PARAM_TEXTBOX_MAX,"256",""),17);
		vtReturn.insertElementAt(createParameterDefinition("FileCount","",ParameterType.PARAM_TEXTBOX_MASK,"999990",""),18);
		return vtReturn;
	}
	///////////////////////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		super.fillParameter();
		mstrLastProcessFile = StringUtil.nvl(mprtParam.get("LastProcessFile"),"");
		if(StringUtil.nvl(mprtParam.get("FileCount"),"").length() == 0)
			miFileCount = 0;
		else
			miFileCount = loadUnsignedInteger("FileCount");
	}
	///////////////////////////////////////////////////////////////////////////
	public File createListItem(File fl)
	{
		fl = super.createListItem(fl);
		if(fl == null)
			return null;
		if(mbListing)
			miMatchedCount++;
		if(mstrLastProcessFile.length() == 0 || compareFileName(fl.getName(),mstrLastProcessFile) > 0)
			return fl;
		return null;
	}
	///////////////////////////////////////////////////////////////////////////
	protected void afterListFile() throws Exception
	{
		Collections.sort(mvtFileList,new Comparator()
		{
			public int compare(Object obj1,Object obj2)
			{
				return compareFileName(((File)obj1).getName(),((File)obj2).getName());
			}
		});
	}
	///////////////////////////////////////////////////////////////////////////
	protected void afterPutFile(File fl) throws Exception
	{
		mstrLastProcessFile = StringUtil.nvl(mprtParam.get("LastProcessFile"),"");
		if(mstrLastProcessFile.length() == 0 || compareFileName(fl.getName(),mstrLastProcessFile) > 0)
		{
			mstrLastProcessFile = fl.getName();
			mprtParam.put("LastProcessFile",mstrLastProcessFile);
		}
		mprtParam.put("FileCount",String.valueOf(++miFileCount));
		storeConfig();
		super.afterPutFile(fl);
	}
	///////////////////////////////////////////////////////////////////////////
	public void changeProcessDate() throws Exception
	{
		// Check received count
		if(mstrRemoteStyle != null &&
		   mstrRemoteStyle.length() > 0 &&
		   !mstrRemoteStyle.equals("Directly"))
		{
			if(miFileCount < miMatchedCount)
			{
				logMonitor("The number of file match wildcard does not equals to the number of file received.\r\n\t"
						   + "You must change process date or FileCount manually to continue receive file.",mbAlertByMail);
				return;
			}
			else if(miFileCount > miMatchedCount)
			{
				miFileCount = miMatchedCount;
				mprtParam.put("FileCount",String.valueOf(miFileCount));
				storeConfig();
			}
		}

		boolean bTemp = mbNewDirectory;
		super.changeProcessDate();

		if(mstrRemoteStyle != null &&
		   mstrRemoteStyle.length() > 0 &&
		   !mstrRemoteStyle.equals("Directly"))
		{
			if(mbNewDirectory && !bTemp)
			{
				miFileCount = 0;
				mprtParam.put("FileCount",String.valueOf(miFileCount));
				storeConfig();
			}
		}
	}
}

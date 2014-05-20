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

public class FTPSequenceSender extends FTPSender
{
	// Parameter variables
	protected int miMinSeqVal;
	protected int miMaxSeqVal;
	protected int miExpectedSeq;
	protected int miFirstSeqPos;
	protected int miLastSeqPos;
	protected boolean mbSkipMissingFile;
	protected boolean mbMaxSeqExist;
	protected boolean mbMinSeqExist;
	protected int miTotalFile;
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = super.getParameterDefinition();

		vtReturn.insertElementAt(createParameterDefinition("MinSeqVal","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990",""),18);
		vtReturn.insertElementAt(createParameterDefinition("MaxSeqVal","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990",""),19);
		vtReturn.insertElementAt(createParameterDefinition("FirstSeqPos","",ParameterType.PARAM_TEXTBOX_MASK,"990",""),20);
		vtReturn.insertElementAt(createParameterDefinition("LastSeqPos","",ParameterType.PARAM_TEXTBOX_MASK,"990",""),21);
		vtReturn.insertElementAt(createParameterDefinition("ExpectedSeq","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990",""),22);
		Vector vtValue = new Vector();
		vtValue.addElement("Y");
		vtValue.addElement("N");
		vtReturn.insertElementAt(createParameterDefinition("SkipMissingFile","",ParameterType.PARAM_COMBOBOX,vtValue,""),23);
		vtReturn.insertElementAt(createParameterDefinition("LastProcessDate","",ParameterType.PARAM_LABEL,"",""),24);

		return vtReturn;
	}
	///////////////////////////////////////////////////////////////////////////
	// Fill parameter
	// Author: ThangPV
	// Modify DateTime: 19/02/2003
	///////////////////////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		super.fillParameter();
		miMinSeqVal = loadUnsignedInteger("MinSeqVal");
		miMaxSeqVal = loadUnsignedInteger("MaxSeqVal");
		miFirstSeqPos = loadUnsignedInteger("FirstSeqPos");
		miLastSeqPos = 0;
		if(StringUtil.nvl(mprtParam.get("LastSeqPos"),"").length() > 0)
			miLastSeqPos = loadUnsignedInteger("LastSeqPos");
		miExpectedSeq = loadUnsignedInteger("ExpectedSeq");
		mbSkipMissingFile = loadYesNo("SkipMissingFile").equals("Y");
	}
	///////////////////////////////////////////////////////////////////////////
	public void validateParameter() throws Exception
	{
		super.validateParameter();
		if(miLastSeqPos < miFirstSeqPos && miLastSeqPos != 0)
			throw new AppException("Value of 'LastSeqPos' can not be smaller than value of 'FirstSeqPos'","FTPSequenceSender.validateParameter","FirstSeqPos");
		if(miMaxSeqVal < miMinSeqVal)
			throw new AppException("Value of 'MaxSeqVal' can not be smaller than value of 'MinSeqVal'","FTPSequenceSender.validateParameter","MinSeqVal");
		if(miExpectedSeq < miMinSeqVal)
			throw new AppException("Value of 'ExpectedSeq' can not be smaller than value of 'MinSeqVal'","FTPSequenceSender.validateParameter","ExpectedSeq");
		if(miExpectedSeq > miMaxSeqVal)
			throw new AppException("Value of 'ExpectedSeq' can not be greater than value of 'MaxSeqVal'","FTPSequenceSender.validateParameter","ExpectedSeq");
	}
	///////////////////////////////////////////////////////////////////////////
	// Get sequence from file name
	// Author: ThangPV
	// Modify DateTime: 19/02/2003
	///////////////////////////////////////////////////////////////////////////
	protected int getFileSequence(File fl) throws Exception
	{
		try
		{
			String strSeq = "";
			if(miLastSeqPos == 0)
			{
				int iLastSeqPos = miFirstSeqPos;
				while(iLastSeqPos < fl.getName().length() &&
					  fl.getName().charAt(iLastSeqPos) >= '0' &&
					  fl.getName().charAt(iLastSeqPos) <= '9')
					iLastSeqPos++;
				strSeq = fl.getName().substring(miFirstSeqPos,iLastSeqPos);
			}
			else
			{
				int iLastSeqPos = miLastSeqPos;
				if(iLastSeqPos > fl.getName().length())
					iLastSeqPos = fl.getName().length();
				strSeq = fl.getName().substring(miFirstSeqPos,iLastSeqPos);
			}
			return Integer.parseInt(strSeq);
		}
		catch(Exception e)
		{
			throw new Exception("Can not get sequence of file '" + fl.getName() + "', please ensure parameter 'FirstSeqPos' and 'LastSeqPos' and 'Wildcard' are correct");
		}
	}
	///////////////////////////////////////////////////////////////////////////
	public void beforeListFile()
	{
		mbMaxSeqExist = false;
		mbMinSeqExist = false;
		miTotalFile = 0;
	}
	///////////////////////////////////////////////////////////////////////////
	// Validate list item of sequence file
	// Author: ThangPV
	// Modify DateTime: 19/02/2003
	///////////////////////////////////////////////////////////////////////////
	public File createListItem(File fl)
	{
		try
		{
			fl = super.createListItem(fl);
			if(fl == null)
				return null;
			if(mbListing)
				miTotalFile++;
			int iFileSeq = getFileSequence(fl);
			if(iFileSeq == miMaxSeqVal)
				mbMaxSeqExist = true;
			else if(iFileSeq == miMinSeqVal)
				mbMinSeqExist = true;

			if(iFileSeq >= miExpectedSeq && iFileSeq >= miMinSeqVal && iFileSeq <= miMaxSeqVal)
				return fl;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	///////////////////////////////////////////////////////////////////////////
	// Event occur after put file list in remote directory and fill into Items
	// Author: HiepTH
	// Modify DateTime: 05/12/2003
	///////////////////////////////////////////////////////////////////////////
	public void afterListFile() throws Exception
	{
		Collections.sort(mvtFileList,new Comparator()
		{
			public int compare(Object obj1,Object obj2)
			{
				try
				{
					int intFirstSeq = getFileSequence(((File)obj1));
					int intSecondSeq = getFileSequence(((File)obj2));
					if (intFirstSeq > intSecondSeq)
						return 1;
					else if(intFirstSeq == intSecondSeq)
						return 0;
					else
						return -1;
				}
				catch(Exception e)
				{
					return -1;
				}
			}
		});
	}
	///////////////////////////////////////////////////////////////////////////
	// validate file before call to putFile
	// Author: ThangPV
	// Modify DateTime: 19/02/2003
	///////////////////////////////////////////////////////////////////////////
	protected String validateFile(File fl) throws Exception
	{
		int iFileSeq = getFileSequence(fl);
		if(iFileSeq != miExpectedSeq)
		{
			if(mstrRemoteStyle.length() > 0 && !mstrRemoteStyle.equals("Directly") &&
			   iFileSeq > miExpectedSeq && mbMaxSeqExist && !mbNewDirectory)
			{
				if(mbMinSeqExist || (!mbMinSeqExist && miExpectedSeq == miMinSeqVal))
				{
					if(miTotalFile == (miExpectedSeq - miMinSeqVal + miMaxSeqVal - iFileSeq + 1))
					{
						miListItemCount = 0;
						return "Turn back detected, application will search for next folder";
					}
					else
						throw new Exception("Turn back detected but the there are some file is missing." +
											"\r\n\tPlease check your missing file and increase the process date manually");
				}
			}
			if(mbSkipMissingFile)
			{
				// Log file missing
				String strLog = "Missing sequence from " + String.valueOf(miExpectedSeq) + " to " + String.valueOf(iFileSeq - 1);
				logMissing(fl,iFileSeq,strLog);

				// Skip put these file
				miExpectedSeq = iFileSeq;
				mprtParam.put("ExpectedSeq",String.valueOf(miExpectedSeq));
				storeConfig();

				// Change next session
				throw new Exception(strLog);
			}
			throw new Exception("Matching sequence " + iFileSeq +
								" when expected sequence is " + miExpectedSeq);
		}
		return super.validateFile(fl);
	}
	///////////////////////////////////////////////////////////////////////////
	// afterPutFile event
	// Author: ThangPV
	// Modify DateTime: 19/02/2003
	///////////////////////////////////////////////////////////////////////////
	protected void afterPutFile(File fl) throws Exception
	{
		// Update into parameter table
		mprtParam.put("LastProcessDate",StringUtil.format(new java.util.Date(),"dd/MM/yyyy HH:mm:ss"));
		if (miExpectedSeq == miMaxSeqVal)
			miExpectedSeq = miMinSeqVal;
		else
			miExpectedSeq++;

		mprtParam.put("ExpectedSeq",String.valueOf(miExpectedSeq));
		storeConfig();
		super.afterPutFile(fl);
	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * Event raised when file missing
	 * @param fl File
	 * @param iFileSeq int
	 * @throws Exception
	 */
	///////////////////////////////////////////////////////////////////////////
	protected void logMissing(File fl,int iFileSeq,String strLog) throws Exception
	{
		// Log error
		logStart(fl.getName());
		this.logDetail(strLog,"Search file","");
		logComplete("1","0","1","M");
	}
}

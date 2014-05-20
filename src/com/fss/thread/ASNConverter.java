package com.fss.thread;

import java.util.*;
import java.io.*;

import com.fss.sql.*;
import com.fss.util.*;
import com.fss.asn1.*;

/**
 * <p>Title: </p>
 * <p>Description: Thread get file from ftp server</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ASNConverter extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	// Convert variables
	protected String mstrImportDir;
	protected String mstrExportDir;
	protected String mstrBackupDir;
	protected String mstrBackupStyle;
	protected String mstrTempDir;
	protected String mstrWildcard;
	protected boolean mbCompressed;
	protected int miMinSeqVal;
	protected int miMaxSeqVal;
	protected int miFirstSeqPos;
	protected int miLastSeqPos;
	protected int miExpectedSeq;
	protected boolean mbSkipMissingFile;
	protected boolean mbMakeOriginalStructure;
	protected ASNFormat mfmt;

	// Common used variables
	protected String mstrFileID;
	protected int miFileSeq;
	protected int miRecordCount;
	protected int miTotalFile;
	protected boolean mbMaxSeqExist;
	protected boolean mbMinSeqExist;
	protected int miMinSeqFound;
	protected String mstrMinFileFound;
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		super.fillParameter();
		////////////////////////////////////////////////////////
		mfmt = (ASNFormat)loadClass("EncoderClass");
		mstrImportDir = loadDirectory("ImportDir",true,true);
		mstrExportDir = loadDirectory("ExportDir",true,true);
		mstrBackupDir = loadDirectory("BackupDir",true,false);
		mstrBackupStyle = StringUtil.nvl(mprtParam.get("BackupStyle"),"");
		mstrTempDir = loadDirectory("TempDir",true,false);
		////////////////////////////////////////////////////////
		mstrWildcard = loadMandatory("Wildcard");
		mbCompressed = loadYesNo("Compressed").equals("Y");
		miMinSeqVal = loadUnsignedInteger("MinSeqVal");
		miMaxSeqVal = loadUnsignedInteger("MaxSeqVal");
		miFirstSeqPos = loadUnsignedInteger("FirstSeqPos");
		miLastSeqPos = 0;
		if(StringUtil.nvl(mprtParam.get("LastSeqPos"),"").length() > 0)
			miLastSeqPos = loadUnsignedInteger("LastSeqPos");
		miExpectedSeq = loadUnsignedInteger("ExpectedSeq");
		mbSkipMissingFile = loadYesNo("SkipMissingFile").equals("Y");
		mbMakeOriginalStructure = StringUtil.nvl(mprtParam.get("OriginalStructure"),"").equals("Make original structure");
	}
	///////////////////////////////////////////////////////////////////////////
	public void validateParameter() throws Exception
	{
		super.validateParameter();
		if(miLastSeqPos < miFirstSeqPos && miLastSeqPos != 0)
			throw new AppException("Value of 'LastSeqPos' can not be smaller than value of 'FirstSeqPos'","ASNConverter.validateParameter","FirstSeqPos");
		if(miMaxSeqVal < miMinSeqVal)
			throw new AppException("Value of 'MaxSeqVal' can not be smaller than value of 'MinSeqVal'","ASNConverter.validateParameter","MinSeqVal");
		if(miExpectedSeq < miMinSeqVal)
			throw new AppException("Value of 'ExpectedSeq' can not be smaller than value of 'MinSeqVal'","ASNConverter.validateParameter","MinSeqVal");
		if(miExpectedSeq > miMaxSeqVal)
			throw new AppException("Value of 'ExpectedSeq' can not be greater than value of 'MaxSeqVal'","ASNConverter.validateParameter","MaxSeqVal");
	}
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = new Vector();

		vtReturn.addElement(createParameterDefinition("EncoderClass","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtReturn.addElement(createParameterDefinition("ImportDir","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtReturn.addElement(createParameterDefinition("ExportDir","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtReturn.addElement(createParameterDefinition("BackupDir","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		Vector vtValue = new Vector();
		vtValue.addElement("Directly");
		vtValue.addElement("Daily");
		vtValue.addElement("Monthly");
		vtValue.addElement("Yearly");
		vtValue.addElement("Delete file");
		vtReturn.addElement(createParameterDefinition("BackupStyle","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtReturn.addElement(createParameterDefinition("TempDir","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtReturn.addElement(createParameterDefinition("Wildcard","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtValue = new Vector();
		vtValue.addElement("Y");
		vtValue.addElement("N");
		vtReturn.addElement(createParameterDefinition("Compressed","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtReturn.addElement(createParameterDefinition("MinSeqVal","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990",""));
		vtReturn.addElement(createParameterDefinition("MaxSeqVal","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990",""));
		vtReturn.addElement(createParameterDefinition("FirstSeqPos","",ParameterType.PARAM_TEXTBOX_MASK,"990",""));
		vtReturn.addElement(createParameterDefinition("LastSeqPos","",ParameterType.PARAM_TEXTBOX_MASK,"990",""));
		vtReturn.addElement(createParameterDefinition("ExpectedSeq","",ParameterType.PARAM_TEXTBOX_MASK,"9999999990",""));
		vtValue = new Vector();
		vtValue.addElement("Y");
		vtValue.addElement("N");
		vtReturn.addElement(createParameterDefinition("SkipMissingFile","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtValue = new Vector();
		vtValue.addElement("Make original structure");
		vtValue.addElement("Don't make original structure");
		vtReturn.addElement(createParameterDefinition("OriginalStructure","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtReturn.addAll(super.getParameterDefinition());

		return vtReturn;
	}
	////////////////////////////////////////////////////////
	public void processSession() throws Exception
	{
		miTotalFile = 0;
		mbMaxSeqExist = false;
		mbMinSeqExist = false;
		miMinSeqFound = miMaxSeqVal;

		// List file and convert
		File fl = new File(mstrImportDir);
		File flFileList[] = fl.listFiles(new WildcardFileFilter(mstrWildcard,false));
		if(flFileList != null && flFileList.length > 0)
		{
			Arrays.sort(flFileList,new Comparator()
			{
				public int compare(Object fl1,Object fl2)
				{
					return ((File)fl1).getName().compareTo(((File)fl2).getName());
				}
			});
			int iFileCount = flFileList.length;
			for(int iFileIndex = 0;iFileIndex < iFileCount && miThreadCommand != ThreadConstant.THREAD_STOP;iFileIndex++)
			{
				if(validateFile(flFileList[iFileIndex].getName()))
				{
					convertFile(flFileList[iFileIndex].getName());
					miTotalFile++;
				}
			}
		}

		// Check turn back
		if(miTotalFile == 0) // No file is validated
		{
			if(mbSkipMissingFile)
			{
				if(miMinSeqFound < miExpectedSeq)
				{
					// Log error
					String strLog = "Missing sequence from " + miExpectedSeq + " to " + miMaxSeqVal +
									" and from " + miMinSeqVal + " to " + (miMinSeqFound - 1);
					logMonitor(strLog,mbAlertByMail);
					logStart(mstrMinFileFound);
					logDetail(strLog,"Search file","");
					logComplete("1","0","1","M");

					// Countinue to convert
					mprtParam.put("ExpectedSeq",String.valueOf(miMinSeqFound));
					storeConfig();
				}
			}
		}
	}
	////////////////////////////////////////////////////////
	public void convertFile(String strFileName) throws Exception
	{
		// Get file id
		if(mcnMain != null)
			mstrFileID = Database.getSequenceValue(mcnMain,"CDR_FILE_SEQ");
		else
			mstrFileID = "Auto";

		miRecordCount = 0;
		try
		{
			// Log start
			logStart(strFileName);
			logMonitor("Start converting file " + strFileName);
			beforeConvert(strFileName);

			// Process file
			String strImportFile = strFileName;

			// Decompress file
			if(mbCompressed)
			{
				strImportFile += ".extracted";
				SmartZip.GUnZip(mstrImportDir + strFileName,mstrImportDir + strImportFile);
			}

			miRecordCount = mfmt.convert(mstrImportDir + strImportFile,
										 mstrTempDir + strFileName,getConvertParameter());

			// Delete temp file
			if(mbCompressed)
				FileUtil.deleteFile(mstrImportDir + strImportFile);

			// Move file to export folder
			if(!FileUtil.renameFile(mstrTempDir + strFileName,mstrExportDir + strFileName + ".txt"))
				throw new Exception("Cannot rename file " + mstrTempDir + strFileName + " to " + mstrExportDir + strFileName + ".txt");

			// Increase expected seq
			if(miExpectedSeq >= miMaxSeqVal)
				miExpectedSeq = miMinSeqVal;
			else
				miExpectedSeq++;
			mprtParam.put("ExpectedSeq",String.valueOf(miExpectedSeq));
			storeConfig();

			// Log completed
			afterConvert(strFileName);
			logComplete(String.valueOf(miRecordCount),String.valueOf(miRecordCount),"0","S");
			logMonitor("Converting file " + strFileName + " completed\r\n\t" + miRecordCount + " records converted");

			// Backup file
			FileUtil.backup(mstrImportDir,mstrBackupDir,strFileName,strFileName,mstrBackupStyle);
		}
		catch(Exception e)
		{
			// Write log
			logMonitor("Error occured\r\n\t" + e.getMessage(),mbAlertByMail);
			logDetail(e.getMessage(),"","");
			logComplete(String.valueOf(miRecordCount),"0",String.valueOf(miRecordCount),"F");

			if(!mbSkipMissingFile)
				throw e;
			else
			{
				e.printStackTrace();
			}
		}
	}
	///////////////////////////////////////////////////////////////////////////
	// validate file before call convert
	// Author: HiepTH
	// Modify DateTime: 09/07/2003
	///////////////////////////////////////////////////////////////////////////
	protected boolean validateFile(String strFileName) throws Exception
	{
		miFileSeq = getFileSequence(strFileName);
		if(miFileSeq == miMaxSeqVal)
			mbMaxSeqExist = true;
		if(miFileSeq == miMinSeqVal)
			mbMinSeqExist = true;
		if(miFileSeq > miMaxSeqVal)
			return false;
		if(miFileSeq != miExpectedSeq)
		{
			if(miFileSeq > miExpectedSeq && mbSkipMissingFile)
			{
				// Log error
				String strLog = "Missing sequence from " + String.valueOf(miExpectedSeq) + " to " + String.valueOf(miFileSeq - 1);
				logStart(strFileName);
				logDetail(strLog,"Search file","");
				logComplete("1","0","1","M");

				// Countinue to convert
				mprtParam.put("ExpectedSeq",String.valueOf(miFileSeq));
				storeConfig();
				throw new Exception(strLog);
			}
			else if(miFileSeq > miExpectedSeq)
				throw new Exception("Matching sequence " + miFileSeq + " when expected sequence is " + miExpectedSeq);
			else
			{
				if(miFileSeq < miMinSeqFound)
				{
					miMinSeqFound = miFileSeq;
					mstrMinFileFound = strFileName;
				}
				return false;
			}
		}
		return true;
	}
	///////////////////////////////////////////////////////////////////////////
	// Get sequence from file name
	// Author: HiepTH
	// Modify DateTime: 07/09/2003
	///////////////////////////////////////////////////////////////////////////
	private int getFileSequence(String strFileName) throws Exception
	{
		try
		{
			String strSeq = "";
			if(miLastSeqPos == 0)
			{
				int iLastSeqPos = miFirstSeqPos;
				while(strFileName.charAt(iLastSeqPos) >= '0' &&
					  strFileName.charAt(iLastSeqPos) <= '9' &&
					  iLastSeqPos <= strFileName.length())
					iLastSeqPos++;
				strSeq = strFileName.substring(miFirstSeqPos,iLastSeqPos);
			}
			else
			{
				int iLastSeqPos = miLastSeqPos;
				if(iLastSeqPos > strFileName.length())
					iLastSeqPos = strFileName.length();
				strSeq = strFileName.substring(miFirstSeqPos,iLastSeqPos);
			}
			return Integer.parseInt(strSeq);
		}
		catch(Exception e)
		{
			throw new Exception("Can not get sequence of file '" + strFileName + "', please ensure parameter 'FirstSeqPos' and 'LastSeqPos' and 'Wildcard' are correct");
		}
	}
	///////////////////////////////////////////////////////////////////////////
	// Before convert event
	// Author: HiepTH
	// Modify DateTime: 14/09/2003
	///////////////////////////////////////////////////////////////////////////
	public void beforeConvert(String strFileName) throws Exception
	{
	}
	///////////////////////////////////////////////////////////////////////////
	// After convert event
	// Author: HiepTH
	// Modify DateTime: 14/09/2003
	///////////////////////////////////////////////////////////////////////////
	public void afterConvert(String strFileName) throws Exception
	{
	}
	///////////////////////////////////////////////////////////////////////////
	// Return parameter to pass to ASNFormat.convert
	// Author: HiepTH
	// Modify DateTime: 09/04/2004
	///////////////////////////////////////////////////////////////////////////
	public Hashtable getConvertParameter() throws Exception
	{
		Hashtable prt = new Hashtable();
		prt.put("FileID",mstrFileID);
		prt.put("MakeOriginalStructure",mbMakeOriginalStructure?"T":"F");
		return prt;
	}
}

package com.fss.thread;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.fss.sql.*;
import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class BackupThread extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected String mstrDBUrl;
	protected String mstrDBUserName;
	protected String mstrDBPassword;
	protected String mstrBackupDir;
	protected String mstrBackupDateFormat;
	protected String mstrAdditionFile;
	protected String mstrCompressMethod;
	protected int miSplitNumber;
	protected String mstrPreCommand;
	protected String mstrPstCommand;
	protected int miExecutionTimeout;
	protected boolean mbBackupReadonlyDataFile;
	protected boolean mbBackupOfflineDataFile;
	private static final int MAX_TRY_COUNT = 100;
	private int miTryCount;
	private boolean mbServerLocked;
	////////////////////////////////////////////////////////
	private Vector mvtFileList = new Vector();
	private Vector mvtThreadList = new Vector();
	////////////////////////////////////////////////////////
	public String getFileFromQueue()
	{
		try
		{
			synchronized(mvtFileList)
			{
				if(mvtFileList.size() > 0)
				{
					String strReturn = (String)mvtFileList.elementAt(0);
					mvtFileList.removeElementAt(0);
					return strReturn;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	////////////////////////////////////////////////////////
	private class BackupSplitThread extends Thread
	{
		private String mstrBackupDir = null;
		private OutputStream moutFileList = null;
		public BackupSplitThread(String strBackupDir,OutputStream outFileList)
		{
			mstrBackupDir = strBackupDir;
			moutFileList = outFileList;
			start();
		}
		public void run()
		{
			try
			{
				while(true)
				{
					String strFilePath = null;
					try
					{
						strFilePath = getFileFromQueue();
						if(strFilePath == null)
							return;
						moutFileList.write((strFilePath + "\r\n").getBytes());
						backup(strFilePath,mstrBackupDir);
					}
					catch(Exception e)
					{
						e.printStackTrace();
						logMonitor("Error occured while backup file " + strFilePath + ":\r\n\t" + e.getMessage(),mbAlertByMail);
					}
				}
			}
			finally
			{
				synchronized(mvtThreadList)
				{
					mvtThreadList.removeElement(this);
				}
			}
		}
	}
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = new Vector();

		vtReturn.addElement(createParameterDefinition("DBUrl","",ParameterType.PARAM_TEXTBOX_MAX,"256","Connection url of database"));
		vtReturn.addElement(createParameterDefinition("DBUserName","",ParameterType.PARAM_TEXTBOX_MAX,"256","DBA user name"));
		vtReturn.addElement(createParameterDefinition("DBPassword","",ParameterType.PARAM_PASSWORD,"100","Password of DBA user name"));
		vtReturn.addElement(createParameterDefinition("BackupDir","",ParameterType.PARAM_TEXTBOX_MAX,"256","Folder will contain compressed data file"));
		vtReturn.addElement(createParameterDefinition("BackupDateFormat","",ParameterType.PARAM_TEXTBOX_MAX,"256","Format date of subfolder"));
		vtReturn.addElement(createParameterDefinition("AdditionFile","",ParameterType.PARAM_TEXTBOX_MAX,"256","Additional file to backup (ex: pfile or config file of database)"));
		Vector vtValue = new Vector();
		vtValue.addElement("Backup");
		vtValue.addElement("Do not backup");
		vtReturn.addElement(createParameterDefinition("ReadonlyDataFile","",ParameterType.PARAM_COMBOBOX,vtValue,"Backup operation for readonly data file"));
		vtValue = new Vector();
		vtValue.addElement("Backup");
		vtValue.addElement("Do not backup");
		vtReturn.addElement(createParameterDefinition("OfflineDataFile","",ParameterType.PARAM_COMBOBOX,vtValue,"Backup operation for offline data file"));
		vtValue = new Vector();
		vtValue.addElement("GZip");
		vtValue.addElement("Zip");
		vtReturn.addElement(createParameterDefinition("CompressMethod","",ParameterType.PARAM_COMBOBOX,vtValue,"Method to compress data file"));
		vtReturn.addElement(createParameterDefinition("SplitNumber","",ParameterType.PARAM_TEXTBOX_MASK,"90","Number of parallel backup thread"));
		vtReturn.addElement(createParameterDefinition("PreCommand","",ParameterType.PARAM_TEXTAREA_MAX,"4000","Command will be excuted before converting (usually is shutdowndb)"));
		vtReturn.addElement(createParameterDefinition("PstCommand","",ParameterType.PARAM_TEXTAREA_MAX,"4000","Command will be excuted after converting (usually is startdowndb)"));
		vtReturn.addElement(createParameterDefinition("ExecutionTimeout","",ParameterType.PARAM_TEXTBOX_MASK,"99990","Excution time out of pre command and pst command"));
		vtReturn.addAll(super.getParameterDefinition());

		for(int iIndex = vtReturn.size() - 1;iIndex >= 0;iIndex--)
		{
			String strParameterName = (String)((Vector)vtReturn.elementAt(iIndex)).elementAt(0);
			if(strParameterName.equals("ConnectDB"))
				vtReturn.removeElementAt(iIndex);
		}

		return vtReturn;
	}
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		mstrDBUrl = loadMandatory("DBUrl");
		mstrDBUserName = loadMandatory("DBUserName");
		mstrDBPassword = loadMandatory("DBPassword");
		mstrBackupDir = loadDirectory("BackupDir",true,true);
		mstrBackupDateFormat = StringUtil.nvl(mprtParam.get("BackupDateFormat"),"");
		mstrAdditionFile = StringUtil.nvl(mprtParam.get("AdditionFile"),"");
		mbBackupReadonlyDataFile = StringUtil.nvl(mprtParam.get("ReadonlyDataFile"),"Backup").equals("Backup");
		mbBackupOfflineDataFile = StringUtil.nvl(mprtParam.get("OfflineDataFile"),"").equals("Backup");
		mstrCompressMethod = loadMandatory("CompressMethod");
		miSplitNumber = loadUnsignedInteger("SplitNumber");
		mstrPreCommand = StringUtil.nvl(mprtParam.get("PreCommand"),"");
		mstrPstCommand = StringUtil.nvl(mprtParam.get("PstCommand"),"");
		miExecutionTimeout = loadUnsignedInteger("ExecutionTimeout") * 1000;
		super.fillParameter();
		mbAutoConnectDB = false;
	}
	////////////////////////////////////////////////////////
	public void validateParameter() throws Exception
	{
		super.validateParameter();
		if(mstrBackupDateFormat.length() > 0)
		{
			try
			{
				new java.text.SimpleDateFormat(mstrBackupDateFormat);
			}
			catch(Exception e)
			{
				throw new AppException("Invalid backup date format","BackupDateFormat");
			}
		}
		if(miSplitNumber == 0)
			throw new AppException("SplitNumber must be greater than zero","BackupThread.validateParameter","SplitNumber");
	}
	////////////////////////////////////////////////////////
	public void beforeSession() throws Exception
	{
		mbServerLocked = false;
		mmgrMain.lockServer();
		mbServerLocked = true;
	}
	////////////////////////////////////////////////////////
	public void afterSession() throws Exception
	{
		if(mbServerLocked)
			mmgrMain.unLockServer();
		mbServerLocked = true;
	}
	////////////////////////////////////////////////////////
	public void processSession() throws Exception
	{
		// Backup dir of this day
		String strBackupDir = mstrBackupDir;
		if(mstrBackupDateFormat.length() > 0)
			strBackupDir += StringUtil.format(new java.util.Date(),mstrBackupDateFormat) + "/";
		FileUtil.forceFolderExist(strBackupDir);

		// Variables to manage file list
		FileOutputStream flFileList = null;
		Connection cn = null;

		try
		{
			logMonitor("Start backup database");

			// Create list of file to be backing up
			String strSQL = "SELECT NAME FROM" +
							" (SELECT NAME,DECODE(STATUS,'OFFLINE','1','0') IS_OFFLINE," +
							" DECODE(ENABLED,'READ ONLY','1','0') IS_READONLY FROM GV$DATAFILE" +
							" UNION ALL" +
							" SELECT NAME,DECODE(STATUS,'OFFLINE','1','0') IS_OFFLINE," +
							" DECODE(ENABLED,'READ ONLY','1','0') IS_READONLY FROM GV$TEMPFILE" +
							" UNION ALL" +
							" SELECT NAME,'0' IS_OFFLINE,'0' IS_READONLY FROM GV$CONTROLFILE" +
							" UNION ALL" +
							" SELECT MEMBER,'0' IS_OFFLINE,'0' IS_READONLY FROM GV$LOGFILE)";
			String strCondition = "";
			if(!mbBackupOfflineDataFile)
				strCondition += " AND IS_OFFLINE<>'1'";
			if(!mbBackupReadonlyDataFile)
				strCondition += " AND IS_READONLY<>'1'";
			if(strCondition.length() > 0)
				strSQL += " WHERE " + strCondition.substring(5,strCondition.length());
			cn = Database.getConnection(mstrDBUrl,mstrDBUserName,mstrDBPassword);
			Statement stmt = cn.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);
			mvtFileList.clear();
			while(rs.next())
				mvtFileList.addElement(rs.getString(1));
			rs.close();
			stmt.close();

			// Analyse addition file
			String strAdditionFile = mstrAdditionFile;
			int iResult = 0;
			while((iResult = strAdditionFile.indexOf(";")) >= 0)
			{
				String strFilePath = strAdditionFile.substring(0,iResult).trim();
				if(strFilePath.length() > 0)
					mvtFileList.addElement(strFilePath);
				strAdditionFile = strAdditionFile.substring(iResult + 1,strAdditionFile.length());
			}
			strAdditionFile = strAdditionFile.trim();
			if(strAdditionFile.length() > 0)
				mvtFileList.addElement(strAdditionFile);

			// Run precommand
			if(mstrPreCommand.length() > 0)
			{
				logMonitor("Execute pre command");
				iResult = exec(mstrPreCommand,miExecutionTimeout);
				if(iResult != 0 && iResult != -1)
					throw new Exception("Execution failure");
				logMonitor("Execute pre command completed");
			}

			// Backup file
			flFileList = new FileOutputStream(strBackupDir + "FileList.txt");
			mvtThreadList = new Vector();
			miTryCount = 0;
			for(int iTheadIndex = 0;iTheadIndex < miSplitNumber;iTheadIndex++)
			{
				mvtThreadList.addElement(new BackupSplitThread(strBackupDir,flFileList));
				try
				{
					Thread.sleep(1000);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}

			// Wait for thread execution
			while(mvtThreadList.size() > 0)
			{
				try
				{
					Thread.sleep(1000);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}

			// Run pstcommand
			if(mstrPstCommand.length() > 0)
			{
				logMonitor("Execute pst command");
				iResult = exec(mstrPstCommand,miExecutionTimeout);
				if(iResult != 0 && iResult != -1)
					throw new Exception("Execution failure");
				logMonitor("Execute pst command completed");
			}

			logMonitor("Backup database completed");
		}
		finally
		{
			Database.closeObject(cn);
			FileUtil.safeClose(flFileList);
		}
	}
	////////////////////////////////////////////////////////
	public void backup(String strFilePath,String strBackupDir) throws Exception
	{
		logMonitor("Start backup file " + strFilePath);
		while(true)
		{
			try
			{
				// Analyse file title & path
				File fl = new File(strFilePath);
				String strFileTitle = fl.getName();
				String strPath = fl.getAbsoluteFile().getParent();

				// Compress & backup file
				String strBackupPath = strBackupDir + StringUtil.replaceAll(strPath,":","") + "/";
				FileUtil.forceFolderExist(strBackupPath);
				if(mstrCompressMethod.equals("GZip"))
					SmartZip.GZip(strFilePath,strBackupPath + strFileTitle + ".gz");
				else
					SmartZip.Zip(strFilePath,strBackupPath + strFileTitle + ".zip",false);
				break;
			}
			catch(Exception e)
			{
				if(miTryCount >= MAX_TRY_COUNT)
					throw e;
				e.printStackTrace();
				miTryCount++;
			}
		}
		logMonitor("Backup file " + strFilePath + " complete");
	}
}

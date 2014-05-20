package com.fss.thread;

import java.io.*;
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

public class RestoreThread extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected String mstrBackupDir;
	protected String mstrRestoreDir;
	protected String mstrCompressMethod;
	protected int miSplitNumber;
	protected String mstrPreCommand;
	protected String mstrPstCommand;
	protected int miExecutionTimeout;
	private static final int MAX_TRY_COUNT = 100;
	private int miTryCount;
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
	private class RestoreSplitThread extends Thread
	{
		private String mstrBackupDir = null;
		private String mstrRestoreDir = null;
		public RestoreSplitThread(String strBackupDir,String strRestoreDir)
		{
			mstrBackupDir = strBackupDir;
			mstrRestoreDir = strRestoreDir;
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
						restore(strFilePath,mstrBackupDir,mstrRestoreDir);
					}
					catch(Exception e)
					{
						e.printStackTrace();
						logMonitor("Error occured while restore file " + strFilePath + ":\r\n\t" + e.getMessage(),mbAlertByMail);
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

		vtReturn.addElement(createParameterDefinition("BackupDir","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtReturn.addElement(createParameterDefinition("RestoreDir","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		Vector vtValue = new Vector();
		vtValue.addElement("GZip");
		vtValue.addElement("Zip");
		vtReturn.addElement(createParameterDefinition("CompressMethod","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtReturn.addElement(createParameterDefinition("SplitNumber","",ParameterType.PARAM_TEXTBOX_MASK,"90",""));
		vtReturn.addElement(createParameterDefinition("PreCommand","",ParameterType.PARAM_TEXTAREA_MAX,"4000",""));
		vtReturn.addElement(createParameterDefinition("PstCommand","",ParameterType.PARAM_TEXTAREA_MAX,"4000",""));
		vtReturn.addElement(createParameterDefinition("ExecutionTimeout","",ParameterType.PARAM_TEXTBOX_MASK,"99990",""));
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
		mstrBackupDir = loadDirectory("BackupDir",true,true);
		mstrRestoreDir = loadDirectory("RestoreDir",true,false);
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
		if(miSplitNumber == 0)
			throw new AppException("SplitNumber must be greater than zero","RestoreThread.validateParameter","SplitNumber");
	}
	////////////////////////////////////////////////////////
	public void processSession() throws Exception
	{
		RandomAccessFile flFileList = null;
		try
		{
			logMonitor("Start restore database");

			// Create list of file to be restore
			flFileList = new RandomAccessFile(mstrBackupDir + "FileList.txt","r");
			String strFilePath = "";

			while((strFilePath = flFileList.readLine()) != null)
				mvtFileList.addElement(strFilePath);

			// Run precommand
			if(mstrPreCommand.length() > 0)
			{
				logMonitor("Execute pre command");
				int iResult = exec(mstrPreCommand,miExecutionTimeout);
				if(iResult != 0 && iResult != -1)
					throw new Exception("Execution failure");
				logMonitor("Execute pre command completed");
			}

			// Restore file
			mvtThreadList = new Vector();
			miTryCount = 0;
			for(int iTheadIndex = 0;iTheadIndex < miSplitNumber;iTheadIndex++)
			{
				mvtThreadList.addElement(new RestoreSplitThread(mstrBackupDir,mstrRestoreDir));
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
				int iResult = exec(mstrPstCommand,miExecutionTimeout);
				if(iResult != 0 && iResult != -1)
					throw new Exception("Execution failure");
				logMonitor("Execute pst command completed");
			}

			logMonitor("Restore database completed");
			miThreadCommand = ThreadConstant.THREAD_STOP;
		}
		finally
		{
			FileUtil.safeClose(flFileList);
		}
	}
	////////////////////////////////////////////////////////
	public void restore(String strFilePath,String strBackupDir,String strRestoreDir) throws Exception
	{
		logMonitor("Start restore file " + strFilePath);

		while(true)
		{
			try
			{
				// Analyse file title & path
				File fl = new File(strFilePath);
				String strFileTitle = fl.getName();
				String strPath = fl.getAbsoluteFile().getParent();

				// Correct backup, restore path
				String strBackupPath = strBackupDir + strPath + "/";
				if(strBackupDir.length() > 0)
					strBackupPath = StringUtil.replaceAll(strBackupPath,":","");
				String strRestorePath = strRestoreDir + strPath + "/";
				if(strRestoreDir.length() > 0)
					strRestorePath = StringUtil.replaceAll(strRestorePath,":","");

				// Decompress & restore file
				FileUtil.forceFolderExist(strRestorePath);
				if(mstrCompressMethod.equals("GZip"))
					SmartZip.GUnZip(strBackupDir + strFilePath + ".gz",strRestorePath + strFileTitle);
				else
					SmartZip.UnZip(strBackupDir + strFilePath + ".zip",strRestorePath);
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
		logMonitor("Restore file " + strFilePath + " complete");
	}
}

package com.fss.thread;

import java.io.*;
import java.sql.*;
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

public class ComputeChargeThread extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variabled
	////////////////////////////////////////////////////////
	protected String mstrTextPath;
	protected String mstrBackupDir;
	protected String mstrBackupStyle;
	protected String mstrRejectDir;
	protected String mstrRejectStyle;
	protected String mstrWildcard;
	protected String mstrSQLValidateCommand;
	protected String mstrSQLCommand;
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		mstrTextPath = loadDirectory("TextPath",true,true);
		mstrBackupDir = loadDirectory("BackupDir",true,false);
		mstrBackupStyle = StringUtil.nvl(mprtParam.get("BackupStyle"),"");
		mstrRejectDir = loadDirectory("RejectDir",true,false);
		mstrRejectStyle = StringUtil.nvl(mprtParam.get("RejectStyle"),"");
		mstrWildcard = loadMandatory("Wildcard");
		mstrSQLValidateCommand = StringUtil.nvl(mprtParam.get("SQLValidateCommand"),"");
		mstrSQLCommand = loadMandatory("SQLCommand");
		super.fillParameter();
		mbAutoConnectDB = true;
	}
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = new Vector();

		vtReturn.addElement(createParameterDefinition("TextPath","",ParameterType.PARAM_TEXTBOX_MAX,"256","Path to directory contain file need to compute"));
		vtReturn.addElement(createParameterDefinition("BackupDir","",ParameterType.PARAM_TEXTBOX_MAX,"256","After compute charge, source file will be moved to this directory"));
		Vector vtValue = new Vector();
		vtValue.addElement("Directly");
		vtValue.addElement("Daily");
		vtValue.addElement("Monthly");
		vtValue.addElement("Yearly");
		vtValue.addElement("Delete file");
		vtReturn.addElement(createParameterDefinition("BackupStyle","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtReturn.addElement(createParameterDefinition("RejectDir","",ParameterType.PARAM_TEXTBOX_MAX,"256","If computing failure, source file will be moved to this directory"));
		vtValue = new Vector();
		vtValue.addElement("Directly");
		vtValue.addElement("Daily");
		vtValue.addElement("Monthly");
		vtValue.addElement("Yearly");
		vtValue.addElement("Delete file");
		vtReturn.addElement(createParameterDefinition("RejectStyle","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtReturn.addElement(createParameterDefinition("Wildcard","",ParameterType.PARAM_TEXTBOX_MAX,"256","Wildcard used to filter file to compute"));
		vtReturn.addElement(createParameterDefinition("SQLValidateCommand","",ParameterType.PARAM_TEXTAREA_MAX,"4000","SQL command used to validate file"));
		vtReturn.addElement(createParameterDefinition("SQLCommand","",ParameterType.PARAM_TEXTAREA_MAX,"4000","SQL command used to compute file"));
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
	protected String validateFile(File fl) throws Exception
	{
		// Validate file name
		if(mstrSQLValidateCommand != null && mstrSQLValidateCommand.length() > 0)
		{
			// SQL command
			String strSQL = mstrSQLValidateCommand;
			strSQL = StringUtil.replaceAll(strSQL,"$ThreadID",mstrThreadID);
			strSQL = StringUtil.replaceAll(strSQL,"$FileName",fl.getName());
			Statement stmt = mcnMain.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);

			String strValidationResult = null;
			if(rs.next())
				strValidationResult = rs.getString(1);

			rs.close();
			stmt.close();
			return strValidationResult;
		}
		return null;
	}
	////////////////////////////////////////////////////////
	public void processSession() throws Exception
	{
		File flDir = new File(mstrTextPath);
		File flFileList[] = flDir.listFiles(new WildcardFileFilter(mstrWildcard,false));
		if(flFileList == null)
			flFileList = new File[0];
		int iFileCount = flFileList.length;

		// Sort by last modified
		Arrays.sort(flFileList,new Comparator()
		{
			public int compare(Object a,Object b)
			{
				long lModifiedA = ((File)a).lastModified();
				long lModifiedB = ((File)b).lastModified();
				if(lModifiedA > lModifiedB)
					return 1;
				else if(lModifiedA < lModifiedB)
					return - 1;
				else return 0;
			}
		});

		for(int iFileIndex = 0;iFileIndex < iFileCount && miThreadCommand != ThreadConstant.THREAD_STOP;iFileIndex++)
		{
			try
			{
				// Log start
				logMonitor("Start compute charge of file " + flFileList[iFileIndex].getName());
				Statement stmt = mcnMain.createStatement();

				// Run SQLCommand
				String strValidateResult = validateFile(flFileList[iFileIndex]);
				boolean bResult = (strValidateResult == null || strValidateResult.length() == 0);
				if(!bResult)
					logMonitor(strValidateResult,mbAlertByMail);
				else
				{
					String strSQL = mstrSQLCommand;
					strSQL = StringUtil.replaceAll(strSQL,"$FileName",flFileList[iFileIndex].getName());
					strSQL = StringUtil.replaceAll(strSQL,"$FilePath",mstrTextPath);
					stmt.executeUpdate(strSQL);

					// Backup file
					FileUtil.backup(mstrTextPath,mstrBackupDir,flFileList[iFileIndex].getName(),flFileList[iFileIndex].getName(),mstrBackupStyle);

					// Log complete
					stmt.close();
					logMonitor("Compute charge of file " + flFileList[iFileIndex].getName() + " completed");
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logMonitor("Error occured: " + e.getMessage(),mbAlertByMail);

				// Reject file
				FileUtil.backup(mstrTextPath,mstrRejectDir,flFileList[iFileIndex].getName(),flFileList[iFileIndex].getName(),mstrRejectStyle);
			}
		}
	}
}

package com.fss.thread;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.fss.sql.*;
import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class FTPSenderGroup extends FTPSender
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected String mstrParameterID;
	protected String mstrDescription;
	protected String mstrServerName;
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		// Fill parameter
		mprtParam.put("LocalDir","/");
		mprtParam.put("FtpDir","/");
		mprtParam.put("BackupDir","/");
		mprtParam.put("Wildcard","*");
		mstrServerName = loadMandatory("ServerName");
		super.fillParameter();
		mbAutoConnectDB = true;
	}
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = super.getParameterDefinition();

		int iLastRemove = 0;
		for(int iIndex = vtReturn.size() - 1;iIndex >= 0;iIndex--)
		{
			String strParameterName = (String)((Vector)vtReturn.elementAt(iIndex)).elementAt(0);
			if(strParameterName.equals("LocalDir") ||
			   strParameterName.equals("LocalStyle") ||
			   strParameterName.equals("LocalFileFormat") ||
			   strParameterName.equals("FtpDir") ||
			   strParameterName.equals("FtpStyle") ||
			   strParameterName.equals("FtpFileFormat") ||
			   strParameterName.equals("BackupDir") ||
			   strParameterName.equals("BackupStyle") ||
			   strParameterName.equals("BackupFileFormat") ||
			   strParameterName.equals("Wildcard") ||
			   strParameterName.equals("ConnectDB"))
			{
				vtReturn.removeElementAt(iIndex);
				iLastRemove = iIndex;
			}
		}
		vtReturn.insertElementAt(createParameterDefinition("ServerName","",ParameterType.PARAM_TEXTBOX_MAX,"256",""),iLastRemove);

		return vtReturn;
	}
	////////////////////////////////////////////////////////
	public void fillParameter(Vector vtParameterValue) throws Exception
	{
		// Fill parameter value
		mstrParameterID = (String)vtParameterValue.elementAt(0);
		mstrDescription = loadMandatory("Description",(String)vtParameterValue.elementAt(1));
		mstrRemoteDir = loadDirectory("FtpDir",(String)vtParameterValue.elementAt(2),false,true);
		mstrRemoteStyle = (String)vtParameterValue.elementAt(3);
		mstrRemoteFileFormat = (String)vtParameterValue.elementAt(4);
		mstrLocalDir = loadDirectory("LocalDir",(String)vtParameterValue.elementAt(5),true,true);
		mstrLocalStyle = (String)vtParameterValue.elementAt(6);
		mstrLocalFileFormat = (String)vtParameterValue.elementAt(7);
		mstrBackupDir = loadDirectory("BackupDir",(String)vtParameterValue.elementAt(8),true,false);
		mstrBackupStyle = (String)vtParameterValue.elementAt(9);
		mstrBackupFileFormat = (String)vtParameterValue.elementAt(10);
		mstrWildcard = loadMandatory("Wildcard",(String)vtParameterValue.elementAt(11));
		mbNewDirectory = !loadYesNo("NewDirectory",(String)vtParameterValue.elementAt(12)).equals("N");

		// Validate parameter
		super.validateParameter();
	}
	////////////////////////////////////////////////////////
	protected void afterPutFile(File fl) throws Exception
	{
		super.afterPutFile(fl);

		// Update parameter
		String strSQL = "UPDATE FTP_PARAM SET" +
						" LAST_PROCESS_FILE='" + fl.getName() + "'," +
						" LAST_PROCESS_DATE=SYSDATE" +
						" WHERE FTP_PARAM_ID='" + mstrParameterID + "'";
		Statement stmt = mcnMain.createStatement();
		stmt.executeUpdate(strSQL);
		stmt.close();
	}
	////////////////////////////////////////////////////////
	public void processSession() throws Exception
	{
		// Parameter list
		Vector vtParameter = queryParameterList();

		for(int iParamIndex = 0;iParamIndex < vtParameter.size();iParamIndex++)
		{
			try
			{
				// Fill parameter value
				fillParameter((Vector)vtParameter.elementAt(iParamIndex));

				// Send file
				super.processSession();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logMonitor("Error occured while running " + mstrDescription + " session: " + e.getMessage(),mbAlertByMail);
			}
			finally
			{
				// Release id
				mstrParameterID = null;
			}
		}
	}
	////////////////////////////////////////////////////////
	protected Vector queryParameterList() throws Exception
	{
		String strSQL = "SELECT FTP_PARAM_ID,DESCRIPTION," +
						"FTP_DIR,FTP_STYLE,FTP_FORMAT," +
						"LOCAL_DIR,LOCAL_STYLE,LOCAL_FORMAT," +
						"BACKUP_DIR,BACKUP_STYLE,BACKUP_FORMAT," +
						"WILDCARD,NEW_DIRECTORY" +
						" FROM FTP_PARAM" +
						" WHERE STATUS<>'0'" +
						" AND UPPER(FTP_SERVER)=UPPER('" + mstrServerName + "')" +
						" AND FTP_TYPE='11'";
		return Database.executeQuery(mcnMain,strSQL);
	}
	////////////////////////////////////////////////////////
	protected void beforeProcessFileList() throws Exception
	{
		super.beforeProcessFileList();
		logMonitor(mstrDescription + " session running");
	}
}

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

public class FTPSequenceSenderGroup extends FTPSequenceSender
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

		// Fill parameter of ftp sequence sender
		mprtParam.put("MinSeqVal","0");
		mprtParam.put("MaxSeqVal","99999");
		mprtParam.put("FirstSeqPos","0");
		mprtParam.put("LastSeqPos","0");
		mprtParam.put("ExpectedSeq","0");
		mprtParam.put("SkipMissingFile","N");

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
			   strParameterName.equals("MinSeqVal") ||
			   strParameterName.equals("MaxSeqVal") ||
			   strParameterName.equals("FirstSeqPos") ||
			   strParameterName.equals("LastSeqPos") ||
			   strParameterName.equals("ExpectedSeq") ||
			   strParameterName.equals("SkipMissingFile") ||
			   strParameterName.equals("DateFormat") ||
			   strParameterName.equals("ProcessDate") ||
			   strParameterName.equals("LastProcessDate") ||
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
	public void storeConfig() throws Exception
	{
		super.storeConfig();

		if(mstrParameterID != null && mstrParameterID.length() > 0)
		{
			Connection cn = null;
			try
			{
				// Connect to db
				cn = mmgrMain.getConnection();

				// Update parameter
				String strSQL = "UPDATE FTP_PARAM SET" +
								" EXPECTED_SEQ=?,PROCESS_DATE=?" +
								" WHERE FTP_PARAM_ID='" + mstrParameterID + "'";

				PreparedStatement stmt = cn.prepareStatement(strSQL);
				stmt.setString(1,String.valueOf(miExpectedSeq));
				stmt.setString(2,mstrProcessDate);

				stmt.executeUpdate();
				stmt.close();
			}
			finally
			{
				Database.closeObject(cn);
			}
		}
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

		// Fill sequence parameter value
		miMinSeqVal = loadUnsignedInteger("MinSeqVal",(String)vtParameterValue.elementAt(13));
		miMaxSeqVal = loadUnsignedInteger("MaxSeqVal",(String)vtParameterValue.elementAt(14));
		miFirstSeqPos = loadUnsignedInteger("FirstSeqPos",(String)vtParameterValue.elementAt(15));
		miLastSeqPos = 0;
		if(((String)vtParameterValue.elementAt(16)).length() > 0);
			miLastSeqPos = loadUnsignedInteger("LastSeqPos",(String)vtParameterValue.elementAt(16));
		miExpectedSeq = loadUnsignedInteger("ExpectedSeq",(String)vtParameterValue.elementAt(17));
		mbSkipMissingFile = loadYesNo("SkipMissingFile",(String)vtParameterValue.elementAt(18)).equals("Y");
		mstrDateFormat = (String)vtParameterValue.elementAt(19);
		mstrProcessDate = (String)vtParameterValue.elementAt(20);

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
						"WILDCARD,NEW_DIRECTORY," +
						"MIN_SEQ_VAL,MAX_SEQ_VAL," +
						"FIRST_SEQ_POS,LAST_SEQ_POS," +
						"EXPECTED_SEQ,SKIP_MISSING_FILE," +
						"DATE_FORMAT,PROCESS_DATE" +
						" FROM FTP_PARAM" +
						" WHERE STATUS<>'0'" +
						" AND UPPER(FTP_SERVER)=UPPER('" + mstrServerName + "')" +
						" AND FTP_TYPE='12'";
		return Database.executeQuery(mcnMain,strSQL);
	}
	////////////////////////////////////////////////////////
	protected void beforeProcessFileList() throws Exception
	{
		super.beforeProcessFileList();
		logMonitor(mstrDescription + " session running");
	}
}

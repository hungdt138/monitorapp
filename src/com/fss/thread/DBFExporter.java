package com.fss.thread;

import java.util.*;
import java.sql.*;
import java.io.*;

import com.fss.util.*;
import com.fss.dbf.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class DBFExporter extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected int miFieldList[];
	protected int miFieldCount;

	// Parameter cache
	protected boolean mbCompressFile;
	protected String mstrExportDir;
	protected String mstrTempDir;
	protected String mstrDBFTemplate;
	protected String mstrFieldList;
	protected String mstrSQLNameCommand;
	protected String mstrSQLCommand;
	protected String mstrPreCommand;
	protected String mstrPstCommand;
	protected int miTotalFile;
	////////////////////////////////////////////////////////
	protected int miRecordCount = 0;
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		super.fillParameter();
		mbAutoConnectDB = true;
	}
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = super.getParameterDefinition();

		for(int iIndex = vtReturn.size() - 1;iIndex >= 0;iIndex--)
		{
			String strParameterName = (String)((Vector)vtReturn.elementAt(iIndex)).elementAt(0);
			if(strParameterName.equals("ConnectDB"))
				vtReturn.removeElementAt(iIndex);
		}

		return vtReturn;
	}
	////////////////////////////////////////////////////////
	public void buildFieldList(DBFUtil flDBF) throws Exception
	{
		// Get DBFFieldList
		String strFieldList = StringUtil.nvl(mstrFieldList,"");
		Vector mvtFieldList = new Vector();
		int iPosition;
		while((iPosition = strFieldList.indexOf(',')) > 0)
		{
			mvtFieldList.add(strFieldList.substring(0,iPosition).trim());
			strFieldList = strFieldList.substring(iPosition + 1,strFieldList.length()).trim();
		}
		if(strFieldList.length() > 0)
			mvtFieldList.add(strFieldList);

		// Build field-index list from tempalte
		miFieldCount = mvtFieldList.size();
		miFieldList = new int[miFieldCount];
		for(int iFieldIndex = 0;iFieldIndex < miFieldList.length;iFieldIndex++)
		{
			miFieldList[iFieldIndex] = flDBF.getFieldIndex((String)mvtFieldList.elementAt(iFieldIndex));
			if(miFieldList[iFieldIndex] < 0)
				 throw new IOException("Field with name " + (String)mvtFieldList.elementAt(iFieldIndex) + " does not exist in DBF file");
		}
	}
	////////////////////////////////////////////////////////
	public void processSession() throws Exception
	{
		// Apply parameter
		String strSQL = StringUtil.nvl(mstrSQLNameCommand,"");
		strSQL = StringUtil.replaceAll(strSQL,"$ThreadID",mstrThreadID);

		Statement stmt = mcnMain.createStatement();
		ResultSet rs = stmt.executeQuery(strSQL);
		miTotalFile = 0;
		while(rs.next() && miThreadCommand != ThreadConstant.THREAD_STOP)
		{
			String strDBFFileName = rs.getString(1);
			if(strDBFFileName != null && strDBFFileName.length() > 0)
			{
				exportFile(strDBFFileName);
				miTotalFile++;
			}
		}
		rs.close();
		stmt.close();
	}
	////////////////////////////////////////////////////////
	// Member function
	////////////////////////////////////////////////////////
	public void exportData(ResultSet rsSrc,DBFUtil flDst) throws IOException,SQLException
	{
		flDst.addRow();
		for(int iFieldIndex = 0;iFieldIndex < miFieldCount;iFieldIndex++)
		{
			String strVal = rsSrc.getString(iFieldIndex + 1);
			if(strVal == null) strVal = "";
			flDst.setFieldValue(strVal,miFieldList[iFieldIndex]);
		}
	}
	////////////////////////////////////////////////////////
	public void executePreCommand(String strFileName) throws Exception
	{
		if(mstrPreCommand != null && mstrPreCommand.length() > 0)
		{
			// Apply parameter
			String strSQL = mstrPreCommand;
			strSQL = StringUtil.replaceAll(strSQL,"$ThreadID",mstrThreadID);
			strSQL = StringUtil.replaceAll(strSQL,"$FileName",strFileName);
			strSQL = StringUtil.replaceAll(strSQL,"$LogID",mstrLogID);
			strSQL = StringUtil.replaceAll(strSQL,"$RecordCount",String.valueOf(miRecordCount));
			strSQL = StringUtil.replaceAll(strSQL,"$CommitCount",String.valueOf(miRecordCount));
			strSQL = StringUtil.replaceAll(strSQL,"$ErrorCount",String.valueOf(0));

			// Execute precommand
			Statement stmt = mcnMain.createStatement();
			stmt.executeUpdate(strSQL);
			stmt.close();
		}
	}
	////////////////////////////////////////////////////////
	public void executePstCommand(String strFileName) throws Exception
	{
		if(mstrPstCommand != null && mstrPstCommand.length() > 0)
		{
			// Apply parameter
			String strSQL = mstrPstCommand;
			strSQL = StringUtil.replaceAll(strSQL,"$ThreadID",mstrThreadID);
			strSQL = StringUtil.replaceAll(strSQL,"$FileName",strFileName);
			strSQL = StringUtil.replaceAll(strSQL,"$LogID",mstrLogID);
			strSQL = StringUtil.replaceAll(strSQL,"$RecordCount",String.valueOf(miRecordCount));
			strSQL = StringUtil.replaceAll(strSQL,"$CommitCount",String.valueOf(miRecordCount));
			strSQL = StringUtil.replaceAll(strSQL,"$ErrorCount",String.valueOf(0));

			// Execute pstcommand
			Statement stmt = mcnMain.createStatement();
			stmt.executeUpdate(strSQL);
			stmt.close();
		}
	}
	////////////////////////////////////////////////////////
	public boolean exportFile(String strFileName)
	{
		// Log to screen
		logMonitor("Start exporting data to file " + strFileName);

		// For parameter passing
		miRecordCount = 0;
		DBFUtil flDBF = null;

		try
		{
			// begin transaction
			mcnMain.setAutoCommit(false);

			// Execute precommand
			executePreCommand(strFileName);

			// Log start
			logStart(strFileName);

			// Full path of file
			String strFilePath = StringUtil.nvl(mstrExportDir,"/") + strFileName;
			String strTmpFile = StringUtil.nvl(mstrTempDir,"/") + strFileName;

			// Copy template file to tmp file
			FileUtil.copyResource(this.getClass(),StringUtil.nvl(mstrDBFTemplate,""),strTmpFile);

			// Open dbf file to put data
			flDBF = new DBFUtil(strTmpFile,false);
			flDBF.clearData();

			// Build field list
			buildFieldList(flDBF);

			// Apply parameter
			String strSQL = StringUtil.nvl(mstrSQLCommand,"");
			strSQL = StringUtil.replaceAll(strSQL,"$ThreadID",mstrThreadID);
			strSQL = StringUtil.replaceAll(strSQL,"$FileName",strFileName);

			// Get data from database
			Statement stmt = mcnMain.createStatement();
			ResultSet rs = stmt.executeQuery(strSQL);

			// Insert data into dbf file
			while(rs.next() && miThreadCommand != ThreadConstant.THREAD_STOP)
			{
				exportData(rs,flDBF);
				miRecordCount++;
			}
			if(miThreadCommand == ThreadConstant.THREAD_STOP)
				throw new SQLException("Thread interrupted");

			// release
			rs.close();
			stmt.close();

			// Commit file
			flDBF.close();
			afterSaveFile(strFileName);

			// Rename and pack file
			FileUtil.renameFile(strTmpFile,strFilePath);
			if(mbCompressFile)
			{
				SmartZip.Zip(strFilePath,strFilePath + ".zip",false);
				FileUtil.deleteFile(strFilePath);
			}

			// Execute pstcommand
			executePstCommand(strFileName);

			// Log completed
			logComplete(String.valueOf(miRecordCount),String.valueOf(miRecordCount),String.valueOf(0),"S");

			// Commit transaction
			mcnMain.commit();
			mcnMain.setAutoCommit(true);

			// Log to screen
			String strLog = "\r\n\tExport file " + strFileName + " completed\r\n\tResult:";
			strLog += "\r\n\t\tTotal records:\t\t" + String.valueOf(miRecordCount);
			strLog += "\r\n\t\tSuccess records:\t" + String.valueOf(miRecordCount);
			strLog += "\r\n\t\tError records:\t\t" + String.valueOf(0);
			logMonitor(strLog);
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			try
			{
				// Write log
				String strLog = "Error occured\n\t" + e.getLocalizedMessage();
				strLog += "\n\tExport file " + strFileName + " failed\n\t";
				logMonitor(strLog,mbAlertByMail);
				logDetail(e.getLocalizedMessage(),"","");
				logComplete(String.valueOf(0),String.valueOf(0),String.valueOf(miRecordCount),"F");

				// Commit transaction
				mcnMain.commit();
				mcnMain.setAutoCommit(true);
			}
			catch(Exception e1)
			{
				e.printStackTrace();
			}
			return false;
		}
		finally
		{
			try
			{
				flDBF.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	////////////////////////////////////////////////////////
	// After save file event
	////////////////////////////////////////////////////////
	public void afterSaveFile(String strFileName) throws Exception
	{
	}
}

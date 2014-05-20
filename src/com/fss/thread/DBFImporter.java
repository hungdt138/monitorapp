package com.fss.thread;

import java.util.*;
import java.sql.*;
import java.io.*;
import oracle.jdbc.driver.*;

import com.fss.dbf.*;
import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class DBFImporter extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected int miFieldList[];
	protected int miFieldCount;

	// Parameter cache
	protected String mstrImportDir;
	protected String mstrBackupDir;
	protected String mstrBackupStyle;
	protected String mstrWildcard;
	protected String mstrFieldList;
	protected String mstrSQLValidateCommand;
	protected String mstrSQLCommand;
	protected String mstrPreCommand;
	protected String mstrPstCommand;
	protected int miBatchSize;
	protected int miTotalFile;

	// Audit variables
	protected int miRecordIndex = 0;
	protected int miRecordCount = 0;
	protected int miCommitCount = 0;
	protected int miErrorCount = 0;
	////////////////////////////////////////////////////////
	public void buildFieldList(DBFUtil flDBF) throws Exception
	{
		// Get DBFFieldList
		String strFieldList = mstrFieldList;
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
	public void processSession() throws Exception
	{
		miTotalFile = 0;
		if(mstrWildcard.length() > 6 && mstrWildcard.substring(0,6).toString().equals("SELECT"))
		{
			Statement stmt = null;
			try
			{
				String strSQL = StringUtil.replaceAll(mstrWildcard,"$ThreadID",mstrThreadID);
				stmt = mcnMain.createStatement();

				ResultSet rs = stmt.executeQuery(strSQL);
				while(rs.next() && miThreadCommand != ThreadConstant.THREAD_STOP)
				{
					String strWildcard = rs.getString(1);
					File fl = new File(mstrImportDir);

					File flFileList[] = fl.listFiles(new WildcardFileFilter(strWildcard,false));
					if(flFileList != null && flFileList.length > 0)
					{
						Arrays.sort(flFileList,new Comparator()
						{
							public int compare(Object fl1,Object fl2)
							{
								return((File)fl1).getName().compareTo(((File)fl2).getName());
							}
						});
						int iFileCount = flFileList.length;
						for(int iFileIndex = 0;iFileIndex < iFileCount && miThreadCommand != ThreadConstant.THREAD_STOP;iFileIndex++)
						{
							miTotalFile++;
							importFile(flFileList[iFileIndex].getName());
						}
					}
				}

				rs.close();
			}
			finally
			{
				stmt.close();
			}
		}
		else
		{
			File fl = new File(mstrImportDir);
			File flFileList[] = fl.listFiles(new WildcardFileFilter(mstrWildcard,false));
			if(flFileList != null && flFileList.length > 0)
			{
				Arrays.sort(flFileList,new Comparator()
				{
					public int compare(Object fl1,Object fl2)
					{
						return((File)fl1).getName().compareTo(((File)fl2).getName());
					}
				});
				int iFileCount = flFileList.length;
				for(int iFileIndex = 0;iFileIndex < iFileCount && miThreadCommand != ThreadConstant.THREAD_STOP;iFileIndex++)
				{
					miTotalFile++;
					importFile(flFileList[iFileIndex].getName());
				}
			}
		}
	}
	////////////////////////////////////////////////////////
	// Member function
	////////////////////////////////////////////////////////
	public void importData(String strFileName,DBFUtil flSrc,OraclePreparedStatement stmtDst) throws Exception
	{
		// Insert loop
		while(flSrc.moveNext() && miThreadCommand != ThreadConstant.THREAD_STOP)
		{
			// Set insert value
			miRecordIndex++;
			for(int iFieldIndex = 0;iFieldIndex < miFieldCount;iFieldIndex++)
				stmtDst.setString(iFieldIndex + 1,flSrc.getFieldValue(miFieldList[iFieldIndex]));

			try
			{
				// Add inserted values to batch
				miCommitCount += stmtDst.executeUpdate();
				if(miRecordIndex % 50000 == 0 || miRecordIndex >= miRecordCount)
				{
					// Commit transaction
					miCommitCount += stmtDst.sendBatch();
					mcnMain.commit();
					mcnMain.setAutoCommit(false);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();

				// Increase commit and error count
				miCommitCount += stmtDst.getUpdateCount();
				miRecordIndex = miCommitCount + miErrorCount + 1;
				flSrc.move(miRecordIndex - 1);
				miErrorCount++;

				// Log detail
				logDetail(e.getLocalizedMessage(),"Row " + String.valueOf(miRecordIndex),"");
			}
		}
		if(miThreadCommand == ThreadConstant.THREAD_STOP)
			throw new SQLException("Thread interrupted");
	}
	////////////////////////////////////////////////////////
	public void executePreCommand(String strFileName) throws SQLException
	{
		if(mstrPreCommand != null && mstrPreCommand.length() > 0)
		{
			// Apply parameter
			String strSQL = mstrPreCommand;
			strSQL = StringUtil.replaceAll(strSQL,"$ThreadID",mstrThreadID);
			strSQL = StringUtil.replaceAll(strSQL,"$FileName",strFileName);
			strSQL = StringUtil.replaceAll(strSQL,"$LogID",mstrLogID);
			strSQL = StringUtil.replaceAll(strSQL,"$RecordCount",String.valueOf(miRecordCount));
			strSQL = StringUtil.replaceAll(strSQL,"$CommitCount",String.valueOf(miCommitCount));
			strSQL = StringUtil.replaceAll(strSQL,"$ErrorCount",String.valueOf(miErrorCount));

			// Execute precommand
			Statement stmt = mcnMain.createStatement();
			stmt.executeUpdate(strSQL);
			stmt.close();
		}
	}
	////////////////////////////////////////////////////////
	public void executePstCommand(String strFileName) throws SQLException
	{
		if(mstrPstCommand != null && mstrPstCommand.length() > 0)
		{
			// Apply parameter
			String strSQL = mstrPstCommand;
			strSQL = StringUtil.replaceAll(strSQL,"$ThreadID",mstrThreadID);
			strSQL = StringUtil.replaceAll(strSQL,"$FileName",strFileName);
			strSQL = StringUtil.replaceAll(strSQL,"$LogID",mstrLogID);
			strSQL = StringUtil.replaceAll(strSQL,"$RecordCount",String.valueOf(miRecordCount));
			strSQL = StringUtil.replaceAll(strSQL,"$CommitCount",String.valueOf(miCommitCount));
			strSQL = StringUtil.replaceAll(strSQL,"$ErrorCount",String.valueOf(miErrorCount));

			// Execute pstcommand
			Statement stmt = mcnMain.createStatement();
			stmt.executeUpdate(strSQL);
			stmt.close();
		}
	}
	////////////////////////////////////////////////////////
	public String validateFile(String strFileName) throws SQLException
	{
		// Validate file name
		if(mstrSQLValidateCommand != null && mstrSQLValidateCommand.length() > 0)
		{
			// SQL command
			String strSQL = mstrSQLValidateCommand;
			strSQL = StringUtil.replaceAll(strSQL,"$ThreadID",mstrThreadID);
			strSQL = StringUtil.replaceAll(strSQL,"$FileName",strFileName);
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
	public boolean importFile(String strFileName)
	{
		// For parameter passing
		DBFUtil flDBF = null;
		boolean bPacked = false;
		miRecordIndex = 0;
		miRecordCount = 0;
		miCommitCount = 0;
		miErrorCount = 0;

		try
		{
			// Check if file packed
			if(strFileName.toUpperCase().endsWith(".ZIP"))
			{
				bPacked = true;
				SmartZip.UnZip(mstrImportDir + strFileName,mstrImportDir);
				strFileName = strFileName.substring(0,strFileName.length() - 4);
			}

			// Validate file name
			String strValidateResult = validateFile(strFileName);

			// if validation failure -> return
			if(strValidateResult != null && strValidateResult.length() > 0)
			{
				if(bPacked) // Delete extracted file
				{
					FileUtil.deleteFile(mstrImportDir + strFileName);
					strFileName += ".zip";
				}
				logMonitor(strValidateResult,mbAlertByMail);
				return false;
			}

			// Log to screen
			logMonitor("Start import file " + strFileName);

			// Open dbf file to get data
			flDBF = new DBFUtil(mstrImportDir + strFileName,true);

			// Log parameter
			miRecordCount = (int)flDBF.getRecordCount();

			// Build field list
			buildFieldList(flDBF);

			// begin transaction
			mcnMain.setAutoCommit(false);

			// Execute precommand
			executePreCommand(strFileName);

			// Log start
			logStart(strFileName);

			// SQL insert command
			String strSQL = mstrSQLCommand;
			strSQL = StringUtil.replaceAll(strSQL,"$ThreadID",mstrThreadID);
			strSQL = StringUtil.replaceAll(strSQL,"$FileName",strFileName);
			OraclePreparedStatement stmtInsert = (OraclePreparedStatement)mcnMain.prepareStatement(strSQL);
			stmtInsert.setExecuteBatch(miBatchSize);

			// Import data
			importData(strFileName,flDBF,stmtInsert);
			if(miThreadCommand == ThreadConstant.THREAD_STOP)
				throw new SQLException("Thread interrupted");
			if(miRecordCount > 0 && miErrorCount == miRecordCount)
				throw new Exception("All records in file was rejected");

			// Release
			stmtInsert.close();
			flDBF.close();

			// Backup file
			if(bPacked) // Delete extracted file
			{
				FileUtil.deleteFile(mstrImportDir + strFileName);
				strFileName += ".zip";
			}
			FileUtil.backup(mstrImportDir,mstrBackupDir,strFileName,strFileName,mstrBackupStyle);

			// Execute pstcommand
			executePstCommand(strFileName);

			// Log compeleted
			logComplete(String.valueOf(miRecordCount),String.valueOf(miCommitCount),String.valueOf(miErrorCount),"S");

			// Commit transaction
			mcnMain.commit();
			mcnMain.setAutoCommit(true);

			// Log to screen
			String strLog = "\r\n\tImport file " + strFileName + " completed\r\n\tResult:";
			strLog += "\r\n\t\tTotal records:\t\t" + String.valueOf(miRecordCount);
			strLog += "\r\n\t\tSuccess records:\t" + String.valueOf(miCommitCount);
			strLog += "\r\n\t\tError records:\t\t" + String.valueOf(miErrorCount);
			logMonitor(strLog,mbAlertByMail && miErrorCount > 0);
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			try
			{
				// Write log
				String strLog = "Error occured\r\n\t" + e.getLocalizedMessage();
				strLog += "\r\n\tImport file " + strFileName + " failed\r\n\tResult:";
				strLog += "\r\n\t\tTotal records:\t" + String.valueOf(miRecordCount);
				strLog += "\r\n\t\tSuccess records:\t" + String.valueOf(miCommitCount);
				strLog += "\r\n\t\tError records:\t" + String.valueOf(miErrorCount);
				logMonitor(strLog,mbAlertByMail);
				logDetail(e.getLocalizedMessage(),"","");
				logComplete(String.valueOf(miRecordCount),String.valueOf(miCommitCount),String.valueOf(miErrorCount),"F");

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
}

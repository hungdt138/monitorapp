package com.fss.thread;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.fss.sql.*;
import com.fss.util.*;

import org.apache.commons.net.ftp.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class FTPSender extends FTPThread
{
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		super.fillParameter();
		mstrBackupDir = loadDirectory("BackupDir",true,false);
	}
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = super.getParameterDefinition();

		for(int iIndex = vtReturn.size() - 1;iIndex >= 0;iIndex--)
		{
			String strParameterName = (String)((Vector)vtReturn.elementAt(iIndex)).elementAt(0);
			if(strParameterName.equals("LocalFileFormat"))
				vtReturn.removeElementAt(iIndex);
		}

		return vtReturn;
	}
	////////////////////////////////////////////////////////
	public void validateParameter() throws Exception
	{
		super.validateParameter();
		if(mstrLocalStyle != null &&
		   mstrLocalStyle.length() > 0 &&
		   !mstrLocalStyle.equals("Directly"))
		{
			if(mstrProcessDate == null || mstrProcessDate.length() == 0)
				throw new AppException("ProcessDate cannot be null when LocalStyle='" + mstrLocalStyle + "'","FTPSender.validateParameter","ProcessDate");
			if(mstrDateFormat == null || mstrDateFormat.length() == 0)
				throw new AppException("DateFormat cannot be null when LocalStyle='" + mstrLocalStyle + "'","FTPSender.validateParameter","DateFormat");
			if(!DateUtil.isDate(mstrProcessDate,mstrDateFormat))
				throw new AppException("ProcessDate does not match DateFormat","FTPSender.validateParameter","ProcessDate");
			if(mstrLocalStyle.equals("Daily") &&
			   mstrDateFormat.indexOf("dd") < 0)
				throw new AppException("DateFormat must contain 'dd' when LocalStyle='" + mstrLocalStyle + "'","FTPSender.validateParameter","DateFormat");
			else if(mstrLocalStyle.equals("Monthly") &&
			   mstrDateFormat.indexOf("MM") < 0)
				throw new AppException("DateFormat must contain 'MM' when LocalStyle='" + mstrLocalStyle + "'","FTPSender.validateParameter","DateFormat");
			else if(mstrLocalStyle.equals("Yearly") &&
			   mstrDateFormat.indexOf("yyyy") < 0)
				throw new AppException("DateFormat must contain 'yyyy' when LocalStyle='" + mstrLocalStyle + "'","FTPSender.validateParameter","DateFormat");
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Process one file
	 * @param iFileIndex int
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void process(int iFileIndex) throws Exception
	{
		// Put file
		File fl = (File)mvtFileList.elementAt(iFileIndex);
		String strValidateResult = validateFile(fl);
		boolean bResult = (strValidateResult == null || strValidateResult.length() == 0);
		if(!bResult)
			logMonitor(strValidateResult,mbAlertByMail);
		putFile(fl);
	}
	////////////////////////////////////////////////////////
	/**
	 * Change next process date
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void changeProcessDate() throws Exception
	{
		if(mstrLocalStyle != null &&
		   mstrLocalStyle.length() > 0 &&
		   !mstrLocalStyle.equals("Directly"))
		{
			java.util.Date dt = DateUtil.toDate(mstrProcessDate,mstrDateFormat);
			do
			{
				if(mstrLocalStyle.equals("Daily"))
					dt = DateUtil.addDay(dt,1);
				else if(mstrLocalStyle.equals("Monthly"))
					dt = DateUtil.addMonth(dt,1);
				else if(mstrLocalStyle.equals("Yearly"))
					dt = DateUtil.addYear(dt,1);
				String strNextProcessDate = StringUtil.format(dt,mstrDateFormat);

				File fl = new File(mstrLocalDir + strNextProcessDate);
				if(fl.exists() && fl.isDirectory())
				{
					// Store config
					mbNewDirectory = true;
					mprtParam.put("NewDirectory","Y");
					mstrProcessDate = strNextProcessDate;
					mprtParam.put("ProcessDate",mstrProcessDate);
					storeConfig();
					return;
				}
			}
			while(dt.getTime() < System.currentTimeMillis());
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Filter file
	 * @param fl File
	 * @return File
	 */
	////////////////////////////////////////////////////////
	public File createListItem(File fl)
	{
		return fl;
	}
	////////////////////////////////////////////////////////
	/**
	 * List file from local folder
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	protected void listFile() throws Exception
	{
		mbListing = true;
		try
		{
			beforeListFile();

			// Get scandir
			if(mstrLocalStyle != null &&
			   mstrLocalStyle.length() > 0 &&
			   !mstrLocalStyle.equals("Directly"))
				mstrScanDir = mstrLocalDir + mstrProcessDate + "/";
			else
				mstrScanDir = mstrLocalDir;

			// Get storage dir
			mstrStorageDir = mstrRemoteDir;
			if(mstrRemoteStyle.equals("Daily"))
				mstrStorageDir += StringUtil.format(new java.util.Date(),"yyyyMMdd") + "/";
			else if(mstrRemoteStyle.equals("Monthly"))
				mstrStorageDir += StringUtil.format(new java.util.Date(),"yyyyMM") + "/";
			else if(mstrRemoteStyle.equals("Yearly"))
				mstrStorageDir += StringUtil.format(new java.util.Date(),"yyyy") + "/";

			// List file
			if(!mftpMain.changeWorkingDirectory(mstrStorageDir))
			{
				if(!mftpMain.makeDirectory(mstrStorageDir))
				{
					if(!mftpMain.changeWorkingDirectory(mstrStorageDir))
						throw new Exception("Could not create foler " + mstrStorageDir + " on ftp server");
				}
			}
			mprtDirectoryList = new Hashtable();
			mvtFileList = new Vector();
			listFile("");

			afterListFile();
		}
		finally
		{
			mbListing = false;
		}
	}
	////////////////////////////////////////////////////////
	protected void listFile(String strAdditionPath) throws Exception
	{
		// Get list of file in folder
		File flLocalDir = new File(mstrScanDir + strAdditionPath);
		File[] flFileList = flLocalDir.listFiles(new WildcardFileFilter(mstrWildcard,mbRecursive));

		if(flFileList != null)
		{
			for(int iFileIndex = 0;iFileIndex < flFileList.length;iFileIndex++)
			{
				if(flFileList[iFileIndex].isDirectory())
				{
					if(mbRecursive)
					{
						String strAdditionChildPath = strAdditionPath + flFileList[iFileIndex].getName() + "/";
						if(!mftpMain.changeWorkingDirectory(mstrStorageDir + strAdditionChildPath))
						{
							if(!mftpMain.makeDirectory(mstrStorageDir + strAdditionChildPath))
							{
								if(!mftpMain.changeWorkingDirectory(mstrStorageDir + strAdditionChildPath))
									throw new Exception("Could not create foler " + mstrStorageDir + strAdditionChildPath + " on ftp server");
							}
						}
						listFile(strAdditionChildPath);
					}
				}
				else
				{
					File fl = createListItem(flFileList[iFileIndex]);
					if(fl != null)
					{
						mvtFileList.addElement(fl);
						if(strAdditionPath.length() > 0)
							mprtDirectoryList.put(fl,strAdditionPath);
					}
				}
			}
		}
	}
	///////////////////////////////////////////////////////////////////////////
	/**
	 * After list file event
	 * @throws Exception
	 */
	///////////////////////////////////////////////////////////////////////////
	protected void afterListFile() throws Exception
	{
		Collections.sort(mvtFileList,new Comparator()
		{
			public int compare(Object obj1,Object obj2)
			{
				return ((File)obj1).getName().compareTo(((File)obj2).getName());
			}
		});
	}
	////////////////////////////////////////////////////////
	/**
	 * Before list file event
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	protected void beforeListFile() throws Exception
	{
	}
	////////////////////////////////////////////////////////
	/**
	 * Validate file
	 * @param fl File
	 * @throws Exception
	 * @return String
	 */
	////////////////////////////////////////////////////////
	protected String validateFile(File fl) throws Exception
	{
		// Validate file name
		if(mstrSQLValidateCommand != null && mstrSQLValidateCommand.length() > 0)
		{
			Connection cn = null;
			try
			{
				if(mcnMain != null)
					cn = mcnMain;
				else
					cn = mmgrMain.getConnection();

				// SQL command
				String strSQL = mstrSQLValidateCommand;
				strSQL = StringUtil.replaceAll(strSQL,"$ThreadID",mstrThreadID);
				strSQL = StringUtil.replaceAll(strSQL,"$FileName",fl.getName());
				Statement stmt = cn.createStatement();
				ResultSet rs = stmt.executeQuery(strSQL);

				String strValidationResult = null;
				if(rs.next())
					strValidationResult = rs.getString(1);

				rs.close();
				stmt.close();
				return strValidationResult;
			}
			finally
			{
				if(cn != null && cn != mcnMain)
					Database.closeObject(cn);
			}
		}
		return "";
	}
	////////////////////////////////////////////////////////
	/**
	 * Before put file event
	 * @param fl FTPFile
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	protected void beforePutFile(File fl) throws Exception
	{
		// Log start
		logStart(fl.getName());
		logMonitor("Start putting file " + fl.getName() + " to ftp server");
	}
	////////////////////////////////////////////////////////
	/**
	 * After put file event
	 * @param fl FTPFile
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	protected void afterPutFile(File fl) throws Exception
	{
		// Log completed
		logComplete("1","1","0","S");
		logMonitor("Putting file " + fl.getName() + " completed");
	}
	////////////////////////////////////////////////////////
	/**
	 * Error put file event
	 * @param fl File
	 * @param strErrorDescription String
	 */
	////////////////////////////////////////////////////////
	protected void errPutFile(File fl,String strErrorDescription)
	{
		try
		{
			// Write log
			logDetail(strErrorDescription,"","");
			logComplete("1","0","1","F");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Put file to ftp server
	 * @param fl FTPFile
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	protected void putFile(File fl) throws Exception
	{
		try
		{
			// pre put file
			beforePutFile(fl);

			// Put file
			String strAdditionPath = StringUtil.nvl(mprtDirectoryList.get(fl),"");
			String strRemoteFilePath = mstrStorageDir + strAdditionPath + FileUtil.formatFileName(fl.getName(),mstrRemoteFileFormat);
			FileInputStream is = null;
			try
			{
				is = new FileInputStream(fl);
				if(!mftpMain.storeFile(strRemoteFilePath + ".TMP",is))
					throw new Exception("Upload file failed");
			}
			finally
			{
				FileUtil.safeClose(is);
			}

			// Validate file size
			try
			{
				FTPFile fflFileList[] = mftpMain.listFiles(strRemoteFilePath + ".TMP");
				if(fflFileList == null || fflFileList.length <= 0)
					throw new Exception("Upload file failed");
				if(fflFileList[0].getSize() != fl.length())
					throw new Exception("Putted file size does not equals to local file size");
			}
			catch(Exception e)
			{
				mftpMain.deleteFile(strRemoteFilePath + ".TMP");
				throw e;
			}
			mftpMain.deleteFile(strRemoteFilePath);
			if(!mftpMain.rename(strRemoteFilePath + ".TMP",strRemoteFilePath))
				throw new Exception("Could not rename temp file (" + strRemoteFilePath + ".TMP) to remote file (" + strRemoteFilePath + ")");

			try
			{
				// Backup file
				FileUtil.backup(fl.getAbsoluteFile().getParent() + "/",mstrBackupDir,
								fl.getName(),FileUtil.formatFileName(fl.getName(),mstrBackupFileFormat),
								mstrBackupStyle,strAdditionPath);

				// pst put file
				afterPutFile(fl);
			}
			catch(Exception e)
			{
				mftpMain.deleteFile(strRemoteFilePath);
				throw e;
			}
		}
		catch(Exception e)
		{
			// err put file
			errPutFile(fl,e.getMessage());
			throw e;
		}
	}
}

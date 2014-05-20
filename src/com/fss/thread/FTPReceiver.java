package com.fss.thread;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.fss.sql.*;
import com.fss.util.*;

import org.apache.commons.net.ftp.*;
import org.apache.commons.net.ftp.parser.*;

/**
 * <p>Title: </p>
 * <p>Description: Thread get file from ftp server</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class FTPReceiver extends FTPThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected String mstrTempDir;
	protected String mstrListingMode;
	private String mstrDirectBackupDir;
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		super.fillParameter();
		mstrTempDir = loadDirectory("TempDir",true,false);
		mstrListingMode = StringUtil.nvl(mprtParam.get("ListingMode"),"");
	}
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = super.getParameterDefinition();

		vtReturn.insertElementAt(createParameterDefinition("TempDir","",ParameterType.PARAM_TEXTBOX_MAX,"256",""),16);
		Vector vtValue = new Vector();
		vtValue.addElement("Auto detect");
		vtValue.addElement("Unix");
		vtValue.addElement("EnterpriseUnix");
		vtValue.addElement("NT");
		vtValue.addElement("OS2");
		vtValue.addElement("OS400");
		vtValue.addElement("VMS");
		vtValue.addElement("VMSVersioning");
		vtReturn.insertElementAt(createParameterDefinition("ListingMode","",ParameterType.PARAM_COMBOBOX,vtValue,""),7);

		for(int iIndex = vtReturn.size() - 1;iIndex >= 0;iIndex--)
		{
			String strParameterName = (String)((Vector)vtReturn.elementAt(iIndex)).elementAt(0);
			if(strParameterName.equals("FtpFileFormat"))
				vtReturn.removeElementAt(iIndex);
		}

		return vtReturn;
	}
	////////////////////////////////////////////////////////
	public void validateParameter() throws Exception
	{
		super.validateParameter();
		if(mstrRemoteStyle != null &&
		   mstrRemoteStyle.length() > 0 &&
		   !mstrRemoteStyle.equals("Directly"))
		{
			if(mstrProcessDate == null || mstrProcessDate.length() == 0)
				throw new AppException("ProcessDate cannot be null when FTPStyle='" + mstrRemoteStyle + "'","FTPReceiver.validateParameter",
									   "ProcessDate");
			if(mstrDateFormat == null || mstrDateFormat.length() == 0)
				throw new AppException("DateFormat cannot be null when FTPStyle='" + mstrRemoteStyle + "'","FTPReceiver.validateParameter",
									   "DateFormat");
			if(!DateUtil.isDate(mstrProcessDate,mstrDateFormat))
				throw new AppException("ProcessDate does not match DateFormat","FTPReceiver.validateParameter","ProcessDate");
			if(mstrRemoteStyle.equals("Daily") &&
			   mstrDateFormat.indexOf("dd") < 0)
				throw new AppException("DateFormat must contain 'dd' when FTPStyle='" + mstrRemoteStyle + "'","FTPReceiver.validateParameter",
									   "DateFormat");
			else if(mstrRemoteStyle.equals("Monthly") &&
					mstrDateFormat.indexOf("MM") < 0)
				throw new AppException("DateFormat must contain 'MM' when FTPStyle='" + mstrRemoteStyle + "'","FTPReceiver.validateParameter",
									   "DateFormat");
			else if(mstrRemoteStyle.equals("Yearly") &&
					mstrDateFormat.indexOf("yyyy") < 0)
				throw new AppException("DateFormat must contain 'yyyy' when FTPStyle='" + mstrRemoteStyle + "'","FTPReceiver.validateParameter",
									   "DateFormat");
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
		// Get file
		FTPFile ffl = (FTPFile)mvtFileList.elementAt(iFileIndex);
		String strValidateResult = validateFile(ffl);
		boolean bResult = (strValidateResult == null || strValidateResult.length() == 0);
		if(!bResult)
			logMonitor(strValidateResult,mbAlertByMail);
		else
			getFile(ffl);
	}
	////////////////////////////////////////////////////////
	/**
	 * Change next process date
	 */
	////////////////////////////////////////////////////////
	public void changeProcessDate() throws Exception
	{
		if(mstrRemoteStyle != null &&
		   mstrRemoteStyle.length() > 0 &&
		   !mstrRemoteStyle.equals("Directly"))
		{
			java.util.Date dt = DateUtil.toDate(mstrProcessDate,mstrDateFormat);
			do
			{
				if(mstrRemoteStyle.equals("Daily"))
					dt = DateUtil.addDay(dt,1);
				else if(mstrRemoteStyle.equals("Monthly"))
					dt = DateUtil.addMonth(dt,1);
				else if(mstrRemoteStyle.equals("Yearly"))
					dt = DateUtil.addYear(dt,1);
				String strNextProcessDate = StringUtil.format(dt,mstrDateFormat);

				if(mftpMain.changeWorkingDirectory(mstrRemoteDir + strNextProcessDate))
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
	public FTPFile createListItem(FTPFile ffl)
	{
		// Some ftp server not support ls [wildcard] -> need to check
		if(!ffl.isFile() || !WildcardFilter.match(mstrWildcard,ffl.getName()))
			return null;
		return ffl;
	}
	////////////////////////////////////////////////////////
	/**
	 * List file from remote folder
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	protected void listFile() throws Exception
	{
		mbListing = true;
		try
		{
			beforeListFile();

			mvtFileList = new Vector();
			if(mstrListingMode == null ||
			   mstrListingMode.length() == 0 ||
			   mstrListingMode.equals("Auto detect"))
			{
				// Log monitor
				logMonitor("Detecting listing mode");

				// Test listing mode
				FTPFile[] fflFileList = null;
				mstrListingMode = "";

				// Test OS400
				if(mstrListingMode.length() == 0)
				{
					fflFileList = mftpMain.listFiles(new OS400FTPEntryParser());
					if(fflFileList.length > 0)
						mstrListingMode = "OS400";
				}

				// Test OS2
				if(mstrListingMode.length() == 0)
				{
					fflFileList = mftpMain.listFiles(new OS2FTPEntryParser());
					if(fflFileList.length > 0)
						mstrListingMode = "OS2";
				}

				// Test VMSVersioning
				if(mstrListingMode.length() == 0)
				{
					fflFileList = mftpMain.listFiles(new VMSVersioningFTPEntryParser());
					if(fflFileList.length > 0)
						mstrListingMode = "VMSVersioning";
				}

				// Test VMS
				if(mstrListingMode.length() == 0)
				{
					fflFileList = mftpMain.listFiles(new VMSFTPEntryParser());
					if(fflFileList.length > 0)
						mstrListingMode = "VMS";
				}

				// Test EnterpriseUnix
				if(mstrListingMode.length() == 0)
				{
					fflFileList = mftpMain.listFiles(new EnterpriseUnixFTPEntryParser());
					if(fflFileList.length > 0)
						mstrListingMode = "EnterpriseUnix";
				}

				// Test Unix
				if(mstrListingMode.length() == 0)
				{
					fflFileList = mftpMain.listFiles(new UnixFTPEntryParser());
					if(fflFileList.length > 0)
						mstrListingMode = "Unix";
				}

				// Test NT
				if(mstrListingMode.length() == 0)
				{
					fflFileList = mftpMain.listFiles(new NTFTPEntryParser());
					if(fflFileList.length > 0)
						mstrListingMode = "NT";
				}

				// Display result
				if(mstrListingMode.length() == 0)
				{
					logMonitor("Could not detect the listing mode of this server");
					mstrListingMode = "Auto detect";
				}
				else
				{
					logMonitor("Detected " + mstrListingMode + " listing mode");
					mprtParam.put("ListingMode",mstrListingMode);
					storeConfig();
				}
			}

			if(mstrListingMode.length() > 0 &&
			   !mstrListingMode.equals("Auto detect"))
			{
				// Create parser
				FTPFileListParser parser = null;
				if(mstrListingMode.equals("EnterpriseUnix"))
					parser = new EnterpriseUnixFTPEntryParser();
				else if(mstrListingMode.equals("NT"))
					parser = new NTFTPEntryParser();
				else if(mstrListingMode.equals("OS2"))
					parser = new OS2FTPEntryParser();
				else if(mstrListingMode.equals("OS400"))
					parser = new OS400FTPEntryParser();
				else if(mstrListingMode.equals("VMS"))
					parser = new VMSFTPEntryParser();
				else if(mstrListingMode.equals("VMSVersioning"))
					parser = new VMSVersioningFTPEntryParser();
				else //if(mstrListingMode.equals("Unix"))
					parser = new UnixFTPEntryParser();

				// Get scandir
				if(mstrRemoteStyle != null &&
				   mstrRemoteStyle.length() > 0 &&
				   !mstrRemoteStyle.equals("Directly"))
					mstrScanDir = mstrRemoteDir + mstrProcessDate + "/";
				else
					mstrScanDir = mstrRemoteDir;

				// Get storage dir
				mstrStorageDir = mstrLocalDir;
				if(mstrLocalStyle.equals("Daily"))
					mstrStorageDir += StringUtil.format(new java.util.Date(),"yyyyMMdd") + "/";
				else if(mstrLocalStyle.equals("Monthly"))
					mstrStorageDir += StringUtil.format(new java.util.Date(),"yyyyMM") + "/";
				else if(mstrLocalStyle.equals("Yearly"))
					mstrStorageDir += StringUtil.format(new java.util.Date(),"yyyy") + "/";
				FileUtil.forceFolderExist(mstrStorageDir);

				// Direct backup dir
				if(mstrBackupDir.length() > 0 && !mstrBackupStyle.equals("Delete file"))
				{
					mstrDirectBackupDir = mstrBackupDir;
					if(mstrBackupStyle.equals("Daily"))
						mstrDirectBackupDir += StringUtil.format(new java.util.Date(),"yyyyMMdd") + "/";
					else if(mstrBackupStyle.equals("Monthly"))
						mstrDirectBackupDir += StringUtil.format(new java.util.Date(),"yyyyMM") + "/";
					else if(mstrBackupStyle.equals("Yearly"))
						mstrDirectBackupDir += StringUtil.format(new java.util.Date(),"yyyy") + "/";
					if(!mftpMain.changeWorkingDirectory(mstrDirectBackupDir))
					{
						if(!mftpMain.makeDirectory(mstrDirectBackupDir))
						{
							if(!mftpMain.changeWorkingDirectory(mstrDirectBackupDir))
								throw new Exception("Could not create foler " + mstrDirectBackupDir + " on ftp server");
						}
					}
				}

				// List file
				mprtDirectoryList = new Hashtable();
				mvtFileList = new Vector();
				listFile("",parser);
			}

			afterListFile();
		}
		finally
		{
			mbListing = false;
		}
	}
	////////////////////////////////////////////////////////
	protected void listFile(String strAdditionPath,FTPFileListParser parser) throws Exception
	{
		// Get list of file in folder
		if(!mftpMain.changeWorkingDirectory(mstrScanDir + strAdditionPath))
			throw new Exception("Could not change working directory to remote directory (" + mstrScanDir + strAdditionPath + ")");
		FTPFile[] fflFileList = mftpMain.listFiles(parser);

		if(fflFileList != null)
		{
			for(int iFileIndex = 0;iFileIndex < fflFileList.length;iFileIndex++)
			{
				if(fflFileList[iFileIndex].isDirectory())
				{
					if(mbRecursive)
					{
						String strAdditionChildPath = strAdditionPath + fflFileList[iFileIndex].getName() + "/";
						FileUtil.forceFolderExist(mstrStorageDir + strAdditionChildPath);

						// Create backup dir
						if(mstrBackupDir.length() > 0 && !mstrBackupStyle.equals("Delete file"))
						{
							if(!mftpMain.changeWorkingDirectory(mstrDirectBackupDir + strAdditionChildPath))
							{
								if(!mftpMain.makeDirectory(mstrDirectBackupDir + strAdditionChildPath))
								{
									if(!mftpMain.changeWorkingDirectory(mstrDirectBackupDir + strAdditionChildPath))
										throw new Exception("Could not create foler " + mstrDirectBackupDir + strAdditionChildPath + " on ftp server");
								}
							}
						}

						listFile(strAdditionChildPath,parser);
					}
				}
				else
				{
					FTPFile ffl = createListItem(fflFileList[iFileIndex]);
					if(ffl != null)
					{
						mvtFileList.addElement(ffl);
						mprtDirectoryList.put(ffl,strAdditionPath);
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
				return ((FTPFile)obj1).getName().compareTo(((FTPFile)obj2).getName());
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
	protected String validateFile(FTPFile ffl) throws Exception
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
				strSQL = StringUtil.replaceAll(strSQL,"$FileName",ffl.getName());
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
	 * Before get file event
	 * @param ffl FTPFile
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	protected void beforeGetFile(FTPFile ffl) throws Exception
	{
		// Log start
		logStart(ffl.getName());
		logMonitor("Start getting file " + ffl.getName() + " from ftp server");
	}
	////////////////////////////////////////////////////////
	/**
	 * After get file event
	 * @param ffl FTPFile
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	protected void afterGetFile(FTPFile ffl) throws Exception
	{
		// Log completed
		logComplete("1","1","0","S");
		logMonitor("Getting file " + ffl.getName() + " completed");
	}
	////////////////////////////////////////////////////////
	/**
	 * Error get file event
	 * @param ffl FTPFile
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	protected void errGetFile(FTPFile ffl,String strErrorDescription)
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
	 * Get file from ftp server
	 * @param ffl FTPFile
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	protected void getFile(FTPFile ffl) throws Exception
	{
		try
		{
			// Before get file event
			beforeGetFile(ffl);

			// Get file
			String strAdditionPath = StringUtil.nvl(mprtDirectoryList.get(ffl),"");
			String strRemoteFilePath = mstrScanDir + strAdditionPath + ffl.getName();
			FileOutputStream os = null;
			try
			{
				os = new FileOutputStream(mstrTempDir + ffl.getName());
				if(!mftpMain.retrieveFile(strRemoteFilePath,os))
					throw new Exception("Download file failed:\r\n\t\t" + mftpMain.getReplyString());
			}
			finally
			{
				FileUtil.safeClose(os);
			}

			// Validate file size
			File fl = new File(mstrTempDir + ffl.getName());
			if(!fl.exists())
				throw new Exception("Download file failed, file does not exist");
			if(fl.length() != ffl.getSize())
				throw new Exception("Getted file size does not equals to ftp file size");

			// Backup file
			if(mstrBackupStyle.equals("Delete file"))
			{
				if(!mftpMain.deleteFile(mstrScanDir + strAdditionPath + ffl.getName()))
					throw new Exception("Cannot delete file " + mstrScanDir + strAdditionPath + ffl.getName());
			}
			else if(mstrBackupDir.length() > 0)
			{
				String strBackupPath = mstrDirectBackupDir + strAdditionPath + FileUtil.formatFileName(ffl.getName(),mstrBackupFileFormat);
				mftpMain.deleteFile(strBackupPath);
				if(!mftpMain.rename(mstrScanDir + strAdditionPath + ffl.getName(),strBackupPath))
					throw new Exception("Cannot rename file " + mstrScanDir + strAdditionPath + ffl.getName() +
										" to " + strBackupPath);
			}

			// Make local file
			String strGettedFilePath = FileUtil.backup(mstrTempDir,mstrLocalDir,ffl.getName(),
													   formatFileName(ffl.getName(),mstrLocalFileFormat),
													   mstrLocalStyle,strAdditionPath);
			try
			{
				// After get file event
				afterGetFile(ffl);
			}
			catch(Exception e)
			{
				FileUtil.deleteFile(strGettedFilePath);
				throw e;
			}
		}
		catch(Exception e)
		{
			errGetFile(ffl,e.getMessage());
			throw e;
		}
	}

	public String formatFileName(String strFileName,String strFileFormat) throws AppException
	{
		if(strFileName == null || strFileName.length() == 0 ||
		   strFileFormat == null || strFileFormat.length() == 0)
			return strFileName;
		int iExtIndex = strFileName.lastIndexOf('.');
		if(iExtIndex < 0)
			iExtIndex = strFileName.length();
		int iBaseIndex = strFileName.lastIndexOf('/');
		if(iBaseIndex < 0)
			iBaseIndex = strFileName.lastIndexOf('\\');
		if(iBaseIndex < 0)
			iBaseIndex = 0;
		String strBaseFileName = strFileName.substring(iBaseIndex,iExtIndex);
		String strFileExtension = "";
		if(iExtIndex < strFileName.length() - 1)
			strFileExtension = strFileName.substring(iExtIndex + 1,strFileName.length());
		strFileFormat = StringUtil.replaceAll(strFileFormat,"$FileName",strFileName);
		strFileFormat = StringUtil.replaceAll(strFileFormat,"$BaseFileName",strBaseFileName);
		strFileFormat = StringUtil.replaceAll(strFileFormat,"$FileExtension",strFileExtension);
		strFileFormat = StringUtil.replaceAll(strFileFormat,"$ProcessDate",mstrProcessDate);
		return strFileFormat;
	}

}

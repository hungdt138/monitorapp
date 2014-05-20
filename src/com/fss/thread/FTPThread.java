package com.fss.thread;

import java.util.*;

import com.fss.util.*;

import org.apache.commons.net.ftp.*;

/**
 * <p>Title: </p>
 * <p>Description: Thread get file from ftp server</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public abstract class FTPThread extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected FTPClient mftpMain;

	// FTP variables
	protected String mstrHost;
	protected int miPort;
	protected String mstrUser;
	protected String mstrPassword;
	protected String mstrTransferType;
	protected String mstrConnectMode;
	protected int miTimeout;
	protected String mstrLocalDir;
	protected String mstrLocalStyle;
	protected String mstrLocalFileFormat;
	protected String mstrRemoteDir;
	protected String mstrRemoteStyle;
	protected String mstrRemoteFileFormat;
	protected String mstrBackupDir;
	protected String mstrBackupStyle;
	protected String mstrBackupFileFormat;
	protected String mstrWildcard;
	protected String mstrSQLValidateCommand;
	protected boolean mbRecursive;
	protected String mstrDateFormat;
	protected String mstrProcessDate;
	protected boolean mbNewDirectory;
	protected String mstrScanDir;
	protected String mstrStorageDir;

	// Common used variables
	protected boolean mbListing = false;
	protected int miListItemCount;

	// DirectoryListItem
	protected Vector mvtFileList;
	protected Hashtable mprtDirectoryList;

	////////////////////////////////////////////////////////
	// Member function
	////////////////////////////////////////////////////////
	protected abstract void process(int iFileIndex) throws Exception;
	protected abstract void listFile() throws Exception;
	protected abstract void changeProcessDate() throws Exception;
	protected void beforeProcessFileList() throws Exception
	{
	}
	protected void afterProcessFileList() throws Exception
	{
	}
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		// Fill parameter
		mstrHost = loadMandatory("Host");
		miPort = loadUnsignedInteger("Port");
		mstrUser = StringUtil.nvl(mprtParam.get("User"),"");
		mstrPassword = StringUtil.nvl(mprtParam.get("Password"),"");
		mstrTransferType = loadMandatory("TransferType");
		mstrConnectMode = loadMandatory("ConnectMode");
		miTimeout = loadUnsignedInteger("Timeout") * 1000;
		mstrLocalDir = loadDirectory("LocalDir",true,true);
		mstrLocalStyle = StringUtil.nvl(mprtParam.get("LocalStyle"),"");
		mstrLocalFileFormat = StringUtil.nvl(mprtParam.get("LocalFileFormat"),"");
		mstrRemoteDir = loadDirectory("FtpDir",false,true);
		mstrRemoteStyle = StringUtil.nvl(mprtParam.get("FtpStyle"),"");
		mstrRemoteFileFormat = StringUtil.nvl(mprtParam.get("FtpFileFormat"),"");
		mstrBackupDir = loadDirectory("BackupDir",false,false);
		mstrBackupStyle = StringUtil.nvl(mprtParam.get("BackupStyle"),"");
		mstrBackupFileFormat = StringUtil.nvl(mprtParam.get("BackupFileFormat"),"");
		mstrWildcard = loadMandatory("Wildcard");
		mstrSQLValidateCommand = StringUtil.nvl(mprtParam.get("SQLValidateCommand"),"");
		mstrDateFormat = StringUtil.nvl(mprtParam.get("DateFormat"),"");
		mstrProcessDate = StringUtil.nvl(mprtParam.get("ProcessDate"),"");
		mbRecursive = loadYesNo("Recursive").equals("Y");
		mbNewDirectory = !loadYesNo("NewDirectory").equals("N");
		super.fillParameter();
	}
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = new Vector();

		vtReturn.addElement(createParameterDefinition("Host","",ParameterType.PARAM_TEXTBOX_FILTER,ParameterType.FILTER_REGULAR,""));
		vtReturn.addElement(createParameterDefinition("Port","",ParameterType.PARAM_TEXTBOX_MASK,"99990",""));
		vtReturn.addElement(createParameterDefinition("User","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtReturn.addElement(createParameterDefinition("Password","",ParameterType.PARAM_PASSWORD,"100",""));
		Vector vtValue = new Vector();
		vtValue.addElement("BINARY");
		vtValue.addElement("ASCII");
		vtReturn.addElement(createParameterDefinition("TransferType","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtValue = new Vector();
		vtValue.addElement("PASSIVE");
		vtValue.addElement("ACTIVE");
		vtReturn.addElement(createParameterDefinition("ConnectMode","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtReturn.addElement(createParameterDefinition("Timeout","",ParameterType.PARAM_TEXTBOX_MASK,"99990",""));
		vtReturn.addElement(createParameterDefinition("FtpDir","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtValue = new Vector();
		vtValue.addElement("Directly");
		vtValue.addElement("Daily");
		vtValue.addElement("Monthly");
		vtValue.addElement("Yearly");
		vtReturn.addElement(createParameterDefinition("FtpStyle","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtReturn.addElement(createParameterDefinition("FtpFileFormat","",ParameterType.PARAM_TEXTBOX_MAX,"256","Format ftp file name.\r\n Can use $FileName,$BaseFileName,$FileExtension as parameter"));
		vtReturn.addElement(createParameterDefinition("LocalDir","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtValue = new Vector();
		vtValue.addElement("Directly");
		vtValue.addElement("Daily");
		vtValue.addElement("Monthly");
		vtValue.addElement("Yearly");
		vtReturn.addElement(createParameterDefinition("LocalStyle","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtReturn.addElement(createParameterDefinition("LocalFileFormat","",ParameterType.PARAM_TEXTBOX_MAX,"256","Format local file name.\r\n Can use $FileName,$BaseFileName,$FileExtension as parameter"));
		vtReturn.addElement(createParameterDefinition("BackupDir","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtValue = new Vector();
		vtValue.addElement("Directly");
		vtValue.addElement("Daily");
		vtValue.addElement("Monthly");
		vtValue.addElement("Yearly");
		vtValue.addElement("Delete file");
		vtReturn.addElement(createParameterDefinition("BackupStyle","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtReturn.addElement(createParameterDefinition("BackupFileFormat","",ParameterType.PARAM_TEXTBOX_MAX,"256","Format backup file name.\r\n Can use $FileName,$BaseFileName,$FileExtension as parameter"));
		vtReturn.addElement(createParameterDefinition("DateFormat","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtReturn.addElement(createParameterDefinition("ProcessDate","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtReturn.addElement(createParameterDefinition("Wildcard","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		vtReturn.addElement(createParameterDefinition("SQLValidateCommand","",ParameterType.PARAM_TEXTAREA_MAX,"4000",""));
		vtValue = new Vector();
		vtValue.addElement("Y");
		vtValue.addElement("N");
		vtReturn.addElement(createParameterDefinition("Recursive","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtReturn.addAll(super.getParameterDefinition());

		return vtReturn;
	}
	////////////////////////////////////////////////////////
	public void beforeSession() throws Exception
	{
		super.beforeSession();

		// Make sure ftp session is closed
		try
		{
			if(mftpMain != null)
				mftpMain.disconnect();
		}
		catch(Exception e)
		{
		}

		// Open FTP Connection
		mftpMain = new FTPClient();
		mftpMain.setDefaultTimeout(miTimeout);
		mftpMain.setDataTimeout(miTimeout);
		mftpMain.connect(mstrHost,miPort);
		if(!FTPReply.isPositiveCompletion(mftpMain.getReplyCode()))
			throw new Exception("FTP server refused connection");
		if(!mftpMain.login(mstrUser,mstrPassword))
			throw new Exception("Could not log in to ftp server. Invalid user name or password");
		mftpMain.setFileTransferMode(FTPClient.COMPRESSED_TRANSFER_MODE);
		if(mstrTransferType.equalsIgnoreCase("ASCII"))
			mftpMain.setFileType(FTPClient.ASCII_FILE_TYPE);
		else
			mftpMain.setFileType(FTPClient.BINARY_FILE_TYPE);
		if(mstrConnectMode.equalsIgnoreCase("ACTIVE"))
			mftpMain.enterLocalActiveMode();
		else
			mftpMain.enterLocalPassiveMode();
	}
	////////////////////////////////////////////////////////
	public void afterSession() throws Exception
	{
		super.afterSession();

		// Release ftp connection
		try
		{
			mftpMain.disconnect();
			mftpMain = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	public void processSession() throws Exception
	{
		// Fill receive file list
		listFile();

		// Receive list of file
		miListItemCount = mvtFileList.size();
		if(miListItemCount > 0)
		{
			beforeProcessFileList();
			for(int iIndex = 0;iIndex < miListItemCount && miThreadCommand != ThreadConstant.THREAD_STOP;iIndex++)
			{
				process(iIndex);
				if(mbNewDirectory)
				{
					mbNewDirectory = false;
					mprtParam.put("NewDirectory","N");
					storeConfig();
				}
			}
			afterProcessFileList();
		}

		// Change next process date
		if(miListItemCount == 0 && miThreadCommand != ThreadConstant.THREAD_STOP)
		   changeProcessDate();
	}
}

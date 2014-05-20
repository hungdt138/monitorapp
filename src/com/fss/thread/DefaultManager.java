package com.fss.thread;

import java.io.*;
import java.sql.*;
import java.util.*;

import com.fss.sql.*;
import com.fss.util.*;

public class DefaultManager implements ProcessorListener
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private Hashtable mprtConfig = new Hashtable();
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public DefaultManager()
	{
		try
		{
			loadServerConfig("/com/fss/thread/ServerConfig.txt");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Load parameter from resource
	// Date: 29/04/2003
	////////////////////////////////////////////////////////
	public void loadServerConfig(String strFileName) throws Exception
	{
		mprtConfig = Global.loadHashtable(DefaultManager.class.getResourceAsStream(strFileName));
	}
	////////////////////////////////////////////////////////
	// Purpose: Get parameter from memory
	// Date: 29/04/2003
	////////////////////////////////////////////////////////
	public String getParameter(String strKey)
	{
		return StringUtil.nvl(mprtConfig.get(strKey),"");
	}
	////////////////////////////////////////////////////////
	// Purpose: Get parameter from memory
	// Date: 29/04/2003
	////////////////////////////////////////////////////////
	public Connection getConnection() throws Exception
	{
		if(getParameter("DatabaseExist").equals("N"))
			return null;
		return Database.getConnection(StringUtil.nvl(mprtConfig.get("DBUrl"),""),
									  StringUtil.nvl(mprtConfig.get("UserName"),""),
									  StringUtil.nvl(mprtConfig.get("Password"),""));
	}
	public void onCreate(ThreadProcessor processor) throws Exception
	{
		if(getParameter("DatabaseExist").equals("N"))
			return;
		processor.log = new DBTableLogUtil();
		processor.authenticator = new com.fss.util.DBTableAuthenticator();
	}
	public void onOpen(ThreadProcessor processor) throws Exception
	{
		if(getParameter("DatabaseExist").equals("N"))
			return;
		processor.mcnMain = getConnection();
		((DBTableLogUtil)processor.log).setConnection(processor.mcnMain);
		if(processor.channel != null && processor.channel.msckMain != null)
			((DBTableLogUtil)processor.log).setIPAddress(processor.channel.msckMain.getInetAddress().getHostAddress());
		((DBTableAuthenticator)processor.authenticator).setConnection(processor.mcnMain);
	}
	///////////////////////////////////////////////////////////
	// Main entry
	///////////////////////////////////////////////////////////
	public static void main(String args[])
	{
		try
		{
			// Change system output to file
			DefaultManager lsn = new DefaultManager();
			String strLogFile = lsn.getParameter("LogFile");
			if(strLogFile == null || strLogFile.length() == 0)
				strLogFile = "error.log";
			String strWorkingDir = System.getProperty("user.dir");
			if(!strWorkingDir.endsWith("/") || !strWorkingDir.endsWith("\\"))
				strWorkingDir += "/";
			PrintStream ps = new PrintStream(new LogOutputStream(strWorkingDir + strLogFile));
			System.setOut(ps);
			System.setErr(ps);

			// Application name
			Global.APP_NAME = ThreadConstant.APP_NAME;
			Global.APP_VERSION = ThreadConstant.APP_VERSION;

			// Port id
			int iPortID = 8338;
			try
			{
				iPortID = Integer.parseInt(lsn.getParameter("PortID"));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ThreadManager cs = new ThreadManager(iPortID,lsn);

			// Set action log file
			strLogFile = lsn.getParameter("ActionLogFile");
			if(strLogFile != null && strLogFile.length() > 0)
				cs.setActionLogFile(strLogFile);

			// Set max logfile size
			try
			{
				if(lsn.getParameter("MaxLogFileSize").length() > 0)
				{
					int iMaxLogFileSize = Integer.parseInt(lsn.getParameter("MaxLogFileSize"));
					if(iMaxLogFileSize > 0)
						cs.setMaxLogFileSize(iMaxLogFileSize);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			// Set MaxLogContentSize
			try
			{
				if(lsn.getParameter("MaxLogContentSize").length() > 0)
				{
					int iMaxLogContentSize = Integer.parseInt(lsn.getParameter("MaxLogContentSize"));
					if(iMaxLogContentSize > 0)
						cs.setMaxLogContentSize(iMaxLogContentSize);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			// Set max connection
			try
			{
				int iMaxConnectionAllowed = Integer.parseInt(lsn.getParameter("MaxConnectionAllowed"));
				if(iMaxConnectionAllowed > 0)
					cs.setMaxConnectionAllowed(iMaxConnectionAllowed);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			// Set loading method
			try
			{
				int iLoadingMethod = Integer.parseInt(lsn.getParameter("LoadingMethod"));
				if(iLoadingMethod == ThreadManager.LOAD_FROM_FILE ||
				   iLoadingMethod == ThreadManager.LOAD_FROM_DB ||
				   iLoadingMethod == ThreadManager.LOAD_FROM_FILE_AND_DB)
					cs.setLoadingMethod(iLoadingMethod);
			}
			catch(Exception e)
			{
				String strLoadingMethod = lsn.getParameter("LoadingMethod");
				if(strLoadingMethod.equals("Database"))
					cs.setLoadingMethod(ThreadManager.LOAD_FROM_DB);
				else if(strLoadingMethod.equals("File"))
					cs.setLoadingMethod(ThreadManager.LOAD_FROM_FILE);
				else
					cs.setLoadingMethod(ThreadManager.LOAD_FROM_FILE_AND_DB);
			}

			// Start manager
			cs.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

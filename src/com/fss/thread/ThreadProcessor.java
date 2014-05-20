package com.fss.thread;

import java.io.*;
import java.sql.*;
import java.net.*;
import java.util.*;
import java.text.*;

import com.fss.sql.*;
import com.fss.util.*;
import com.fss.ddtp.*;
import com.fss.dictionary.*;
import com.fss.dictionary.Dictionary;

/**
 * <p>Title: </p>
 * <p>Description:
 *    DDTPServer only process request from client
 *    and pass response for it
 * </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author
 * - Thai Hoang Hiep
 * - Dang Dinh Trung
 * @version 2.0
 */

public class ThreadProcessor extends DBSocketProcessor
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	public static final String SYSTEM_PASSWORD_FILE = "system.pwd";
	private ThreadManager mmgrMain;
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private ProcessorListener mlsn = null;
	////////////////////////////////////////////////////////
	/**
	 * Set processor listener
	 * @param lsn ThreadProcessorListener
	 */
	////////////////////////////////////////////////////////
	public void setProcessorListener(ProcessorListener lsn)
	{
		mlsn = lsn;
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Create new instance of ThreadProcessor
	 * @throws java.lang.Exception
	 */
	/////////////////////////////////////////////////////////////////
	public ThreadProcessor() throws Exception
	{
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Create new instance of ThreadProcessor
	 * @param mgr ThreadManager
	 * @throws Exception
	 */
	/////////////////////////////////////////////////////////////////
	public ThreadProcessor(ThreadManager mgr) throws Exception
	{
		mmgrMain = mgr;
		setProcessorListener(mmgrMain.getProcessorListener());
		try
		{
			mlsn.onCreate(this);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/////////////////////////////////////////////////////////////////
	/**
	 *
	 * @param objCaller Object
	 */
	/////////////////////////////////////////////////////////////////
	public void setCaller(Object objCaller)
	{
		super.setCaller(objCaller);
		if(channel != null && channel.mobjParent instanceof ThreadManager)
		{
			mmgrMain = (ThreadManager)channel.mobjParent;
			setProcessorListener(mmgrMain.getProcessorListener());
			try
			{
				mlsn.onCreate(this);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	/////////////////////////////////////////////////////////////////
	/**
	 * Open connection to database
	 * @throws java.lang.Exception
	 */
	/////////////////////////////////////////////////////////////////
	public void open() throws Exception
	{
		mlsn.onOpen(this);
	}
	////////////////////////////////////////////////////////
	/**
	 * Check accesstime of a user
	 * @param strUserID User ID
	 * @return true if user is allowed to access at the moment
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public boolean checkAccessTime(String strUserID)
	{
		if(strUserID.equals("0"))
			return true;

		if(authenticator == null)
			return true;

		try
		{
			open();
			return authenticator.checkAccessTime(strUserID);
		}
		catch(Exception e)
		{
			return false;
		}
		finally
		{
			close();
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Check access right of a user
	// Inputs: ID of user, module id and right code
	// Outputs: Exception throw if user does not have
	// previledge to access module
	// Author: HiepTH
	// Date: 01/05/2003
	////////////////////////////////////////////////////////
	public void checkPrivilege(String strUserID,String strModuleName,String strRightCode) throws Exception
	{
		if(strUserID.equals("0"))
			return;

		if(authenticator == null)
			return;

		try
		{
			open();
			authenticator.checkPrivilege(strUserID,strModuleName,strRightCode);
		}
		finally
		{
			close();
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Verify user password
	// Inputs: user's name,user's password
	// Outputs: Exception throw if user does not exist
	// or password is incorrect
	// Author: HiepTH
	// Date: 01/05/2003
	////////////////////////////////////////////////////////
	public Vector login(String strUserName,String strPassword,InetAddress address) throws Exception
	{
		Vector vtReturn = new Vector();
		if(strUserName.equals("system"))
		{
			if(!strPassword.equals(getSystemPassword()))
				throw new AppException("FSS-00006","ThreadProcessor.verifyPassword");
			vtReturn.addElement("0");
			vtReturn.addElement("1");
			return vtReturn;
		}

		if(authenticator == null)
			throw new AppException("FSS-10015","ThreadProcessor.verifyPassword");

		try
		{
			open();
			String strUserID = authenticator.verifyPassword(strUserName,strPassword);
			authenticator.checkIP(address,strUserID);
			authenticator.checkPrivilege(strUserID,ThreadConstant.SYSTEM_THREAD_MANAGER,"S");
			logModuleAccess(mcnMain,ThreadConstant.SYSTEM_THREAD_MANAGER,strUserID,address.getHostAddress());
			vtReturn.addElement(strUserID);
			if(authenticator.isPasswordExpired(strUserID))
				vtReturn.addElement("0");
			else
				vtReturn.addElement("1");
			return vtReturn;
		}
		finally
		{
			close();
		}
	}
	////////////////////////////////////////////////////////
	// Change password of a user
	// Inputs: Connection to database,user's name,old password,new password
	// Author: HiepTH
	// Date: 21/09/2003
	////////////////////////////////////////////////////////
	public void changePassword(String strUserName,String strOldPassword,String strNewPassword,String strRealPassword) throws Exception
	{
		if(strUserName.equals("system"))
		{
			if(!strOldPassword.equals(getSystemPassword()))
				throw new AppException("FSS-00006","ThreadProcessor.verifyPassword");
			setSystemPassword(strNewPassword);
			return;
		}

		if(authenticator == null)
			throw new AppException("FSS-10015","ThreadProcessor.verifyPassword");

		try
		{
			open();
			authenticator.validatePassword(strRealPassword);
			authenticator.changePassword(strUserName,strOldPassword,strNewPassword);
		}
		finally
		{
			close();
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Set password of system user
	// Inputs: Password to set
	// Outputs: Exception throw if error ocurred
	// Author: HiepTH
	// Date: 01/05/2003
	////////////////////////////////////////////////////////
	public static void setSystemPassword(String strPassword) throws AppException
	{
		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(SYSTEM_PASSWORD_FILE);
			os.write(strPassword.getBytes());
		}
		catch(IOException e)
		{
			throw new AppException(e,"ThreadProcessor.setSystemPassword");
		}
		finally
		{
			FileUtil.safeClose(os);
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: get password of system user
	// Outputs: password of system user of Exception throw if error
	// Author: HiepTH
	// Date: 01/05/2003
	////////////////////////////////////////////////////////
	public static String getSystemPassword()
	{
		FileInputStream is = null;
		try
		{
			// Password cannot be greater than 1024 character
			byte buf[] = new byte[1024];
			is = new FileInputStream(SYSTEM_PASSWORD_FILE);
			int iResult = is.read(buf);
			if(iResult < 0)
				iResult = 0;
			return new String(buf,0,iResult);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			FileUtil.safeClose(is);
		}
		return "";
	}
	///////////////////////////////////////////////////////////
	public void startThread() throws Exception
	{
		// Get thread monitor to start
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.startThread",strThreadID);

		// Check priviledge
		checkPrivilege(channel.getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"U");

		// Start thread
		mmgrMain.logAction("User '" + channel.getUserName() + "' try to start thread '" + thread.getThreadName() + "'");
		mmgrMain.startThread(thread);
	}
	///////////////////////////////////////////////////////////
	public void stopThread() throws Exception
	{
		// Get thread monitor to stop
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.stopThread",strThreadID);

		// Check priviledge
		checkPrivilege(channel.getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"U");

		// Stop thread
		mmgrMain.logAction("User '" + channel.getUserName() + "' try to stop thread '" + thread.getThreadName() + "'");
		mmgrMain.stopThread(thread);
	}
	////////////////////////////////////////////////////////
	/**
	 * Destroy manageable thread
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void destroyThread() throws Exception
	{
		// Get thread monitor to destroy
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.stopThread",strThreadID);

		// Check priviledge
		checkPrivilege(channel.getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"U");

		// Destroy thread
		mmgrMain.logAction("User '" + channel.getUserName() + "' try to destroy thread '" + thread.getThreadName() + "'");
		thread.destroy();
		thread.logMonitor("Thread destroyed");
	}
	///////////////////////////////////////////////////////////
	public void loadSetting() throws Exception
	{
		// Get thread monitor to load setting
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.loadSetting",strThreadID);

		// Load parameter from database
		thread.loadConfig();

		// Fill ThreadClass
		response.setString("ThreadClassName",thread.getClassName());

		// Fill Startup Type
		Vector vtStartupType = new Vector();
		Vector vtRow = new Vector();
		vtRow.addElement("0");
		vtRow.addElement("Disabled");
		vtStartupType.addElement(vtRow);
		vtRow = new Vector();
		vtRow.addElement("1");
		vtRow.addElement("Automatic");
		vtStartupType.addElement(vtRow);
		vtRow = new Vector();
		vtRow.addElement("2");
		vtRow.addElement("Manual");
		vtStartupType.addElement(vtRow);
		response.setVector("vtStartupType",vtStartupType);
		response.setString("ThreadStartupType",thread.getStartupType());

		// Fill parameter value
		Vector vtSetting = thread.getParameterDefinition();
		int iSize = vtSetting.size();
		for(int iParameterIndex = 0;iParameterIndex < iSize;iParameterIndex++)
		{
			Vector vtSettingRow = (Vector)vtSetting.elementAt(iParameterIndex);
			String strParamName = (String)vtSettingRow.elementAt(0);
			Object objValue = thread.getParameter(strParamName);
			vtSettingRow.setElementAt(objValue,1);
		}
		response.setVector("vtSetting",vtSetting);
	}
	///////////////////////////////////////////////////////////
	public void storeSetting() throws Exception
	{
		// Get thread monitor to store setting
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		String strThreadName = StringUtil.nvl(request.getString("ThreadName"),"");
		String strThreadClass = StringUtil.nvl(request.getString("ThreadClass"),"");
		String strThreadStartupType = StringUtil.nvl(request.getString("ThreadStartupType"),"");
		Vector vtSetting = request.getVector("vtSetting");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.storeSetting",strThreadID);

		// Check priviledge
		checkPrivilege(channel.getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"U");

		// Write thread data to storage
		mmgrMain.logAction("User '" + channel.getUserName() + "' try to update parameter of thread '" + strThreadName + "'");
		try
		{
			open();
			if(mcnMain != null)
				mcnMain.setAutoCommit(false);

			// Validate input
			ManageableThread thrTemp = (ManageableThread)Class.forName(strThreadClass).newInstance();
			thrTemp.mmgrMain = mmgrMain;
			thrTemp.setThreadName(strThreadName);
			thrTemp.setClassName(strThreadClass);
			thrTemp.setStartupType(strThreadStartupType);
			thrTemp.setThreadID(strThreadID);
			thrTemp.miManageMethod = thread.miManageMethod;
			if(vtSetting != null && vtSetting.size() > 0)
			{
				// Validate using temp thread
				thrTemp.removeAllParameter();
				for(int iRowIndex = 0;iRowIndex < vtSetting.size();iRowIndex++)
				{
					String strName = StringUtil.nvl(((Vector)vtSetting.elementAt(iRowIndex)).elementAt(0).toString(),"");
					thrTemp.setParameter(strName,((Vector)vtSetting.elementAt(iRowIndex)).elementAt(1));
				}
				thrTemp.fillParameter();
				thrTemp.validateParameter();
				thrTemp.storeConfig(channel,log,mcnMain);
			}

			thrTemp.updateStorage(channel,log,mcnMain);
			if(mcnMain != null)
			{
				mcnMain.commit();
				mcnMain.setAutoCommit(true);
			}
		}
		finally
		{
			close();
		}
		mmgrMain.logAction("Parameter of thread '" + strThreadName + "' updated");
	}
	///////////////////////////////////////////////////////////
	public void manageThreadsLoad() throws Exception
	{
		Vector vtTableData = new Vector();
		if(mmgrMain.getLoadingMethod() == ThreadManager.LOAD_FROM_FILE_AND_DB ||
		   mmgrMain.getLoadingMethod() == ThreadManager.LOAD_FROM_DB)
		{
			Connection cn = null;
			try
			{
				// Get db thread
				cn = mlsn.getConnection();
				String strSQL = "SELECT THREAD_NAME,CLASS_NAME,STARTUP_TYPE,THREAD_ID" +
								" FROM THREAD ORDER BY THREAD_NAME";
				vtTableData = Database.executeQuery(cn,strSQL);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				Database.closeObject(cn);
			}
		}

		if(mmgrMain.getLoadingMethod() == ThreadManager.LOAD_FROM_FILE_AND_DB ||
		   mmgrMain.getLoadingMethod() == ThreadManager.LOAD_FROM_FILE)
		{
			// Get file thread
			Dictionary dicThreadList = ManageableThread.loadThreadConfig();
			if(dicThreadList == null || dicThreadList.mndRoot.mvtChild == null)
				return;

			Vector vtThreadList = dicThreadList.mndRoot.mvtChild;
			for(int iIndex = 0;iIndex < vtThreadList.size();iIndex++)
			{
				// Get thread info
				DictionaryNode nd = (DictionaryNode)vtThreadList.elementAt(iIndex);
				Dictionary dicThreadInfo = new Dictionary(nd);
				String strThreadID = nd.mstrName;
				String strThreadName = dicThreadInfo.getString("ThreadName");
				String strClassName = dicThreadInfo.getString("ClassName");
				String strStartupType = dicThreadInfo.getString("StartupType");

				Vector vtRowData = new Vector();
				vtRowData.addElement(strThreadName);
				vtRowData.addElement(strClassName);
				vtRowData.addElement(strStartupType);
				vtRowData.addElement(strThreadID);
				vtTableData.addElement(vtRowData);
			}
		}
		response.setVector("vtTableData",vtTableData);

		// Fix startup type
		Vector vtStartupType = new Vector();
		Vector vtRow = new Vector();
		vtRow.addElement("0");
		vtRow.addElement("Disabled");
		vtStartupType.addElement(vtRow);
		vtRow = new Vector();
		vtRow.addElement("1");
		vtRow.addElement("Automatic");
		vtStartupType.addElement(vtRow);
		vtRow = new Vector();
		vtRow.addElement("2");
		vtRow.addElement("Manual");
		vtStartupType.addElement(vtRow);
		response.setVector("vtStartupType",vtStartupType);
	}
	///////////////////////////////////////////////////////////
	public void manageThreadsStore() throws Exception
	{
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		String strThreadName = request.getString("ThreadName");
		String strThreadClass = request.getString("ThreadClass");
		String strThreadStartupType = request.getString("ThreadStartupType");

		// Check priviledge
		checkPrivilege(channel.getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"U");

		// Update database & file
		mmgrMain.logAction("User '" + channel.getUserName() + "' try to update Hashtable of thread '" + strThreadName + "'");
		try
		{
			try
			{
				open();
				if(mcnMain != null)
					mcnMain.setAutoCommit(false);
				ManageableThread thrTemp = (ManageableThread)Class.forName(strThreadClass).newInstance();
				if(!thrTemp.updateDBThread(mcnMain,strThreadName,strThreadClass,strThreadStartupType,strThreadID,channel,log))
					throw new AppException("FSS-00021","ThreadProcessor.manageThreadsStore",strThreadID);
				if(mcnMain != null)
				{
					mcnMain.commit();
					mcnMain.setAutoCommit(true);
				}
			}
			finally
			{
				close();
			}
		}
		catch(Exception e)
		{
			try
			{
				open();
				if(mcnMain != null)
					mcnMain.setAutoCommit(false);
				ManageableThread thrTemp = (ManageableThread)Class.forName(strThreadClass).newInstance();
				if(!thrTemp.updateFileThread(strThreadName,strThreadClass,strThreadStartupType,strThreadID,channel,log))
					throw e;
				if(mcnMain != null)
				{
					mcnMain.commit();
					mcnMain.setAutoCommit(true);
				}
			}
			finally
			{
				close();
			}
		}
		ManageableThread thr = mmgrMain.getThread(strThreadID);
		if(thr != null)
		{
			thr.setThreadName(strThreadName);
			thr.setStartupType(strThreadStartupType);
		}
		mmgrMain.logAction("Hashtable of thread '" + strThreadName + "' updated");
	}
	///////////////////////////////////////////////////////////
	public void closeServer() throws Exception
	{
		checkPrivilege(channel.getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"U");
		mmgrMain.logAction("User '" + channel.getUserName() + "' try to shutdown server");
		mmgrMain.serverSocket.close();
	}
	///////////////////////////////////////////////////////////
	public void changePassword() throws Exception
	{
		String strOldPassword = StringUtil.nvl(request.getString("OldPassword"),"");
		String strNewPassword = StringUtil.nvl(request.getString("NewPassword"),"");
		String strRealPassword = request.getString("RealPassword");
		changePassword(channel.getUserName(),strOldPassword,strNewPassword,strRealPassword);
	}
	///////////////////////////////////////////////////////////
	public void sendMessage() throws Exception
	{
		String strMessage = "<FONT style=\"background-color:#FFE4C4\">" +
							"<FONT color=\"#660000\"><U><EM>" + channel.getUserName() +
							" said:</EM></U></FONT>&nbsp;" +
							"<FONT color=\"#003399\"><B>" + request.getString("strMessage") +
							"</B></FONT></FONT>";
		mmgrMain.logAction(strMessage);
	}
	///////////////////////////////////////////////////////////
	public Vector queryUserList() throws Exception
	{
		Vector vtUser = new Vector();
		for(int iIndex = 0;iIndex < mmgrMain.getChannelCount();iIndex++)
		{
			Vector vtData = new Vector();
			vtData.addElement(mmgrMain.getChannel(iIndex).toString());
			vtData.addElement(mmgrMain.getChannel(iIndex).getUserName());
			vtData.addElement(Global.FORMAT_DATE_TIME.format(mmgrMain.getChannel(iIndex).dtStart));
			vtData.addElement(mmgrMain.getChannel(iIndex).msckMain.getInetAddress().getHostName());
			vtUser.addElement(vtData);
		}
		return vtUser;
	}
	///////////////////////////////////////////////////////////
	public void kickUser() throws Exception
	{
		// Check priviledge
		checkPrivilege(channel.getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"E");
		String strChannel = request.getString("strChannel");
		for(int iIndex = 0;iIndex < mmgrMain.getChannelCount();iIndex++)
		{
			if(mmgrMain.getChannel(iIndex).toString().equals(strChannel))
			{
				mmgrMain.logAction("User '" + channel.getUserName() + "' try to kick user '" + mmgrMain.getChannel(iIndex).getUserName() + "'");
				mmgrMain.getChannel(iIndex).close();
				return;
			}
		}
		throw new AppException("FSS-00016","ThreadProcessor.kickUser");
	}
	///////////////////////////////////////////////////////////
	public void addSchedule() throws Exception
	{
		checkPrivilege(channel.getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"U");
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.addSchedule",strThreadID);
		String strSchedule = StringUtil.nvl(request.getString("Schedule"),"");
		try
		{
			open();
			if(mcnMain != null)
				mcnMain.setAutoCommit(false);
			response.setString("ScheduleID",thread.addSchedule(strSchedule,channel,log,mcnMain));
			if(mcnMain != null)
			{
				mcnMain.commit();
				mcnMain.setAutoCommit(true);
			}
		}
		finally
		{
			close();
		}
	}
	///////////////////////////////////////////////////////////
	public void updateSchedule() throws Exception
	{
		checkPrivilege(channel.getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"U");
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.updateSchedule",strThreadID);
		String strScheduleID = StringUtil.nvl(request.getString("ScheduleID"),"");
		String strSchedule = StringUtil.nvl(request.getString("Schedule"),"");
		try
		{
			open();
			if(mcnMain != null)
				mcnMain.setAutoCommit(false);
			thread.updateSchedule(strScheduleID,strSchedule,channel,log,mcnMain);
			if(mcnMain != null)
			{
				mcnMain.commit();
				mcnMain.setAutoCommit(true);
			}
		}
		finally
		{
			close();
		}
	}
	///////////////////////////////////////////////////////////
	public void deleteSchedule() throws Exception
	{
		checkPrivilege(channel.getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"U");
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.deleteSchedule",strThreadID);
		String strScheduleID = StringUtil.nvl(request.getString("ScheduleID"),"");
		try
		{
			open();
			if(mcnMain != null)
				mcnMain.setAutoCommit(false);
			thread.deleteSchedule(strScheduleID,channel,log,mcnMain);
			if(mcnMain != null)
			{
				mcnMain.commit();
				mcnMain.setAutoCommit(true);
			}
		}
		finally
		{
			close();
		}
	}
	///////////////////////////////////////////////////////////
	public String querySchedule() throws Exception
	{
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.querySchedule",strThreadID);
		return thread.querySchedule();
	}
	///////////////////////////////////////////////////////////
	public void startImmediate() throws Exception
	{
		checkPrivilege(channel.getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"U");
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.querySchedule",strThreadID);
		thread.runImmediate();
	}
	///////////////////////////////////////////////////////////
	public void loadThreadLog() throws Exception
	{
		// Get thread monitor to load setting
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.loadThreadLog",strThreadID);

		// Load parameter from database
		thread.loadConfig();

		// Get log dir
		String strLogDir = StringUtil.nvl(thread.getParameter("LogDir"),"");
		File fl = new File(strLogDir);
		File flFileList[] = fl.listFiles(new WildcardFileFilter("*.log",false));
		if(flFileList != null && flFileList.length > 0)
		{
			Arrays.sort(flFileList,new Comparator()
			{
				public int compare(Object fl1,Object fl2)
				{
					return -((File)fl1).getName().compareTo(((File)fl2).getName());
				}
			});

			String strOldMonth = "";
			String strNewMonth = "";

			Vector vtLogDir = new Vector();
			Vector vtLogNode = null;

			Vector vtOther = new Vector();
			vtOther.addElement("Other");
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			dateFormat.setLenient(false);

			for(int i = 0;i < flFileList.length;i++)
			{
				String strFileName = flFileList[i].getName();
				// Test date format
				String strDate = strFileName.substring(0,strFileName.length() - 4);
				try
				{
					java.util.Date date = dateFormat.parse(strDate);

					strNewMonth = strFileName.substring(0,strFileName.length() - 6);
					if(!strOldMonth.equals(strNewMonth))
					{
						if(vtLogNode != null && vtLogNode.size() > 0)
							vtLogDir.addElement(vtLogNode);
						strOldMonth = strNewMonth;
						vtLogNode = new Vector();
						vtLogNode.addElement(strOldMonth);
					}
					vtLogNode.addElement(strFileName);
				}
				catch(Exception e)
				{
					vtOther.addElement(strFileName);
				}
			}
			if(vtLogNode != null && vtLogNode.size() > 0)
				vtLogDir.addElement(vtLogNode);
			if(vtOther.size() > 1)
				vtLogDir.addElement(vtOther);

			response.setVector("vtDirLog",vtLogDir);
		}
		else
			response.setVector("vtDirLog",new Vector());
	}
	///////////////////////////////////////////////////////////
	public void loadThreadLogContent() throws Exception
	{
		// Get thread monitor to load setting
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		String strFileName = StringUtil.nvl(request.getString("ThreadLogName"),"");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.loadThreadLogContent",strThreadID);

		// Load parameter from database
		thread.loadConfig();

		// Get log dir
		String strLogDir = (String)thread.getParameter("LogDir");
		StringBuffer buf = new StringBuffer();
		FileInputStream is = null;
		try
		{
			is = new FileInputStream(strLogDir + "/" + strFileName);
			byte[] bt = new byte[8192];
			int iByteRead = 0;
			while((iByteRead = is.read(bt)) >= 0)
				buf.insert(buf.length(),new String(bt,0,iByteRead,Global.ENCODE));
		}
		finally
		{
			FileUtil.safeClose(is);
		}

		if(buf.length() > 0)
			response.setString("LogContent",buf.toString());
		else
			response.setString("LogContent","Empty log");
	}
	///////////////////////////////////////////////////////////
	public void deleteThreadLog() throws Exception
	{
		// Get thread monitor to load setting
		String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
		String strFileName = StringUtil.nvl(request.getString("ThreadLogName"),"");
		ManageableThread thread = mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new AppException("FSS-00021","ThreadProcessor.deleteThreadLog",strThreadID);

		// Check priviledge
		checkPrivilege(channel.getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"U");

		// Load parameter from database
		thread.loadConfig();

		// Delete log file
		String strLogDir = (String)thread.getParameter("LogDir");
		FileUtil.deleteFile(strLogDir + "/" +strFileName);
		response.setString("DeleteResult","Delete file " + strFileName + " successfully");
	}
	////////////////////////////////////////////////////////
	public static void logModuleAccess(Connection cn,String strModuleName,String strUserID,String strIPAddress) throws Exception
	{
		// Execute insert
		String strSQL = "INSERT INTO SEC_ACCESS_LOG(USER_ID, MODULE_ID, ACCESS_DATE, IP_ADDRESS) " +
						"VALUES(?,(SELECT MODULE_ID FROM SEC_MODULE WHERE MODULE_NAME = ?),SYSDATE, ?)";
		PreparedStatement stmt = cn.prepareStatement(strSQL);
		stmt.setString(1,strUserID);
		stmt.setString(2,strModuleName);
		stmt.setString(3,strIPAddress);
		stmt.executeUpdate();
	}
}

package com.fss.thread;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import com.fss.sql.*;
import com.fss.ddtp.*;
import com.fss.util.*;
import com.fss.dictionary.*;
import com.fss.dictionary.Dictionary;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: BU5 - FPT </p>
 * @author:
 * - Thai Hoang Hiep
 * - Dang Dinh Trung
 * @version 1.0
 */

public class ThreadManager extends Object implements Runnable
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	public static final int LOAD_FROM_FILE = 1;
	public static final int LOAD_FROM_DB = 2;
	public static final int LOAD_FROM_FILE_AND_DB = 3;
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	private int iMaxLogFileSize = 1048576;
	private int iMaxLogContentSize = 16384;
	private String mstrActionLogFile = "action.log";
	private int miLoadingMethod = LOAD_FROM_FILE_AND_DB;
	private int miMaxConnectionAllowed = 3;
	private boolean mbLocked = false;
	////////////////////////////////////////////////////////
	private String mstrErrorMessage;
	public ServerSocket serverSocket;
	public ThreadServer threadServer;
	public ThreadController threadController;
	protected Vector mvtChannel = new Vector();
	protected Vector mvtThread = new Vector();
	protected Thread mthrMain;
	private ProcessorListener mlsn = null;
	//////////////////////////////////////////////////////////////
	/**
	 * Create thread manager on specified port
	 * @param port int port number
	 * @param lsn ProcessorListener
	 * @throws Exception
	 */
	//////////////////////////////////////////////////////////////
	public ThreadManager(int port,ProcessorListener lsn) throws Exception
	{
		setProcessorListener(lsn);
		serverSocket = new ServerSocket(port);
		threadServer = new ThreadServer(serverSocket,this);
		threadServer.start();
		threadController = new ThreadController(this);
		threadController.start();
	}
	////////////////////////////////////////////////////////
	/**
	 * Manage thread
	 */
	////////////////////////////////////////////////////////
	public void run()
	{
		Connection cn = null;
		while(isOpen())
		{
			if(!isServerLocked())
			{
				////////////////////////////////////////////////////////
				// Load thread from datababse
				////////////////////////////////////////////////////////
				if(getLoadingMethod() == LOAD_FROM_FILE_AND_DB ||
				   getLoadingMethod() == LOAD_FROM_DB)
				{
					try
					{
						if(cn == null)
							cn = getConnection();
						unloadDisabledThread(cn);
						loadThread(cn);
					}
					catch(Exception e)
					{
						Database.closeObject(cn);
						cn = null;

						String str = e.getMessage();
						if(str == null || !str.equals(mstrErrorMessage))
						{
							mstrErrorMessage = e.getMessage();
							e.printStackTrace();
						}
					}
				}
				////////////////////////////////////////////////////////
				// Load thread from file
				////////////////////////////////////////////////////////
				if(getLoadingMethod() == LOAD_FROM_FILE_AND_DB ||
				   getLoadingMethod() == LOAD_FROM_FILE)
				{
					try
					{
						unloadDisabledThread();
						loadThread();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				////////////////////////////////////////////////////////
				// Load thread custom thead
				////////////////////////////////////////////////////////
				try
				{
					manageCustomThread();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			////////////////////////////////////////////////////////
			// Sleep
			////////////////////////////////////////////////////////
			try
			{
				mthrMain.sleep(10000); // Fix 5 second
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		////////////////////////////////////////////////////////
		// Release all
		////////////////////////////////////////////////////////
		close();
	}
	////////////////////////////////////////////////////////
	/**
	 * Start manage thread
	 */
	////////////////////////////////////////////////////////
	public void start()
	{
		if(mthrMain != null)
			mthrMain.stop();
		mthrMain = new Thread(this);
		mthrMain.start();
	}
	////////////////////////////////////////////////////////
	/**
	 * Start manageable thread
	 * @param thread manageable thread to start
	 */
	////////////////////////////////////////////////////////
	public void startThread(ManageableThread thread)
	{
		if(thread.getThreadStatus() == ThreadConstant.THREAD_START)
			threadController.addCommand(thread,ThreadConstant.THREAD_STOP);
		threadController.addCommand(thread,ThreadConstant.THREAD_START);
	}
	////////////////////////////////////////////////////////
	/**
	 * Stop manageable thread
	 * @param thread manageable thread to stop
	 */
	////////////////////////////////////////////////////////
	public void stopThread(ManageableThread thread)
	{
		threadController.addCommand(thread,ThreadConstant.THREAD_STOP);
	}
	////////////////////////////////////////////////////////
	/**
	 * Manage custom thread
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void manageCustomThread() throws Exception
	{
	}
	////////////////////////////////////////////////////////
	/**
	 * Unload disable thread from database
	 * @param cn connection to database
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void unloadDisabledThread(Connection cn) throws Exception
	{
		String strSQL = getSelectDisbledThreadSQLCommand();
		Statement stmt = cn.createStatement();
		ResultSet rs = stmt.executeQuery(strSQL);
		while(rs.next())
		{
			// Unload thread
			String strThreadID = rs.getString(1);
			unloadThread(strThreadID,ManageableThread.DB_MANAGEMENT);
		}

		// Release
		rs.close();
		stmt.close();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String getSelectDisbledThreadSQLCommand()
	{
		return "SELECT THREAD_ID FROM THREAD WHERE STARTUP_TYPE = 0";
	}
	////////////////////////////////////////////////////////
	/**
	 * Unload disable thread from file
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void unloadDisabledThread() throws Exception
	{
		// Load thread config
		Dictionary dicThreadList = ManageableThread.loadThreadConfig();
		if(dicThreadList == null || dicThreadList.mndRoot.mvtChild == null)
			return;

		Vector vtThreadList = dicThreadList.mndRoot.mvtChild;
		for(int iIndex = 0;iIndex < vtThreadList.size();iIndex++)
		{
			// Get thread info
			DictionaryNode nd = (DictionaryNode)vtThreadList.elementAt(iIndex);
			String strThreadID = nd.mstrName;
			String strStartupType = (new Dictionary(nd)).getString("StartupType");

			if(strStartupType.equals("0"))
				unloadThread(strThreadID,ManageableThread.FILE_MANAGEMENT);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String getSelectLoadableThreadSQLCommand()
	{
		return "SELECT THREAD_ID,THREAD_NAME,CLASS_NAME,STARTUP_TYPE,COMMAND" +
			" FROM THREAD WHERE STARTUP_TYPE > 0 ORDER BY THREAD_ID";
	}
	////////////////////////////////////////////////////////
	/**
	 * Load thread from database
	 * @param cn connection to database
	 * @throws java.lang.Exception
	 */
	////////////////////////////////////////////////////////
	public void loadThread(Connection cn) throws Exception
	{
		loadThread(cn,getSelectLoadableThreadSQLCommand());
	}
	////////////////////////////////////////////////////////
	public void loadThread(Connection cn,String strSQLCommand) throws Exception
	{
		String strSQL = strSQLCommand;
		Statement stmtThread = cn.createStatement();
		ResultSet rsThread = stmtThread.executeQuery(strSQL);
		while(rsThread.next())
		{
			// get thread ID
			String strThreadID = StringUtil.nvl(rsThread.getString(1),"");
			String strThreadName = StringUtil.nvl(rsThread.getString(2),"");
			String strClassName = StringUtil.nvl(rsThread.getString(3),"");
			String strStartupType = StringUtil.nvl(rsThread.getString(4),"");

			// Find the monitor if it is already loaded
			ManageableThread thread = getThread(strThreadID);
			if(thread != null && thread.miManageMethod == ManageableThread.DB_MANAGEMENT) // loaded
			{
				if(!strStartupType.equals(thread.getStartupType()))
					thread.setStartupType(strStartupType);
				if(!strClassName.equals(thread.getClassName()))
				{
					stopThread(thread);
					DDTP request = new DDTP();
					request.setString("ThreadID",strThreadID);
					if(isConnected())
						sendRequestToAll(request,"unloadThread","MonitorProcessor");
					mvtThread.removeElement(thread);
					thread = null;
				}
				else if(!strThreadName.equals(thread.getThreadName()))
				{
					DDTP request = new DDTP();
					thread.setThreadName(strThreadName);
					request.setString("ThreadID",strThreadID);
					request.setString("ThreadName",strThreadName);
					if(isConnected())
						sendRequestToAll(request,"renameThread","MonitorProcessor");
				}
			}

			// Haven't loaded
			if(thread == null)
				thread = loadThread(strThreadID,strThreadName,strClassName,
									strStartupType,ManageableThread.DB_MANAGEMENT);

			if(thread != null)
			{
				// Command
				String strCommand = rsThread.getString(5);
				if(strCommand == null)
					strCommand = "0";
				if(!strCommand.equals("0"))
				{
					// Process command
					int iCommand = Integer.parseInt(strCommand);
					if(iCommand == ThreadConstant.THREAD_START)
						startThread(thread);
					else if(iCommand == ThreadConstant.THREAD_STOP)
						stopThread(thread);
				}
			}
		}
		// Release
		rsThread.close();
		stmtThread.close();
	}
	////////////////////////////////////////////////////////
	/**
	 * Load thread from file
	 * @throws java.lang.Exception
	 */
	////////////////////////////////////////////////////////
	public void loadThread() throws Exception
	{
		// Load thread config
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
			if(strStartupType == null || strStartupType.length() == 0)
				strStartupType = "2";
			if(strThreadName == null || strThreadName.length() == 0 ||
			   strClassName == null || strClassName.length() == 0)
				continue;

			if(!strStartupType.equals("0"))
			{
				// Find the monitor if it is already loaded
				ManageableThread thread = getThread(strThreadID);
				if(thread != null && thread.miManageMethod == ManageableThread.FILE_MANAGEMENT) // Loaded
				{
					if(!strStartupType.equals(thread.getStartupType()))
						thread.setStartupType(strStartupType);
					if(!strClassName.equals(thread.getClassName()))
					{
						stopThread(thread);
						DDTP request = new DDTP();
						request.setString("ThreadID",strThreadID);
						if(isConnected())
							sendRequestToAll(request,"unloadThread","MonitorProcessor");
						mvtThread.removeElement(thread);
						thread = null;
					}
					else if(!strThreadName.equals(thread.getThreadName()))
					{
						DDTP request = new DDTP();
						thread.setThreadName(strThreadName);
						request.setString("ThreadID",strThreadID);
						request.setString("ThreadName",strThreadName);
						if(isConnected())
							sendRequestToAll(request,"renameThread","MonitorProcessor");
					}
				}

				// Haven't loaded
				if(thread == null)
					thread = loadThread(strThreadID,strThreadName,strClassName,strStartupType,ManageableThread.FILE_MANAGEMENT);
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Unload manageable thread
	 * @param strThreadID String
	 * @param iMannageMethod int
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void unloadThread(String strThreadID,int iMannageMethod) throws Exception
	{
		// Find the monitor if it is already loaded
		ManageableThread thread = getThread(strThreadID);
		if(thread != null && thread.miManageMethod == iMannageMethod)
		{
			// Found -> unload it
			stopThread(thread);
			DDTP request = new DDTP();
			request.setString("ThreadID",strThreadID);
			if(isConnected())
				sendRequestToAll(request,"unloadThread","MonitorProcessor");
			mvtThread.removeElement(thread);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Load manageable thread
	 * @param strThreadID thread id
	 * @param strThreadName thread name
	 * @param strClassName thread class
	 * @param strStartupType startup type
	 * @param iManageMethod manage method
	 * @return thread loaded
	 */
	////////////////////////////////////////////////////////
	public ManageableThread loadThread(String strThreadID,String strThreadName,String strClassName,String strStartupType,int iManageMethod)
	{
		if(getThread(strThreadID) != null)
			return null;

		ManageableThread thread = null;
		try
		{
			thread = (ManageableThread)Class.forName(strClassName).newInstance();
			thread.mmgrMain = this;
			thread.setThreadID(strThreadID);
			thread.setThreadName(strThreadName);
			thread.setClassName(strClassName);
			thread.setStartupType(strStartupType);
			thread.miManageMethod = iManageMethod;
			mvtThread.addElement(thread);

			try
			{
				thread.loadConfig();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			if(strStartupType.equals("1"))
				startThread(thread);

			if(isConnected())
			{
				int strThreadStatus = thread.getThreadStatus();
				DDTP request = new DDTP();
				request.setString("ThreadID",strThreadID);
				request.setString("ThreadName",strThreadName);
				request.setString("ThreadStatus",String.valueOf(strThreadStatus));
				sendRequestToAll(request,"loadThread","MonitorProcessor");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return thread;
	}
	////////////////////////////////////////////////////////
	/**
	 * Destroy manage thread
	 */
	////////////////////////////////////////////////////////
	public void close()
	{
		// Close all connection
		closeAll();

		// Stop all thread
		int iMonitorCount = mvtThread.size();
		for(int iMonitorIndex = 0;iMonitorIndex < iMonitorCount;iMonitorIndex++)
		{
			ManageableThread thread = (ManageableThread)mvtThread.elementAt(iMonitorIndex);
			if(thread.getThreadStatus() == ThreadConstant.THREAD_STARTED)
				stopThread(thread);
		}

		// Stop thread controller
		while(!threadController.complete())
		{
			try
			{
				Thread.sleep(1000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		threadController.close();

		// Stop server
		try
		{
			mvtThread.removeAllElements();
			if(serverSocket != null)
				serverSocket.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		logAction("Server Stopped");
	}
	////////////////////////////////////////////////////////
	// Purpose: Return thread monitor base on its ThreadID
	// Inputs: ThreadID
	// Outputs: ThreadMonitor if found otherwise null
	// Author: HiepTH
	// Date: 08/10/2003
	////////////////////////////////////////////////////////
	public ManageableThread getThread(String strThreadID)
	{
		for(int iIndex = 0;iIndex <mvtThread.size();iIndex++)
		{
			ManageableThread thread = (ManageableThread)mvtThread.elementAt(iIndex);
			if(thread.getThreadID().equals(strThreadID))
				return thread;
		}
		return null;
	}
	////////////////////////////////////////////////////////
	// Purpose: Return info of all threads managed by thi manager
	// Outputs: ThreadMonitorList
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public Vector getThreadList()
	{
		return mvtThread;
	}
	////////////////////////////////////////////////////////
	// Purpose: Return all threads managed by thi manager
	// Outputs: ThreadMonitorList
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public Vector getThreadInfoList()
	{
		int rowSize = mvtThread.size();
		Vector vtReturn = new Vector();
		for(int i = 0;i < rowSize;i++)
		{
			ManageableThread thread = (ManageableThread)mvtThread.elementAt(i);
			Vector vtRow = new Vector();
			vtRow.add(thread.getThreadID());
			vtRow.add(thread.getThreadName());
			vtRow.add(String.valueOf(thread.getThreadStatus()));
			vtRow.add(thread.getLogContent());
			vtReturn.add(vtRow);
		}
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	// Purpose: Send request to all connected user
	// Inputs: Request, function name, class name
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public void sendRequestToAll(DDTP request,String strFunctionName,String strClassName) throws Exception
	{
		for(int iIndex = 0;iIndex < getChannelCount();iIndex++)
		{
			SocketTransmitter channel = getChannel(iIndex);
			if(channel.isOpen())
				channel.sendRequest(strClassName,strFunctionName,request);
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Send request to all connected user
	// Inputs: Request, function name, class name
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public void notifyUserConnected(SocketTransmitter channel)
	{
		try
		{
			if(channel != null && channel.msckMain != null)
			{
				DDTP request = new DDTP();
				request.setString("strChannel",channel.toString());
				request.setString("strUserName",channel.getUserName());
				request.setString("strStartDate",Global.FORMAT_DATE_TIME.format(channel.dtStart));
				request.setString("strHost",channel.msckMain.getInetAddress().getHostAddress());
				sendRequestToAll(request,"userConnected","MonitorProcessor");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	// Purpose: Send request to all connected user
	// Inputs: Request, function name, class name
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public void notifyUserDisconnected(SocketTransmitter channel)
	{
		try
		{
			DDTP request = new DDTP();
			request.setString("strChannel",channel.toString());
			sendRequestToAll(request,"userDisconnected","MonitorProcessor");
		}
		catch(Exception e)
		{
		}
	}
	///////////////////////////////////////////////////////////
	// Purpose: Send action log to all connected user
	// Inputs: Request, function name, class name
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public synchronized void logAction(String strLog)
	{
		final java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM HH:mm:ss");
		if(!strLog.endsWith("\n"))
			strLog += "\n";
		strLog = fmt.format(new java.util.Date()) +
				 " " + strLog;
		DDTP request = new DDTP();
		request.setString("strLog",strLog);
		try
		{
			sendRequestToAll(request,"logAction","MonitorProcessor");
		}
		catch(Exception e)
		{
		}

		if(getActionLogFile() != null && getActionLogFile().length() > 0)
		{
			try
			{
				FileUtil.backup(getActionLogFile(),getMaxLogFileSize());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			RandomAccessFile fl = null;
			try
			{
				fl = new RandomAccessFile(getActionLogFile(),"rw");
				fl.seek(fl.length());
				fl.write(strLog.getBytes(Global.ENCODE));
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				FileUtil.safeClose(fl);
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Get log content from thread log file
	 * @return Log content
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String getLogContent()
	{
		byte btBuffer[] = new byte[getMaxLogContentSize()];
		String strLog = null;
		RandomAccessFile fl = null;
		try
		{
			File flLogFile = new File(getActionLogFile());
			if(flLogFile.exists())
			{
				fl = new RandomAccessFile(flLogFile,"r");
				int iByteSkip = (int)fl.length() - getMaxLogContentSize();
				if(iByteSkip > 0)
					fl.seek(iByteSkip);
				int iByteRead = fl.read(btBuffer);
				strLog = new String(btBuffer,0,iByteRead,Global.ENCODE);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			FileUtil.safeClose(fl);
		}
		return strLog;
	}
	////////////////////////////////////////////////////////
	// Purpose: Get all open channel
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public int getChannelCount()
	{
		return mvtChannel.size();
	}
	////////////////////////////////////////////////////////
	// Purpose: Get all open channel
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public SocketTransmitter getChannel(int iChannelIndex)
	{
		return (SocketTransmitter)mvtChannel.elementAt(iChannelIndex);
	}
	////////////////////////////////////////////////////////
	// Purpose: Add new channel
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public synchronized void addChannel(SocketTransmitter channel)
	{
		mvtChannel.addElement(channel);
	}
	////////////////////////////////////////////////////////
	// Purpose: remove a channel
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public synchronized void removeChannel(SocketTransmitter channel)
	{
		mvtChannel.remove(channel);
	}
	////////////////////////////////////////////////////////
	// Purpose: Close all open channel
	// Author: HiepTH
	// Date: 29/11/2003
	////////////////////////////////////////////////////////
	public synchronized void closeAll()
	{
		while(getChannelCount() > 0)
			getChannel(0).close();
	}
	///////////////////////////////////////////////////////////
	// Is connected
	///////////////////////////////////////////////////////////
	public boolean isConnected()
	{
		while(getChannelCount() > 0)
		{
			if(getChannel(0).isOpen())
				return true;
			getChannel(0).close();
		}
		return false;
	}
	///////////////////////////////////////////////////////////
	// Is full connected
	///////////////////////////////////////////////////////////
	public boolean isFullConnected()
	{
		if(getChannelCount() >= getMaxConnectionAllowed())
			return true;
		return false;
	}
	///////////////////////////////////////////////////////////
	/**
	 * Lock all thread
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	///////////////////////////////////////////////////////////
	public void lockServer() throws Exception
	{
		if(isServerLocked())
			throw new Exception("Server is currently locked");
		logAction("Server is locked");
		mbLocked = true;
	}
	///////////////////////////////////////////////////////////
	/**
	 * Unlock all thread
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	///////////////////////////////////////////////////////////
	public void unLockServer() throws Exception
	{
		logAction("Server is unlocked");
		mbLocked = false;
	}
	///////////////////////////////////////////////////////////
	/**
	 * Return lock status of server
	 * @return true if server locked otherwise false
	 * @author Thai Hoang Hiep
	 */
	///////////////////////////////////////////////////////////
	public boolean isServerLocked()
	{
		return mbLocked;
	}
	///////////////////////////////////////////////////////////
	/**
	 *
	 * @param maxConnection int
	 */
	///////////////////////////////////////////////////////////
	public void setMaxConnectionAllowed(int maxConnection)
	{
		miMaxConnectionAllowed = maxConnection;
		if(miMaxConnectionAllowed < 1)
			miMaxConnectionAllowed = 1;
	}
	///////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	///////////////////////////////////////////////////////////
	public int getMaxConnectionAllowed()
	{
		return miMaxConnectionAllowed;
	}
	///////////////////////////////////////////////////////////
	/**
	 *
	 * @param fileName String
	 */
	///////////////////////////////////////////////////////////
	public void setActionLogFile(String fileName)
	{
		mstrActionLogFile = fileName;
	}
	///////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	///////////////////////////////////////////////////////////
	public String getActionLogFile()
	{
		return mstrActionLogFile;
	}
	///////////////////////////////////////////////////////////
	/**
	 *
	 * @param maxSize int
	 */
	///////////////////////////////////////////////////////////
	public void setMaxLogFileSize(int maxSize)
	{
		iMaxLogFileSize = maxSize;
	}
	///////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	///////////////////////////////////////////////////////////
	public int getMaxLogFileSize()
	{
		return iMaxLogFileSize;
	}
	///////////////////////////////////////////////////////////
	/**
	 *
	 * @param maxSize int
	 */
	///////////////////////////////////////////////////////////
	public void setMaxLogContentSize(int maxSize)
	{
		iMaxLogContentSize = maxSize;
	}
	///////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	///////////////////////////////////////////////////////////
	public int getMaxLogContentSize()
	{
		return iMaxLogContentSize;
	}
	///////////////////////////////////////////////////////////
	/**
	 *
	 * @param iLoadingMethod int
	 */
	///////////////////////////////////////////////////////////
	public void setLoadingMethod(int iLoadingMethod)
	{
		if(iLoadingMethod == LOAD_FROM_FILE_AND_DB ||
		   iLoadingMethod == LOAD_FROM_DB ||
		   iLoadingMethod == LOAD_FROM_FILE)
			miLoadingMethod = iLoadingMethod;
	}
	///////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	///////////////////////////////////////////////////////////
	public int getLoadingMethod()
	{
		return miLoadingMethod;
	}
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
	////////////////////////////////////////////////////////
	/**
	 * Get processor listener
	 * @return ProcessorListener
	 */
	////////////////////////////////////////////////////////
	public ProcessorListener getProcessorListener()
	{
		return mlsn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get connection
	 * @return Connection
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public Connection getConnection() throws Exception
	{
		return getProcessorListener().getConnection();
	}
	//////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	//////////////////////////////////////////////////////////
	public boolean isOpen()
	{
		return serverSocket == null || !serverSocket.isClosed();
	}
}

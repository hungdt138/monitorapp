package com.fss.thread;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.text.*;

import com.fss.sql.*;
import com.fss.ddtp.*;
import com.fss.util.*;
import com.fss.dictionary.*;
import com.fss.dictionary.Dictionary;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public abstract class ManageableThread extends ParameterUtil implements Runnable
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	private static final int MAX_LOG_SIZE = 16384;
	private static final int MAX_LOG_DATE = 60;
	public static final int FILE_MANAGEMENT = 0;
	public static final int DB_MANAGEMENT = 1;
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	public int miThreadStatus = ThreadConstant.THREAD_STOPPED;
	public int miThreadCommand = ThreadConstant.THREAD_NONE;
	public int miManageMethod = DB_MANAGEMENT;
	protected Thread mthrMain;
	protected String mstrThreadID = "";
	protected String mstrLogID = "";
	protected String mstrThreadName = "";
	protected String mstrClassName = "";
	protected String mstrStartupType = "";
	protected Vector mvtSchedule;
	////////////////////////////////////////////////////////
	private boolean mbRunning = false;
	protected Properties mprtParam = new Properties();
	protected Connection mcnMain = null;
	protected Dictionary mdicSchedule;
	protected ThreadManager mmgrMain;
	////////////////////////////////////////////////////////
	protected String mstrLogFileName;
	protected boolean mbAlertByMail = false;
	protected boolean mbAutoConnectDB = false;
	protected int miExecutionCount = 0;
	protected int miDelayTime = 300; // Delay time = 300 for first use
	private static Dictionary mdicThreadList;
	private static long mlThreadFileLastModified;
	private static long mlThreadFileSize;
	////////////////////////////////////////////////////////
	// Hashtable
	////////////////////////////////////////////////////////
	public void setConnection(Connection cn)
	{
		mcnMain = cn;
	}
	////////////////////////////////////////////////////////
	public Connection getConnection()
	{
		return mcnMain;
	}

	////////////////////////////////////////////////////////
	public void setThreadID(String strThreadID)
	{
		mstrThreadID = strThreadID;
	}
	////////////////////////////////////////////////////////
	public String getThreadID()
	{
		return mstrThreadID;
	}
	////////////////////////////////////////////////////////
	public void setThreadName(String strThreadName)
	{
		mstrThreadName = strThreadName;
	}
	////////////////////////////////////////////////////////
	public String getThreadName()
	{
		return mstrThreadName;
	}
	////////////////////////////////////////////////////////
	public void setClassName(String strClassName)
	{
		mstrClassName = strClassName;
	}
	////////////////////////////////////////////////////////
	public String getClassName()
	{
		return mstrClassName;
	}
	////////////////////////////////////////////////////////
	public int getThreadStatus()
	{
		return miThreadStatus;
	}
	////////////////////////////////////////////////////////
	public void removeAllParameter()
	{
		mprtParam.clear();
	}
	////////////////////////////////////////////////////////
	public Properties getParameter()
	{
		return mprtParam;
	}
	////////////////////////////////////////////////////////
	public Object getParameter(String strParameterName)
	{
		return mprtParam.get(strParameterName);
	}
	////////////////////////////////////////////////////////
	public void setParameter(String strParameterName,Object objParameterValue)
	{
		mprtParam.put(strParameterName,objParameterValue);
	}
	////////////////////////////////////////////////////////
	public void setParameter(Properties prtParam)
	{
		mprtParam = prtParam;
	}
	////////////////////////////////////////////////////////
	public String getStartupType()
	{
		return mstrStartupType;
	}
	////////////////////////////////////////////////////////
	public void setStartupType(String strStartupType)
	{
		mstrStartupType = strStartupType;
	}
	////////////////////////////////////////////////////////
	/**
	 * Execute system command
	 * @param strCommand system command to execute
	 * @param iTimeout Time out in ms
	 * @throws java.lang.Exception
	 * @return int
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected int exec(String strCommand,int iTimeout) throws Exception
	{
		// Execute command
		Process process = Runtime.getRuntime().exec(strCommand);

		// Run cleaner
		new ProcessCleaner(process);

		StringBuffer strLog = new StringBuffer();
		StringBuffer strErrLog = new StringBuffer();
		long lStartDate = System.currentTimeMillis();
		char cLog;
		char cErrLog;

		try
		{
			while(true)
			{
				int iAvailable = process.getInputStream().available();
				int iErrAvailable = process.getErrorStream().available();
				while(iAvailable > 0 || iErrAvailable > 0)
				{
					if(iAvailable > 0)
					{
						// Fill log
						cLog = (char)process.getInputStream().read();
						if(cLog == '\n' || cLog == 13)
						{
							if(strLog.length() > 0)
								logMonitor("\t" + strLog.toString());
							lStartDate = new java.util.Date().getTime();
							strLog = new StringBuffer();
						}
						else
							strLog.append(cLog);
						iAvailable--;
					}
					else
					{
						// Fill log
						cErrLog = (char)process.getErrorStream().read();
						if(cErrLog == '\n' || cErrLog == 13)
						{
							if(strErrLog.length() > 0)
								logMonitor("\t" + strErrLog.toString());
							lStartDate = new java.util.Date().getTime();
							strErrLog = new StringBuffer();
						}
						else
							strErrLog.append(cErrLog);
						iAvailable--;
					}
				}
				if(System.currentTimeMillis() - lStartDate > iTimeout)
				{
					process.destroy();
					throw new AppException("Time out reached, command execution was destroyed.");
				}
			}
		}
		catch(AppException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logMonitor("Process terminated with code " + process.exitValue());
		}
		return process.exitValue();
	}
	////////////////////////////////////////////////////////
	/**
	 * Open connection to database
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void openConnection() throws Exception
	{
		// Make sure connection is closed
		closeConnection();

		// Connect to database
		mcnMain = mmgrMain.getConnection();
	}
	////////////////////////////////////////////////////////
	/**
	 * Close database connection
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void closeConnection()
	{
		Database.closeObject(mcnMain);
		mcnMain = null;
	}
	////////////////////////////////////////////////////////
	/**
	 * Log process to database
	 * @param strFileName File being processed
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void logStart(String strFileName) throws Exception
	{
		if(miManageMethod == DB_MANAGEMENT && mcnMain != null && !mcnMain.isClosed())
		{
			// Get log id
			mstrLogID = Database.getSequenceValue(mcnMain,"THREAD_LOG_SEQ");

			// Insert log info
			String strSQL = "INSERT INTO THREAD_LOG(LOG_ID,THREAD_ID,FILE_NAME,START_DATE) VALUES(?,?,?,SYSDATE)";
			PreparedStatement stmt = null;
			try
			{
				stmt = mcnMain.prepareStatement(strSQL);
				stmt.setString(1,mstrLogID);
				stmt.setString(2,mstrThreadID);
				stmt.setString(3,strFileName);
				stmt.executeUpdate();
			}
			finally
			{
				Database.closeObject(stmt);
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Log process error
	 * @param strDescription error desccription
	 * @param strContext error context
	 * @param strNote error note
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void logDetail(String strDescription,String strContext,String strNote) throws Exception
	{
		if(miManageMethod == DB_MANAGEMENT && mcnMain != null && !mcnMain.isClosed())
		{
			// Insert log info
			String strSQL = "INSERT INTO THREAD_LOG_DETAIL(LOG_ID,CONTEXT,DESCRIPTION,NOTE,LOG_DATE)";
			strSQL += " VALUES(?,?,?,?,SYSDATE)";

			// Execute
			PreparedStatement stmt = null;
			try
			{
				stmt = mcnMain.prepareStatement(strSQL);
				stmt.setString(1,mstrLogID);
				stmt.setString(2,strContext);
				stmt.setString(3,strDescription);
				stmt.setString(4,strNote);
				stmt.executeUpdate();
			}
			finally
			{
				Database.closeObject(stmt);
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Update log info after process
	 * @param strTotalRecords total records processed
	 * @param strSuccessRecords successful records
	 * @param strErrorRecords error records
	 * @param strStatus successful or failure
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void logComplete(String strTotalRecords,String strSuccessRecords,String strErrorRecords,String strStatus) throws Exception
	{
		if(miManageMethod == DB_MANAGEMENT && mcnMain != null && !mcnMain.isClosed())
		{
			// Insert log info
			String strSQL = "UPDATE THREAD_LOG SET TOTAL_RECORDS=?,SUCCESS_RECORDS=?,";
			strSQL += "ERROR_RECORDS=?,STATUS=?,END_DATE=SYSDATE WHERE LOG_ID=?";

			// Execute
			PreparedStatement stmt = null;
			try
			{
				stmt = mcnMain.prepareStatement(strSQL);
				stmt.setString(1,strTotalRecords);
				stmt.setString(2,strSuccessRecords);
				stmt.setString(3,strErrorRecords);
				stmt.setString(4,strStatus);
				stmt.setString(5,mstrLogID);
				stmt.executeUpdate();
			}
			finally
			{
				Database.closeObject(stmt);
			}
		}
	}
	///////////////////////////////////////////////////////////
	/**
	 * Add new schedule to schedule list
	 * @param strSchedule String
	 * @throws Exception
	 * @return String
	 * @author Thai Hoang Hiep
	 */
	///////////////////////////////////////////////////////////
	public String addSchedule(String strSchedule) throws Exception
	{
		return addSchedule(strSchedule,null,null,null);
	}
	///////////////////////////////////////////////////////////
	protected String addSchedule(String strSchedule,SocketTransmitter channel,LogInterface log,Connection cn) throws Exception
	{
		// Load from storage
		loadSchedule();

		// Get dictionary
		ByteArrayInputStream is = new ByteArrayInputStream(strSchedule.getBytes());
		Dictionary dic = new Dictionary(is);

		// Get schedule id
		int iMaxID = 0;
		for(int iIndex = 0;iIndex < mvtSchedule.size();iIndex++)
		{
			int iScheduleID = Integer.parseInt(((Dictionary)mvtSchedule.elementAt(iIndex)).getString("ScheduleID"));
			if(iScheduleID > iMaxID)
				iMaxID = iScheduleID;
		}
		String strScheduleID = String.valueOf(iMaxID + 1);
		dic.mndRoot.setChildValue("ScheduleID",strScheduleID);
		mvtSchedule.addElement(dic);

		// Update storage
		storeSchedule(cn);

		// Log after insert
		if(log != null && channel != null)
		{
			String strLogID = log.logHeader(ThreadConstant.SYSTEM_THREAD_MANAGER,channel.getUserName(),"I");
			String strChangeID = log.logTableChange(strLogID,getThreadTableName(),strScheduleID,"Insert");
			log.logColumnChange(strChangeID,"SCHEDULE","",strSchedule);
		}

		// Return
		return strScheduleID;
	}
	///////////////////////////////////////////////////////////
	/**
	 * Update schedule of schedule list
	 * @param strScheduleID String
	 * @param strSchedule String
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	///////////////////////////////////////////////////////////
	public void updateSchedule(String strScheduleID,String strSchedule) throws Exception
	{
		updateSchedule(strScheduleID,strSchedule,null,null,null);
	}
	///////////////////////////////////////////////////////////
	protected void updateSchedule(String strScheduleID,String strSchedule,SocketTransmitter channel,LogInterface log,Connection cn) throws Exception
	{
		// Load from storage
		Dictionary dicOld = loadSchedule();

		// Get dictionary
		ByteArrayInputStream is = new ByteArrayInputStream(strSchedule.getBytes());
		Dictionary dic = new Dictionary(is);

		// Get schedule id
		boolean bFound = false;
		int iIndex = 0;
		while(!bFound && iIndex < mvtSchedule.size())
		{
			int iScheduleID = Integer.parseInt(((Dictionary)mvtSchedule.elementAt(iIndex)).getString("ScheduleID"));
			if(iScheduleID == Integer.parseInt(strScheduleID))
				bFound = true;
			else
				iIndex++;
		}
		if(!bFound)
			throw new AppException("FSS-00012","ThreadProcessor.updateSchedule","Schedule");

		// Update schedule
		if(dic.getString("ScheduleID") == null || dic.getString("ScheduleID").length() == 0)
			dic.mndRoot.setChildValue("ScheduleID",strScheduleID);
		mvtSchedule.setElementAt(dic,iIndex);

		// Update storage
		storeSchedule(cn);

		// Log after update
		if(log != null && channel != null)
		{
			String strLogID = log.logHeader(ThreadConstant.SYSTEM_THREAD_MANAGER,channel.getUserName(),"U");
			String strChangeID = log.logTableChange(strLogID,getThreadTableName(),strScheduleID,"Update");
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dicOld.store(os);
			os.close();
			log.logColumnChange(strChangeID,"SCHEDULE",new String(os.toByteArray()),strSchedule);
		}
	}
	///////////////////////////////////////////////////////////
	/**
	 * Delete schedule
	 * @param strScheduleID String
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 */
	///////////////////////////////////////////////////////////
	public void deleteSchedule(String strScheduleID) throws Exception
	{
		deleteSchedule(strScheduleID,null,null,null);
	}
	///////////////////////////////////////////////////////////
	protected void deleteSchedule(String strScheduleID,SocketTransmitter channel,LogInterface log,Connection cn) throws Exception
	{
		// Load from storage
		Dictionary dicOld = loadSchedule();

		// Get schedule id
		boolean bFound = false;
		int iIndex = 0;
		while(!bFound && iIndex < mvtSchedule.size())
		{
			int iScheduleID = Integer.parseInt(((Dictionary)mvtSchedule.elementAt(iIndex)).getString("ScheduleID"));
			if(iScheduleID == Integer.parseInt(strScheduleID))
				bFound = true;
			else
				iIndex++;
		}
		if(!bFound)
			throw new AppException("FSS-00012","ThreadProcessor.updateSchedule","Schedule");

		// Update schedule
		mvtSchedule.removeElementAt(iIndex);

		// Update storage
		storeSchedule(cn);

		// Log after delete
		if(log != null && channel != null)
		{
			String strLogID = log.logHeader(ThreadConstant.SYSTEM_THREAD_MANAGER,channel.getUserName(),"D");
			String strChangeID = log.logTableChange(strLogID,getThreadTableName(),strScheduleID,"Delete");
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dicOld.store(os);
			os.close();
			log.logColumnChange(strChangeID,"SCHEDULE",new String(os.toByteArray()),"");
		}
	}
	///////////////////////////////////////////////////////////
	/**
	 * Query list of schedule
	 * @throws Exception
	 * @return Vector
	 * @author Thai Hoang Hiep
	 */
	///////////////////////////////////////////////////////////
	public String querySchedule() throws Exception
	{
		// Reload schedule
		loadSchedule();

		// Store schedule to script
		Dictionary dic = ScheduleUtil.scheduleToScript(mvtSchedule);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		dic.store(os);

		// Return
		return new String(os.toByteArray());
	}
	///////////////////////////////////////////////////////////
	/**
	 * Load schedule list from storage
	 * @author Thai Hoang Hiep
	 * @throws Exception
	 * @return Dictionary
	 */
	///////////////////////////////////////////////////////////
	public Dictionary loadSchedule() throws Exception
	{
		// Load script
		Dictionary dic = new Dictionary();
		if(miManageMethod == DB_MANAGEMENT)
		{
			Connection cn = null;
			try
			{
				cn = mmgrMain.getConnection();
				String strSQL = getSelectScheduleSQLCommand();
				Statement stmt = cn.createStatement();
				ResultSet rs = stmt.executeQuery(strSQL);
				if(!rs.next())
					throw new AppException("FSS-00021","ThreadProcessor.loadSchedule",mstrThreadID);
				InputStream is = rs.getAsciiStream(1);
				if(is != null)
					dic.load(is);
				rs.close();
				stmt.close();
			}
			finally
			{
				Database.closeObject(cn);
			}
		}
		else if(miManageMethod == FILE_MANAGEMENT)
		{
			// Load thread config
			Dictionary dicThreadList = loadThreadConfig();

			// Get schedule
			DictionaryNode ndThread = dicThreadList.mndRoot.setChildValue(mstrThreadID,null);
			DictionaryNode ndSchedule = ndThread.setChildValue("Schedule",null);
			dic = new Dictionary(ndSchedule);
		}

		// Load schedule from script
		mvtSchedule = ScheduleUtil.scriptToSchedule(dic);
		return dic;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	protected String getSelectScheduleSQLCommand()
	{
		return "SELECT SCHEDULE FROM " + getThreadTableName() + " WHERE " + getThreadIDFieldName() + "=" + mstrThreadID;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	protected String getUpdateScheduleSQLCommand()
	{
		return "UPDATE " + getThreadTableName() + " SET SCHEDULE=? WHERE " + getThreadIDFieldName() + "=?";
	}
	///////////////////////////////////////////////////////////
	/**
	 * Update schedule list to storage
	 * @param cn Connection
	 * @author Thai Hoang Hiep
	 * @throws Exception
	 */
	///////////////////////////////////////////////////////////
	public void storeSchedule(Connection cn) throws Exception
	{
		// Store schedule to script
		Dictionary dic = new Dictionary();
		dic.mndRoot.mvtChild = new Vector();
		for(int iIndex = 0;iIndex < mvtSchedule.size();iIndex++)
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			((Dictionary)mvtSchedule.elementAt(iIndex)).store(os);
			DictionaryNode nd = new DictionaryNode();
			nd.mstrName = "Schedule";
			nd.mstrValue = new String(os.toByteArray());
			dic.mndRoot.mvtChild.addElement(nd);
		}

		// Store script
		if(miManageMethod == DB_MANAGEMENT)
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			dic.store(os);

			boolean bConnectionNotExist = (cn == null);
			try
			{
				if(bConnectionNotExist)
					cn = mmgrMain.getConnection();
				String strSQL = getUpdateScheduleSQLCommand();
				PreparedStatement stmt = cn.prepareStatement(strSQL);
				ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
				stmt.setAsciiStream(1,is,os.size());
				stmt.setString(2,mstrThreadID);
				stmt.executeUpdate();
				stmt.close();
			}
			finally
			{
				if(bConnectionNotExist)
					Database.closeObject(cn);
			}
		}
		else if(miManageMethod == FILE_MANAGEMENT)
		{
			synchronized(ThreadConstant.THREAD_CONFIG_FILE)
			{
				// Load thread config
				Dictionary dicThreadList = loadThreadConfig();

				// Set schedule
				DictionaryNode ndThread = dicThreadList.mndRoot.setChildValue(mstrThreadID,null);
				DictionaryNode ndSchedule = ndThread.setChildValue("Schedule",null);
				ndSchedule.mvtChild = dic.mndRoot.mvtChild;

				// Store thread config
				storeThreadConfig(dicThreadList);
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Start thread
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void start()
	{
		// Destroy previous if it's constructed
		destroy();

		// Start new thread
		mthrMain = new Thread(this);
		mthrMain.start();
	}
	////////////////////////////////////////////////////////
	/**
	 * Destroy thread
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void destroy()
	{
		if(mthrMain != null)
		{
			try
			{
				mthrMain.stop();
				mthrMain = null;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		miThreadCommand = ThreadConstant.THREAD_NONE;
		miThreadStatus = ThreadConstant.THREAD_STOP;
		mmgrMain.threadController.removeThread(this);
		mbRunning = false;
		updateStatus();
	}
	////////////////////////////////////////////////////////
	/**
	 * Load thread parameter
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void loadParameter() throws Exception
	{
		// Load parameter
		loadConfig();

		// Other parameter
		fillParameter();

		// Validate parameter
		validateParameter();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @throws AppException
	 */
	////////////////////////////////////////////////////////
	public void fillLogFile() throws AppException
	{
		mstrLogFileName = loadDirectory("LogDir",true,true);
		mstrLogFileName += StringUtil.format(new java.util.Date(),"yyyyMMdd") + ".log";
	}
	////////////////////////////////////////////////////////
	/**
	 * Load thread parameter from mprtParam to member variables
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		fillLogFile();
		String strAutoConnect = StringUtil.nvl(mprtParam.get("ConnectDB"),"");
		if(strAutoConnect.equals("Manual"))
			mbAutoConnectDB = false;
		else if(strAutoConnect.equals("Automatic"))
			mbAutoConnectDB = true;
		else
		{
			if(miManageMethod == DB_MANAGEMENT)
				mbAutoConnectDB = true;
			else
				mbAutoConnectDB = false;
		}
		mbAlertByMail = StringUtil.nvl(mprtParam.get("AlertByMail"),"N").equals("Y");
		miDelayTime = loadUnsignedInteger("DelayTime");
	}
	////////////////////////////////////////////////////////
	/**
	 * Validate parameter after load
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void validateParameter() throws Exception
	{
		if(mstrLogFileName.length() > 0)
		{
			File flTest = new File(mstrLogFileName);
			if(!flTest.exists())
			{
				try
				{
					if(!flTest.createNewFile())
						throw new AppException("Could not create log file",
							"ManageableThread.validateParameter","LogDir");
				}
				catch(Exception e)
				{
					throw new AppException("Could not create log file",
							"ManageableThread.validateParameter","LogDir");
				}
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Write log to file and show to user (with date in prefix)
	 * @param strLog Log content
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void logMonitor(String strLog)
	{
		logMonitor(strLog,false);
	}
	////////////////////////////////////////////////////////
	public void logMonitor(String strLog,boolean bSendMail)
	{
		if(bSendMail)
			alertByMail(strLog);
		final java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM HH:mm:ss");
		strLog = fmt.format(new java.util.Date()) + " " + strLog + "\r\n";
		log(strLog);
	}
	////////////////////////////////////////////////////////
	/**
	 * Send alert to user by mail
	 * @param strLog Log content
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void alertByMail(String strLog)
	{
		Connection cn = null;
		try
		{
			cn = mmgrMain.getConnection();
			if(cn != null)
			{
				String strSQL = "INSERT INTO THREAD_SMTP_QUEUE (BATCH_ID,MESSAGE_DATE,SOURCE,MESSAGE)" +
								" VALUES(?,SYSDATE,?,?)";
				PreparedStatement stmt = cn.prepareStatement(strSQL);
				stmt.setString(1,getSmtpBatchID(cn));
				stmt.setString(2,mstrThreadName);
				stmt.setString(3,strLog);
				stmt.executeUpdate();
				stmt.close();
			}
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
	////////////////////////////////////////////////////////
	public String getSmtpBatchID(Connection cn) throws Exception
	{
		Statement stmt = null;
		try
		{
			// Get current if exist
			stmt = cn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT MAX(BATCH_ID) FROM THREAD_SMTP_BATCH WHERE NVL(STATUS,0)=0");
			String strBatchID = null;
			if(rs.next())
				strBatchID = rs.getString(1);
			rs.close();
			if(strBatchID != null && strBatchID.length() > 0)
				return strBatchID;

			// Insert new
			strBatchID = Database.getSequenceValue(cn,"THREAD_SMTP_SEQ");
			stmt.executeUpdate("INSERT INTO THREAD_SMTP_BATCH(" +
							   "BATCH_ID,PROCESS_DATE,STATUS)" +
							   " VALUES('" + strBatchID + "',SYSDATE,0)");
			return strBatchID;
		}
		finally
		{
			Database.closeObject(stmt);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Write log to file and show to user (without date in prefix)
	 * @param strLog Log content
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void log(String strLog)
	{
		if(!strLog.endsWith("\n"))
			strLog += "\n";
		logToUser(strLog);
		logToFile(strLog);
	}
	////////////////////////////////////////////////////////
	/**
	 * Write log to user (without date in prefix)
	 * @param strLog Log content
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void logToUser(String strLog)
	{
		if(mmgrMain != null && mmgrMain.isConnected())
		{
			DDTP request = new DDTP();
			request.setString("ThreadID",this.mstrThreadID);
			request.setString("LogResult",strLog);
			request.setString("ThreadStatus",String.valueOf(this.miThreadStatus));
			try
			{
				mmgrMain.sendRequestToAll(request,"logMonitor","MonitorProcessor");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Write log to file (without date in prefix)
	 * @param strLog Log content
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void logToFile(String strLog)
	{
		if(mstrLogFileName != null && mstrLogFileName.length() > 0)
		{
			RandomAccessFile fl = null;
			try
			{
				fl = new RandomAccessFile(mstrLogFileName,"rw");
				fl.seek(fl.length());
				fl.write(strLog.getBytes());
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
		try
		{
			String strLogDir = loadDirectory("LogDir",true,true);
			StringBuffer strLog = new StringBuffer();
			java.util.Date dt = new java.util.Date();
			int iByteRead = 0;
			int iDateCount = 0;
			byte btBuffer[] = new byte[MAX_LOG_SIZE];
			while(iByteRead < MAX_LOG_SIZE && iDateCount < MAX_LOG_DATE)
			{
				// Read from log file
				RandomAccessFile fl = null;
				try
				{
					File flLogFile = new File(strLogDir + StringUtil.format(dt,"yyyyMMdd") + ".log");
					if(flLogFile.exists())
					{
						fl = new RandomAccessFile(flLogFile,"r");
						int iByteSkip = (int)fl.length() + iByteRead - MAX_LOG_SIZE;
						if(iByteSkip > 0)
							fl.seek(iByteSkip);
						int iBufferRead = fl.read(btBuffer);
						if(iBufferRead > 0)
						{
							iByteRead += iBufferRead;
							strLog.insert(0,new String(btBuffer,0,iBufferRead));
						}
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

				// Increase date count
				iDateCount++;
				dt = DateUtil.addDay(dt,-1);
			}
			return strLog.toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Definite thread parameter
	 * @return Vector contain thread parameter
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = new Vector();

		vtReturn.addElement(createParameterDefinition("LogDir","",ParameterType.PARAM_TEXTBOX_MAX,"256",""));
		Vector vtValue = new Vector();
		vtValue.addElement("Automatic");
		vtValue.addElement("Manual");
		vtReturn.addElement(createParameterDefinition("ConnectDB","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtValue = new Vector();
		vtValue.addElement("Y");
		vtValue.addElement("N");
		vtReturn.addElement(createParameterDefinition("AlertByMail","",ParameterType.PARAM_COMBOBOX,vtValue,""));
		vtReturn.addElement(createParameterDefinition("DelayTime","",ParameterType.PARAM_TEXTBOX_MASK,"99990","Delay time between sessions (in second)"));

		return vtReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate mandatory parameter
	 * @param strParameterName parameter name
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String loadMandatory(String strParameterName) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadMandatory(strParameterName,strParameterValue);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate integer parameter
	 * @param strParameterName parameter name
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public int loadInteger(String strParameterName) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadInteger(strParameterName,strParameterValue);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate long parameter
	 * @param strParameterName parameter name
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public long loadLong(String strParameterName) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadLong(strParameterName,strParameterValue);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate double parameter
	 * @param strParameterName parameter name
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public double loadDouble(String strParameterName) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadDouble(strParameterName,strParameterValue);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate unsigned integer parameter
	 * @param strParameterName parameter name
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public int loadUnsignedInteger(String strParameterName) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadUnsignedInteger(strParameterName,strParameterValue);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate unsigned long parameter
	 * @param strParameterName parameter name
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public long loadUnsignedLong(String strParameterName) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadUnsignedLong(strParameterName,strParameterValue);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate unsigned double parameter
	 * @param strParameterName parameter name
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public double loadUnsignedDouble(String strParameterName) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadUnsignedDouble(strParameterName,strParameterValue);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate time parameter
	 * @param strParameterName parameter name
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public java.util.Date loadTime(String strParameterName) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadTime(strParameterName,strParameterValue);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate date parameter
	 * @param strParameterName parameter name
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public java.util.Date loadDate(String strParameterName) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadDate(strParameterName,strParameterValue);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate date parameter using custom format
	 * @param strParameterName parameter name
	 * @param strDateFormat db date format
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String loadCustomDate(String strParameterName,String strDateFormat) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadCustomDate(strParameterName,strDateFormat,strParameterValue);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate directory parameter
	 * @param strParameterName parameter name
	 * @param bAutoCreate autocreate if not exist
	 * @param bMandatory required
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String loadDirectory(String strParameterName,boolean bAutoCreate,boolean bMandatory) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadDirectory(strParameterName,strParameterValue,bAutoCreate,bMandatory);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate class parameter
	 * @param strParameterName parameter name
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public Object loadClass(String strParameterName) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadClass(strParameterName,strParameterValue);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate resource parameter
	 * @param strParameterName parameter name
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String loadResource(String strParameterName) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadResource(strParameterName,strParameterValue);
	}
	////////////////////////////////////////////////////////
	/**
	 * Load and validate yes/no parameter
	 * @param strParameterName parameter name
	 * @return parameter value
	 * @throws AppException
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String loadYesNo(String strParameterName) throws AppException
	{
		String strParameterValue = StringUtil.nvl(mprtParam.get(strParameterName),"");
		return loadYesNo(strParameterName,strParameterValue);
	}
	////////////////////////////////////////////////////////
	public void runImmediate() throws Exception
	{
		final Thread thr = new Thread()
		{
			public void run()
			{
				logMonitor("Start performing immediate execution");
				try
				{
					session();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					logMonitor("Error occured: " + e.getMessage(),mbAlertByMail);
				}
				logMonitor("Immediate execution completed");
			}
		};
		thr.start();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public int getScheduleStatus() throws Exception
	{
		// Load schedule
		loadSchedule();
		int iScheduleStatus = ScheduleUtil.STATUS_WAIT;
		if(mvtSchedule == null || mvtSchedule.size() <= 0)
			iScheduleStatus = ScheduleUtil.STATUS_READY;
		else
		{
			int iScheduleIndex = 0;
			while(iScheduleStatus != ScheduleUtil.STATUS_READY &&
				  iScheduleIndex < mvtSchedule.size())
			{
				mdicSchedule = (Dictionary)mvtSchedule.elementAt(iScheduleIndex);
				iScheduleStatus = ScheduleUtil.getStatus(mdicSchedule);
				if(iScheduleStatus == ScheduleUtil.STATUS_NEED_CHANGE)
				{
					ScheduleUtil.changeNextDate(mdicSchedule,true);
					storeSchedule(null);
				}
				else if(iScheduleStatus != ScheduleUtil.STATUS_READY)
					iScheduleIndex++;
			}
		}
		return iScheduleStatus;
	}
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public void run()
	{
		// Load config to get log dir
		try
		{
			loadConfig();
			mstrLogFileName = loadDirectory("LogDir",true,true);
			mstrLogFileName += StringUtil.format(new java.util.Date(),"yyyyMMdd") + ".log";
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		// Report
		miThreadStatus = ThreadConstant.THREAD_STARTED;
		logMonitor(mstrThreadName + " started");
		mmgrMain.logAction("<FONT color=\"#0033CC\"><B>" + mstrThreadName + " started</B></FONT>");
		updateStatus();

		while(miThreadCommand != ThreadConstant.THREAD_STOP)
		{
			try
			{
				if(!mmgrMain.isServerLocked()) // No flag active
				{
					// Get schedule status
					int iScheduleStatus = getScheduleStatus();

					// Ready to run
					if(iScheduleStatus == ScheduleUtil.STATUS_READY)
					{
						if(session() && mdicSchedule != null)
						{
							ScheduleUtil.increaseExecutionCount(mdicSchedule);
							storeSchedule(null);
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logMonitor("Error occured: " + e.getMessage(),mbAlertByMail);
			}
			finally
			{
				// Wait some time
				try
				{
					// Delay
					for(int iIndex = 0;iIndex < miDelayTime && miThreadCommand != ThreadConstant.THREAD_STOP;iIndex++)
						mthrMain.sleep(1000); // Time unit is second
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		// Report
		miThreadStatus = ThreadConstant.THREAD_STOPPED;
		logMonitor(mstrThreadName + " stopped");
		mmgrMain.logAction("<FONT color=\"#CC0033\"><B>" + mstrThreadName + " stopped</B></FONT>");
		updateStatus();
	}
	////////////////////////////////////////////////////////
	/**
	 * Perform one session
	 * @return true if session performed successfully otherwise false
	 * @throws java.lang.Exception
	 */
	////////////////////////////////////////////////////////
	public boolean session() throws Exception
	{
		if(mbRunning)
			throw new Exception("Thread is running");
		mbRunning = true;
		try
		{
			loadParameter();
			beforeSession();
			processSession();
			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logMonitor("Error occured: " + e.getMessage(),mbAlertByMail);
		}
		finally
		{
			try
			{
				afterSession();
			}
			finally
			{
				mbRunning = false;
			}
		}
		return false;
	}
	////////////////////////////////////////////////////////
	/**
	 * Load thread parameter from storage (file or db) to mprtParam
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void loadConfig() throws Exception
	{
		if(miManageMethod == FILE_MANAGEMENT)
		{
			synchronized(ThreadConstant.THREAD_CONFIG_FILE)
			{
				// Load thread config
				Dictionary dicThreadList = loadThreadConfig();

				// Load parameter
				DictionaryNode ndThread = dicThreadList.mndRoot.setChildValue(mstrThreadID,null);
				DictionaryNode ndParameter = ndThread.setChildValue("Parameter",null);
				mprtParam = (new Dictionary(ndParameter)).toProperties();
			}
		}
		else
		{
			Connection cn = null;
			try
			{
				// Open new connection
				cn = mmgrMain.getConnection();

				// Format sql command to get param
				String strSQL = getSelectParameterSQLCommand();
				Statement stmt = cn.createStatement();
				ResultSet rs = stmt.executeQuery(strSQL);
				while(rs.next())
				{
					String strName = rs.getString(1);
					String strValue = StringUtil.nvl(rs.getString(2),"");
					if(strValue.startsWith("$Vector$"))
					{
						byte bt[] = rs.getString(2).getBytes();
						ByteArrayInputStream is = new ByteArrayInputStream(bt);
						com.fss.dictionary.Dictionary dic = new com.fss.dictionary.Dictionary(is);
						is.close();
						Vector vt = dic.mndRoot.getChild("$Vector$").getVector();
						mprtParam.put(strName,vt);
					}
					else
						mprtParam.put(strName,StringUtil.nvl(strValue,""));
				}
				rs.close();
				stmt.close();
			}
			finally
			{
				Database.closeObject(cn);
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	protected String getThreadTableName()
	{
		return "THREAD";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	protected String getThreadParamTableName()
	{
		return "THREAD_PARAM";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	protected String getThreadIDFieldName()
	{
		return "THREAD_ID";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	protected String getThreadNameFieldName()
	{
		return "THREAD_NAME";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	protected String getClassNameFieldName()
	{
		return "CLASS_NAME";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	protected String getStartupTypeFieldName()
	{
		return "STARTUP_TYPE";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	protected String getParameterNameFieldName()
	{
		return "PARAM_NAME";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	protected String getParameterValueFieldName()
	{
		return "PARAM_VALUE";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	protected String getSelectParameterSQLCommand()
	{
		return "SELECT " + getParameterNameFieldName() + "," + getParameterValueFieldName() + " FROM " + getThreadParamTableName() + " WHERE " + getThreadIDFieldName() + "=" + mstrThreadID;
	}
	////////////////////////////////////////////////////////
	/**
	 * Store thread parameter from mprtParam to storage (file or db)
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void storeConfig() throws Exception
	{
		storeConfig(null,null,null);
	}
	////////////////////////////////////////////////////////
	protected void storeConfig(SocketTransmitter channel,LogInterface log,Connection cn) throws Exception
	{
		if(miManageMethod == FILE_MANAGEMENT)
		{
			synchronized(ThreadConstant.THREAD_CONFIG_FILE)
			{
				// Load thread config
				Dictionary dicThreadList = loadThreadConfig();

				// Get path
				DictionaryNode ndThread = dicThreadList.mndRoot.getChild(mstrThreadID,true);
				DictionaryNode ndParameter = ndThread.getChild("Parameter",true);

				// Backup old value
				Hashtable prtOld = new Dictionary(ndParameter).toHashtable();

				// Update new value
				ndThread.setChildValue("Parameter",null,(new Dictionary(mprtParam)).mndRoot.mvtChild);

				// Store thread config
				storeThreadConfig(dicThreadList);

				// Log after update
				if(log != null && channel != null)
				{
					String strLogID = log.logHeader(ThreadConstant.SYSTEM_THREAD_MANAGER,channel.getUserName(),"U");
					String strChangeID = log.logTableChange(strLogID,getThreadParamTableName(),getThreadID(),"Update");
					Enumeration enm = mprtParam.keys();
					while(enm.hasMoreElements())
					{
						Object objKey = enm.nextElement();
						log.logColumnChange(strChangeID,
											StringUtil.nvl(objKey,""),
											StringUtil.nvl(prtOld.get(objKey),""),
											StringUtil.nvl(mprtParam.get(objKey),""));
						prtOld.remove(objKey);
					}
					enm = prtOld.keys();
					while(enm.hasMoreElements())
					{
						Object objKey = enm.nextElement();
						log.logColumnChange(strChangeID,StringUtil.nvl(objKey,""),StringUtil.nvl(prtOld.get(objKey),""),"");
					}
				}
			}
		}
		else
		{
			// Get key and value list
			Object[] objKeyList = mprtParam.keySet().toArray();
			Object[] objValueList = mprtParam.values().toArray();

			boolean bConnectionNotExist = (cn == null);
			try
			{
				// Open new connection
				if(bConnectionNotExist)
				{
					cn = mmgrMain.getConnection();
					cn.setAutoCommit(false);
				}

				// Update database
				storeConfigToDB(cn,objKeyList,objValueList,channel,log);

				// Commit transaction
				if(bConnectionNotExist)
					cn.commit();
			}
			finally
			{
				if(bConnectionNotExist)
					Database.closeObject(cn);
			}
		}
	}
	////////////////////////////////////////////////////////
	protected String getUpdateParameterSQLCommand()
	{
		return "UPDATE " + getThreadParamTableName() + " SET " + getParameterValueFieldName() + "=? WHERE " + getParameterNameFieldName() + "=? AND " + getThreadIDFieldName() + "=" + mstrThreadID;
	}
	////////////////////////////////////////////////////////
	protected String getInsertParameterSQLCommand()
	{
		return "INSERT INTO " + getThreadParamTableName() + "(" + getThreadIDFieldName() + "," + getParameterNameFieldName() + "," + getParameterValueFieldName() + ") VALUES(" + mstrThreadID + ",?,?)";
	}
	////////////////////////////////////////////////////////
	/**
	 * Update thread parameter
	 * @param cn connection to database
	 * @param objKeyList parameter list
	 * @param objValueList parameter value list
	 * @return inserted parameter list
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public String storeConfigToDB(Connection cn,Object[] objKeyList,Object[] objValueList) throws Exception
	{
		return storeConfigToDB(cn,objKeyList,objValueList,null,null);
	}
	////////////////////////////////////////////////////////
	protected String storeConfigToDB(Connection cn,Object[] objKeyList,Object[] objValueList,SocketTransmitter channel,LogInterface log) throws Exception
	{
		// Log before delete
		String strLogID = null;
		if(log != null && channel != null)
		{
			strLogID = log.logHeader(ThreadConstant.SYSTEM_THREAD_MANAGER,channel.getUserName(),"U");
			log.logBeforeDelete(strLogID,getThreadParamTableName(),getThreadIDFieldName() + "=" + getThreadID());
		}

		// Format sql command to update, insert data
		String strSQL = getUpdateParameterSQLCommand();
		PreparedStatement stmtUpdate = cn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
		strSQL = getInsertParameterSQLCommand();
		PreparedStatement stmtInsert = cn.prepareStatement(strSQL,ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);

		// Process update, insert
		String strInsertParam = "";
		int iParamCount = objKeyList.length;
		for(int iParamIndex = 0;iParamIndex < iParamCount;iParamIndex++)
		{
			if(objValueList[iParamIndex] != null)
			{
				stmtUpdate.setString(2,(String)objKeyList[iParamIndex]);
				if(objValueList[iParamIndex] instanceof Vector)
				{
					com.fss.dictionary.Dictionary dic = new com.fss.dictionary.Dictionary();
					dic.mndRoot.setChildVector("$Vector$",(Vector)objValueList[iParamIndex]);
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					dic.store(os);
					os.flush();
					stmtUpdate.setString(1,new String(os.toByteArray()));
					os.close();
				}
				else
					stmtUpdate.setString(1,(String)objValueList[iParamIndex]);
				if(stmtUpdate.executeUpdate() <= 0)
				{
					strInsertParam += "'" + objKeyList[iParamIndex] + "',";
					stmtInsert.setString(1,(String)objKeyList[iParamIndex]);
					if(objValueList[iParamIndex] instanceof Vector)
					{
						com.fss.dictionary.Dictionary dic = new com.fss.dictionary.Dictionary();
						dic.mndRoot.setChildVector("$Vector$",(Vector)objValueList[iParamIndex]);
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						dic.store(os);
						os.flush();
						stmtInsert.setString(2,new String(os.toByteArray()));
						os.close();
					}
					else
						stmtInsert.setString(2,(String)objValueList[iParamIndex]);
					stmtInsert.executeUpdate();
				}
			}
		}
		stmtInsert.close();
		stmtUpdate.close();

		// Log after insert
		if(log != null && channel != null)
			log.logAfterInsert(strLogID,getThreadParamTableName(),getThreadIDFieldName() + "=" + getThreadID());

		// Return value
		if(strInsertParam.length() > 0)
			strInsertParam = strInsertParam.substring(0,strInsertParam.length() - 1);
		return strInsertParam;
	}
	////////////////////////////////////////////////////////
	/**
	 * Update thread property in database
	 * @param cn Connection
	 * @param strThreadName thread name
	 * @param strThreadClass thread class
	 * @param strThreadStartupType startup type
	 * @param strThreadID thread id
	 * @throws Exception
	 * @author TrungDD
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean updateDBThread(Connection cn,String strThreadName,String strThreadClass,String strThreadStartupType,String strThreadID) throws Exception
	{
		return updateDBThread(cn,strThreadName,strThreadClass,strThreadStartupType,strThreadID,null,null);
	}
	////////////////////////////////////////////////////////
	protected boolean updateDBThread(Connection cn,String strThreadName,String strThreadClass,String strThreadStartupType,String strThreadID,SocketTransmitter channel,LogInterface log) throws Exception
	{
		Vector vtChangeID = null;
		if(log != null && channel != null)
		{
			String strLogID = log.logHeader(ThreadConstant.SYSTEM_THREAD_MANAGER,channel.getUserName(),"U");
			vtChangeID = log.logBeforeUpdate(strLogID,getThreadTableName(),getThreadIDFieldName() + "=" + strThreadID);
		}
		String strSQL = "UPDATE " + getThreadTableName() +
						" SET " + getThreadNameFieldName() + " = ?," +
						getClassNameFieldName() + "=?," +
						getStartupTypeFieldName() + "=?" +
						" WHERE " + getThreadIDFieldName() + " = ?";
		PreparedStatement stmt = cn.prepareStatement(strSQL);
		stmt.setString(1,strThreadName);
		stmt.setString(2,strThreadClass);
		stmt.setString(3,strThreadStartupType);
		stmt.setString(4,strThreadID);
		int iCount = stmt.executeUpdate();
		stmt.close();
		if(log != null && channel != null && vtChangeID != null)
			log.logAfterUpdate(vtChangeID);
		return iCount > 0;
	}
	////////////////////////////////////////////////////////
	/**
	 * Update thread property in thread config file
	 * @param strThreadName thread name
	 * @param strClassName String
	 * @param strStartupType String
	 * @param strThreadID thread id
	 * @throws Exception
	 * @author Thai Hoang Hiep
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean updateFileThread(String strThreadName,String strClassName,String strStartupType,String strThreadID) throws Exception
	{
		return updateFileThread(strThreadName,strClassName,strStartupType,strThreadID,null,null);
	}
	////////////////////////////////////////////////////////
	protected boolean updateFileThread(String strThreadName,String strClassName,String strStartupType,String strThreadID,SocketTransmitter channel,LogInterface log) throws Exception
	{
		synchronized(ThreadConstant.THREAD_CONFIG_FILE)
		{
			// Load thread config
			Dictionary dicThreadList = loadThreadConfig();

			// Set thread property
			DictionaryNode ndThread = dicThreadList.mndRoot.getChild(strThreadID);
			if(ndThread == null)
				return false;
			DictionaryNode nd = null;
			nd = ndThread.getChild("ThreadName",true);
			String strOldThreadName = StringUtil.nvl(nd.mstrValue,"");
			nd.mstrValue = strThreadName;

			nd = ndThread.getChild("ClassName",true);
			String strOldClassName = StringUtil.nvl(nd.mstrValue,"");
			nd.mstrValue = strClassName;

			nd = ndThread.getChild("StartupType",true);
			String strOldStartupType = StringUtil.nvl(nd.mstrValue,"");
			nd.mstrValue = strStartupType;

			// Store thread config
			storeThreadConfig(dicThreadList);

			// Log change
			if(log != null && channel != null)
			{
				String strLogID = log.logHeader(ThreadConstant.SYSTEM_THREAD_MANAGER,channel.getUserName(),"U");
				String strChangeID = log.logTableChange(strLogID,getThreadTableName(),strThreadID,"Update");
				log.logColumnChange(strChangeID,getThreadNameFieldName(),strOldThreadName,strThreadName);
				log.logColumnChange(strChangeID,getClassNameFieldName(),strOldClassName,strClassName);
				log.logColumnChange(strChangeID,getStartupTypeFieldName(),strOldStartupType,strStartupType);
			}
			return true;
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Load thread config from thread.cfg
	 * @return Dictionary
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static synchronized Dictionary loadThreadConfig() throws Exception
	{
		return loadThreadConfig(ThreadConstant.THREAD_CONFIG_FILE);
	}
	////////////////////////////////////////////////////////
	public static synchronized Dictionary loadThreadConfig(String strFileName) throws Exception
	{
		File fl = new File(strFileName);
		if(!fl.exists())
			return new Dictionary();
		else if(mlThreadFileSize != fl.length() ||
				mlThreadFileLastModified != fl.lastModified())
		{
			FileInputStream is = null;
			try
			{
				is = new FileInputStream(fl);

				// Change cache
				mdicThreadList = new Dictionary(is);
				mlThreadFileSize = fl.length();
				mlThreadFileLastModified = fl.lastModified();
			}
			finally
			{
				FileUtil.safeClose(is);
			}
		}
		return mdicThreadList;
	}
	////////////////////////////////////////////////////////
	/**
	 * Load thread config from thread.cfg
	 * @param dicThreadList Dictionary
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public static synchronized void storeThreadConfig(Dictionary dicThreadList) throws Exception
	{
		storeThreadConfig(dicThreadList,ThreadConstant.THREAD_CONFIG_FILE);
	}
	////////////////////////////////////////////////////////
	public static synchronized void storeThreadConfig(Dictionary dicThreadList,String strFileName) throws Exception
	{
		FileOutputStream os = null;
		try
		{
			os = new FileOutputStream(strFileName + ".TMP");
			dicThreadList.store(os);
		}
		finally
		{
			FileUtil.safeClose(os);
		}
		if(!FileUtil.renameFile(strFileName + ".TMP",strFileName))
			throw new Exception("Could not rename file " + strFileName + ".TMP to " + strFileName);

		// Change cache
		mdicThreadList = dicThreadList;
		File fl = new File(strFileName);
		mlThreadFileSize = fl.length();
		mlThreadFileLastModified = fl.lastModified();
	}
	////////////////////////////////////////////////////////
	/**
	 * Write thread info to storage (file or db)
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void updateStorage() throws Exception
	{
		updateStorage(null,null,null);
	}
	////////////////////////////////////////////////////////
	protected void updateStorage(SocketTransmitter channel,LogInterface log,Connection cn) throws Exception
	{
		if(miManageMethod == DB_MANAGEMENT)
		{
			boolean bConnectionNotExist = (cn == null);
			try
			{
				if(bConnectionNotExist)
					cn = mmgrMain.getConnection();
				updateDBThread(cn,mstrThreadName,mstrClassName,mstrStartupType,mstrThreadID,channel,log);
			}
			finally
			{
				if(bConnectionNotExist)
					Database.closeObject(cn);
			}
		}
		else
			updateFileThread(mstrThreadName,mstrClassName,mstrStartupType,mstrThreadID,channel,log);
	}
	////////////////////////////////////////////////////////
	/**
	 * Send status to client and storage
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void updateStatus()
	{
		if(miManageMethod == FILE_MANAGEMENT)
		{
			try
			{
				synchronized(ThreadConstant.THREAD_CONFIG_FILE)
				{
					// Load thread config
					Dictionary dicThreadList = loadThreadConfig();

					// Set parameter
					DictionaryNode ndThread = dicThreadList.mndRoot.setChildValue(mstrThreadID,null);
					ndThread.setChildValue("Status",String.valueOf(miThreadStatus));

					// Store thread config
					storeThreadConfig(dicThreadList);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			Connection cn = null;
			try
			{
				cn = mmgrMain.getConnection();
				String strStatus = String.valueOf(miThreadStatus);
				String strSQL = getUpdateThreadStatusSQLCommand(strStatus);
				Statement stmt = cn.createStatement();
				stmt.executeUpdate(strSQL);
				stmt.close();
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
	}
	////////////////////////////////////////////////////////
	protected String getUpdateThreadStatusSQLCommand(String strStatus)
	{
		return "UPDATE " + getThreadTableName() + " SET STATUS=" + strStatus +
			" WHERE " + getThreadIDFieldName() + "=" + mstrThreadID;
	}
	////////////////////////////////////////////////////////
	/**
	 * Event raised when session prepare to run
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void beforeSession() throws Exception
	{
		if(mbAutoConnectDB)
			openConnection();
	}
	////////////////////////////////////////////////////////
	/**
	 * Event raised when session finish
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected void afterSession() throws Exception
	{
		closeConnection();
	}
	////////////////////////////////////////////////////////
	/**
	 * Session process
	 * @throws java.lang.Exception
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	protected abstract void processSession() throws Exception;
}

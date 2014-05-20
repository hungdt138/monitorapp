package com.fss.thread;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.reflect.*;

import com.fss.util.*;
import com.fss.dictionary.*;
import com.fss.server.SystemInputStream;
import com.fss.server.SystemPrintStream;
import com.fss.server.telnet.TelnetSession;
import com.fss.server.telnet.TelnetInputStream;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class TelnetCommander extends TelnetSession
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	private static final String[][] mstrLocalCommand =
		   {{"help","List all command supported by this server"},
		   {"chpwd","Change user password"},
		   {"lsthread","List available thread"},
		   {"startthread","Start a thread"},
		   {"stopthread","Stop a thread"},
		   {"lssession","List available session"},
		   {"killsession","Kill a session"},
		   {"dtfmt","Get / set date format"},
		   {"sysdate","Show system date"},
		   {"eval","Execute a command script"},
		   {"evalbatch","Execute batch command script"},
		   {"shutdown","Shutdown system"},
		   {"exit","Terminate current session"}};
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	private TelnetServer server;
	private Socket socket;
	private String mstrUserID;
	private String mstrUserName;
	private String mstrSessionID;
	private java.util.Date mdtLogin;
	public long mlExpire;
	public java.text.SimpleDateFormat mfmtSession = new java.text.SimpleDateFormat("dd/MM/yyyy");
	private final com.fss.dictionary.Dictionary dicError = ErrorDictionary.getDictionary("EN");
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param server TelnetServer
	 * @param in SystemInputStream
	 * @param out SystemPrintStream
	 * @param err SystemPrintStream
	 * @param socket Socket
	 */
	////////////////////////////////////////////////////////
	public TelnetCommander(TelnetServer server,SystemInputStream in,SystemPrintStream out,SystemPrintStream err,Socket socket)
	{
		super(in,out,err,socket,null);
		mfmtSession.setLenient(false);
		this.server = server;
		this.socket = socket;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strCommand String
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public static boolean isLocalCommand(String strCommand)
	{
		for(int iIndex = 0;iIndex < mstrLocalCommand.length;iIndex++)
		{
			if(mstrLocalCommand[iIndex][0].equals(strCommand))
				return true;
		}
		return false;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	protected void updatePrompt()
	{
		String strPrompt = server.getPromptFormat();
		strPrompt = StringUtil.replaceAllIgnoreCase(strPrompt,"$UserID",getUserID());
		strPrompt = StringUtil.replaceAllIgnoreCase(strPrompt,"$UserName",getUserName());
		updatePrompt(strPrompt);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strUserName String
	 * @param strPassword String
	 * @return boolean
	 * @throws IOException
	 */
	////////////////////////////////////////////////////////
	public boolean login(String strUserName,String strPassword)throws IOException
	{
		try
		{
			// Check username, password
			ThreadProcessor processor = new ThreadProcessor(server.mmgrMain);
			Vector vtLogin = processor.login(strUserName,strPassword,getInetAddress());
			mstrUserID = (String)vtLogin.elementAt(0);
			mstrUserName = strUserName;

			// Check priviledge
			processor.checkPrivilege(getUserID(),ThreadConstant.SYSTEM_THREAD_MANAGER,"U");

			// Check expired
			if(!vtLogin.elementAt(1).equals("1"))
			{
				print("Password expired, force change password\r\n");
				String str = chpwd("chpwd");
				print(str + "\r\n");
				if(!str.endsWith("Password has been changed\r\n"))
					return false;
			}

			// Return
			mstrSessionID = server.generateSessionID();
			mdtLogin = new java.util.Date();
			print("Your session id is " + getSessionID() + "\r\n");
			server.mmgrMain.logAction("<FONT color=\"#2266CC\"><U>User '" + strUserName + "' connected successfully to " + server.getThreadName() + ", sessionid=" + getSessionID() + "</U></FONT>");
			updatePrompt();
			updateExpireTime();
			return true;
		}
		catch(Exception e)
		{
			try
			{
				clearUserInfo();
				if(out != null)
					print(dicError.getString(e) + "\r\n");
				flush();
			}
			catch(Exception e1)
			{
			}
			if(e.getMessage().startsWith("FSS-00022"))
			{
				try
				{
					Thread.sleep(1000);
				}
				catch(Exception e1)
				{
				}
				endSession();
			}
			return false;
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Logs the user into the system.  This function will prompt the user
	 * for username and password.
	 * @throws IOException
	 */
	////////////////////////////////////////////////////////
	public void login() throws IOException
	{
		boolean connected = false;
		welcome();
		int loginAttempts = 0;
		while(!connected)
		{
			try
			{
				TelnetInputStream tin = (TelnetInputStream)in;
				tin.negotiateEcho();
				print("login: ");
				userName = in.readLine();
				if(userName == null)
				{
					forceEndSession();
					return;
				}

				print("password: ");
				boolean ec = in.getEcho();
				in.setEcho(false); // tell the input stream not to echo this
				password = in.readLine();
				if(password == null)
				{
					forceEndSession();
					return;
				}
				in.setEcho(ec);

				print("\r\nEncryption algorithm (SHA/RSA, empty for not encrypted): ");
				String strEncryptAlgorithm = getNextCommand();
				if(!strEncryptAlgorithm.equalsIgnoreCase("SHA") &&
				   !strEncryptAlgorithm.equalsIgnoreCase("RSA") &&
				   strEncryptAlgorithm.length() > 0)
					print("Invalid encryption algorithm, must be SHA, RSA or not encrypted\r\n");
				else
				{
					if(password.length() > 0 && strEncryptAlgorithm.length() > 0)
						password = StringUtil.encrypt(password,strEncryptAlgorithm);

					if(login(userName,password))
						connected = true;

					if(!connected)
					{
						print("Login incorrect.\r\n");
						if(++loginAttempts == 5)
						{
							forceEndSession();
							return;
						}
					}
					if(out.checkError())
					{
						forceEndSession();
						return;
					}
				}
			}
			catch(Exception e)
			{
				forceEndSession();
				throw new IOException();
			}
		}
		out.setSession(this); // tell the output stream who the session is
		in.setSession(this); // tell the input stream who the session is

		currentCommandFinished();
	}
	////////////////////////////////////////////////////////
	/**
	 * Show welcome message
	 */
	////////////////////////////////////////////////////////
	public void welcome()
	{
		String welcome = "************************************************************\r\n" +
						 "*" + StringUtil.align("Thread manager telnet interface",StringUtil.ALIGN_CENTER,58) + "*\r\n" +
						 "*" + StringUtil.align("Version " + ThreadConstant.APP_VERSION,StringUtil.ALIGN_CENTER,58) + "*\r\n" +
						 "************************************************************\r\n";
		print(welcome);
		flush();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return Vector
	 */
	////////////////////////////////////////////////////////
	public static Vector analyseCommand(String strFullCommand)
	{
		Vector vtReturn = new Vector();
		int iStart = -1;
		char cStart = 0;
		for(int iIndex = 0;iIndex < strFullCommand.length();iIndex++)
		{
			char c = strFullCommand.charAt(iIndex);
			if(cStart != 0)
			{
				 if(c == cStart)
				 {
					 vtReturn.addElement(strFullCommand.substring(iStart + 1,iIndex));
					 iStart = -1;
					 cStart = 0;
				 }
			}
			else
			{
				if(c == '\'' || c == '\"')
				{
					if(iStart >= 0)
						vtReturn.addElement(strFullCommand.substring(iStart,iIndex));
					cStart = c;
					iStart = iIndex;
				}
				else
				{
					if(c > 32)
					{
						if(iStart < 0)
							iStart = iIndex;
					}
					else
					{
						if(iStart >= 0)
						{
							vtReturn.addElement(strFullCommand.substring(iStart,iIndex));
							iStart = -1;
						}
					}
				}
			}
		}
		if(iStart >= 0)
			vtReturn.addElement(strFullCommand.substring(iStart,strFullCommand.length()));
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Process local command
	 * @param strCommand String
	 * @param strFullCommand String
	 * @throws Exception
	 */
	public void processLocalCommand(String strCommand,String strFullCommand) throws Exception
	{
		try
		{
			final Class[] clsParameterTypes = new Class[] {String.class};
			Method method = getClass().getDeclaredMethod(strCommand,clsParameterTypes);
			print((String)method.invoke(this,new Object[] {strFullCommand}) + "\r\n");
		}
		catch(Exception e)
		{
			if(e instanceof InvocationTargetException)
				e = (Exception)((InvocationTargetException)e).getTargetException();
			if(!(e instanceof AppException))
				e.printStackTrace();
			print("Error:\r\n" + dicError.getString(e) + "\r\n");
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String help(String strFullCommand) throws Exception
	{
		StringBuffer buf = new StringBuffer();
		for(int iIndex = 0;iIndex < mstrLocalCommand.length;iIndex++)
		{
			buf.append(StringUtil.align(mstrLocalCommand[iIndex][0],StringUtil.ALIGN_LEFT,20));
			buf.append(mstrLocalCommand[iIndex][1]);
			buf.append("\r\n");
		}
		return buf.toString();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String chpwd(String strFullCommand) throws Exception
	{
		Vector vtArgument = analyseCommand(strFullCommand);
		if(vtArgument.size() != 1)
			throw new AppException("Syntax error\r\nUsage:\r\n\tchpwd");
		boolean bEcho = in.getEcho();
		try
		{
			// Get parameter
			in.setEcho(false);
			print("Old password: ");
			String strOldPassword = getNextCommand();
			print("\r\nEncryption algorithm (SHA/RSA, empty for not encrypted): ");
			String strOldEncryptAlgorithm = getNextCommand();
			if(!strOldEncryptAlgorithm.equalsIgnoreCase("SHA") &&
			   !strOldEncryptAlgorithm.equalsIgnoreCase("RSA") &&
			   strOldEncryptAlgorithm.length() > 0)
				throw new AppException("Invalid encryption algorithm, must be SHA, RSA or not encrypted");
			print("\r\nNew password: ");
			String strNewPassword = getNextCommand();
			print("\r\nConfirm new password: ");
			String strConfirmPassword = getNextCommand();
			print("\r\nEncryption algorithm (SHA/RSA, empty for not encrypted): ");
			String strNewEncryptAlgorithm = getNextCommand();
			if(!strNewEncryptAlgorithm.equalsIgnoreCase("SHA") &&
			   !strNewEncryptAlgorithm.equalsIgnoreCase("RSA") &&
			   strNewEncryptAlgorithm.length() > 0)
				throw new AppException("Invalid encryption algorithm, must be SHA, RSA or not encrypted");
			print("\r\n");
			if(!strConfirmPassword.equals(strNewPassword))
				throw new AppException("Confirm password incorrect");
			if(strOldPassword.length() > 0 && strOldEncryptAlgorithm.length() > 0)
				strOldPassword = StringUtil.encrypt(strOldPassword,strOldEncryptAlgorithm);
			if(strNewPassword.length() > 0 && strNewEncryptAlgorithm.length() > 0)
				strNewPassword = StringUtil.encrypt(strNewPassword,strNewEncryptAlgorithm);

			// Change password
			ThreadProcessor processor = new ThreadProcessor(server.mmgrMain);
			processor.changePassword(getUserName(),strOldPassword,strNewPassword,strConfirmPassword);
			return "Password has been changed";
		}
		finally
		{
			in.setEcho(bEcho);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String dtfmt(String strFullCommand) throws Exception
	{
		// Check argument
		Vector vtArgument = analyseCommand(strFullCommand);
		final String strHelp = "Syntax error\r\nUsage:\r\n\tdtfmt [format]\r\nExample:\r\n\tdtfmt" +
							   " 'dd/MM/yyyy HH:mm:ss'\r\nAcceptable format pattern:\r\n" +
							   "Letter Component            Display     Examples\r\n" +
							   "G      Era designator       Text        AD\r\n" +
							   "y      Year                 Year        1996; 96\r\n" +
							   "M      Month in year        Month       July; Jul; 07\r\n" +
							   "w      Week in year         Number      27\r\n" +
							   "W      Week in month        Number      2\r\n" +
							   "D      Day in year          Number      189\r\n" +
							   "d      Day in month         Number      10\r\n" +
							   "F      Day of week in month Number      2\r\n" +
							   "E      Day in week          Text        Tuesday; Tue\r\n" +
							   "a      Am/pm marker         Text        PM\r\n" +
							   "H      Hour in day (0-23)   Number      0\r\n" +
							   "k      Hour in day (1-24)   Number      24\r\n" +
							   "K      Hour in am/pm (0-11) Number      0\r\n" +
							   "h      Hour in am/pm (1-12) Number      12\r\n" +
							   "m      Minute in hour       Number      30\r\n" +
							   "s      Second in minute     Number      55\r\n" +
							   "S      Millisecond          Number      978\r\n" +
							   "z      Time zone            General tz  Pacific Standard Time; PST; GMT-08:00\r\n" +
							   "Z      Time zone            RFC 822 tz  -0800\r\n";
		if(vtArgument.size() == 2 && ((String)vtArgument.elementAt(1)).trim().length() > 0)
		{
			String strFormat = StringUtil.nvl(vtArgument.elementAt(1),"");
			mfmtSession = new java.text.SimpleDateFormat(strFormat);
			return "Date format changed to '"  + mfmtSession.toPattern() + "'";
		}
		else if(vtArgument.size() == 1)
			return "Current date format is '" + mfmtSession.toPattern()+ "'";
		else
			throw new AppException(strHelp);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String sysdate(String strFullCommand) throws Exception
	{
		// Check argument
		Vector vtArgument = analyseCommand(strFullCommand);
		final String strHelp = "Syntax error\r\nUsage:\r\n\tsysdate [format]\r\nExample:\r\n\tsysdate" +
							   " 'dd/MM/yyyy HH:mm:ss'\r\nAcceptable format pattern:\r\n" +
							   "Letter Component            Display     Examples\r\n" +
							   "G      Era designator       Text        AD\r\n" +
							   "y      Year                 Year        1996; 96\r\n" +
							   "M      Month in year        Month       July; Jul; 07\r\n" +
							   "w      Week in year         Number      27\r\n" +
							   "W      Week in month        Number      2\r\n" +
							   "D      Day in year          Number      189\r\n" +
							   "d      Day in month         Number      10\r\n" +
							   "F      Day of week in month Number      2\r\n" +
							   "E      Day in week          Text        Tuesday; Tue\r\n" +
							   "a      Am/pm marker         Text        PM\r\n" +
							   "H      Hour in day (0-23)   Number      0\r\n" +
							   "k      Hour in day (1-24)   Number      24\r\n" +
							   "K      Hour in am/pm (0-11) Number      0\r\n" +
							   "h      Hour in am/pm (1-12) Number      12\r\n" +
							   "m      Minute in hour       Number      30\r\n" +
							   "s      Second in minute     Number      55\r\n" +
							   "S      Millisecond          Number      978\r\n" +
							   "z      Time zone            General tz  Pacific Standard Time; PST; GMT-08:00\r\n" +
							   "Z      Time zone            RFC 822 tz  -0800\r\n";
		if(vtArgument.size() == 2 && ((String)vtArgument.elementAt(1)).trim().length() > 0)
		{
			String strFormat = StringUtil.nvl(vtArgument.elementAt(1),"");
			return StringUtil.format(new java.util.Date(),strFormat);
		}
		else if(vtArgument.size() == 1)
			return mfmtSession.format(new java.util.Date());
		else
			throw new AppException(strHelp);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String lsthread(String strFullCommand) throws Exception
	{
		Vector vtArgument = analyseCommand(strFullCommand);
		if(vtArgument.size() != 1)
			throw new AppException("Syntax error\r\nUsage:\r\n\tlsthread");
		String strSeparator = " ";
		strSeparator = "|";
		StringBuffer buf = new StringBuffer();
		buf.append(" -----------------------------------------------------------------------------\r\n");
		buf.append(strSeparator + StringUtil.align("ID",StringUtil.ALIGN_CENTER,5) + strSeparator);
		buf.append(StringUtil.align("Name",StringUtil.ALIGN_CENTER,20) + strSeparator);
		buf.append(StringUtil.align("ClassName",StringUtil.ALIGN_CENTER,35) + strSeparator);
		buf.append(StringUtil.align("Startup",StringUtil.ALIGN_CENTER,7) + strSeparator);
		buf.append(StringUtil.align("Status",StringUtil.ALIGN_CENTER,6) + strSeparator + "\r\n");
		buf.append(" -----------------------------------------------------------------------------\r\n");

		Vector vt = server.mmgrMain.getThreadList();
		for(int iIndex = 0;iIndex < vt.size();iIndex++)
		{
			ManageableThread thr = (ManageableThread)vt.elementAt(iIndex);
			buf.append(strSeparator + StringUtil.align(StringUtil.nvl(thr.getThreadID(),""),StringUtil.ALIGN_CENTER,5) + strSeparator);
			buf.append(StringUtil.align(StringUtil.nvl(thr.getThreadName(),""),StringUtil.ALIGN_CENTER,20) + strSeparator);
			buf.append(StringUtil.align(StringUtil.nvl(thr.getClassName(),""),StringUtil.ALIGN_CENTER,35) + strSeparator);
			buf.append(StringUtil.align(StringUtil.nvl(thr.getStartupType(),""),StringUtil.ALIGN_CENTER,7) + strSeparator);
			buf.append(StringUtil.align(String.valueOf(thr.getThreadStatus()),StringUtil.ALIGN_CENTER,6) + strSeparator + "\r\n");
		}

		buf.append(" -----------------------------------------------------------------------------\r\n");
		return buf.toString();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String startthread(String strFullCommand) throws Exception
	{
		Vector vtArgument = analyseCommand(strFullCommand);
		if(vtArgument.size() != 2 || ((String)vtArgument.elementAt(1)).trim().length() == 0)
			throw new AppException("Syntax error\r\nUsage:\r\n\tstartthread <threadid>\r\nExample:\r\n\tstartthread 1");
		String strThreadID = StringUtil.nvl(vtArgument.elementAt(1),"");

		ManageableThread thread = server.mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new Exception("Thread " + strThreadID + " has not loaded into memory");
		server.mmgrMain.logAction("User '" + getUserName() + "' try to stop thread '" + thread.getThreadName() + "'");
		server.mmgrMain.startThread(thread);
		return "Request start for thread '" + strThreadID + "' has been sent, please use command 'lsthread' to check thread status";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String stopthread(String strFullCommand) throws Exception
	{
		Vector vtArgument = analyseCommand(strFullCommand);
		if(vtArgument.size() != 2 || ((String)vtArgument.elementAt(1)).trim().length() == 0)
			throw new AppException("Syntax error\r\nUsage:\r\n\tstopthread <threadid>\r\nExample:\r\n\tstopthread 1");
		String strThreadID = StringUtil.nvl(vtArgument.elementAt(1),"");

		ManageableThread thread = server.mmgrMain.getThread(strThreadID);
		if(thread == null)
			throw new Exception("Thread " + strThreadID + " has not loaded into memory");
		server.mmgrMain.logAction("User '" + getUserName() + "' try to stop thread '" + thread.getThreadName() + "'");
		server.mmgrMain.stopThread(thread);
		return "Request stop for thread '" + strThreadID + "' has been sent, please use command 'lsthread' to check thread status";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String lssession(String strFullCommand) throws Exception
	{
		Vector vtArgument = analyseCommand(strFullCommand);
		if(vtArgument.size() != 1)
			throw new AppException("Syntax error\r\nUsage:\r\n\tlssession");
		String strSeparator = " ";
		strSeparator = "|";
		StringBuffer buf = new StringBuffer();
		buf.append(" -------------------------------------------------------------------------\r\n");
		buf.append(strSeparator + StringUtil.align("ID",StringUtil.ALIGN_CENTER,10) + strSeparator);
		buf.append(StringUtil.align("User Name",StringUtil.ALIGN_CENTER,15) + strSeparator);
		buf.append(StringUtil.align("Server",StringUtil.ALIGN_CENTER,15) + strSeparator);
		buf.append(StringUtil.align("Login Time",StringUtil.ALIGN_CENTER,14) + strSeparator);
		buf.append(StringUtil.align("IP Address",StringUtil.ALIGN_CENTER,15) + strSeparator + "\r\n");
		buf.append(" -------------------------------------------------------------------------\r\n");

		Vector vtThread = server.mmgrMain.getThreadList();
		for(int iIndex = 0;iIndex < vtThread.size();iIndex++)
		{
			ManageableThread thr = (ManageableThread)vtThread.elementAt(iIndex);
			if(thr instanceof TelnetServer)
			{
				Vector vtChannel = ((TelnetServer)thr).getChannelList();
				for(int iChannelIndex = 0;iChannelIndex < vtChannel.size();iChannelIndex++)
				{
					TelnetCommander channel = (TelnetCommander)vtChannel.elementAt(iChannelIndex);
					if(channel.getSessionID() != null && channel.getSessionID().length() > 0) // Logged in
					{
						buf.append(strSeparator + StringUtil.align(StringUtil.nvl(channel.getSessionID(),""),StringUtil.ALIGN_CENTER,10) + strSeparator);
						buf.append(StringUtil.align(StringUtil.nvl(channel.getUserName(),""),StringUtil.ALIGN_CENTER,15) + strSeparator);
						buf.append(StringUtil.align(StringUtil.nvl(channel.server.getThreadName(),""),StringUtil.ALIGN_CENTER,15) + strSeparator);
						buf.append(StringUtil.align(StringUtil.format(channel.getLoginTime(),"yyyyMMddHHmmss"),StringUtil.ALIGN_CENTER,14) + strSeparator);
						buf.append(StringUtil.align(StringUtil.nvl(channel.getInetAddress().getHostAddress(),""),StringUtil.ALIGN_CENTER,15) + strSeparator + "\r\n");
					}
				}
			}
		}

		buf.append(" -------------------------------------------------------------------------\r\n");
		return buf.toString();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String killsession(String strFullCommand) throws Exception
	{
		Vector vtArgument = analyseCommand(strFullCommand);
		if(vtArgument.size() != 2 || ((String)vtArgument.elementAt(1)).trim().length() == 0)
			throw new AppException("Syntax error\r\nUsage:\r\n\tkillsession <sessionid>\r\nExample:\r\n\tkillsession 1");
		String strSessionID = StringUtil.nvl(vtArgument.elementAt(1),"");

		server.mmgrMain.logAction("User '" + getUserName() + "' try to kill session '" + strSessionID + "'");
		Vector vtThread = server.mmgrMain.getThreadList();
		for(int iIndex = 0;iIndex < vtThread.size();iIndex++)
		{
			ManageableThread thr = (ManageableThread)vtThread.elementAt(iIndex);
			if(thr instanceof TelnetServer)
			{
				Vector vtChannel = ((TelnetServer)thr).getChannelList();
				for(int iChannelIndex = 0;iChannelIndex < vtChannel.size();iChannelIndex++)
				{
					TelnetCommander channel = (TelnetCommander)vtChannel.elementAt(iChannelIndex);
					if(channel.getSessionID() != null &&
					   channel.getSessionID().equals(strSessionID))
					{
						channel.close("Session killed by '" + getUserName() + "'");
						return "Session '" + strSessionID + "' has been killed";
					}
				}
			}
		}
		throw new AppException("Session '" + strSessionID + "' does not exist or already be killed");
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String eval(String strFullCommand) throws Exception
	{
		Vector vtArgument = analyseCommand(strFullCommand);
		if(vtArgument.size() != 2 || ((String)vtArgument.elementAt(1)).trim().length() == 0)
			throw new AppException("Syntax error\r\nUsage:\r\n\teval <command script>");
		String strStatement = StringUtil.nvl(vtArgument.elementAt(1),"");
//		bsh.Interpreter interpreter = new bsh.Interpreter();
//		interpreter.eval("import com.fss.thread.*;\r\n");
//		interpreter.eval("ThreadManager mmgr;\r\n");
//		interpreter.getNameSpace().setVariable("mmgr",server.mmgrMain,true);
//
//		server.mmgrMain.logAction("User " + getUserName() + " try to perform script\r\n" + strStatement);
//		return StringUtil.nvl(interpreter.eval(strStatement),"");
		return "";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String evalbatch(String strFullCommand) throws Exception
	{
		Vector vtArgument = analyseCommand(strFullCommand);
		if(vtArgument.size() != 2 || ((String)vtArgument.elementAt(1)).trim().length() == 0)
			throw new AppException("Syntax error\r\nUsage:\r\n\tevalbatch <filename>");
		String strBatchFile = StringUtil.nvl(vtArgument.elementAt(1),"");
//		bsh.Interpreter interpreter = new bsh.Interpreter();
//		interpreter.eval("import com.fss.thread.*;\r\n");
//		interpreter.eval("ThreadManager mmgr;\r\n");
//		interpreter.getNameSpace().setVariable("mmgr",server.mmgrMain,true);
//
//		String strStatement = FileUtil.readSmallFile(strBatchFile);
//		server.mmgrMain.logAction("User " + getUserName() + " try to perform script\r\n" + strStatement);
//		return StringUtil.nvl(interpreter.eval(strStatement),"");
		return "";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public String shutdown(String strFullCommand) throws Exception
	{
		Vector vtArgument = analyseCommand(strFullCommand);
		if(vtArgument.size() != 1)
			throw new AppException("Syntax error\r\nUsage:\r\n\tshutdown");
		server.mmgrMain.logAction("User '" + getUserName() + "' try to shutdown server");
		server.mmgrMain.serverSocket.close();
		return "System is going to down";
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @param in SystemInputStream
	 * @param out SystemPrintStream
	 * @param err SystemPrintStream
	 * @param env Hashtable
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void execute(String strFullCommand,SystemInputStream in,SystemPrintStream out,SystemPrintStream err,Hashtable env) throws Exception
	{
		// Get command
		String strCommandLower = analyseCommandName(strFullCommand.trim()).toLowerCase();
		updateExpireTime();

		// Process
		try
		{
			if(strCommandLower.length() == 0)
				print("\r\n");
			else if(strCommandLower.equalsIgnoreCase("quit") ||
					strCommandLower.equalsIgnoreCase("exit") ||
					strCommandLower.equalsIgnoreCase("logout") ||
					strCommandLower.equalsIgnoreCase("bye")) // Process exit command
				close("Logged out");
			else if(isLocalCommand(strCommandLower))
				processLocalCommand(strCommandLower,strFullCommand);
			else
				throw new Exception("Command not supported");
		}
		catch(Exception e)
		{
			if(!(e instanceof AppException))
				e.printStackTrace();
			print(dicError.getString(e) + "\r\n");
		}
		updateExpireTime();
	}
	///////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	///////////////////////////////////////////////////////
	public String getUserID()
	{
		return mstrUserID;
	}
	///////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	///////////////////////////////////////////////////////
	public String getUserName()
	{
		return mstrUserName;
	}
	///////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	///////////////////////////////////////////////////////
	public String getSessionID()
	{
		return mstrSessionID;
	}
	///////////////////////////////////////////////////////
	/**
	 *
	 * @return Date
	 */
	///////////////////////////////////////////////////////
	public java.util.Date getLoginTime()
	{
		return mdtLogin;
	}
	///////////////////////////////////////////////////////
	/**
	 *
	 * @return Date
	 */
	///////////////////////////////////////////////////////
	public java.net.InetAddress getInetAddress()
	{
		return socket.getInetAddress();
	}
	///////////////////////////////////////////////////////
	/**
	 *
	 */
	///////////////////////////////////////////////////////
	public void clearUserInfo()
	{
		mstrUserID = "";
		mstrUserName = "";
	}
	///////////////////////////////////////////////////////
	/**
	 *
	 * @param strCommand String
	 * @throws Exception
	 */
	///////////////////////////////////////////////////////
	public void checkCommand(String strCommand) throws Exception
	{
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFullCommand String
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public static String analyseCommandName(String strFullCommand)
	{
		String strCommand = null;
		int iIndex = 0;
		for(iIndex = 0;iIndex < strFullCommand.length();iIndex++)
		{
			if((strFullCommand.charAt(iIndex) < 'a' ||
				strFullCommand.charAt(iIndex) > 'z') &&
			   (strFullCommand.charAt(iIndex) < 'A' ||
				strFullCommand.charAt(iIndex) > 'Z') &&
			   (strFullCommand.charAt(iIndex) < '0' ||
				strFullCommand.charAt(iIndex) > '9') &&
			   strFullCommand.charAt(iIndex) != '_')
			{
				strCommand = strFullCommand.substring(0,iIndex);
				break;
			}
		}
		if(iIndex >= strFullCommand.length())
			strCommand = strFullCommand;
		return strCommand;
	}
	///////////////////////////////////////////////////////
	/**
	 *
	 * @param strReasonDescription String
	 */
	///////////////////////////////////////////////////////
	public void close(String strReasonDescription)
	{
		// Remove channel from server
		try
		{
			print(strReasonDescription);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		server.removeChannel(this);

		if(socket != null)
		{
			// Log action
			server.mmgrMain.logAction("<FONT color=\"#CC6622\"><U>User '" + mstrUserName + "' disconnected from " + server.getThreadName() + ", sessionid=" + getSessionID() + ", reason is \"" + strReasonDescription + "\"</U></FONT>");

			// Ensure close only one time
			Socket sck = socket;
			socket = null;

			// Flush all remaining data
			try
			{
				sck.getOutputStream().flush();
				Thread.sleep(500);
			}
			catch(Exception e)
			{
			}

			// Close socket
			try
			{
				sck.close();
			}
			catch(Exception e)
			{
			}
		}
	}
	///////////////////////////////////////////////////////
	/**
	 *
	 */
	///////////////////////////////////////////////////////
	public void updateExpireTime()
	{
		mlExpire = System.currentTimeMillis() + server.getExpireDuration() * 1000;
	}
}

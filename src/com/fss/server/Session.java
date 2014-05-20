/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000 Dallas Semiconductor Corporation, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DALLAS SEMICONDUCTOR BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dallas Semiconductor
 * shall not be used except as stated in the Dallas Semiconductor
 * Branding Policy.
 *---------------------------------------------------------------------------
 */

package com.fss.server;

import java.io.*;
import java.util.*;

/**
 * A session represents a single connection to the TINI system shell.
 * When someone attempts to login, a server object spins off a session
 * (an implementation of this class) to handle the communication.  The session
 * allows you to log into the system, then processes all system requests.
 * Sessions are designed to be multi-threaded, allowing multiple simultaneous connections.
 */
public abstract class Session extends Thread
{
	/**
	 * The input stream of the remote connection.
	 */
	protected SystemInputStream in;
	/**
	 * The output stream of the remote connection.
	 */
	protected SystemPrintStream out;
	/**
	 * The error stream of the remote connection.
	 */
	protected SystemPrintStream err;
	/**
	 * Specifies that a command is currently being processed by this session.
	 */
	protected boolean inCommand;
	/**
	 * The environment associated with this session.
	 */
	protected Hashtable environment;
	/**
	 * Specifies that the session should attempt to close the connection after it has
	 * finished processing the current request.
	 */
	protected boolean shutdown;
	/**
	 * A list of the last few commands issued in this session.  This list is implemented
	 * as a circular buffer.
	 */
	protected String[] commandHistory;
	/**
	 * Marks the end of the commandHistory buffer.
	 */
	protected int lastCommand = 0;
	/**
	 * Marks the beginning of the commandHistory buffer.
	 */
	protected int currentCommand = 0;
	/**
	 * The text used as the command line prompt for this session.
	 */
	protected String prompt; //This is for speed of prompt writing.
	/**
	 * The server that created this session.
	 */
	protected Server server;
	/**
	 * The user that is currently logged in with this session.
	 */
	protected String userName;
	/**
	 * The password for the user that is currently logged in.
	 */
	protected String password;
	/**
	 * The list of users that are currently logged into this session.
	 */
	protected Vector loginStack;
	/**
	 * The thread ID of this session.
	 */
	protected Object myThreadID;
	/**
	 * Used as temporary storage when parsing the parameters for a command.
	 */
	protected Vector paramsVector;
	/**
	 * Used as temporary storage when parsing the parameters for a command.
	 */
	protected Object[] paramsArray;
	/**
	 * Used as temporary storage when parsing the parameters for a command.
	 */
	protected Object[] retArray;

	/**
	 * The key used to index the current directory in system environments.
	 */
	public static String CURRENT_DIRECTORY = "current directory";

	/**
	 * The key used to index the current command in system environments.
	 */
	public static String CURRENT_COMMAND = "current command";

	/**
	 * The message shown to all users when they login to this session.
	 */
	public String welcomeMessage = null;

	/**
	 * Intializes the session.
	 *
	 * @param in        stream this session should use to get data from user
	 * @param out       stream this session should use to output to user
	 * @param err       stream this session should use to output errors to user
	 * @param server    the server in charge of this session
	 */
	protected Session(SystemInputStream in,SystemPrintStream out,SystemPrintStream err,Server server)
	{
		this.in = in;
		this.out = out;
		this.err = err;
		inCommand = false;
		this.server = server;
		environment = new Hashtable();
		welcomeMessage = "Telnet server";
		String histSize = (String)environment.get("HISTORY");
		if(histSize != null)
		{
			try
			{
				int size = Integer.parseInt(histSize);

				commandHistory = new String[size];
			}
			catch(NumberFormatException nfe)
			{
				commandHistory = new String[50];
			}
		}
		else
			commandHistory = new String[50];
		loginStack = new Vector(5);
		paramsVector = new Vector(5);
		paramsArray = new Object[5];
		for(int i = 0;i < paramsArray.length;i++)
			paramsArray[i] = new String[i];
		retArray = new Object[2];
	}

	/**
	 * Returns the user name associated with this session.
	 *
	 * @return the current user's name
	 */
	public String getUserName()
	{
		return userName;
	}

	/**
	 * Logs a user into the system.  This method is responsible for identifying
	 * and verifying the user.  Typically this is done with a user name and password.
	 * @throws IOException
	 */
	protected abstract void login() throws IOException;

	/**
	 * Starts the communication loop of the session.  First the <code>login()</code> method
	 * is called.  If the user is sucessfully logged into the system, commands are accepted
	 * and processed until the session is terminated.
	 */
	public final void run()
	{
		myThreadID = Thread.currentThread();

		try
		{
			login();
		}
		catch(IOException ioe)
		{
			endSession();
			return;
		}

		while(!shutdown)
		{
			try
			{
				String command = getNextCommand();
				if(command == null)
				{
					forceEndSession();
					return;
				}
				else
				{
					if(command.length() == 0)
						currentCommandFinished();
					execute(command);
				}

				if((!shutdown) && out.checkError())
				{
					forceEndSession();
					return;
				}
			}
			catch(Throwable e) //(IOException ioe)
			{
				if(!shutdown)
				{
					forceEndSession();
					return;
				}
			}
		}

	}

	/**
	 * Executes the given command in the current shell.  The <code>commandStr</code>
	 * parameter can contain any number of elements.  These elements will be separated
	 * into <code>String[]</code> for the command interpreter.
	 *
	 * @param commandStr  full command line to execute
	 */
	protected void execute(String commandStr)
	{
		try
		{
			if((commandStr == null) || (commandStr.length() == 0))
				return;
			if(commandStr.startsWith("!"))
			{
				if(commandStr.equals("!!"))
				{
					commandStr = stepUpHistory();
					print(commandStr + "\r\n");
				}
				else
				{
					commandStr = commandStr.substring(1);
					try
					{
						commandStr = getHistoryNumber(Integer.parseInt(commandStr));
					}
					catch(NumberFormatException nfe)
					{
						boolean found = false;

						//Must be a string match request
						for(int i = 0;i < commandHistory.length;i++)
						{
							if(commandHistory[i] != null)
							{
								if(commandHistory[i].startsWith(commandStr))
								{
									commandStr = commandHistory[i];
									found = true;
									break;
								}
							}
						}
						if(!found)
							throw new Exception("No match found.");
					}
					print(commandStr + "\r\n");
				}
			}

			inCommand = true;
			addToHistory(commandStr);
			environment.put(CURRENT_COMMAND,commandStr);
			execute(commandStr,in,out,err,environment);
		}
		catch(Exception e)
		{
			if(out.checkError())
				forceEndSession();
			else
				exceptionThrown(e);
		}
		if(!shutdown)
		{
			currentCommandFinished();
			environment.put(CURRENT_COMMAND,"");
		}
		inCommand = false;
	}

	/**
	 * Gets the stream this session uses for error notification and critical messages.
	 * @return  the session's PrintStream used for errors
	 */
	public PrintStream getErrStream()
	{
		return err;
	}

	/**
	 * Gets the stream this session uses for output.
	 * @return  the session's PrintStream
	 */
	public PrintStream getOutputStream()
	{
		return out;
	}

	/**
	 * Notifies the server this session is ending and forces the session to terminate.
	 * This is used when some error has occurred that the session and server do not know how to handle.
	 */
	public final synchronized void forceEndSession()
	{
		//If we are not already attempting to shutdown...
		if(!shutdown)
		{
			shutdown = true;
			sessionEnding();
			if(server != null)
				server.sessionEnded(this);
		}
	}

	/**
	 * Cleans up the resources used by this session.
	 */
	public final synchronized void endSession()
	{
		if(loginStack.size() > 0)
		{
			Login login = (Login)loginStack.elementAt(0);
			loginStack.removeElementAt(0);
			userName = login.userName;
			password = login.password;
			currentCommandFinished();
		}
		else
		{
			sessionEnding();
			if(server != null)
				server.sessionEnded(this);
			shutdown = true;
		}
	}

	/**
	 * Encapsulates all of the information needed to log a user into the
	 * system.  This includes the user's name and password.
	 */
	protected class Login
	{
		String userName;
		String password;

		/**
		 * Stores the user's name and password.
		 * @param userName the user's name
		 * @param password the user's password
		 */
		public Login(String userName,String password)
		{
			this.userName = userName;
			this.password = password;
		}
	}

	/**
	 * Allows the current user to login as another user.  The old user's
	 * identity is added to a login stack.  Once the new user terminates
	 * their session, the old user idenity is resumed.
	 * @param userName  new user's name
	 * @param password  new user's password
	 * @return <code>true</code> if login was successful, <code>false</code>
	 * otherwise
	 */
	public boolean su(String userName,String password)
	{
		loginStack.insertElementAt(new Login(this.userName,this.password),0);
		this.userName = userName;
		this.password = password;
		return true;
	}

	/**
	 * Notifies this session that the current command has completed.  For example,
	 * the user types "ls".  The command is received and parsed, then the appropriate
	 * command is called in the shell.  Finally, this session may need to again
	 * display the system prompt or perform other session-specific functions.
	 * Only call this from a synchronized block!!!
	 */
	protected abstract void currentCommandFinished();

	/**
	 * Cleans up any resources associated with this session when it terminates.
	 */
	protected abstract void sessionEnding();

	/**
	 * Notifies this session that exception was thrown when executing a command.
	 * This method will attempt to notify the user.  Any exceptions raised specifically by
	 * shell commands should try to give as descriptive a message to the
	 * exception as possible.
	 * @param ex  the exception thrown
	 */
	protected abstract void exceptionThrown(Exception ex);

	/**
	 * Gets the next command from this session's input stream.
	 * This effectively performs a readLine(), but gives this session a chance
	 * to parse the incoming data for special characters and commands.
	 * @return  the next command from the input stream
	 * @return String
	 * @throws IOException
	 */
	public String getNextCommand() throws IOException
	{
		return in.readLine();
	}

	/**
	 * Gets a reference to the current environment.
	 * @return  the current environment as a <code>Hashtable</code>
	 */
	public Hashtable getEnvironment()
	{
		return environment;
	}

	/**
	 * Gets the value of the key from the current environment.
	 * @param key  name of desired environment variable
	 * @return the value specified by the given key, or <code>null</code>
	 * if that key does not exist in the environment
	 */
	public String getFromEnvironment(String key)
	{
		return(String)environment.get(key);
	}

	/**
	 * Adds a new command to the history buffer.
	 *
	 * @param str  command to add to the history
	 */
	public void addToHistory(String str)
	{
		commandHistory[lastCommand] = str;
		lastCommand = (lastCommand + 1) % commandHistory.length;
		commandHistory[lastCommand] = null;
		currentCommand = lastCommand;
	}

	/**
	 * Moves the current position in the history buffer up one
	 * and returns the command at that position.
	 *
	 * @return  the command one up from the current position in the
	 * history buffer
	 */
	public String stepUpHistory()
	{
		if(currentCommand == 0)
			currentCommand = commandHistory.length - 1;
		else
			currentCommand = (currentCommand - 1) % commandHistory.length;
		if(commandHistory[currentCommand] == null)
		{
			currentCommand = (currentCommand + 1) % commandHistory.length;
			return null;
		}
		return commandHistory[currentCommand];
	}

	/**
	 * Moves the current position in the history buffer down one
	 * and returns the command at that position.
	 *
	 * @return  the command one down from the current position in the
	 * history buffer
	 */
	public String stepDownHistory()
	{
		if(commandHistory[currentCommand] == null)
			return "";
		currentCommand = (currentCommand + 1) % commandHistory.length;
		if(commandHistory[currentCommand] == null)
			return "";
		return commandHistory[currentCommand];
	}

	/**
	 * Gets the command at the given index of the command history.
	 *
	 * @param number  index into the history cache.
	 *
	 * @return the command specified by the given number
	 */
	public String getHistoryNumber(int number)
	{
		if((number > commandHistory.length) || (commandHistory[number - 1] == null))
			return "";
		return commandHistory[number - 1];
	}

	/**
	 * Prints the list of commands stored in the history buffer of this session.
	 * @param out  stream used to print the history
	 */
	public void printHistory(PrintStream out)
	{
		for(int i = 0;i < commandHistory.length;i++)
			print((i + 1) + " " + ((commandHistory[i] == null) ? "" : commandHistory[i]) + "\r\n");
	}

	/**
	 * Indicates whether this session is executing a shell command.
	 * @return <code>true</code> if the session is executing a shell command
	 */
	public boolean inCommand()
	{
		return inCommand;
	}

	/**
	 * Notifies this session of a directory change.  This allows the command line
	 * prompt to reflect the change.
	 * @param withThis  the new directory name
	 */
	public void updatePrompt(String withThis)
	{
	}

	/**
	 * Displays a message in this session.  This functionality may be disabled
	 * by setting the environment variable "BROADCASTS" to "false".
	 * @param sendThis  message to display
	 */
	public void broadcast(String sendThis)
	{
		String disable = (String)environment.get("BROADCASTS");
		if((disable != null) && (disable.equals("false")))
			return;
		print(sendThis + "\r\n");
	}

	/**
	 * Executes a command in the shell.
	 * @param commandLine  An Object array containing the command in the first
	 * element, followed by any parameters need for that command in a String[]
	 * in the second element.
	 * @param in  The stream the command will use to get input.
	 * @param out  The stream used to report non-critical messages.
	 * @param err  The stream used to report critical messages.
	 * @param env  A table of environment variables.
	 * @throws Exception Any exception raised by the command.
	 */
	public void execute(String commandLine,SystemInputStream in,
						SystemPrintStream out,SystemPrintStream err,
						Hashtable env) throws Exception
	{
	}

	/**
	 *
	 * @param str String
	 */
	public void print(String str)
	{
		out.print(str);
	}
}

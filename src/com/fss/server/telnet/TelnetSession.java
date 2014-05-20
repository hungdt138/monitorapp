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

package com.fss.server.telnet;

import com.fss.server.*;
import java.io.*;
import java.net.*;

/**
 * This class encapsulates all functionality for a Telnet session.
 * The Telnet server listens to port 23 for connection requests.
 * When someone attempts to start a Telnet session, they contact
 * port 23.  The server spins off a session (an instance of this
 * class) to handle that session.  The session allows you to log
 * into the system, then handles the communication between that
 * remote computer and the TINI system.  The Telnet server and
 * sessions are designed to be multi-threaded, allowing multiple
 * simultaneous Telnet connections.
 */
public class TelnetSession extends Session
{
	Socket socket;
	/**
	 * Initializes the session.
	 * @param in SystemInputStream
	 * @param out SystemPrintStream
	 * @param err SystemPrintStream
	 * @param s Socket
	 * @param server Server
	 */
	public TelnetSession(SystemInputStream in,SystemPrintStream out,
						 SystemPrintStream err,Socket s,Server server)
	{
		super(in,out,err,server);
		socket = s;
		prompt = "";
	}
	public void flush()
	{
		out.flush();
	}
	public void welcome()
	{
		print(welcomeMessage + "\r\n");
	}
	public boolean login(String strUserName,String strPassword) throws IOException
	{
		return true;
	}
	/**
	 * Logs the user into the system.  This function will prompt the user
	 * for username and password.
	 * @throws IOException
	 */
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
				print("\r\n");

				if(userName.equals("root") && server != null && !server.isRootAllowed())
					connected = false;
				else if(login(userName,password))
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
			catch(Exception e)
			{
				forceEndSession();
				throw new IOException();
			}
		}
		out.setSession(this); // tell the output stream who the session is
		in.setSession(this); // tell the input stream who the session is
		if(server != null)
		{
			String welcomeMsg = server.getWelcomeMsg();
			print(welcomeMsg + "\r\n");
		}
		currentCommandFinished();
	}
	/**
	 * Called to clean up when the session is ending.  (i.e. the
	 * user typed "exit", or something bad happened that causes
	 * the session to terminate.)  Closes all sockets opened by
	 * this session.
	 */
	protected synchronized void sessionEnding()
	{
		try
		{
			socket.close();
			out = null;
			err = null;
			in = null;
		}
		catch(Throwable t)
		{
		}
	}
	/**
	 * Called after each command is completed.  For example, the user
	 * types "ls".  The command is received and parsed, then the appropriate
	 * command is called in the shell.  Finally, we need to again display the
	 * system prompt.
	 */
	public void currentCommandFinished()
	{
		try
		{
			print(prompt);
		}
		catch(Exception e)
		{
		}
	}
	/**
	 * This method was added to speed up prompt printing.  It is called when
	 * the prompt needs to change because the user changed directory.
	 * @param withThis  the current directory
	 */
	public void updatePrompt(String withThis)
	{
		prompt = withThis;
	}
	/**
	 * Called when an exception is thrown in a command.  This function will
	 * attempt to notify the user.  Any exceptions raised specifically by
	 * shell commands should try to give as descriptive a message to the
	 * exception as possible.
	 * @param ex  the exception thrown
	 */
	protected void exceptionThrown(Exception ex)
	{
		try
		{
			String message = ex.getMessage();
			if((message == null) || (message.length() == 0))
				print("Exception occurred: " + ex.toString() + "\r\n");
			else
				print(message + "\r\n");
			if(out.checkError())
				forceEndSession();
		}
		catch(Exception ioe)
		{
			forceEndSession();
		}
	}
}

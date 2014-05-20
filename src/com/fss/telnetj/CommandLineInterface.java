package com.fss.telnetj;

/**
 * <br><br><center><table border="1" width="80%"><hr>
 * <strong><a href="http://www.amherst.edu/~tliron/telnetj">telnetj</a></strong>
 * <p>
 * Copyright (C) 2001 by Tal Liron
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * <a href="http://www.gnu.org/copyleft/lesser.html">GNU Lesser General Public License</a>
 * for more details.
 * <p>
 * You should have received a copy of the <a href="http://www.gnu.org/copyleft/lesser.html">
 * GNU Lesser General Public License</a> along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <hr></table></center>
 **/

import java.io.*;
import java.util.*;

public abstract class CommandLineInterface implements Runnable
{
	//
	// Construction
	//
	public abstract void cleanup();

	protected CommandLineInterface(TelnetConnection telnet,Host host)
	{
		this.telnet = telnet;
		this.host = host;
	}

	//
	// Operations
	//

	public void start()
	{
		_thread = new Thread(this);
		_thread.start();
	}

	public void end()
	{
		synchronized(this)
		{
			_alive = false;
		}
	}

	public void execute(String commandline) throws IOException
	{
	}

	public void sleep(int ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch(InterruptedException x)
		{
		}
	}

	//
	// Runnable
	//
	public void run()
	{
		String commandline = null;
		try
		{
			while(true)
			{
				synchronized(this)
				{
					// Dead?
					if(!_alive)
						break;
				}

				telnet.print(prompt);
				telnet.flush();

				commandline = telnet.readLine();
				execute(commandline);
			}
		}
		catch(IOException x)
		{
		}

		try
		{
			telnet.close();
			cleanup();
			_alive = false;
		}
		catch(Exception x)
		{
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////
	protected boolean login(String username,String password)
	{
		return true;
	}

	protected boolean login() throws Exception
	{
		boolean blnLoginStatus = false;
		String strUserName = "";
		String strPassword = "";
		for(int i = 0;i < 3;i++)
		{
			prompt = "login: ";
			telnet.print(prompt);
			telnet.flush();
			strUserName = telnet.readLine();
			prompt = "password: ";
			telnet.print(prompt);
			telnet.flush();
			strPassword = telnet.readLine(false);
			if(!login(strUserName,strPassword))
			{
				telnet.print("Login incorrect\r\n\r\n");
				telnet.flush();
			}
			else
			{
				java.util.Date dtNow = new java.util.Date();
				String strHostAddress = (telnet.isViaSocket() ? telnet.getSocket().getInetAddress().getHostAddress() : "unknown");
				telnet.print("Login at: " + dtNow.toLocaleString() + " from " + strHostAddress + "\r\n");
				blnLoginStatus = true;
				break;
			}
		}
		return blnLoginStatus;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	// Private

	//
	// Attributes
	//
	protected String prompt = "$:";
	protected TelnetConnection telnet;
	protected Host host;

	//protected Object _lastParameter = null;
	//protected Command _currentCommand = null;
	//protected Map _commandMap = new HashMap();

	private Thread _thread = null;
	protected boolean _alive = true;
}

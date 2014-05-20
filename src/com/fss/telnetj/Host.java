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

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

public class Host implements Runnable
{
    public int getPort()
    {
        return port;
    }

    // Construction
    public Host(int port, InterfaceFactory interfaceFactory)
    {
		hostMap = new HashMap();
        this.port = port;
        this.interfaceFactory = interfaceFactory;
        this.thread = new Thread(this);
        hostMap.put(new Integer(port), this);
        this.thread.start();
    }
    //////////////////////////////////////////////////////////////////
	protected boolean allowAccess(String strIPAddress)
	{
		return true;
	}
    //////////////////////////////////////////////////////////////////
	protected boolean allowCommand(String commandline)
	{
		return true;
	}
    //////////////////////////////////////////////////////////////////
    public void run()
    {
        ServerSocket serverSocket = null;
        try
        {
            serverSocket = new ServerSocket(port);
        }
        catch( IOException x )
        {
            x.printStackTrace();
            return;
        }

        System.out.println( "telnetj host started on port " + port );

        Socket socket;
        while(true)
        {
            try
            {
                // Accept connections
				socket = serverSocket.accept();
				if(allowAccess(socket.getInetAddress().getHostAddress()))
				{
					TelnetConnection t = new TelnetConnection(socket, this);
					interfaceFactory.connect(t, this);
				}
				else
				{
					socket.close();
				}
		    }
            catch(Exception x )
            {
                x.printStackTrace();
            }
        }
    }
    //////////////////////////////////////////////////////////////////
	public synchronized void execute(TelnetConnection telnet, String commandline) throws IOException
	{
		this.telnet = telnet;
		commandline = commandline.trim();
		if(commandline.equals("")) return;
		try
		{
			if(allowCommand(commandline))
				commandDispather(commandline);
			else
				telnet.println("Command not found");
		}
		catch(Exception e)
		{
			telnet.println(e.getMessage());
		}
	}
    //////////////////////////////////////////////////////////////////
	private void commandDispather(String commandline) throws Exception
	{
		Method method = null;
		String methodName = commandline;
		if(commandline.indexOf(" ") >= 0)
			methodName = commandline.substring(0,commandline.indexOf(" "));

		Class[] arrClass = new Class[1];
		arrClass[0] = String.class;
		Object[] arrArg = new Object[1];
		arrArg[0] = commandline;
		try
		{
			method = this.getClass().getDeclaredMethod(methodName, arrClass);
			if (method != null)
			{
				if (!Modifier.isAbstract(method.getModifiers()))
				{
					if (!Modifier.isPublic(method.getModifiers()) &&
						!Modifier.isProtected(method.getModifiers()))
					{
						throw new Exception("Method '" + methodName + "' is private in the class " + this.getClass().getName());
					}
				}
				else
				{
					throw new Exception(methodName + " is an abstract method. ");
				}
				method.invoke(this, arrArg);
			}
		}
		catch (NoSuchMethodException e)
		{
			throw new Exception("Command not found");
		}
		catch (InvocationTargetException e)
		{
			throw new Exception(e.getTargetException().getMessage());
		}
	}
    //////////////////////////////////////////////////////////////////
	protected String parseCommand(String commandline)
	{
		String strReturn = "";
		char lastCharacter = 0;
		char[] temp = commandline.toCharArray();
		for(int i = 0; i<temp.length; i++)
		{
			if(lastCharacter != ' ' || temp[i] != ' ')
				strReturn += temp[i];
			lastCharacter = temp[i];
		}
		return strReturn;
	}
    //////////////////////////////////////////////////////////////////
	protected String addCharacter(String strSource, int targetLenght, String strAdd)
	{
		String strReturn = strSource;
		if(strSource.length() < targetLenght)
		{
			for(int i = 0; i< targetLenght-strSource.length(); i++)
			{
				strReturn += strAdd;
			}
		}
		return strReturn;
	}
    //////////////////////////////////////////////////////////////////
    // Private
    //////////////////////////////////////////////////////////////////
    private int port;
    private InterfaceFactory interfaceFactory;
    public Thread thread;
    public HashMap hostMap;
	//////////////////////////////////////////////////////////////////
	// Protected
	//////////////////////////////////////////////////////////////////
	protected TelnetConnection telnet;
}

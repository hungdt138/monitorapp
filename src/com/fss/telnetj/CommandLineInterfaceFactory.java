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

import java.lang.reflect.*;

public class CommandLineInterfaceFactory implements InterfaceFactory
{
    //
    // Construction
    //
	public CommandLineInterfaceFactory(Class commandLineInterfaceClass) throws NoSuchMethodException
	{
		commandLineInterfaceConstructor = commandLineInterfaceClass.getConstructor( new Class[] {TelnetConnection.class, Host.class} );
	}

	//
	// HandlerFactory
	//
    ///////////////////////////////////////////////////////////////////////////////////////
	public void connect(TelnetConnection telnet, Host host)
	{
		try
		{
			commandLineInterfaceConstructor.newInstance(new Object[] {telnet, host});
		}
		catch( InstantiationException x )
		{
			x.printStackTrace();
		}
		catch( IllegalAccessException x )
		{
			x.printStackTrace();
		}
		catch( InvocationTargetException x )
		{
			x.printStackTrace();
		}
	}
	///////////////////////////////////////////////////////////////////////////////////////
	// Private
	private Constructor commandLineInterfaceConstructor;
}

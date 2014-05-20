package com.fss.monitor;

import javax.swing.*;

import com.fss.util.*;
import com.fss.swing.*;

/**
 * <p>Title: GUI of client</p>
 * <p>Description: Client communicate with server throw socket</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author
 *  - Thai Hoang Hiep
 *  - Dang Dinh Trung
 * @version 1.0
 */

public class AppletThreadManager extends JApplet
{
	////////////////////////////////////////////////////////
	// Main object
	////////////////////////////////////////////////////////
	public PanelThreadManager pnlThread = new PanelThreadManager();
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	public void init()
	{
		try
		{
			jbInit();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}
	////////////////////////////////////////////////////////
	public void start()
	{
		pnlThread.login();
	}
	////////////////////////////////////////////////////////
	public void stop()
	{
		onExit();
	}
	////////////////////////////////////////////////////////
	public void destroy()
	{
		onExit();
	}
	////////////////////////////////////////////////////////
	/**
	 * Initialize user interface
	 * @throws java.lang.Exception
	 */
	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		setJMenuBar(pnlThread.mnuMain);
		setContentPane(pnlThread);
	}
	//////////////////////////////////////////////////////////
	// Purpose: onExit event
	// Author: HiepTH
	// Date: 09/2003
	//////////////////////////////////////////////////////////
	private void onExit()
	{
		if(pnlThread != null)
		{
			pnlThread.mbAutoLogIn = false;
			pnlThread.disconnect();
		}
	}
}

package com.fss.monitor;

import javax.swing.*;
import java.awt.event.*;

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

public class FrameThreadManager extends JFrame
{
	////////////////////////////////////////////////////////
	// Main object
	////////////////////////////////////////////////////////
	public PanelThreadManager pnlThread = new PanelThreadManager();
	////////////////////////////////////////////////////////
	/**
	 * Create new instance of FrameThreadManager
	 */
	////////////////////////////////////////////////////////
	public FrameThreadManager()
	{
		try
		{
			jbInit();
			this.setTitle(Global.APP_NAME);
			this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			this.setSize(720,540);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
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
		////////////////////////////////////////////////////////
		// Event handler
		////////////////////////////////////////////////////////
		this.addWindowListener(new java.awt.event.WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				onClose();
			}
			public void windowOpened(WindowEvent e)
			{
				pnlThread.login();
			}
		});
	}
	//////////////////////////////////////////////////////////
	/**
	 * Handling window closing event
	 */
	//////////////////////////////////////////////////////////
	private void onClose()
	{
		try
		{
			if(pnlThread.isOpen())
			{
				if(MessageBox.showConfirmDialog(this,MonitorDictionary.getString("Confirm.Exit"),Global.APP_NAME,MessageBox.YES_NO_OPTION) == MessageBox.NO_OPTION)
					return;
				if(pnlThread != null)
				{
					pnlThread.mbAutoLogIn = false;
					pnlThread.disconnect();
				}
			}
			dispose();
			System.exit(0);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	// Main entry of client
	////////////////////////////////////////////////////////
	public static void main(String[] args) throws Exception
	{
		FrameThreadManager frame = new FrameThreadManager();
		WindowManager.centeredWindow(frame);
	}
}

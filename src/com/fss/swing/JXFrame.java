package com.fss.swing;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public abstract class JXFrame extends JFrame implements LanguageUpdatable
{
	protected JXFrame()
	{
		////////////////////////////////////////////////////////
		this.addWindowListener(new java.awt.event.WindowAdapter()
		{
			public void windowClosed(WindowEvent e)
			{
				onClosed();
			}
			public void windowOpened(WindowEvent e)
			{
				onOpened();
			}
		});
		this.addWindowFocusListener(new java.awt.event.WindowFocusListener()
		{
			public void windowGainedFocus(WindowEvent e)
			{
			}
			public void windowLostFocus(WindowEvent e)
			{
			}
		});
	}
	////////////////////////////////////////////////////////
	public void onOpened()
	{
		WindowManager.refresh();
		Window wnd = WindowManager.getWindow(this.getClass());
		if(wnd != null)
		{
			wnd.requestFocus();
			this.dispose();
		}
		else
			WindowManager.addWindow(this);
		try
		{
			afterOpen();
		}
		catch(Exception e)
		{
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			this.dispose();
		}
	}
	////////////////////////////////////////////////////////
	public void onClosed()
	{
		WindowManager.removeWindow(this);
	}
	////////////////////////////////////////////////////////
	protected void afterOpen() throws Exception
	{
	}
	////////////////////////////////////////////////////////
	public void updateLanguage() throws Exception
	{
	}
}

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

public class JXDialog extends JDialog implements LanguageUpdatable
{
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param parent Component
	 * @param bModal boolean
	 */
	////////////////////////////////////////////////////////
	private KeyListener lsnCancel;
	public JXDialog(Component parent,boolean bModal)
	{
		super(JOptionPane.getFrameForComponent(parent),bModal);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		////////////////////////////////////////////////////////
		// Event handler
		////////////////////////////////////////////////////////
		addWindowListener(new java.awt.event.WindowAdapter()
		{
			public void windowClosed(WindowEvent e)
			{
				onClosed();
			}
			public void windowOpened(WindowEvent e)
			{
				onOpened();
			}
			public void windowClosing(WindowEvent evt)
			{
				onCancel();
			}
		});
		////////////////////////////////////////////////////////
		lsnCancel = new KeyAdapter()
		{
			public void keyPressed(KeyEvent evt)
			{
				if(evt.isConsumed())
					return;
				if(evt.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					if((evt.getSource() instanceof JComboBox) &&
					   ((JComboBox)evt.getSource()).isPopupVisible())
						return;
					else if(evt.getSource() instanceof Component)
					{
						Component cmp = ((Component)evt.getSource()).getParent();
						if(cmp != null &&
						   (cmp instanceof JTable ||
							cmp instanceof JTree ||
							cmp instanceof javax.swing.text.JTextComponent ||
							cmp instanceof AbstractButton))
							return;
					}
					onCancel();
					evt.consume();
				}
			}
		};
		////////////////////////////////////////////////////////
		ContainerListener lsn = new ContainerListener()
		{
			public void componentAdded(ContainerEvent evt)
			{
				componentAdded(evt.getChild());
			}
			public void componentRemoved(ContainerEvent evt)
			{
				componentRemoved(evt.getChild());
			}
			private void componentAdded(Component comp)
			{
				comp.addKeyListener(lsnCancel);
				if(comp instanceof Container)
				{
					Container cont = (Container)comp;
					cont.addContainerListener(this);
					Component[] comps = cont.getComponents();
					for(int i = 0;i < comps.length;i++)
						componentAdded(comps[i]);
				}
			}
			private void componentRemoved(Component comp)
			{
				comp.removeKeyListener(lsnCancel);
				if(comp instanceof Container)
				{
					Container cont = (Container)comp;
					cont.removeContainerListener(this);
					Component[] comps = cont.getComponents();
					for(int i = 0;i < comps.length;i++)
						componentRemoved(comps[i]);
				}
			}
		};
		////////////////////////////////////////////////////////
		addKeyListener(lsnCancel);
		((Container)getLayeredPane()).addContainerListener(lsn);
		getContentPane().addContainerListener(lsn);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	protected void onOpened()
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
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	protected void onClosed()
	{
		WindowManager.removeWindow(this);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	protected void afterOpen() throws Exception
	{
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	public void onCancel()
	{
		dispose();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void updateLanguage() throws Exception
	{
	}
}

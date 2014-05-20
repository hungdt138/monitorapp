package com.fss.monitor;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

import com.fss.ddtp.*;
import com.fss.swing.*;
import com.fss.util.*;
import com.fss.thread.ThreadConstant;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author
 * -Thai Hoang Hiep
 * -Dang Dinh Trung
 * @version 1.0
 */

public class PanelThreadMonitor extends JPanel
{
	////////////////////////////////////////////////////////
	public JTextArea txtMonitor = new JTextArea();
	private JLabel lblStatus = new JLabel();
	private JPopupMenu pmn = new JPopupMenu();
	private JMenuItem mnuSelectAll = new JMenuItem();
	private JMenuItem mnuClearAll = new JMenuItem();
	private JMenuItem mnuClearSelected = new JMenuItem();
	private JButton btnStart = new JButton();
	private JXPopupButton btnStop = new JXPopupButton();
	private JMenuItem mnuStopNormal = new JMenuItem();
	private JMenuItem mnuStopDestroy = new JMenuItem();
	private JButton btnSetting = new JButton();
	private JButton btnStartImmediate = new JButton();
	private JButton btnSchedule = new JButton();
	private JButton btnViewLog = new JButton();
	////////////////////////////////////////////////////////
	public String mstrThreadID;
	private String mstrThreadName;
	private int miThreadStatus;
	private DialogParamSetting dlgParaSetting;
	private DialogLogViewer dlgLogViewer;
	private SocketTransmitter channel;
	////////////////////////////////////////////////////////
	public PanelThreadMonitor(SocketTransmitter channel) throws Exception
	{
		this.channel = channel;
		jbInit();
	}
	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		JPanel pnlButton = new JPanel(new GridLayout(1,3,4,4));
		btnStop.addMenuItem(mnuStopNormal);
		btnStop.addMenuItem(mnuStopDestroy);
		pnlButton.add(btnStart);
		pnlButton.add(btnStop);
		pnlButton.add(btnSetting);
		pnlButton.add(btnSchedule);
		pnlButton.add(btnViewLog);
		pnlButton.add(btnStartImmediate);
		////////////////////////////////////////////////////////
		pmn.add(mnuSelectAll);
		pmn.addSeparator();
		pmn.add(mnuClearSelected);
		pmn.add(mnuClearAll);
		////////////////////////////////////////////////////////
		setLayout(new GridBagLayout());
		add(new JScrollPane(txtMonitor),new GridBagConstraints(0,0,2,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,1),0,0));
		add(pnlButton,new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		lblStatus.setBorder(Skin.BORDER_LOWRED);
		add(lblStatus,new GridBagConstraints(1,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		updateStatus();
		////////////////////////////////////////////////////////
		txtMonitor.setEditable(false);
		txtMonitor.setTabSize(4);
		txtMonitor.setLineWrap(true);
		txtMonitor.setWrapStyleWord(true);
		////////////////////////////////////////////////////////
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		txtMonitor.setAutoscrolls(true);
		////////////////////////////////////////////////////////
		updateLanguage();
		Skin.applySkin(btnStop.popupMenu);
		Skin.applySkin(pmn);
		Skin.applySkin(this);
		////////////////////////////////////////////////////////
		// Event map
		////////////////////////////////////////////////////////
		btnStart.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				startThread();
			}
		});
		////////////////////////////////////////////////////////
		mnuStopDestroy.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				destroyThread();
			}
		});
		////////////////////////////////////////////////////////
		mnuStopNormal.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				stopThread();
			}
		});
		////////////////////////////////////////////////////////
		btnSetting.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setting();
			}
		});
		////////////////////////////////////////////////////////
		btnSchedule.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				schedule();
			}
		});
		////////////////////////////////////////////////////////
		btnViewLog.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				viewLog();
			}
		});
		////////////////////////////////////////////////////////
		btnStartImmediate.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				startImmediate();
			}
		});
		////////////////////////////////////////////////////////
		mnuClearAll.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				txtMonitor.setText("");
			}
		});
		////////////////////////////////////////////////////////
		mnuClearSelected.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				clearSelected();
			}
		});
		////////////////////////////////////////////////////////
		mnuSelectAll.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				txtMonitor.requestFocus();
				selectAll();
			}
		});
		////////////////////////////////////////////////////////
		txtMonitor.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getButton() == e.BUTTON3)
					pmn.show(txtMonitor,e.getX(),e.getY());
			}
		});
	}
	//////////////////////////////////////////////////////////////
	public void updateUI()
	{
		super.updateUI();
		if(pmn != null)
		{
			SwingUtilities.updateComponentTreeUI(pmn);
			Skin.applySkin(pmn);
			Skin.applySkin(btnStop.popupMenu);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Update language
	 */
	////////////////////////////////////////////////////////
	public void updateLanguage()
	{
		MonitorDictionary.applyButton(btnStart,"Start");
		MonitorDictionary.applyButton(btnStop,"Stop");
		MonitorDictionary.applyButton(mnuStopDestroy,"StopDestroy");
		MonitorDictionary.applyButton(mnuStopNormal,"StopNormal");
		MonitorDictionary.applyButton(btnSetting,"Setting");
		MonitorDictionary.applyButton(btnSchedule,"Schedule");
		MonitorDictionary.applyButton(btnViewLog,"ViewLog");
		MonitorDictionary.applyButton(btnStartImmediate,"StartImmediate");
		MonitorDictionary.applyButton(mnuSelectAll,"jmenu.Edit.SelectAll");
		MonitorDictionary.applyButton(mnuClearAll,"jmenu.Edit.ClearAll");
		MonitorDictionary.applyButton(mnuClearSelected,"jmenu.Edit.ClearSelected");
		updateStatus();
	}
	////////////////////////////////////////////////////////
	// Event handler
	////////////////////////////////////////////////////////
	public void updateStatus()
	{
		if(btnStart != null)
		{
			if(miThreadStatus == ThreadConstant.THREAD_START)
			{
				JButton btn = MonitorDictionary.createButton("Restart");
				btnStart.setText(btn.getText());
				btnStart.setMnemonic(btn.getMnemonic());
				btnStop.setEnabled(true);
				lblStatus.setText(MonitorDictionary.getString("Status.ThreadStarted"));
				this.firePropertyChange("Status","Stopped","Started");
			}
			else if(miThreadStatus == ThreadConstant.THREAD_STOP)
			{
				JButton btn = MonitorDictionary.createButton("Start");
				btnStart.setText(btn.getText());
				btnStart.setMnemonic(btn.getMnemonic());
				btnStop.setEnabled(false);
				lblStatus.setText(MonitorDictionary.getString("Status.ThreadStopped"));
				this.firePropertyChange("Status","Started","Stopped");
			}
		}
	}
	////////////////////////////////////////////////////////
	public void startThread()
	{
		try
		{
			DDTP request = new DDTP();
			request.setString("ThreadID",mstrThreadID);
			channel.sendRequest("ThreadProcessor","startThread",request);
		}
		catch(Exception e)
		{
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	public void stopThread()
	{
		try
		{
			DDTP request = new DDTP();
			request.setString("ThreadID",mstrThreadID);
			channel.sendRequest("ThreadProcessor","stopThread",request);
		}
		catch(Exception e)
		{
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	public void destroyThread()
	{
		if(MessageBox.showConfirmDialog(this,MonitorDictionary.getString("Confirm.Destroy"),Global.APP_NAME,JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
		try
		{
			DDTP request = new DDTP();
			request.setString("ThreadID",mstrThreadID);
			channel.sendRequest("ThreadProcessor","destroyThread",request);
		}
		catch(Exception e)
		{
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	void setting()
	{
		try
		{
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			DDTP response = channel.sendRequest("ThreadProcessor","loadSetting",request);
			if(response!=null)
			{
				dlgParaSetting = new DialogParamSetting(this,mstrThreadID,mstrThreadName,
					response.getString("ThreadClassName"),response.getVector("vtSetting"),channel);
				dlgParaSetting.setStartupTypeCombo(response.getVector("vtStartupType"),
					response.getString("ThreadStartupType"));
				WindowManager.centeredWindow(dlgParaSetting);
			}
		}
		catch(Exception e)
		{
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	void schedule()
	{
		try
		{
			DialogSchedule dlg = new DialogSchedule(this,mstrThreadID,channel);
			WindowManager.centeredWindow(dlg);
		}
		catch(Exception e)
		{
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	public void viewLog()
	{
		try
		{
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			DDTP response = channel.sendRequest("ThreadProcessor","loadThreadLog",request);
			if(response!=null)
			{
				Vector vtDirLog = response.getVector("vtDirLog");
				dlgLogViewer = new DialogLogViewer(this,mstrThreadID,mstrThreadName,vtDirLog,channel);
				WindowManager.centeredWindow(dlgLogViewer);
			}
		}
		catch(Exception e)
		{
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	void startImmediate()
	{
		if(MessageBox.showConfirmDialog(this,MonitorDictionary.getString("Confirm.Start"),Global.APP_NAME,JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
			return;
		try
		{
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			channel.sendRequest("ThreadProcessor","startImmediate",request);
		}
		catch(Exception e)
		{
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	public void setThreadID(String strThreadID)
	{
		mstrThreadID = strThreadID;
	}
	////////////////////////////////////////////////////////
	public void setThreadName(String threadName)
	{
		mstrThreadName = threadName;
	}
	////////////////////////////////////////////////////////
	public void setThreadStatus(int threadStatus)
	{
		miThreadStatus = threadStatus;
	}
	/////////////////////////////////////////////////////////////
	// Author: TrungDD
	// Date: 05/12/2003
	void clearSelected()
	{
		txtMonitor.setEditable(true);
		txtMonitor.replaceSelection("");
		txtMonitor.setEditable(false);
	}
	/////////////////////////////////////////////////////////////
	// Author: TrungDD
	// Date: 05/12/2003
	void selectAll()
	{
		txtMonitor.selectAll();
	}
}

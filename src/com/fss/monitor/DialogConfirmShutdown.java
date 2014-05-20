package com.fss.monitor;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import com.fss.ddtp.*;
import com.fss.swing.*;
import com.fss.dictionary.Dictionary;

/**
 * <p>Title: Dialoge confirm depends on time </p>
 * <p>Description:
 *   -  If it reaches time out, one action will be fired
 * </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT-FSS-BU5</p>
 * @author Dang Dinh Trung
 * @version 1.0
 */

public class DialogConfirmShutdown extends JXDialog implements Runnable
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private Dictionary mdic = null;
	public boolean bAcceptToExit = true;
	private int miTimeOut = 7; // second
	private int miMintime = 0;
	private int miMaxtime = 1000;
	////////////////////////////////////////////////////////
	private JButton btnOK = new JButton();
	private JButton btnCancel = new JButton();
	private JLabel lblConfirm1 = new JLabel();
	private JLabel lblConfirm2 = new JLabel();
	private JProgressBar prgTimeOut = new JProgressBar();
	private boolean bActionPerform = false;
	////////////////////////////////////////////////////////
	public DialogConfirmShutdown(Component parent) throws Exception
	{
		////////////////////////////////////////////////////////
		super(parent,true);
		jbInit();
		////////////////////////////////////////////////////////
		setTitle(mdic.getString("Title"));
		this.pack();
		this.setSize(this.getHeight() * 3,this.getHeight());
	}
	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		//////////////////////////////////////////////////////////
		JPanel pnlButton = new JPanel(new GridLayout(1,2,4,4));
		pnlButton.add(btnOK);
		pnlButton.add(btnCancel);
		this.getRootPane().setDefaultButton(btnOK);
		//////////////////////////////////////////////////////////
		Container pnlMain = this.getContentPane();
		pnlMain.setLayout(new GridBagLayout());
		pnlMain.add(lblConfirm1,new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(8,2,2,2),0,0));
		pnlMain.add(lblConfirm2,new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlMain.add(prgTimeOut,new GridBagConstraints(0,2,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(8,2,8,2),0,0));
		pnlMain.add(pnlButton,new GridBagConstraints(0,3,2,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,8,2),0,0));
		////////////////////////////////////////////////////////
		updateLanguage();
		Skin.applySkin(this);
		////////////////////////////////////////////////////////
		btnOK.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				onOK();
			}
		});
		////////////////////////////////////////////////////////
		btnCancel.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onCancel();
			}
		});
		////////////////////////////////////////////////////////
		start();
	}
	////////////////////////////////////////////////////////////
	public void updateLanguage()
	{
		mdic = MonitorDictionary.getChildDictionary("DialogConfirmShutdown");
		MonitorDictionary.applyButton(btnOK,"OK");
		MonitorDictionary.applyButton(btnCancel,"Cancel");
		lblConfirm1.setText(mdic.getString("ConfirmMessage1"));
		lblConfirm2.setText(mdic.getString("ConfirmMessage2"));
	}
	////////////////////////////////////////////////////////////
	public void onOK()
	{
		bActionPerform = true;
		bAcceptToExit = true;
		dispose();
	}
	////////////////////////////////////////////////////////////
	public void onCancel()
	{
		bActionPerform = true;
		bAcceptToExit = false;
		dispose();
	}
	////////////////////////////////////////////////////////////
	// Purpose: process the progess bar
	// Author: Dang Dinh Trung
	// Date: 02/10/2003
	////////////////////////////////////////////////////////////
	public void run()
	{
		prgTimeOut.setMinimum(miMintime);
		prgTimeOut.setMaximum(miMaxtime);
		prgTimeOut.setSize(100,100);
		int iIndex = miMintime;
		int iSleep = miTimeOut * 1000 / (miMaxtime-miMintime);
		bAcceptToExit = true;
		while((iIndex < miMaxtime) && !bActionPerform )
		{
			try
			{
				prgTimeOut.setValue(iIndex);
				Thread.sleep(iSleep);
				iIndex++;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		this.dispose();
	}
	////////////////////////////////////////////////////////////
	public void start()
	{
		Thread mthrWait = new Thread(this);
		mthrWait.start();
	}
}

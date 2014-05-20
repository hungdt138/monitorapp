package com.fss.monitor;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

import com.fss.util.*;
import com.fss.swing.*;
import com.fss.thread.*;
import com.fss.dictionary.*;
import com.fss.dictionary.Dictionary;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class DialogLogin extends JXDialog
{
	////////////////////////////////////////////////////////
	private Dictionary mdic = null;
	private JXText txtUserName = new JXText();
	private JXPassword txtPassword = new JXPassword();
	private JComboBox cboEncryptAlgorithm = new JComboBox();
	private JXText txtHost = new JXText();
	private JXText txtPort = new JXText();
	private JXCombo cboLanguage = new JXCombo();
	private JButton btnOK = new JButton();
	private JButton btnCancel = new JButton();
	private JLabel lblLoginName = new JLabel();
	private JLabel lblPassword = new JLabel();
	private JLabel lblEncryptAlgorithm = new JLabel();
	private JLabel lblHost = new JLabel();
	private JLabel lblPort = new JLabel();
	private JLabel lblLanguage = new JLabel();
	private Vector mvtLanguage = new Vector();
	////////////////////////////////////////////////////////
	public String mstrUserID;
	public String mstrUserName;
	public String mstrPassword;
	public String mstrHost;
	public String mstrPort;
	public String mstrAlgorithm;
	public String mstrLanguage;
	public int miReturn = JOptionPane.CANCEL_OPTION;
	////////////////////////////////////////////////////////
	public DialogLogin(Component parent) throws Exception
	{
		////////////////////////////////////////////////////////
		super(parent,true);
		////////////////////////////////////////////////////////
		jbInit();
		////////////////////////////////////////////////////////
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack();
		this.setSize(250,this.getHeight());
	}
	////////////////////////////////////////////////////////
	public void afterOpen()
	{
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		if(txtUserName.getText().length() > 0)
			txtPassword.requestFocusInWindow();
		else
			txtUserName.requestFocusInWindow();
	}
	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		String[] strLanguage = MonitorDictionary.getSupportedLanguage();
		for(int iIndex = 0;iIndex < strLanguage.length;iIndex++)
		{
			mvtLanguage.addElement(strLanguage[iIndex]);
			cboLanguage.addItem(MonitorDictionary.getDictionary(strLanguage[iIndex]).getLanguage());
		}
		int iIndex = mvtLanguage.indexOf(MonitorDictionary.getCurrentLanguage());
		if(iIndex >= 0)
			cboLanguage.setSelectedIndex(iIndex);
		////////////////////////////////////////////////////////
		cboEncryptAlgorithm.addItem(MonitorDictionary.getString("NotEncrypt"));
		cboEncryptAlgorithm.addItem("SHA");
		cboEncryptAlgorithm.addItem("RSA");
		////////////////////////////////////////////////////////
		Hashtable prt = null;
		try
		{
			prt = Global.loadHashtable(Global.FILE_CONFIG);
		}
		catch(Exception e)
		{
			prt = new Hashtable();
		}
		txtUserName.setText(StringUtil.nvl(prt.get("UserName"),""));
		txtHost.setText(StringUtil.nvl(prt.get("Host"),""));
		txtPort.setText(StringUtil.nvl(prt.get("Port"),""));
		String strAlgorithm = StringUtil.nvl(prt.get("Algorithm"),"");
		if(strAlgorithm == null)
			cboEncryptAlgorithm.setSelectedIndex(1);
		else if(strAlgorithm.length() == 0)
			cboEncryptAlgorithm.setSelectedIndex(0);
		else
			cboEncryptAlgorithm.setSelectedItem(strAlgorithm);
		////////////////////////////////////////////////////////
		JPanel pnlButton = new JPanel(new GridLayout(1,2,4,4));
		txtUserName.setFilter(":;<=>?@[\\]^`");
		txtUserName.addAllowanceBoundary((char)48,(char)122);
		txtUserName.setMaxLength(30);
		txtPassword.setMaxLength(30);
		txtHost.setFilter(ParameterType.FILTER_REGULAR);
		pnlButton.add(btnOK);
		pnlButton.add(btnCancel);
		getRootPane().setDefaultButton(btnOK);
		////////////////////////////////////////////////////////
		Container pnlMain = this.getContentPane();
		pnlMain.setLayout(new GridBagLayout());
		pnlMain.add(lblLoginName,new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,4,2,2),0,0));
		pnlMain.add(txtUserName,new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(4,2,2,4),0,0));
		pnlMain.add(lblPassword,new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,4,2,2),0,0));
		pnlMain.add(txtPassword,new GridBagConstraints(1,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,4),0,0));
		pnlMain.add(lblEncryptAlgorithm,new GridBagConstraints(0,2,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,4,2,2),0,0));
		pnlMain.add(cboEncryptAlgorithm,new GridBagConstraints(1,2,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,4),0,0));
		pnlMain.add(lblHost,new GridBagConstraints(0,3,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,4,2,2),0,0));
		pnlMain.add(txtHost,new GridBagConstraints(1,3,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,4),0,0));
		pnlMain.add(lblPort,new GridBagConstraints(0,4,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,4,2,2),0,0));
		pnlMain.add(txtPort,new GridBagConstraints(1,4,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,4),0,0));
		pnlMain.add(lblLanguage,new GridBagConstraints(0,5,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,4,2,2),0,0));
		pnlMain.add(cboLanguage,new GridBagConstraints(1,5,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,4),0,0));
		pnlMain.add(pnlButton,new GridBagConstraints(0,6,2,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(6,2,6,2),0,0));
		////////////////////////////////////////////////////////
		updateLanguage();
		Skin.applySkin(this);
		////////////////////////////////////////////////////////
		txtUserName.removeKeyListener(Skin.LANGUAGE_KEY_LISTENER);
		////////////////////////////////////////////////////////
		// Event map
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
	}
	////////////////////////////////////////////////////////
	public void updateLanguage()
	{
		mdic = MonitorDictionary.getChildDictionary("DialogLogin");
		MonitorDictionary.applyButton(btnOK,"OK");
		MonitorDictionary.applyButton(btnCancel,"Cancel");
		lblLoginName.setText(mdic.getString("LoginName"));
		lblPassword.setText(mdic.getString("Password"));
		lblEncryptAlgorithm.setText(mdic.getString("EncryptAlgorithm"));
		lblHost.setText(mdic.getString("Host"));
		lblPort.setText(mdic.getString("Port"));
		lblLanguage.setText(mdic.getString("Language"));
		setTitle(mdic.getString("Title"));
	}
	////////////////////////////////////////////////////////
	public void onOK()
	{
		////////////////////////////////////////////////////////
		// Validate input
		////////////////////////////////////////////////////////
		mstrUserName = txtUserName.getText();
		mstrPassword = new String(txtPassword.getPassword());
		mstrHost = txtHost.getText();
		mstrPort = txtPort.getText();
		mstrAlgorithm = "";
		if(cboEncryptAlgorithm.getSelectedIndex() > 0)
		{
			mstrAlgorithm = (String)cboEncryptAlgorithm.getSelectedItem();
			if(mstrPassword.length() > 0)
			{
				try
				{
					mstrPassword = StringUtil.encrypt(mstrPassword.getBytes(),mstrAlgorithm);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		mstrLanguage = (String)mvtLanguage.elementAt(cboLanguage.getSelectedIndex());
		////////////////////////////////////////////////////////
		if(mstrUserName.length() == 0)
		{
			MessageBox.showMessageDialog(this,ErrorDictionary.getString("FSS-00002",mdic.getString("LoginName")),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			txtUserName.requestFocus();
			return;
		}
		////////////////////////////////////////////////////////
		if(mstrHost.length() == 0)
		{
			MessageBox.showMessageDialog(this,ErrorDictionary.getString("FSS-00002",mdic.getString("Host")),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			txtHost.requestFocus();
			return;
		}
		////////////////////////////////////////////////////////
		if(mstrPort.length() == 0)
		{
			MessageBox.showMessageDialog(this,ErrorDictionary.getString("FSS-00002",mdic.getString("Port")),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			txtPort.requestFocus();
			return;
		}
		////////////////////////////////////////////////////////
		// Store config
		////////////////////////////////////////////////////////
		Hashtable prt = null;
		try
		{
			prt = Global.loadHashtable(Global.FILE_CONFIG);
		}
		catch(Exception e)
		{
			prt = new Hashtable();
		}
		prt.put("UserName",mstrUserName);
		prt.put("Host",mstrHost);
		prt.put("Port",mstrPort);
		prt.put("Algorithm",mstrAlgorithm);
		prt.put("Language",mstrLanguage);
		try
		{
			Global.storeHashtable(prt,Global.FILE_CONFIG);
		}
		catch(Exception e)
		{
		}
		if(Skin.LANGUAGE_CHANGE_LISTENER != null && !MonitorDictionary.getCurrentLanguage().equals(mstrLanguage))
			Skin.LANGUAGE_CHANGE_LISTENER.changeDictionary(mstrLanguage);
		miReturn = JOptionPane.OK_OPTION;
		this.dispose();
	}
}

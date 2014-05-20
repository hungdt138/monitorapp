package com.fss.monitor;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;

import com.fss.ddtp.*;
import com.fss.util.*;
import com.fss.swing.*;
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

public class DialogChangePassword extends JXDialog
{
	////////////////////////////////////////////////////////
	private Dictionary mdic = null;
	private JXPassword txtOldPwd = new JXPassword();
	private JXPassword txtNewPwd = new JXPassword();
	private JXPassword txtConfirmPwd = new JXPassword();
	private JComboBox cboNewEncryptAlgorithm = new JComboBox();
	private JComboBox cboOldEncryptAlgorithm = new JComboBox();
	private JButton btnOK = new JButton();
	private JButton btnCancel = new JButton();
	private JLabel lblNewPwd = new JLabel();
	private JLabel lblConfirmPwd = new JLabel();
	private JLabel lblOldPwd = new JLabel();
	private JLabel lblNewEncryptAlgorithm = new JLabel();
	private JLabel lblOldEncryptAlgorithm = new JLabel();
	private SocketTransmitter channel;
	public int miReturnValue = JOptionPane.CANCEL_OPTION;
	////////////////////////////////////////////////////////
	public DialogChangePassword(Component parent,SocketTransmitter channel) throws Exception
	{
		////////////////////////////////////////////////////////
		super(parent,true);
		this.channel = channel;
		////////////////////////////////////////////////////////
		jbInit();
		////////////////////////////////////////////////////////
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack();
		this.setSize(250,this.getHeight());
	}
	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		txtOldPwd.setMaxLength(30);
		txtNewPwd.setMaxLength(30);
		txtConfirmPwd.setMaxLength(30);
		////////////////////////////////////////////////////////
		cboNewEncryptAlgorithm.addItem(MonitorDictionary.getString("NotEncrypt"));
		cboNewEncryptAlgorithm.addItem("SHA");
		cboNewEncryptAlgorithm.addItem("RSA");
		cboNewEncryptAlgorithm.setSelectedIndex(1);
		////////////////////////////////////////////////////////
		cboOldEncryptAlgorithm.addItem(MonitorDictionary.getString("NotEncrypt"));
		cboOldEncryptAlgorithm.addItem("SHA");
		cboOldEncryptAlgorithm.addItem("RSA");
		cboOldEncryptAlgorithm.setSelectedIndex(1);
		////////////////////////////////////////////////////////
		JPanel pnlButton = new JPanel(new GridLayout(1,2,4,4));
		pnlButton.add(btnOK);
		pnlButton.add(btnCancel);
		getRootPane().setDefaultButton(btnOK);
		////////////////////////////////////////////////////////
		Container pnlMain = this.getContentPane();
		pnlMain.setLayout(new GridBagLayout());
		pnlMain.add(lblOldPwd,new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(4,4,2,2),0,0));
		pnlMain.add(txtOldPwd,new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(4,2,2,4),0,0));
		pnlMain.add(lblOldEncryptAlgorithm,new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,4,2,2),0,0));
		pnlMain.add(cboOldEncryptAlgorithm,new GridBagConstraints(1,1,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,4),0,0));
		pnlMain.add(lblNewPwd,new GridBagConstraints(0,2,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(4,4,2,2),0,0));
		pnlMain.add(txtNewPwd,new GridBagConstraints(1,2,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,4),0,0));
		pnlMain.add(lblConfirmPwd,new GridBagConstraints(0,3,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,4,2,2),0,0));
		pnlMain.add(txtConfirmPwd,new GridBagConstraints(1,3,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,4),0,0));
		pnlMain.add(lblNewEncryptAlgorithm,new GridBagConstraints(0,4,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,4,2,2),0,0));
		pnlMain.add(cboNewEncryptAlgorithm,new GridBagConstraints(1,4,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,4),0,0));
		pnlMain.add(pnlButton,new GridBagConstraints(0,5,2,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		////////////////////////////////////////////////////////
		updateLanguage();
		Skin.applySkin(this);
		////////////////////////////////////////////////////////
		// Event map
		////////////////////////////////////////////////////////
		btnOK.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
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
		mdic = MonitorDictionary.getChildDictionary("DialogChangePassword");
		MonitorDictionary.applyButton(btnOK,"OK");
		MonitorDictionary.applyButton(btnCancel,"Cancel");
		lblNewPwd.setText(mdic.getString("NewPassword"));
		lblConfirmPwd.setText(mdic.getString("ConfirmPassword"));
		lblOldPwd.setText(mdic.getString("OldPassword"));
		lblNewEncryptAlgorithm.setText(mdic.getString("EncryptAlgorithm"));
		lblOldEncryptAlgorithm.setText(mdic.getString("EncryptAlgorithm"));
		setTitle(mdic.getString("Title"));
	}
	////////////////////////////////////////////////////////
	public void onOK()
	{
		try
		{
			////////////////////////////////////////////////////////
			String strOldPassword = new String(txtOldPwd.getPassword());
			String strConfirmPassword = new String(txtConfirmPwd.getPassword());
			String strNewPassword = new String(txtNewPwd.getPassword());
			String strRealPassword = strNewPassword;
			////////////////////////////////////////////////////////
			if(!strConfirmPassword.equals(strNewPassword))
			{
				MessageBox.showMessageDialog(this,ErrorDictionary.getString("FSS-00007"),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
				txtConfirmPwd.requestFocus();
				return;
			}
			////////////////////////////////////////////////////////
			String strOldEncryptAlgorithm = "",strNewEncryptAlgorithm = "";
			if(cboNewEncryptAlgorithm.getSelectedIndex() > 0)
				strNewEncryptAlgorithm = (String)cboNewEncryptAlgorithm.getSelectedItem();
			if(cboOldEncryptAlgorithm.getSelectedIndex() > 0)
				strOldEncryptAlgorithm = (String)cboOldEncryptAlgorithm.getSelectedItem();
			if(strOldPassword.length() > 0 && strOldEncryptAlgorithm.length() > 0)
				strOldPassword = StringUtil.encrypt(strOldPassword,strOldEncryptAlgorithm);
			if(strNewPassword.length() > 0 && strNewEncryptAlgorithm.length() > 0)
				strNewPassword = StringUtil.encrypt(strNewPassword,strNewEncryptAlgorithm);
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("OldPassword",strOldPassword);
			request.setString("NewPassword",strNewPassword);
			request.setString("RealPassword",strRealPassword);
			channel.sendRequest("ThreadProcessor","changePassword",request);
			MessageBox.showMessageDialog(this,mdic.getString("SuccessMessage"),Global.APP_NAME,JOptionPane.INFORMATION_MESSAGE);
			miReturnValue = JOptionPane.OK_OPTION;
			////////////////////////////////////////////////////////
			// Store config
			////////////////////////////////////////////////////////
			Hashtable prt = Global.loadHashtable(Global.FILE_CONFIG);
			prt.put("Algorithm",strNewEncryptAlgorithm);
			Global.storeHashtable(prt,Global.FILE_CONFIG);
			this.dispose();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			if(e instanceof AppException && ((AppException)e).getContext().equals("validatePassword"))
				txtNewPwd.requestFocus();
			else
				txtOldPwd.requestFocus();
			return;
		}
	}
}

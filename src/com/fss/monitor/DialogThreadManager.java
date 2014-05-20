package com.fss.monitor;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

import com.fss.util.*;
import com.fss.ddtp.*;
import com.fss.swing.*;
import com.fss.dictionary.*;
import com.fss.dictionary.Dictionary;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author
 *  - Thai Hoang Hiep
 *  - Dang Dinh Trung
 * @version 1.0
 */

public class DialogThreadManager extends JXDialog
{
	////////////////////////////////////////////////////////
	private Dictionary mdic = null;
	private JButton btnUpdate = new JButton();
	private JButton btnExit = new JButton();
	private JButton btnCommit = new JButton();
	private JButton btnCancel = new JButton();
	private JXText txtThreadName = new JXText();
	private JXText txtThreadClass = new JXText();
	private JLabel lblThreadName = new JLabel();
	private JLabel lblThreadClass = new JLabel();
	private JLabel lblStartupType = new JLabel();
	private JPanel pnlNormal = new JPanel(new GridLayout(1,2,4,4));
	private JPanel pnlActive = new JPanel(new GridLayout(1,2,4,4));
	private JComboBox cboStartupType = new JComboBox();
	private VectorTable tblContent = new VectorTable(4);
	private JTableContainer pnlContent = new JTableContainer(tblContent);
	////////////////////////////////////////////////////////
	private Vector vtTableData = new Vector();
	private Vector mvtComboStartupType = new Vector();
	private String mstrThreadID;
	private SocketTransmitter channel;
	////////////////////////////////////////////////////////
	public DialogThreadManager(Component parent,SocketTransmitter channel,Vector vtTableData) throws Exception
	{
		////////////////////////////////////////////////////////
		super(parent,true);
		this.channel = channel;
		this.vtTableData = vtTableData;
		jbInit();
		////////////////////////////////////////////////////////
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(480,360);
	}
	////////////////////////////////////////////////////////
	public void setComboStartupType(Vector vtStartupType,String defaultValue)
	{
		createCombo(cboStartupType,mvtComboStartupType,vtStartupType,defaultValue);
		showDetailValue();
	}
	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		JPanel pnlParams = new JPanel(new GridBagLayout());
		pnlParams.add(lblThreadName,new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlParams.add(txtThreadName,new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2 ),0,0));
		pnlParams.add(lblThreadClass,new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlParams.add(txtThreadClass,new GridBagConstraints(1,1,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
		pnlParams.add(lblStartupType,new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2 ),0,0));
		pnlParams.add(cboStartupType,new GridBagConstraints(1,2,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2 ),0,0));
		cboStartupType.setPreferredSize(txtThreadName.getPreferredSize());
		////////////////////////////////////////////////////////
		tblContent.addColumn("",0,false);
		tblContent.addColumn("",1,false);
		tblContent.addColumn("",2,false);
		tblContent.getColumnEx(0).setPreferredWidth(110);
		tblContent.getColumnEx(1).setPreferredWidth(185);
		tblContent.getColumnEx(2).setPreferredWidth(47);
		tblContent.setColumnEditorEx(cboStartupType,2);
		tblContent.setColumnValueMapEx(mvtComboStartupType,2);
		////////////////////////////////////////////////////////
		pnlNormal.add(btnUpdate);
		pnlNormal.add(btnExit);
		pnlActive.add(btnCommit);
		pnlActive.add(btnCancel);
		setNormalState();
		////////////////////////////////////////////////////////
		Container pnlMain = this.getContentPane();
		pnlMain.setLayout(new GridBagLayout());
		pnlMain.add(pnlParams,new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		pnlMain.add(pnlContent,new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.EAST,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		pnlMain.add(pnlNormal,new GridBagConstraints(0,2,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlMain.add(pnlActive,new GridBagConstraints(0,2,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		////////////////////////////////////////////////////////
		updateLanguage();
		Skin.applySkin(this);
		fillValue();
		////////////////////////////////////////////////////////
		// Event map
		////////////////////////////////////////////////////////
		tblContent.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				showDetailValue();
			}
		});
		////////////////////////////////////////////////////////
		btnUpdate.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doUpdate();
			}
		});
		////////////////////////////////////////////////////////
		btnCommit.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doCommit();
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
		btnExit.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
	}
	////////////////////////////////////////////////////////
	public void updateLanguage()
	{
		mdic = MonitorDictionary.getChildDictionary("ThreadManager");
		MonitorDictionary.applyButton(btnUpdate,"Modify");
		MonitorDictionary.applyButton(btnExit,"Exit");
		MonitorDictionary.applyButton(btnCommit,"OK");
		MonitorDictionary.applyButton(btnCancel,"Cancel");
		lblThreadName.setText(mdic.getString("ThreadName"));
		lblThreadClass.setText(mdic.getString("ThreadClass"));
		lblStartupType.setText(mdic.getString("StartupType"));
		tblContent.setColumnNameEx(mdic.getString("ThreadName"),0);
		tblContent.setColumnNameEx(mdic.getString("ThreadClass"),1);
		tblContent.setColumnNameEx(mdic.getString("StartupType"),2);
		pnlContent.setTitle(mdic.getString("ThreadList"));
		setTitle(mdic.getString("Title"));
	}
	////////////////////////////////////////////////////////
	public void fillValue()
	{
		try
		{
			if(vtTableData.size() > 0)
			{
				tblContent.setData(vtTableData);
				tblContent.setRowSelectionInterval(0,0);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	public void showDetailValue()
	{
		////////////////////////////////////////////////////////
		int iSelectedRow = tblContent.getSelectedRow();
		if(iSelectedRow >= 0)
		{
			Vector vtRow = (Vector)tblContent.getRow(iSelectedRow);
			mstrThreadID = vtRow.elementAt(3).toString();
			txtThreadName.setText(vtRow.elementAt(0).toString());
			txtThreadClass.setText(vtRow.elementAt(1).toString());
			cboStartupType.setSelectedIndex(mvtComboStartupType.indexOf(vtRow.elementAt(2).toString()));
		}
	}
	////////////////////////////////////////////////////////
	// Date: 15/11/2003
	private void createCombo(JComboBox cboBox,Vector vtCombo,Vector vtSource,String strSeletectedValue)
	{
		try
		{
			if(vtSource != null)
			{
				for(int iItemIndex = 0;iItemIndex < vtSource.size();iItemIndex++)
				{
					vtCombo.addElement(((Vector)vtSource.elementAt(iItemIndex)).elementAt(0));
					cboBox.addItem(((Vector)vtSource.elementAt(iItemIndex)).elementAt(1));
				}
			}
			if (strSeletectedValue.length() == 0)
				cboBox.setSelectedIndex(0);
			else
				cboBox.setSelectedIndex(vtCombo.indexOf(strSeletectedValue));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	////////////////////////////////////////////////////////
	// Author: TrungDD
	// Date: 22/11/2003
	////////////////////////////////////////////////////////
	void setNormalState()
	{
		txtThreadName.setEnabled(false);
		txtThreadClass.setEnabled(false);
		cboStartupType.setEnabled(false);
		tblContent.setEnabled(true);
		pnlNormal.setVisible(true);
		pnlActive.setVisible(false);
	}
	////////////////////////////////////////////////////////
	// Author: TrungDD
	// Date: 22/11/2003
	////////////////////////////////////////////////////////
	void setActiveState()
	{
		txtThreadName.requestFocus();
		txtThreadName.setEnabled(true);
		txtThreadClass.setEnabled(true);
		cboStartupType.setEnabled(true);
		tblContent.setEnabled(false);
		pnlNormal.setVisible(false);
		pnlActive.setVisible(true);
	}
	void updateTable(String strThreadName,String strClassName,String strStartupType)
	{
		int iSelectedRow = tblContent.getSelectedRow();
		Vector vtRow = (Vector)tblContent.getRow(iSelectedRow);
		vtRow.setElementAt(strThreadName,0);
		vtRow.setElementAt(strClassName,1);
		vtRow.setElementAt(strStartupType,2);
	}
	////////////////////////////////////////////////////////
	// Author: TrungDD
	// Date: 22/11/2003
	////////////////////////////////////////////////////////
	void doUpdate()
	{
		setActiveState();
	}
	////////////////////////////////////////////////////////
	// Author: TrungDD
	// Date: 22/11/2003
	////////////////////////////////////////////////////////
	public void onCancel()
	{
		setNormalState();
		super.onCancel();
	}
	////////////////////////////////////////////////////////
	// Author: TrungDD
	// Date: 22/11/2003
	////////////////////////////////////////////////////////
	void doCommit()
	{
		// validate data
		String strThreadName = txtThreadName.getText();
		if(strThreadName.length() == 0)
		{
			MessageBox.showMessageDialog(this,ErrorDictionary.getString("FSS-00002",mdic.getString("ThreadName")),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			txtThreadName.requestFocus();
			return;
		}
		String strClassName = txtThreadClass.getText();
		if(strClassName.length() == 0)
		{
			MessageBox.showMessageDialog(this,ErrorDictionary.getString("FSS-00002",mdic.getString("ThreadClass")),Global.APP_NAME,MessageBox.ERROR_MESSAGE);
			txtThreadClass.requestFocus();
			return;
		}
		String strStartupType = mvtComboStartupType.elementAt(cboStartupType.getSelectedIndex()).toString();

		// send data to server
		try
		{
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			request.setString("ThreadName",strThreadName);
			request.setString("ThreadClass",strClassName);
			request.setString("ThreadStartupType",strStartupType);
			channel.sendRequest("ThreadProcessor","manageThreadsStore",request);
			updateTable(strThreadName,strClassName,strStartupType);
			setNormalState();
		}
		catch(Exception e)
		{
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}
}

package com.fss.monitor;

import java.util.*;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;

import com.fss.ddtp.*;
import com.fss.util.*;
import com.fss.swing.*;
import com.fss.thread.ParameterType;
import com.fss.dictionary.*;
import com.fss.dictionary.Dictionary;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author
 * - Thai Hoang Hiep
 * - Dang Dinh Trung
 * @version 1.0
 */

public class DialogParamSetting extends JXDialog
{
	////////////////////////////////////////////////////////
	// Construct vector table to store row editor
	////////////////////////////////////////////////////////
	private VectorTable tblContent = new VectorTable(3)
	{
		////////////////////////////////////////////////////////
		public JComponent getCellEditorComponent(int iRowIndex,int iColIndex)
		{
			if(iColIndex == 1 && iRowIndex >= 0 && iRowIndex < this.getRowCount())
				return (JComponent)this.getRow(iRowIndex).elementAt(3);
			return null;
		}
		////////////////////////////////////////////////////////
		public JComponent getCellRendererComponent(int iRowIndex,int iColIndex)
		{
			return getCellEditorComponent(iRowIndex,iColIndex);
		}
		////////////////////////////////////////////////////////
		public void applyCellEditorComponent(JComponent cmp,int iRowIndex,int iColIndex)
		{
			cmp.setToolTipText((String)this.getRow(iRowIndex).elementAt(2));
			super.applyCellEditorComponent(cmp,iRowIndex,iColIndex);
		}
		////////////////////////////////////////////////////////
		public void applyCellRendererComponent(JComponent cmp,int iRowIndex,int iColIndex)
		{
			applyCellEditorComponent(cmp,iRowIndex,iColIndex);
			super.applyCellRendererComponent(cmp,iRowIndex,iColIndex);
		}
	};
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	private Dictionary mdic = null;
	private Vector mvtParameter = new Vector();
	private JTableContainer pnlParameter = new JTableContainer(tblContent);
	private String mstrThreadID;
	private JXText txtThreadName = new JXText();
	private JXText txtThreadClass = new JXText();
	private JComboBox cboStartupType = new JComboBox();
	private Vector mvtStartupType = new Vector();
	private JButton btnOK = new JButton();
	private JButton btnCancel = new JButton();
	private JLabel lblThreadName = new JLabel();
	private JLabel lblThreadClass = new JLabel();
	private JLabel lblStartupType = new JLabel();
	private SocketTransmitter channel;

	////////////////////////////////////////////////////////
	public DialogParamSetting(Component parent,
							  String strThreadID,String strThreadName,
							  String strThreadClass,Vector vtParameter,
							  SocketTransmitter channel) throws Exception
	{
		////////////////////////////////////////////////////////
		super(parent,true);
		this.channel = channel;
		mstrThreadID = strThreadID;
		mvtParameter = vtParameter;
		txtThreadName.setText(strThreadName);
		txtThreadClass.setText(strThreadClass);
		////////////////////////////////////////////////////////
		jbInit();
		////////////////////////////////////////////////////////
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setSize(480,360);
	}
	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		tblContent.addColumn("",0,false);
		tblContent.addColumn("",1,true);
		tblContent.setAllowInsert(false);
		tblContent.setAllowDelete(false);
		////////////////////////////////////////////////////////
		JPanel pnlButton = new JPanel(new GridLayout(1,2,4,4));
		pnlButton.add(btnOK);
		pnlButton.add(btnCancel);
		getRootPane().setDefaultButton(btnOK);
		tblContent.getColumnEx(0).setPreferredWidth(100);
		tblContent.getColumnEx(1).setPreferredWidth(200);
		////////////////////////////////////////////////////////
		JPanel pnlDetail = new JPanel(new GridBagLayout());
		pnlDetail.add(lblThreadName,new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlDetail.add(txtThreadName,new GridBagConstraints(1,0,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
		pnlDetail.add(lblThreadClass,new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlDetail.add(txtThreadClass,new GridBagConstraints(1,1,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
		pnlDetail.add(lblStartupType,new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
		pnlDetail.add(cboStartupType,new GridBagConstraints(1,2,1,1,1,0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
		cboStartupType.setPreferredSize(txtThreadName.getPreferredSize());
		////////////////////////////////////////////////////////
		Container pnlMain = this.getContentPane();
		pnlMain.setLayout(new GridBagLayout());
		pnlMain.add(pnlDetail,new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		pnlMain.add(pnlParameter,new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		pnlMain.add(pnlButton,new GridBagConstraints(0,2,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(4,2,2,2),0,0));
		////////////////////////////////////////////////////////
		updateLanguage();
		Skin.applySkin(this);
		fillControlValue();
		tblContent.backup();
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
		mdic = MonitorDictionary.getChildDictionary("ThreadManager");
		MonitorDictionary.applyButton(btnOK,"OK");
		MonitorDictionary.applyButton(btnCancel,"Cancel");
		lblThreadName.setText(mdic.getString("ThreadName"));
		lblThreadClass.setText(mdic.getString("ThreadClass"));
		lblStartupType.setText(mdic.getString("StartupType"));
		tblContent.setColumnNameEx(mdic.getString("Parameter"),0);
		tblContent.setColumnNameEx(mdic.getString("Value"),1);
		pnlParameter.setTitle(mdic.getString("ParameterList"));
		setTitle(mdic.getString("Title"));
	}
	////////////////////////////////////////////////////////
	public void setStartupTypeCombo(Vector vtStartupType,String defaultValue)
	{
		createCombo(cboStartupType,mvtStartupType,vtStartupType,defaultValue);
	}
	////////////////////////////////////////////////////////
	// Purpose:
	////////////////////////////////////////////////////////
	public static JComponent createEditor(Component cmp,
										  String strName,
										  String strType,
										  Object objDefinition,
										  boolean bApplySkin)
	{
		if(strType.equals(ParameterType.PARAM_TEXTBOX_MASK)) // Text field
		{
			JXText txtEditor = new JXText();
			if(bApplySkin)
				Skin.applySkin(txtEditor);
			String strMask = (String)objDefinition;
			if(strMask.length() > 0)
				txtEditor.setMask(strMask);
			txtEditor.setName(strName);
			return txtEditor;
		}
		else if(strType.equals(ParameterType.PARAM_TEXTBOX_MAX))
		{
			JXText txtEditor = new JXText();
			if(bApplySkin)
				Skin.applySkin(txtEditor);
			String strLength = (String)objDefinition;
			if(strLength.length() == 0)
			{
				if(strLength.length() > 0)
				{
					try
					{
						int iLength = Integer.parseInt(strLength);
						txtEditor.setMaxLength(iLength);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			txtEditor.setName(strName);
			return txtEditor;
		}
		else if(strType.equals(ParameterType.PARAM_TEXTBOX_FILTER)) // Text field
		{
			JXText txtEditor = new JXText();
			if(bApplySkin)
				Skin.applySkin(txtEditor);
			String strFilter = (String)objDefinition;
			if(strFilter.length() > 0)
				txtEditor.setFilter(strFilter);
			txtEditor.setName(strName);
			return txtEditor;
		}
		else if(strType.equals(ParameterType.PARAM_TEXTAREA_MAX))
		{
			JTextArea txtEditor = new JTextArea();
			if(bApplySkin)
				Skin.applySkin(txtEditor);
			String strLength = (String)objDefinition;
			if(strLength.length() == 0)
			{
				if(strLength.length() > 0)
				{
					try
					{
						int iLength = Integer.parseInt(strLength);
						txtEditor.setDocument(new LimitedDocument(iLength));
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			ExpandableTextEditorMouseAdapter lsn = new ExpandableTextEditorMouseAdapter();
			lsn.mstrParameterName = strName;
			lsn.mtxtMain = txtEditor;
			txtEditor.addMouseListener(lsn);
			txtEditor.setName(strName);
			return txtEditor;
		}
		else if(strType.equals(ParameterType.PARAM_PASSWORD)) // Password field
		{
			JXPassword txtEditor = new JXPassword();
			if(bApplySkin)
				Skin.applySkin(txtEditor);
			String strLength = (String)objDefinition;
			if(strLength.length() > 0)
			{
				try
				{
					int iLength = Integer.parseInt(strLength);
					txtEditor.setMaxLength(iLength);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			txtEditor.setName(strName);
			return txtEditor;
		}
		else if(strType.equals(ParameterType.PARAM_COMBOBOX)) // Combobox
		{
			JComboBox cboEditor = new JComboBox();
			Vector vtItem = (Vector)objDefinition;
			for(int iItemIndex = 0;iItemIndex < vtItem.size();iItemIndex++)
				cboEditor.addItem(vtItem.elementAt(iItemIndex));
			if(bApplySkin)
			{
				Skin.applySkin(cboEditor);
				((JButton)cboEditor.getComponent(0)).setBorder(BorderFactory.createLineBorder(Color.gray,1));
			}
			cboEditor.setName(strName);
			return cboEditor;
		}
		else if(strType.equals(ParameterType.PARAM_LABEL)) // Label
		{
			JXText txtEditor = new JXText();
			Skin.applySkin(txtEditor);
			txtEditor.setEditable(false);
			txtEditor.setName(strName);
			return txtEditor;
		}
		else if(strType.equals(ParameterType.PARAM_TABLE)) // Table Editor
			return new TableParameterEditor(cmp,true,MonitorDictionary.getString("DialogTableParameter.Title",strName),(Vector)objDefinition);
		else if(strType.equals(ParameterType.PARAM_SUB_TABULAR_EDITOR)) // Tabular Editor
			return new TabularParameterEditor(cmp,true,MonitorDictionary.getString("DialogTableParameter.Title",strName),(Vector)objDefinition);
		return null;
	}
	////////////////////////////////////////////////////////
	public void fillControlValue() throws Exception
	{
		for(int iParameterIndex = 0;iParameterIndex < mvtParameter.size();iParameterIndex++)
		{
			Vector vtParameterRow = (Vector)mvtParameter.elementAt(iParameterIndex);
			String strName = (String)vtParameterRow.elementAt(0);
			String strType = (String)vtParameterRow.elementAt(2);
			Object objDefinition = vtParameterRow.elementAt(3);
			vtParameterRow.removeElementAt(3);
			vtParameterRow.removeElementAt(2);
			while(vtParameterRow.size() > 3)
				vtParameterRow.removeElementAt(vtParameterRow.size() - 1);
			while(vtParameterRow.size() < 3)
				vtParameterRow.addElement("");
			vtParameterRow.addElement(createEditor(tblContent,strName,strType,objDefinition,true));
		}
		tblContent.setData(mvtParameter);
	}
	////////////////////////////////////////////////////////
	// Event handling
	////////////////////////////////////////////////////////
	public void onOK()
	{
		try
		{
			// Validate input
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
			String strStartupType = mvtStartupType.elementAt(cboStartupType.getSelectedIndex()).toString();

			// Fill setting value
			Vector vtSetting = new Vector();
			if(tblContent.isChanged())
			{
				for(int iIndex = 0;iIndex < mvtParameter.size();iIndex++)
				{
					Vector vtSettingRow = new Vector();
					Vector mvtParameterRow = (Vector)mvtParameter.elementAt(iIndex);
					vtSettingRow.addElement(mvtParameterRow.elementAt(0));
					vtSettingRow.addElement(mvtParameterRow.elementAt(1));
					vtSetting.addElement(vtSettingRow);
				}
			}

			// Send command
			DDTP request = new DDTP();
			request.setRequestID(String.valueOf(System.currentTimeMillis()));
			request.setString("ThreadID",mstrThreadID);
			request.setString("ThreadName",strThreadName);
			request.setString("ThreadClass",strClassName);
			request.setString("ThreadStartupType",strStartupType);
			if(tblContent.isChanged())
				request.setVector("vtSetting",vtSetting);
			channel.sendRequest("ThreadProcessor","storeSetting",request);
			dispose();
		}
		catch(Exception e)
		{
			if(e instanceof AppException)
				editParameter(((AppException)e).getInfo());
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}
	////////////////////////////////////////////////////////
	public void onCancel()
	{
		if(tblContent.isChanged())
		{
			int iResult = MessageBox.showConfirmDialog(this,
				MonitorDictionary.getString("Confirm.SaveOnExit"),Global.APP_NAME,MessageBox.YES_NO_CANCEL_OPTION);
			if(iResult == MessageBox.CANCEL_OPTION)
				return;
			if(iResult == MessageBox.YES_OPTION)
				onOK();
			else
				dispose();
		}
		else
			dispose();
	}
	////////////////////////////////////////////////////////
	// Date: 06/11/2003
	////////////////////////////////////////////////////////
	private void editParameter(String strParamName)
	{
		if(strParamName == null)
			return;
		for(int i=0; i<mvtParameter.size();i++)
		{
			if (strParamName.startsWith(StringUtil.nvl(((Vector)mvtParameter.elementAt(i)).elementAt(0).toString(),"")))
				tblContent.editCellAtEx(i,1);
		}
	}
	////////////////////////////////////////////////////////
	// Date: 15/11/2003
	////////////////////////////////////////////////////////
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
			cboBox.setSelectedIndex(vtCombo.indexOf(strSeletectedValue));
			if(cboBox.getSelectedIndex() < 0 && cboBox.getItemCount() > 0)
				cboBox.setSelectedIndex(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
////////////////////////////////////////////////////////
class ExpandableTextEditorMouseAdapter extends MouseAdapter
{
	public String mstrParameterName;
	public JTextComponent mtxtMain;
	private int miReturn = JOptionPane.CANCEL_OPTION;
	private JXDialog dlg;
	public void mouseClicked(MouseEvent e)
	{
		if(e.getClickCount() > 1)
		{
			// Init variable
			dlg = new JXDialog(mtxtMain,true);
			dlg.setTitle(mstrParameterName);
			String strOrigin = mtxtMain.getText();
			JTextArea txt = new JTextArea();
			txt.setDocument(mtxtMain.getDocument());
			txt.setText(mtxtMain.getText());

			// Init UI
			JButton btnOK = new JButton();
			JButton btnCancel = new JButton();
			JPanel pnlButton = new JPanel(new GridLayout(1,2,4,4));
			pnlButton.add(btnOK);
			pnlButton.add(btnCancel);
			Container pnlMain = dlg.getContentPane();
			pnlMain.setLayout(new GridBagLayout());
			pnlMain.add(new JScrollPane(txt),new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
			pnlMain.add(pnlButton,new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
			txt.setAutoscrolls(true);
			txt.setLineWrap(true);
			txt.setWrapStyleWord(true);

			// Format dialog
			dlg.setSize(440,330);
			dlg.setDefaultCloseOperation(dlg.DISPOSE_ON_CLOSE);
			Skin.applySkin(dlg);
			DefaultDictionary.applyButton(btnOK,"OK");
			DefaultDictionary.applyButton(btnCancel,"Cancel");

			// Handle event
			btnOK.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					miReturn = JOptionPane.OK_OPTION;
					dlg.dispose();
				}
			});
			btnCancel.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					miReturn = JOptionPane.CANCEL_OPTION;
					dlg.dispose();
				}
			});

			// Show dialog
			WindowManager.centeredWindow(dlg);
			if(miReturn != JOptionPane.OK_OPTION)
				mtxtMain.setText(strOrigin);
		}
	}
}

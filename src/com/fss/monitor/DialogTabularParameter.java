package com.fss.monitor;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.event.*;

import com.fss.swing.*;
import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author HungHM
 * @version 1.0
 */

public class DialogTabularParameter extends JXDialog
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	private Vector mvtValue;
	private Vector mvtDefinition;
	private Vector mvtEditor;

	////////////////////////////////////////////////////////
	// Construct vector table to store row editor
	////////////////////////////////////////////////////////
	private VectorTable tblContent = new VectorTable(3)
	{
		////////////////////////////////////////////////////////
		public JComponent getCellEditorComponent(int iRowIndex,int iColIndex)
		{
			if(iColIndex == 1 && iRowIndex >= 0 && iRowIndex < this.getRowCount())
			{
				return(JComponent)this.getRow(iRowIndex).elementAt(3);
			}
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
	private Vector mvtParameter = new Vector();
	private JTableContainer pnlParameter = new JTableContainer(tblContent);
	private JButton btnOK = new JButton();
	private JButton btnCancel = new JButton();

	////////////////////////////////////////////////////////
	// Contructor
	///////////////////////////////////////////////////////
	public DialogTabularParameter(Component cmpParent,boolean bModal,String strTitle,Vector vtDefinition,Vector vtValue) throws Exception
	{
		super(cmpParent,bModal);
		setTitle(strTitle);
		mvtValue = vtValue;
		if(mvtValue == null)
		{
			mvtValue = new Vector();
		}
		mvtDefinition = vtDefinition;
		jbInit();
		////////////////////////////////////////////////////////
		this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		this.setSize(480,360);
	}

	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
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
		Container pnlMain = this.getContentPane();
		pnlMain.setLayout(new GridBagLayout());
		pnlMain.add(pnlParameter,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		pnlMain.add(pnlButton,new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(4,2,2,2),0,0));
		////////////////////////////////////////////////////////
		updateLanguage();
		Skin.applySkin(this);
		////////////////////////////////////////////////////////
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
	// Member function
	////////////////////////////////////////////////////////
	public Vector getValue()
	{
		return mvtValue;
	}

	private void onOK()
	{
		try
		{
			// Validate input

			// Set Value
			Vector vtNewParameter = tblContent.getData();
			if(mvtValue.size() == 0)
			{
				Vector vtChildValue = new Vector(vtNewParameter.size());
				mvtValue.add(vtChildValue);
			}

			for(int iParameterIndex = 0;iParameterIndex < vtNewParameter.size();iParameterIndex++)
			{
				Vector vtParameterRow = (Vector)((Vector)vtNewParameter.elementAt(iParameterIndex)).clone();
				if(iParameterIndex < (((Vector)mvtValue.elementAt(0)).size()))
				{
					((Vector)mvtValue.elementAt(0)).setElementAt(vtParameterRow.elementAt(1),iParameterIndex);
				}
				else
				{
					((Vector)mvtValue.elementAt(0)).addElement(vtParameterRow.elementAt(1));
				}

			}
			dispose();
		}
		catch(Exception e)
		{
			if(e instanceof AppException)
			{
				editParameter(((AppException)e).getInfo());
			}
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}

	}

	////////////////////////////////////////////////////////
	private void fillControlValue()
	{
		// mvtDefinition
		// mvtValue

		for(int iDefinitionIndex = 0;iDefinitionIndex < mvtDefinition.size();iDefinitionIndex++)
		{
			Vector vtParameterRow = (Vector)((Vector)mvtDefinition.elementAt(iDefinitionIndex)).clone();
			if(mvtValue.size() > 0)
			{
				if(iDefinitionIndex < ((Vector)mvtValue.elementAt(0)).size())
				{
					Object objValue = ((Vector)mvtValue.elementAt(0)).elementAt(iDefinitionIndex);
					vtParameterRow.setElementAt(objValue,1);
				}
			}

			String strName = (String)vtParameterRow.elementAt(0);
			String strType = (String)vtParameterRow.elementAt(2);
			Object objDefinition = vtParameterRow.elementAt(3);
			vtParameterRow.removeElementAt(3);
			vtParameterRow.removeElementAt(2);
			while(vtParameterRow.size() > 3)
			{
				vtParameterRow.removeElementAt(vtParameterRow.size() - 1);
			}
			while(vtParameterRow.size() < 3)
			{
				vtParameterRow.addElement("");
			}
			vtParameterRow.addElement(com.fss.monitor.DialogParamSetting.createEditor(tblContent,strName,strType,objDefinition,true));
			mvtParameter.add(vtParameterRow);
		}
		tblContent.setData(mvtParameter);
	}

	////////////////////////////////////////////////////////
	// Implementation
	////////////////////////////////////////////////////////
	public void onCancel()
	{
		super.onCancel();
	}

	////////////////////////////////////////////////////////
	public void exit()
	{
		dispose();
	}

	////////////////////////////////////////////////////////
	public void updateLanguage()
	{
		MonitorDictionary.applyButton(btnOK,"OK");
		MonitorDictionary.applyButton(btnCancel,"Cancel");
	}

	////////////////////////////////////////////////////////
	private void editParameter(String strParamName)
	{
		if(strParamName == null)
		{
			return;
		}
		for(int i = 0;i < mvtParameter.size();i++)
		{
			if(strParamName.startsWith(StringUtil.nvl(((Vector)mvtParameter.elementAt(i)).elementAt(0).toString(),"")))
			{
				tblContent.editCellAtEx(i,1);
			}
		}
	}

}

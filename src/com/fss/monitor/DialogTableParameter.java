package com.fss.monitor;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.event.*;

import com.fss.swing.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class DialogTableParameter extends JXDialog implements ControlButtonListener
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	private VectorTable tblParamterList;
	private JTableContainer pnlParamterList;
	private PanelControlButton pnlButton = new PanelControlButton(this);
	////////////////////////////////////////////////////////
	private Vector mvtValue;
	private Vector mvtDefinition;
	private Vector mvtEditor;
	////////////////////////////////////////////////////////
	public DialogTableParameter(Component cmpParent,boolean bModal,String strTitle,Vector vtDefinition,Vector vtValue) throws Exception
	{
		super(cmpParent,bModal);
		setTitle(strTitle);
		mvtValue = vtValue;
		if(mvtValue == null)
			mvtValue = new Vector();
		mvtDefinition = vtDefinition;
		int iRowSize = 0;
		for(int iIndex = 0;iIndex < mvtDefinition.size();iIndex++)
		{
			Vector vtDefinitionRow = (Vector)mvtDefinition.elementAt(iIndex);
			int iValue = Integer.parseInt((String)vtDefinitionRow.elementAt(5)) + 1;
			if(iRowSize < iValue)
				iRowSize = iValue;
		}
		tblParamterList = new VectorTable(iRowSize);
		pnlParamterList = new JTableContainer(tblParamterList);
		////////////////////////////////////////////////////////
		jbInit();
		////////////////////////////////////////////////////////
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		int iSize = vtDefinition.size();
		if(iSize > 15)
			iSize = 15;
		if(iSize < 4)
			iSize = 4;
		this.setSize(iSize * 32 + 320,iSize * 24 + 240);
		////////////////////////////////////////////////////////
		// Init value
		////////////////////////////////////////////////////////
		pnlButton.setAllowSearch(false);
		onChangeAction(ACTION_NONE,ACTION_NONE);
		pnlButton.setNormalState();
		tblParamterList.setData(mvtValue);
	}
	////////////////////////////////////////////////////////
	private void jbInit() throws Exception
	{
		////////////////////////////////////////////////////////
		mvtEditor = new Vector();
		JPanel pnlInput = new JPanel(new GridBagLayout());
		int iAdditionIndex = 0;
		boolean bHaveBigComponent = false;
		for(int iIndex = 0;iIndex < mvtDefinition.size();iIndex++)
		{
			Vector vtDefinitionRow = (Vector)mvtDefinition.elementAt(iIndex);
			String strName = (String)vtDefinitionRow.elementAt(0);
			String strType = (String)vtDefinitionRow.elementAt(2);
			Object objDefinition = vtDefinitionRow.elementAt(3);
			String strIndex = (String)vtDefinitionRow.elementAt(5);
			JComponent cmp = com.fss.monitor.DialogParamSetting.createEditor(this,strName,strType,objDefinition,false);
			mvtEditor.addElement(cmp);
			int iX = (iAdditionIndex + iIndex) % 2;
			int iY = (iAdditionIndex + iIndex) / 2;
			if(cmp instanceof JTextArea ||
			   cmp instanceof JEditorPane ||
			   cmp instanceof JTree ||
			   cmp instanceof JTable)
			{
				bHaveBigComponent = true;
				iAdditionIndex++;
				if(iIndex % 2 != 0)
				{
					iAdditionIndex++;
					iY++;
				}
				pnlInput.add(new JLabel(strName),new GridBagConstraints(0,iY,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
				pnlInput.add(new JScrollPane(cmp),
							 new GridBagConstraints(1,iY,3,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,
					new Insets(2,2,2,2),0,0));
			}
			else
			{
				pnlInput.add(new JLabel(strName),new GridBagConstraints(iX * 2,iY,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
				pnlInput.add(cmp,
							 new GridBagConstraints(iX * 2 + 1,iY,1,1,1.0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,
					new Insets(2,2,2,2),0,0));
			}
			tblParamterList.addColumn(strName,Integer.parseInt(strIndex),false);
		}
		if(mvtDefinition.size() > 8)
			tblParamterList.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblParamterList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		////////////////////////////////////////////////////////
		Container pnlMain = this.getContentPane();
		pnlMain.setLayout(new GridBagLayout());
		if(bHaveBigComponent)
			pnlMain.add(pnlInput,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		else
			pnlMain.add(pnlInput,new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		pnlMain.add(pnlButton,new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(4,2,4,2),0,0));
		pnlMain.add(pnlParamterList,new GridBagConstraints(0,2,1,1,1.0,1.38,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(2,2,2,2),0,0));
		////////////////////////////////////////////////////////
		updateLanguage();
		Skin.applySkin(this);
		////////////////////////////////////////////////////////
		tblParamterList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if(e.getClickCount() > 1)
					pnlButton.btnModify.doClick();
			}
		});
		////////////////////////////////////////////////////////
		tblParamterList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if(e.getValueIsAdjusting())
					return;
				fillDetailValue();
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
	////////////////////////////////////////////////////////
	private void fillDetailValue()
	{
		int iSelected = tblParamterList.getSelectedRow();
		if(iSelected < 0)
		{
			clearDetailValue();
			return;
		}
		Vector vtRow = tblParamterList.getRow(iSelected);
		for(int iIndex = 0;iIndex < mvtDefinition.size();iIndex++)
		{
			Vector vtDefinitionRow = (Vector)mvtDefinition.elementAt(iIndex);
			String strIndex = (String)vtDefinitionRow.elementAt(5);
			JComponent cmp = (JComponent)mvtEditor.elementAt(iIndex);
			Object objValue = vtRow.elementAt(Integer.parseInt(strIndex));
			if(objValue == null)
				objValue = "";
			if(cmp instanceof JToggleButton)
				((JToggleButton)cmp).setSelected(Boolean.getBoolean(objValue.toString()));
			else if(cmp instanceof JTextComponent)
				((JTextComponent)cmp).setText(objValue.toString());
			else if(cmp instanceof JComboBox)
				((JComboBox)cmp).setSelectedItem(objValue);
		}
	}
	////////////////////////////////////////////////////////
	private void clearDetailValue()
	{
		for(int iIndex = 0;iIndex < mvtDefinition.size();iIndex++)
		{
			JComponent cmp = (JComponent)mvtEditor.elementAt(iIndex);
			if(cmp instanceof JToggleButton)
				((JToggleButton)cmp).setSelected(false);
			else if(cmp instanceof JTextComponent)
				((JTextComponent)cmp).setText("");
			else if(cmp instanceof JComboBox)
				((JComboBox)cmp).setSelectedIndex(0);
		}
	}
	////////////////////////////////////////////////////////
	private void fillDefaultValue()
	{
		for(int iIndex = 0;iIndex < mvtDefinition.size();iIndex++)
		{
			JComponent cmp = (JComponent)mvtEditor.elementAt(iIndex);
			if(cmp instanceof JToggleButton)
				((JToggleButton)cmp).setSelected(false);
			else if(cmp instanceof JTextComponent)
				((JTextComponent)cmp).setText("");
			else if(cmp instanceof JComboBox)
				((JComboBox)cmp).setSelectedIndex(0);
		}
	}
	////////////////////////////////////////////////////////
	// Implementation
	////////////////////////////////////////////////////////
	public boolean validateInput(int iOldAction,int iNewAction)
	{
		if(iOldAction == ACTION_NONE && (iNewAction == ACTION_MODIFY || iNewAction == ACTION_REMOVE))
		{
			if(tblParamterList.getSelectedRow() < 0)
				return false;
		}
		return true;
	}
	////////////////////////////////////////////////////////
	public void onChangeAction(int iOldAction,int iNewAction)
	{
		if(iNewAction == ACTION_NONE)
		{
			// Set control state
			for(int iIndex = 0;iIndex < mvtEditor.size();iIndex++)
				((JComponent)mvtEditor.elementAt(iIndex)).setEnabled(false);
			tblParamterList.setEnabled(true);

			// Default focus
			tblParamterList.requestFocus();

			// Fill detail value
			fillDetailValue();
		}
		else if(iNewAction == ACTION_ADD || iNewAction == ACTION_MODIFY
				|| iNewAction == ACTION_SEARCH || iNewAction == ACTION_ADD_COPY)
		{
			for(int iIndex = 0;iIndex < mvtEditor.size();iIndex++)
			{
				// Set control state
				((JComponent)mvtEditor.elementAt(iIndex)).setEnabled(true);

				// Default focus
				if(iIndex == 0)
					((JComponent)mvtEditor.elementAt(iIndex)).requestFocus();
			}
			tblParamterList.setEnabled(false);

			if(iNewAction == ACTION_ADD)
				fillDefaultValue();
			else if(iNewAction == ACTION_SEARCH)
				clearDetailValue();
			else if (iNewAction == ACTION_ADD_COPY)
				fillDetailValue();
		}
	}
	////////////////////////////////////////////////////////
	public void backup()
	{
		for(int iIndex = 0;iIndex < mvtDefinition.size();iIndex++)
		{
			if(mvtEditor.elementAt(iIndex) instanceof TrackChangeListener)
				((TrackChangeListener)mvtEditor.elementAt(iIndex)).backup();
		}
	}
	////////////////////////////////////////////////////////
	public void restore()
	{
		for(int iIndex = 0;iIndex < mvtDefinition.size();iIndex++)
		{
			if(mvtEditor.elementAt(iIndex) instanceof TrackChangeListener)
				((TrackChangeListener)mvtEditor.elementAt(iIndex)).restore();
		}
	}
	////////////////////////////////////////////////////////
	public boolean isRestorable()
	{
		for(int iIndex = 0;iIndex < mvtDefinition.size();iIndex++)
		{
			if(mvtEditor.elementAt(iIndex) instanceof TrackChangeListener)
			{
				if(((TrackChangeListener)mvtEditor.elementAt(iIndex)).isChanged())
					return true;
			}
		}
		return false;
	}
	////////////////////////////////////////////////////////
	public void clearBackup()
	{
		for(int iIndex = 0;iIndex < mvtDefinition.size();iIndex++)
		{
			if(mvtEditor.elementAt(iIndex) instanceof TrackChangeListener)
				((TrackChangeListener)mvtEditor.elementAt(iIndex)).clearBackup();
		}
	}
	////////////////////////////////////////////////////////
	public boolean add()
	{
		Vector vtParameterRow = new Vector();
		for(int iIndex = 0;iIndex < tblParamterList.getRowSize();iIndex++)
			vtParameterRow.addElement("");
		for(int iIndex = 0;iIndex < mvtDefinition.size();iIndex++)
		{
			Vector vtDefinitionRow = (Vector)mvtDefinition.elementAt(iIndex);
			String strIndex = (String)vtDefinitionRow.elementAt(5);
			JComponent cmp = (JComponent)mvtEditor.elementAt(iIndex);
			Object objValue = null;
			if(cmp instanceof JToggleButton)
				objValue = String.valueOf(((JToggleButton)cmp).isSelected());
			else if(cmp instanceof JTextComponent)
				objValue = ((JTextComponent)cmp).getText();
			else if(cmp instanceof JComboBox)
				objValue = ((JComboBox)cmp).getSelectedItem();
			vtParameterRow.setElementAt(objValue,Integer.parseInt(strIndex));
		}
		tblParamterList.addRow(vtParameterRow);
		if(tblParamterList.getRowCount() > 0)
		{
			int iSelected = tblParamterList.getRowCount() - 1;
			tblParamterList.changeSelectedRow(iSelected);
		}
		return true;
	}
	////////////////////////////////////////////////////////
	public boolean modify()
	{
		int iSelected = tblParamterList.getSelectedRow();
		if(iSelected < 0)
			return false;
		Vector vtParameterRow = tblParamterList.getRow(iSelected);
		for(int iIndex = 0;iIndex < mvtDefinition.size();iIndex++)
		{
			Vector vtDefinitionRow = (Vector)mvtDefinition.elementAt(iIndex);
			String strIndex = (String)vtDefinitionRow.elementAt(5);
			JComponent cmp = (JComponent)mvtEditor.elementAt(iIndex);
			Object objValue = null;
			if(cmp instanceof JToggleButton)
				objValue = String.valueOf(((JToggleButton)cmp).isSelected());
			else if(cmp instanceof JTextComponent)
				objValue = ((JTextComponent)cmp).getText();
			else if(cmp instanceof JComboBox)
				objValue = ((JComboBox)cmp).getSelectedItem();
			vtParameterRow.setElementAt(objValue,Integer.parseInt(strIndex));
		}
		tblParamterList.setRow(iSelected,vtParameterRow);
		if(tblParamterList.getRowCount() > 0)
		{
			if(iSelected < 0 || iSelected >= tblParamterList.getRowCount())
				iSelected = 0;
			tblParamterList.changeSelectedRow(iSelected);
		}
		return true;
	}
	////////////////////////////////////////////////////////
	public boolean remove()
	{
		int[] iSelected = tblParamterList.getSelectedRows();
		if(iSelected == null || iSelected.length < 0)
			return false;
		Arrays.sort(iSelected);
		for(int iIndex = iSelected.length - 1;iIndex >= 0;iIndex--)
			tblParamterList.deleteRow(iSelected[iIndex]);

		if(tblParamterList.getRowCount() > 0)
		{
			if(iSelected[0] < 0 || iSelected[0] >= tblParamterList.getRowCount())
				iSelected[0] = tblParamterList.getRowCount() - 1;
			tblParamterList.changeSelectedRow(iSelected[0]);
		}
		return true;
	}
	////////////////////////////////////////////////////////
	public boolean search()
	{
		return true;
	}
	////////////////////////////////////////////////////////
	public boolean print()
	{
		return true;
	}
	////////////////////////////////////////////////////////
	public void onCancel()
	{
		if(pnlButton.exit())
			super.onCancel();
	}
	////////////////////////////////////////////////////////
	public void exit()
	{
		dispose();
	}
	////////////////////////////////////////////////////////
	public String getPermission()
	{
		return "SIUD";
	}
}

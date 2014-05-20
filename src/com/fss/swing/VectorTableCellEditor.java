package com.fss.swing;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.table.*;
import javax.swing.border.*;

import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

class VectorTableCellEditor extends AbstractCellEditor implements TableCellEditor,ActionListener
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected VectorTableColumn mcol;
	protected JComponent mcmpCurrentEditor;
	protected KeyListener lsnEditKey = new KeyAdapter()
	{
		public void keyPressed(KeyEvent e)
		{
			char chrKeyChar = e.getKeyChar();
			if(chrKeyChar == KeyEvent.VK_ENTER)
				onKeyEnterPressed(e);
			else if(chrKeyChar == KeyEvent.VK_TAB)
				onKeyTabPressed(e);
		}
	};
	////////////////////////////////////////////////////////
	protected void onKeyEnterPressed(KeyEvent e)
	{
		// Stop editing first
		stopCellEditing();

		// Edit next column
		mcol.getParentTable().editNextColumn();

		// Consume event
		e.consume();
	}
	////////////////////////////////////////////////////////
	protected void onKeyTabPressed(KeyEvent e)
	{
		// Stop editing first
		stopCellEditing();
		mcol.getParentTable().dispatchEvent(e);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param col VectorTableColumn
	 */
	////////////////////////////////////////////////////////
	public VectorTableCellEditor(VectorTableColumn col)
	{
		mcol = col;
	}
	////////////////////////////////////////////////////////
	// Implements
	////////////////////////////////////////////////////////
	public Object getCellEditorValue()
	{
		if(mcmpCurrentEditor instanceof ControlableValue)
			return ((ControlableValue)mcmpCurrentEditor).getValue();
		else if(mcmpCurrentEditor instanceof JTextComponent)
			return ((JTextComponent)mcmpCurrentEditor).getText();
		else if(mcmpCurrentEditor instanceof JToggleButton)
		{
			if(((JToggleButton)mcmpCurrentEditor).isSelected())
				return "TRUE";
			return "FALSE";
		}
		else if(mcmpCurrentEditor instanceof JComboBox)
		{
			int iSelected = ((JComboBox)mcmpCurrentEditor).getSelectedIndex();
			if(iSelected < 0)
				return "";
			if(mcol.getValueMap() == null)
			{
				if(mcmpCurrentEditor instanceof JXCombo)
				{
					Object obj = ((JXCombo)mcmpCurrentEditor).getSelectedValue();
					if(obj != null)
						return StringUtil.nvl(obj,"");
				}
				return StringUtil.nvl(((JComboBox)mcmpCurrentEditor).getSelectedItem(),"");
			}
			else
				return StringUtil.nvl(mcol.getValueMap().elementAt(iSelected),"");
		}
		return "";
	}
	////////////////////////////////////////////////////////
	protected static JComponent prepareComponent(VectorTableColumn col,JTable table,Object value,boolean isSelected,int row,int column)
	{
		JComponent cmp = null;
		if(((VectorTable)table).isAllowInsert() && row >= table.getRowCount() - 1)
			((VectorTable)table).addRow();

		// If special cell editor is available -> using it
		if((cmp = ((VectorTable)table).getCellEditorComponent(row,column)) == null)
			cmp = col.getEditorComponent();

		// Check enabled
		if(!cmp.isEnabled())
			return null;

		// Set display value
		if(cmp instanceof ControlableValue)
			((ControlableValue)cmp).setValue(value);
		else
		{
			if(value == null)
				value = "";
			else if(col.getFormat() != null && !(value instanceof String))
				value = col.getFormat().format(value);
			if(cmp instanceof JTextComponent)
				((JTextComponent)cmp).setText(value.toString());
			else if(cmp instanceof JToggleButton)
			{
				try
				{
					int iValue = Integer.parseInt(value.toString());
					if(iValue == 0)
						value = "FALSE";
					else
						value = "TRUE";
				}
				catch(Exception e)
				{
				}

				((JToggleButton)cmp).setSelected(Boolean.valueOf(value.toString().trim()).booleanValue());
				((JToggleButton)cmp).setHorizontalAlignment(JLabel.CENTER);
			}
			else if(cmp instanceof JComboBox)
			{
				Vector vctKey = col.getValueMap();
				if(vctKey != null)
				{
					int iSelected = vctKey.indexOf(value);
					if(iSelected >= 0 && iSelected < ((JComboBox)cmp).getItemCount())
						((JComboBox)cmp).setSelectedIndex(iSelected);
				}
				else if(cmp instanceof JXCombo)
				{
					if(!((JXCombo)cmp).setSelectedValue(value))
						((JComboBox)cmp).setSelectedItem(value);
				}
				else
					((JComboBox)cmp).setSelectedItem(value);
			}
		}
		return cmp;
	}
	////////////////////////////////////////////////////////
	public Component getTableCellEditorComponent(JTable table,Object value,boolean isSelected,int row,int column)
	{
		mcmpCurrentEditor = prepareComponent(mcol,table,value,isSelected,row,column);
		if(mcmpCurrentEditor instanceof JTextField)
		{
			((JTextField)mcmpCurrentEditor).removeActionListener(this);
			((JTextField)mcmpCurrentEditor).addActionListener(this);
		}
		else if(mcmpCurrentEditor instanceof JPasswordField)
		{
			((JPasswordField)mcmpCurrentEditor).removeActionListener(this);
			((JPasswordField)mcmpCurrentEditor).addActionListener(this);
		}
		else if(mcmpCurrentEditor instanceof AbstractButton)
		{
			((AbstractButton)mcmpCurrentEditor).removeActionListener(this);
			((AbstractButton)mcmpCurrentEditor).addActionListener(this);
		}

		// Key, action event
		mcmpCurrentEditor.removeKeyListener(lsnEditKey);
		mcmpCurrentEditor.addKeyListener(lsnEditKey);

		// Appearance
		mcmpCurrentEditor.setFont(table.getFont());
		mcmpCurrentEditor.setForeground(table.getForeground());
		mcmpCurrentEditor.setBackground(VectorTableCellRenderer.FOCUSED_COLOR);
		if(mcmpCurrentEditor instanceof JTextComponent)
			mcmpCurrentEditor.setBorder(new EmptyBorder(0,2,0,2));
		else
			mcmpCurrentEditor.setBorder(new EmptyBorder(0,0,0,0));
		((VectorTable)table).applyCellRendererComponent(mcmpCurrentEditor,row,column);
		return mcmpCurrentEditor;
	}
	////////////////////////////////////////////////////////
	// When an action is performed, editing is ended.
	////////////////////////////////////////////////////////
	public void actionPerformed(ActionEvent e)
	{
		fireEditingStopped();
	}
}

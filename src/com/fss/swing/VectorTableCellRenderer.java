package com.fss.swing;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class VectorTableCellRenderer implements TableCellRenderer
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	public static Color EVEN_COLOR = new Color(255,255,255);
	public static Color ODD_COLOR = new Color(220,245,245);
	public static Color SELECTED_COLOR = new Color(245,220,245);
	public static Color FOCUSED_COLOR = new Color(220,220,245);
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected VectorTableColumn mcol;
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param col VectorTableColumn
	 */
	////////////////////////////////////////////////////////
	public VectorTableCellRenderer(VectorTableColumn col)
	{
		mcol = col;
	}
	////////////////////////////////////////////////////////
	// Implements
	////////////////////////////////////////////////////////
	protected JComponent prepareComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column)
	{
		// Set display value
		if(value != null && !value.equals("") && mcol.getFormat() != null)
		{
			Object obj = value;
			if(value instanceof String)
			{
				try
				{
					obj = mcol.getFormat().parseObject((String)value);
					value = mcol.getFormat().format(obj);
				}
				catch(Exception e)
				{
				}
			}
		}

		// If special cell editor is available -> using it
		JComponent cmp;
		if((cmp = ((VectorTable)table).getCellRendererComponent(row,column)) == null)
			cmp = mcol.getEditorComponent();

		if(cmp instanceof ControlableValue)
		{
			((ControlableValue)cmp).setValue(value);
			value = ((ControlableValue)cmp).getDisplayValue();
		}
		else
		{
			if(value == null)
				value = "";
			if(cmp instanceof JComboBox)
			{
				Vector vctKey = mcol.getValueMap();
				if(vctKey != null)
				{
					int iSelected = vctKey.indexOf(value);
					if(iSelected >= 0 && iSelected < ((JComboBox)cmp).getItemCount())
						value = ((JComboBox)cmp).getItemAt(iSelected).toString();
				}
				else if(cmp instanceof JXCombo)
				{
					Object obj = ((JXCombo)cmp).getItem(value);
					if(obj != null)
						value = obj;
				}
			}
		}

		JComponent cmpRenderer = null;
		if(cmp instanceof JToggleButton)
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

			cmpRenderer = new JCheckBox();
			((JToggleButton)cmpRenderer).setSelected(Boolean.valueOf(value.toString()).booleanValue());
			((JToggleButton)cmpRenderer).setHorizontalAlignment(JLabel.CENTER);
		}
		else if (cmp instanceof JPasswordField)
		{
			cmpRenderer = new JPasswordField();
			((JPasswordField)cmpRenderer).setHorizontalAlignment(mcol.getAlignment());
			((JPasswordField)cmpRenderer).setText(value.toString());
		}
		else
		{
			cmpRenderer = new JLabel();
			((JLabel)cmpRenderer).setHorizontalAlignment(mcol.getAlignment());
			((JLabel)cmpRenderer).setText(value.toString());
		}
		return cmpRenderer;
	}
	////////////////////////////////////////////////////////
	public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column)
	{
		JComponent cmpRenderer = prepareComponent(table,value,isSelected,hasFocus,row,column);
		cmpRenderer.setFont(table.getFont());
		cmpRenderer.setForeground(table.getForeground());
		if(hasFocus)
			cmpRenderer.setBackground(FOCUSED_COLOR);
		else if(isSelected)
			cmpRenderer.setBackground(SELECTED_COLOR);
		else
		{
			if(row % 2 == 0)
				cmpRenderer.setBackground(EVEN_COLOR);
			else
				cmpRenderer.setBackground(ODD_COLOR);
		}
		cmpRenderer.setOpaque(true);
		if(cmpRenderer instanceof JLabel || cmpRenderer instanceof JPasswordField)
			cmpRenderer.setBorder(new EmptyBorder(0,2,0,2));
		else
			cmpRenderer.setBorder(null);
		((VectorTable)table).applyCellRendererComponent(cmpRenderer,row,column);
		return cmpRenderer;
	}
}

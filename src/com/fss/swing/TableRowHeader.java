 package com.fss.swing;

import javax.swing.JTable;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */
public class TableRowHeader extends JTable
{
	////////////////////////////////////////////////////////
	// Nested class
	////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////
	// VectorModel
	// Stored table data
	////////////////////////////////////////////////////////
	private class RowHeaderModel extends AbstractTableModel
	{
		////////////////////////////////////////////////////////
		private TableModel parentModel;
		////////////////////////////////////////////////////////
		public RowHeaderModel(TableModel model)
		{
			parentModel = model;
		}
		////////////////////////////////////////////////////////
		public int getRowCount()
		{
			return parentModel.getRowCount();
		}
		////////////////////////////////////////////////////////
		public int getColumnCount()
		{
			return parentModel.getColumnCount();
		}
		////////////////////////////////////////////////////////
		public Class getColumnClass(int iColIndex)
		{
			return parentModel.getColumnClass(iColIndex);
		}
		////////////////////////////////////////////////////////
		public String getColumnName(int iColIndex)
		{
			return "";
		}
		////////////////////////////////////////////////////////
		public boolean isCellEditable(int iRowIndex,int iColIndex)
		{
			return false;
		}
		////////////////////////////////////////////////////////
		public Object getValueAt(int iRowIndex,int iColIndex)
		{
			return parentModel.getValueAt(iRowIndex,iColIndex);
		}
		////////////////////////////////////////////////////////
		public void setValueAt(Object objValue,int iRowIndex,int iColIndex)
		{
		}
	}
	////////////////////////////////////////////////////////
	private class HeaderCellRenderer extends JLabel implements TableCellRenderer
	{
		////////////////////////////////////////////////////////
		// Constructor
		////////////////////////////////////////////////////////
		JTable mtblParent;
		public HeaderCellRenderer(JTable tbl)
		{
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setForeground(tbl.getForeground());
			setBackground(tbl.getTableHeader().getBackground());
			setBorder(new EmptyBorder(0,2,0,2));
			mtblParent = tbl;
		}
		////////////////////////////////////////////////////////
		// Implements
		////////////////////////////////////////////////////////
		public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column)
		{
			// Set display value
			if(value == null)
				value = "";
			setFont(mtblParent.getFont());
			setText(value.toString());
			return this;
		}
	}
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public TableRowHeader()
	{
		this.setAutoCreateColumnsFromModel(false);
		this.defaultEditorsByColumnClass.clear();
		this.defaultRenderersByColumnClass.clear();
	}
	////////////////////////////////////////////////////////
	// Refresh
	////////////////////////////////////////////////////////
	public void refresh(VectorTable tbl)
	{
		setModel(new RowHeaderModel(tbl.getModel()));
	}
	////////////////////////////////////////////////////////
	// Column processing
	////////////////////////////////////////////////////////
	public void addColumn(String strName,int iModelIndex,int iWidth)
	{
		TableColumn col = new TableColumn(iModelIndex,iWidth);
		col.setHeaderValue(strName);
		col.setCellRenderer(new HeaderCellRenderer(this));
		getColumnModel().addColumn(col);
	}
}

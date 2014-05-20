package com.fss.swing;

import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class VectorTableColumn extends TableColumn
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private VectorTable mtblParent;
	private int miAlignment = JLabel.LEFT;
	private String mstrDefaultValue = "";
	private String mstrFilterValue = "";
	private boolean mbHeaderSort = true;
	private JComponent mcmpEditor;
	private boolean mbEditable;
	private Vector mvtValueMap;
	private Format mfmt;
	////////////////////////////////////////////////////////
	// Constructor, destructor
	////////////////////////////////////////////////////////
	public VectorTableColumn(VectorTable tblParent)
	{
		mtblParent = tblParent;
		mcmpEditor = new JTextField();
		cellEditor = new VectorTableCellEditor(this);
		cellRenderer = new VectorTableCellRenderer(this);
	}
	////////////////////////////////////////////////////////
	public VectorTableColumn()
	{
	}
	////////////////////////////////////////////////////////
	public VectorTableColumn duplicate()
	{
		VectorTableColumn col = new VectorTableColumn();
		col.setAlignment(miAlignment);
		col.setCellEditor(getCellEditor());
		col.setCellRenderer(getCellRenderer());
		col.setDefaultValue(getDefaultValue());
		col.setEditable(isEditable());
		col.setEditorComponent(getEditorComponent());
		col.setFilterValue(getFilterValue());
		col.setFormat(getFormat());
		col.setHeaderRenderer(getHeaderRenderer());
		col.setHeaderSort(isHeaderSort());
		col.setHeaderValue(getHeaderValue());
		col.setMaxWidth(getMaxWidth());
		col.setMinWidth(getMinWidth());
		col.setModelIndex(getModelIndex());
		col.setParentTable(getParentTable());
		col.setPreferredWidth(getPreferredWidth());
		col.setResizable(getResizable());
		col.setValueMap(getValueMap());
		return col;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public VectorTable getParentTable()
	{
		return mtblParent;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bEditable boolean
	 */
	////////////////////////////////////////////////////////
	public void setParentTable(VectorTable tblParent)
	{
		mtblParent = tblParent;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean isEditable()
	{
		return mbEditable;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bEditable boolean
	 */
	////////////////////////////////////////////////////////
	public void setEditable(boolean bEditable)
	{
		mbEditable = bEditable;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public String getFilterValue()
	{
		return mstrFilterValue;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bEditable boolean
	 */
	////////////////////////////////////////////////////////
	public void setFilterValue(String strFilterValue)
	{
		mstrFilterValue = strFilterValue;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean isHeaderSort()
	{
		return mbHeaderSort;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bEditable boolean
	 */
	////////////////////////////////////////////////////////
	public void setHeaderSort(boolean bHeaderSort)
	{
		mbHeaderSort = bHeaderSort;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public Format getFormat()
	{
		return mfmt;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bEditable boolean
	 */
	////////////////////////////////////////////////////////
	public void setFormat(Format fmt)
	{
		mfmt = fmt;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public String getDefaultValue()
	{
		return mstrDefaultValue;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bEditable boolean
	 */
	////////////////////////////////////////////////////////
	public void setDefaultValue(String str)
	{
		mstrDefaultValue = str;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public int getAlignment()
	{
		return miAlignment;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bEditable boolean
	 */
	////////////////////////////////////////////////////////
	public void setAlignment(int iAlign)
	{
		miAlignment = iAlign;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get column editor
	 * @return JComponent
	 */
	////////////////////////////////////////////////////////
	public TableCellEditor getEditor()
	{
		return cellEditor;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return Vector
	 */
	////////////////////////////////////////////////////////
	public Vector getValueMap()
	{
		return mvtValueMap;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param vtValue Vector
	 */
	////////////////////////////////////////////////////////
	public void setValueMap(Vector vtValue)
	{
		mvtValueMap = vtValue;
	}
	////////////////////////////////////////////////////////
	/**
	 * Set column editor component
	 * @param cmpEditor JComponent
	 */
	////////////////////////////////////////////////////////
	public void setEditorComponent(JComponent cmpEditor)
	{
		mcmpEditor = cmpEditor;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get column editor component
	 * @return JComponent
	 */
	////////////////////////////////////////////////////////
	public JComponent getEditorComponent()
	{
		return mcmpEditor;
	}
}

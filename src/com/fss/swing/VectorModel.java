package com.fss.swing;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

/**
* <p>Title: </p>
* <p>Description: </p>
* <p>Copyright: Copyright (c) 2001</p>
* <p>Company: FPT</p>
* @author Thai Hoang Hiep
* @version 1.0
*/

public class VectorModel extends AbstractTableModel implements MutableComboBoxModel
{
	////////////////////////////////////////////////////////
	protected EventListenerList listenerList = new EventListenerList();
	protected Vector vtSelected;
	protected VectorTable mtbl;
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param tbl VectorTable
	 */
	////////////////////////////////////////////////////////
	public VectorModel(VectorTable tbl)
	{
		mtbl = tbl;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getRowCount()
	{
		if(mtbl.isAllowInsert())
			return mtbl.getFilteredData().size() + 1;
		return mtbl.getFilteredData().size();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getColumnCount()
	{
		return mtbl.getRowSize();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iColIndex int
	 * @return Class
	 */
	////////////////////////////////////////////////////////
	public Class getColumnClass(int iColIndex)
	{
		return String.class;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iColIndex int
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String getColumnName(int iColIndex)
	{
		VectorTableColumn col = mtbl.getColumnEx(iColIndex);
		return col.getHeaderValue().toString();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iRowIndex int
	 * @param iColIndex int
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean isCellEditable(int iRowIndex,int iColIndex)
	{
		VectorTableColumn col = mtbl.getColumnEx(iColIndex);
		return col.isEditable();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iRowIndex int
	 * @param iColIndex int
	 * @return Object
	 */
	////////////////////////////////////////////////////////
	public Object getValueAt(int iRowIndex,int iColIndex)
	{
		if(iRowIndex >= mtbl.getFilteredData().size())
			return mtbl.getSampleRow().elementAt(iColIndex);
		Vector vtRowData = (Vector)mtbl.getFilteredData().elementAt(iRowIndex);
		return vtRowData.elementAt(iColIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param objValue Object
	 * @param iRowIndex int
	 * @param iColIndex int
	 */
	////////////////////////////////////////////////////////
	public void setValueAt(Object objValue,int iRowIndex,int iColIndex)
	{
		Vector vtRowData = null;
		if(iRowIndex >= mtbl.getFilteredData().size())
			vtRowData = mtbl.getSampleRow();
		else
			vtRowData = (Vector)mtbl.getFilteredData().elementAt(iRowIndex);
		if(!vtRowData.elementAt(iColIndex).equals(objValue))
		{
			vtRowData.setElementAt(objValue,iColIndex);
			mtbl.correctData(iRowIndex);
			this.fireTableCellUpdated(iRowIndex,iColIndex);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Adds an item at the end of the model. The implementation of this method
	 * should notify all registered <code>ListDataListener</code>s that the
	 * item has been added.
	 *
	 * @param obj the <code>Object</code> to be added
	 */
	////////////////////////////////////////////////////////
	public void addElement(Object obj)
	{
		mtbl.addRow((Vector)obj);
	}
	////////////////////////////////////////////////////////
	/**
	 * Removes an item from the model. The implementation of this method should
	 * should notify all registered <code>ListDataListener</code>s that the
	 * item has been removed.
	 *
	 * @param obj the <code>Object</code> to be removed
	 */
	////////////////////////////////////////////////////////
	public void removeElement(Object obj)
	{
		mtbl.deleteRow((Vector)obj);
	}
	////////////////////////////////////////////////////////
	/**
	 * Adds an item at a specific index.  The implementation of this method
	 * should notify all registered <code>ListDataListener</code>s that the
	 * item has been added.
	 *
	 * @param obj  the <code>Object</code> to be added
	 * @param index  location to add the object
	 */
	////////////////////////////////////////////////////////
	public void insertElementAt(Object obj,int index)
	{
		mtbl.insertRow(index,(Vector)obj);
	}
	////////////////////////////////////////////////////////
	/**
	 * Removes an item at a specific index. The implementation of this method
	 * should notify all registered <code>ListDataListener</code>s that the
	 * item has been removed.
	 *
	 * @param index  location of object to be removed
	 */
	////////////////////////////////////////////////////////
	public void removeElementAt(int index)
	{
		mtbl.deleteRow(index);
	}
	////////////////////////////////////////////////////////
	/**
	 * Set the selected item. The implementation of this  method should notify
	 * all registered <code>ListDataListener</code>s that the contents
	 * have changed.
	 *
	 * @param anItem the list object to select or <code>null</code>
	 *        to clear the selection
	 */
	////////////////////////////////////////////////////////
	public void setSelectedItem(Object anItem)
	{
		mtbl.getSelectionModel().clearSelection();
		vtSelected = (Vector)anItem;
		for(int iIndex = 0;iIndex < mtbl.getRowCount();iIndex++)
		{
			if(mtbl.getRow(iIndex) == anItem)
			{
				vtSelected = mtbl.getRow(iIndex);
				mtbl.changeSelectedRow(iIndex);
				fireContentsChanged(this, -1, -1);
				return;
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Returns the selected item
	 * @return The selected item or <code>null</code> if there is no selection
	 */
	////////////////////////////////////////////////////////
	public Object getSelectedItem()
	{
		int iIndex = mtbl.getSelectedRow();
		if(iIndex >= 0 && iIndex < getRowCount())
			return mtbl.getRow(iIndex);
		return vtSelected;
	}
	////////////////////////////////////////////////////////
	/**
	 * Returns the value at the specified index.
	 * @param index the requested index
	 * @return the value at <code>index</code>
	 */
	////////////////////////////////////////////////////////
	public Object getElementAt(int index)
	{
		return mtbl.getRow(index);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getSize()
	{
		return mtbl.getRowCount();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param l ListDataListener
	 */
	////////////////////////////////////////////////////////
	public void addListDataListener(ListDataListener l)
	{
		listenerList.add(ListDataListener.class,l);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param l ListDataListener
	 */
	////////////////////////////////////////////////////////
	public void removeListDataListener(ListDataListener l)
	{
		listenerList.remove(ListDataListener.class,l);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return ListDataListener[]
	 */
	////////////////////////////////////////////////////////
	public ListDataListener[] getListDataListeners()
	{
		return(ListDataListener[])listenerList.getListeners(
			ListDataListener.class);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param source Object
	 * @param index0 int
	 * @param index1 int
	 */
	////////////////////////////////////////////////////////
	protected void fireContentsChanged(Object source,int index0,int index1)
	{
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for(int i = listeners.length - 2;i >= 0;i -= 2)
		{
			if(listeners[i] == ListDataListener.class)
			{
				if(e == null)
					e = new ListDataEvent(source,ListDataEvent.CONTENTS_CHANGED,index0,index1);
				((ListDataListener)listeners[i + 1]).contentsChanged(e);
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param source Object
	 * @param index0 int
	 * @param index1 int
	 */
	////////////////////////////////////////////////////////
	protected void fireIntervalAdded(Object source,int index0,int index1)
	{
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for(int i = listeners.length - 2;i >= 0;i -= 2)
		{
			if(listeners[i] == ListDataListener.class)
			{
				if(e == null)
					e = new ListDataEvent(source,ListDataEvent.INTERVAL_ADDED,index0,index1);
				((ListDataListener)listeners[i + 1]).intervalAdded(e);
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param source Object
	 * @param index0 int
	 * @param index1 int
	 */
	////////////////////////////////////////////////////////
	protected void fireIntervalRemoved(Object source,int index0,int index1)
	{
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for(int i = listeners.length - 2;i >= 0;i -= 2)
		{
			if(listeners[i] == ListDataListener.class)
			{
				if(e == null)
					e = new ListDataEvent(source,ListDataEvent.INTERVAL_REMOVED,index0,index1);
				((ListDataListener)listeners[i + 1]).intervalRemoved(e);
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param listenerType Class
	 * @return EventListener[]
	 */
	////////////////////////////////////////////////////////
	public EventListener[] getListeners(Class listenerType)
	{
		return listenerList.getListeners(listenerType);
	}
}

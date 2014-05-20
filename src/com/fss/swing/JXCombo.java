package com.fss.swing;

import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class JXCombo extends JComboBox implements TrackChangeListener
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	public Object objCurrentValue = null;
	private boolean bStored = false;
	private boolean nextFocusOnEnter = false;
	private VectorTable mtbl;
	private int miMaxPopupWidth = 600;
	private String mstrSelected = "";
	private JToolTip tooltip = new JToolTip();
	private Popup tipWindow;
	////////////////////////////////////////////////////////
	public JXCombo()
	{
		this(null);
	}
	////////////////////////////////////////////////////////
	public JXCombo(VectorTable tbl)
	{
		mtbl = tbl;
		if(mtbl == null)
		{
			mtbl = new VectorTable(1);
			mtbl.addColumn("",0,false);
			mtbl.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			Skin.applySkin(mtbl);
		}

		setModel((VectorModel)mtbl.getModel());
		setUI(new JXComboUI());
		mtbl.setBorder(BorderFactory.createEmptyBorder());
		this.addKeyListener(new java.awt.event.KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				this_keyPressed(e);
			}
		});
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return VectorTable
	 */
	////////////////////////////////////////////////////////
	public VectorTable getPopupTable()
	{
		return mtbl;
	}
	////////////////////////////////////////////////////////
	// Purpose: backup current value of text field to variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void backup()
	{
		objCurrentValue = getSelectedValue();
		bStored = true;
	}
	////////////////////////////////////////////////////////
	// Purpose: backup current value of text field to variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public Object getBackupData()
	{
		return objCurrentValue;
	}
	////////////////////////////////////////////////////////
	// Purpose: Restore value of text field
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void restore()
	{
		if(!bStored)
			return;
		setSelectedValue(objCurrentValue);
		clearBackup();
	}
	////////////////////////////////////////////////////////
	// Purpose: Return changed status
	// Return true if text field is changed, otherwise false
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public boolean isChanged()
	{
		if(!bStored)
			return false;
		Object obj = getSelectedValue();
		if(obj == null || objCurrentValue == null)
		{
			if(obj == null && objCurrentValue == null)
				return false;
			else
				return true;
		}
		return !objCurrentValue.equals(obj);
	}
	////////////////////////////////////////////////////////
	// Purpose: Clear backup variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void clearBackup()
	{
		objCurrentValue = null;
		bStored = false;
	}
	////////////////////////////////////////////////////////
	public void setNextFocusOnEnter(boolean value)
	{
		nextFocusOnEnter = value;
	}
	////////////////////////////////////////////////////////
	public boolean getNextFocusOnEnter()
	{
		return nextFocusOnEnter;
	}
	////////////////////////////////////////////////////////
	private void this_keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == e.VK_ENTER && getNextFocusOnEnter())
			transferFocus();
	}
	////////////////////////////////////////////////////////
	/**
	 * Fill item list for combo
	 * @param vtData table data
	 * @param iColumnIndex column to map to combo
	 * @author Binhtx
	 * @since 22/02/2004
	 */
	////////////////////////////////////////////////////////
	public void fillValue(Vector vtData,int iColumnIndex)
	{
		fillValue(vtData,iColumnIndex,false);
	}
	////////////////////////////////////////////////////////
	/**
	 * Fill item list for combo
	 * @param vtData Vector
	 * @param iColumnIndex int
	 * @param bAddNullValue boolean
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void fillValue(Vector vtData,int iColumnIndex,boolean bAddNullValue)
	{
		getPopupTable().deleteAllRow();
		if(vtData.size() > 0)
		{
			int iNewRowsize = ((Vector)vtData.elementAt(0)).size();
			if(iNewRowsize > getPopupTable().getRowSize())
				getPopupTable().setRowSize(iNewRowsize);
		}
		if(bAddNullValue)
			getPopupTable().addRow();
		getPopupTable().appendData(vtData);
		getPopupTable().setDisplayIndex(iColumnIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 * Fill item list for combo
	 * @param vtData Vector
	 * @param iColumnIndex int
	 * @param iValueIndex int
	 * @param bAddNullValue boolean
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public void fillValue(Vector vtData,int iColumnIndex,int iValueIndex,boolean bAddNullValue)
	{
		fillValue(vtData,iColumnIndex,bAddNullValue);
		getPopupTable().setValueIndex(iValueIndex);
	}
	////////////////////////////////////////////////////////
	public void fillValue(Vector vtData,int iColumnIndex,int iValueIndex,boolean bAddNullValue,int iSelectedIndex)
	{
		fillValue(vtData,iColumnIndex,iValueIndex,bAddNullValue);
		setSelectedIndex(iSelectedIndex);
	}
	////////////////////////////////////////////////////////
	public void fillValue(Vector vtData,int iColumnIndex,int iValueIndex,int iSelectedIndex)
	{
		fillValue(vtData,iColumnIndex,iValueIndex,false);
		setSelectedIndex(iSelectedIndex);
	}
	////////////////////////////////////////////////////////
	public void fillValue(Vector vtData,int iColumnIndex,int iValueIndex,boolean bAddNullValue,Object objSelectedValue)
	{
		fillValue(vtData,iColumnIndex,iValueIndex,bAddNullValue);
		setSelectedValue(objSelectedValue);
	}
	////////////////////////////////////////////////////////
	public void fillValue(Vector vtData,int iColumnIndex,int iValueIndex,Object objSelectedValue)
	{
		fillValue(vtData,iColumnIndex,iValueIndex,false);
		setSelectedValue(objSelectedValue);
	}
	////////////////////////////////////////////////////////
	public void fillValue(Vector vtData,int iColumnIndex,int iValueIndex)
	{
		fillValue(vtData,iColumnIndex,iValueIndex,false);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return Vector
	 */
	////////////////////////////////////////////////////////
	public Vector getValueList()
	{
		Vector vtReturn = new Vector();
		Vector vtData = getPopupTable().getData();
		int iValueIndex = getPopupTable().getValueIndex();
		for(int iIndex = 0;iIndex < vtData.size();iIndex++)
			vtReturn.addElement(((Vector)vtData.elementAt(iIndex)).elementAt(iValueIndex));
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return Object
	 */
	////////////////////////////////////////////////////////
	public Object getSelectedValue()
	{
		Vector vt = (Vector)dataModel.getSelectedItem();
		if(vt != null)
			return vt.elementAt(getPopupTable().getValueIndex());
		else
			return null;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return Object
	 */
	////////////////////////////////////////////////////////
	public boolean setSelectedValue(Object obj)
	{
		Vector vtData = getPopupTable().getData();
		for(int iIndex = 0;iIndex < vtData.size();iIndex++)
		{
			if(((Vector)vtData.elementAt(iIndex)).elementAt(getPopupTable().getValueIndex()).equals(obj))
			{
				dataModel.setSelectedItem(vtData.elementAt(iIndex));
				selectedItemChanged();
				return true;
			}
		}
		return false;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param objValue Object
	 * @return Object
	 */
	////////////////////////////////////////////////////////
	public Object getItem(Object objValue)
	{
		int iSelectedIndex = getPopupTable().getRowIndexForValueData(objValue);
		if(iSelectedIndex < 0)
			return null;
		return getItemAt(iSelectedIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getSelectedIndex()
	{
		if(mtbl != null)
			return mtbl.getSelectedRow();
		else
			return super.getSelectedIndex();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return Object
	 */
	////////////////////////////////////////////////////////
	public Object getSelectedItem()
	{
		if(mtbl != null)
		{
			Vector vt = (Vector)dataModel.getSelectedItem();
			if(vt == null)
				return null;
			else
				return vt.elementAt(mtbl.getDisplayIndex());
		}
		else
			return super.getSelectedItem();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public Object getItemAt(int iIndex)
	{
		if(mtbl != null)
			return mtbl.getRow(iIndex).elementAt(mtbl.getDisplayIndex());
		else
			return super.getItemAt(iIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param objValue Object
	 * @return Object
	 */
	////////////////////////////////////////////////////////
	public void addItem(Object obj)
	{
		Vector vt = new Vector();
		for(int iIndex = 1;iIndex < mtbl.getRowSize();iIndex++)
			vt.addElement("");
		vt.insertElementAt(obj,mtbl.getDisplayIndex());
		super.addItem(vt);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param obj Object
	 * @param iIndex int
	 */
	////////////////////////////////////////////////////////
	public void insertItemAt(Object obj,int index)
	{
		Vector vt = new Vector();
		for(int iIndex = 1;iIndex < mtbl.getRowSize();iIndex++)
			vt.addElement("");
		vt.insertElementAt(obj,mtbl.getDisplayIndex());
		super.insertItemAt(vt,index);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param obj Object
	 */
	////////////////////////////////////////////////////////
	public void setSelectedItem(Object obj)
	{
		Vector vt = new Vector();
		for(int iIndex = 1;iIndex < mtbl.getRowSize();iIndex++)
			vt.addElement("");
		vt.insertElementAt(obj,mtbl.getDisplayIndex());
		super.setSelectedItem(vt);
	}
	////////////////////////////////////////////////////////
	public void setSelectedIndex(int anIndex)
	{
		int size = dataModel.getSize();

		if(anIndex == -1)
			setSelectedItem(null);
		else if (anIndex < -1 || anIndex >= size)
			throw new IllegalArgumentException("setSelectedIndex: " + anIndex + " out of bounds");
		else
			super.setSelectedItem(dataModel.getElementAt(anIndex));
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getMaximumPopupWidth()
	{
		return miMaxPopupWidth;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iMaxWidth int
	 */
	////////////////////////////////////////////////////////
	public void setMaximumPopupWidth(int iMaxWidth)
	{
		miMaxPopupWidth = iMaxWidth;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param keyChar char
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean selectWithKeyChar(char keyChar)
	{
		if(keyChar == '\b' || keyChar >= 32)
		{
			try
			{
				if(keyChar == '\b')
				{
					if(mstrSelected.length() > 0)
					{
						mstrSelected = mstrSelected.substring(0,mstrSelected.length() - 1);
						if(mstrSelected.length() <= 0)
						{
							setSelectedIndex(0);
							return true;
						}
						else
						{
							if(selectWithWildcard(0,mstrSelected + "*"))
								return true;
						}
					}
					return false;
				}

				int iIndex = mtbl.getSelectedRow();
				if(iIndex >= 0)
				{
					String str = StringUtil.nvl(mtbl.getRow(iIndex).elementAt(mtbl.getDisplayIndex()),"");
					if(mstrSelected.length() == 0 || WildcardFilter.match(mstrSelected + "*",str))
					{
						if(selectWithWildcard(iIndex,mstrSelected + keyChar + "*"))
						{
							mstrSelected += String.valueOf(keyChar);
							return true;
						}
						else if(mstrSelected.equalsIgnoreCase(String.valueOf(keyChar)) &&
								iIndex + 1 < mtbl.getRowCount())
						{
							str = StringUtil.nvl(mtbl.getRow(iIndex + 1).elementAt(mtbl.getDisplayIndex()),"");
							if(WildcardFilter.match(mstrSelected + "*",str))
							{
								setSelectedIndex(iIndex + 1);
								return true;
							}
						}

					}
				}
				if(selectWithWildcard(0,keyChar + "*"))
				{
					mstrSelected = String.valueOf(keyChar);
					return true;
				}
				else
					return false;
			}
			finally
			{
				hideTip();
				if(mstrSelected.length() > 0)
				{
					tooltip.setTipText(mstrSelected);
					java.awt.Point p = this.getLocationOnScreen();
					tipWindow = PopupFactory.getSharedInstance().getPopup(null,tooltip,p.x,p.y + getHeight());
					tipWindow.show();
				}
			}
		}
		else
		{
			hideTip();
			return false;
		}
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 */
	////////////////////////////////////////////////////////
	public void hideTip()
	{
		if(tipWindow != null)
			tipWindow.hide();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iIndex int
	 * @param strWildcard String
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	private boolean selectWithWildcard(int iIndex,String strWildcard)
	{
		while(iIndex < mtbl.getRowCount())
		{
			String str = StringUtil.nvl(mtbl.getRow(iIndex).elementAt(mtbl.getDisplayIndex()),"");
			if(WildcardFilter.match(strWildcard,str))
			{
				setSelectedIndex(iIndex);
				return true;
			}
			iIndex++;
		}
		return false;
	}
}

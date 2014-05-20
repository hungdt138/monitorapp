/*
 * HistoryModel.java - History list model
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2003 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.fss.swing;

//{{{ Imports
import java.util.*;

import java.awt.*;
import com.fss.util.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;

//}}}
/**
 * A history list. One history list can be used by several history text
 * fields. Note that the list model implementation is incomplete; no events
 * are fired when the history model changes.
 * @author Slava Pestov
 * @version $Id: HistoryModel.java,v 1.17 2004/05/29 01:55:25 spestov Exp $
 */
public class HistoryModel extends AbstractListModel
{
	/**
	 * Creates a new history list. Calling this is normally not
	 * necessary.
	 * @param name String
	 * @param group HistoryModelGroup
	 */
	public HistoryModel(String name,HistoryModelGroup group)
	{
		this.name = name;
		this.group = group;
		data = new Vector(max);
		list = new JList(new VectorListModel(data))
		{
			public void processMouseEvent(MouseEvent e)
			{
				if(e.isControlDown())
				{
					e = new MouseEvent((Component)e.getSource(),e.getID(),e.getWhen(),
									   e.getModifiers() ^ InputEvent.CTRL_MASK,
									   e.getX(),e.getY(),e.getClickCount(),e.isPopupTrigger());
				}
				super.processMouseEvent(e);
			}
			public void setSelectedIndex(int index)
			{
				super.setSelectedIndex(index);
				ensureIndexIsVisible(index);
			}
		};
		list.setSelectionForeground(UIManager.getColor("ComboBox.selectionForeground"));
		list.setSelectionBackground(UIManager.getColor("ComboBox.selectionBackground"));
		list.setBorder(null);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addMouseMotionListener(new MouseMotionHandler());
		list.addMouseListener(mousehandler);
	}
	/**
	 * Adds an item to the end of this history list, trimming the list
	 * to the maximum number of items if necessary.
	 * @param text The item
	 */
	public void addItem(String text)
	{
		if(text == null || text.length() == 0)
			return;
		group.modified = true;
		int index = data.indexOf(text);
		if(index != -1)
			data.removeElementAt(index);
		data.insertElementAt(text,0);
		while(getSize() > max)
			data.removeElementAt(data.size() - 1);
		group.saveHistory();
	}
	public boolean isItemExist(String strItem)
	{
		return data.indexOf(strItem) >= 0;
	}
	/**
	 * Returns an item from the history list.
	 * @param index The index
	 * @return String
	 */
	public String getItem(int index)
	{
		return(String)data.elementAt(index);
	}
	/**
	 * Returns an item from the history list. This method returns the
	 * same thing as {@link #getItem(int)} and only exists so that
	 * <code>HistoryModel</code> instances can be used as list modelgroups.
	 * @param index The index
	 * @since jEdit 4.2pre2
	 * @return Object
	 */
	public Object getElementAt(int index)
	{
		return getItem(index);
	}
	/**
	 * Removes all entries from this history model.
	 * @since jEdit 4.2pre2
	 */
	public void clear()
	{
		group.modified = true;
		data.removeAllElements();
	}
	/**
	 * Returns the number of elements in this history list.
	 * @return int
	 */
	public int getSize()
	{
		return data.size();
	}
	/**
	 * Returns the name of this history list. This can be passed
	 * to the HistoryTextField constructor.
	 * @return String
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * Returns a named model. If the specified model does not
	 * already exist, it will be created.
	 * @param fullname The model name
	 * @return HistoryModel
	 */
	public static HistoryModel getModel(String fullname)
	{
		int iIndex = fullname.indexOf("|");
		if(iIndex < 0)
			return null;
		String strGroupName = fullname.substring(0,iIndex);
		String strModelName = fullname.substring(iIndex + 1,fullname.length());
		if(strGroupName.length() == 0 || strModelName.length() == 0)
			return null;
		if(groups == null)
			groups = new Hashtable();
		HistoryModelGroup group = (HistoryModelGroup)groups.get(strGroupName);
		if(group == null)
		{
			group = new HistoryModelGroup(strGroupName);
			groups.put(strGroupName,group);
		}

		HistoryModel model = (HistoryModel)group.models.get(strModelName);
		if(model == null)
		{
			model = new HistoryModel(strModelName,group);
			group.models.put(strModelName,model);
		}

		return model;
	}
	////////////////////////////////////////////////////////
	public String historyPrevious()
	{
		try
		{
			if(index > getSize())
				index = getSize();
			index--;
			if(index < 0)
			{
				index = getSize();
				return "";
			}
			else
				return getItem(index);
		}
		finally
		{
			if(list != null && list.isVisible())
				list.setSelectedIndex(index);
		}
	}
	////////////////////////////////////////////////////////
	public String historyNext()
	{
		try
		{
			if(index < -1)
				index = -1;
			index++;
			if(index > getSize() - 1)
			{
				index = -1;
				return "";
			}
			else
				return getItem(index);
		}
		finally
		{
			if(list != null && list.isVisible())
				list.setSelectedIndex(index);
		}
	}
	////////////////////////////////////////////////////////
	public void showPopupMenu(JTextComponent txt)
	{
		if(popup != null && popup.isVisible())
		{
			popup.setVisible(false);
			return;
		}
		MenuSelectionManager.defaultManager().clearSelectedPath();
		mousehandler.setTextComponent(txt);

		addItem(txt.getText());
		if(data.size() > 0)
		{
			// Create popup
			JScrollPane scroller = new JScrollPane(list,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			scroller.setBorder(null);
			popup = new JPopupMenu();
			popup.setLayout(new BoxLayout(popup,BoxLayout.Y_AXIS));
			popup.setBorderPainted(true);
			popup.setBorder(BorderFactory.createLineBorder(Color.black));
			popup.setOpaque(false);
			popup.add(scroller);
			popup.setFocusable(false);
			popup.setDoubleBuffered(true);
			Skin.applySkin(popup);

			// Show popup
			Point location = getPopupLocation(txt,popup,scroller);
			popup.show(txt,location.x,location.y);
			list.setModel(new VectorListModel(data));
			list.setSelectedIndex(0);
			list.requestFocus();
		}
		txt.requestFocusInWindow();
	}
	/**
	 * Retrieves the height of the popup based on the current
	 * ListCellRenderer and the maximum row count.
	 */
	protected int getPopupHeightForRowCount(int maxRowCount)
	{
		// Set the cached value of the minimum row count
		int minRowCount = Math.min(maxRowCount,list.getModel().getSize());
		int height = 0;
		ListCellRenderer renderer = list.getCellRenderer();
		Object value = null;

		for(int i = 0;i < minRowCount;++i)
		{
			value = list.getModel().getElementAt(i);
			Component c = renderer.getListCellRendererComponent(list,value,i,false,false);
			int iValue = c.getPreferredSize().height;
			if(iValue <= 0)
				iValue = 20;
			height += iValue;
		}

		return height == 0 ? 100 : height;
	}
	/**
	 * Calculate the placement and size of the popup portion of the combo box based
	 * on the combo box location and the enclosing screen bounds. If
	 * no transformations are required, then the returned rectangle will
	 * have the same values as the parameters.
	 *
	 * @param px starting x location
	 * @param py starting y location
	 * @param pw starting width
	 * @param ph starting height
	 * @return a rectangle which represents the placement and size of the popup
	 */
	protected Rectangle computePopupBounds(int px,int py,int pw,int ph)
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Rectangle screenBounds;

		// Calculate the desktop dimensions relative to the combo box.
		GraphicsConfiguration gc = list.getGraphicsConfiguration();
		Point p = new Point();
		SwingUtilities.convertPointFromScreen(p,list);
		if(gc != null)
		{
			Insets screenInsets = toolkit.getScreenInsets(gc);
			screenBounds = gc.getBounds();
			screenBounds.width -= (screenInsets.left + screenInsets.right);
			screenBounds.height -= (screenInsets.top + screenInsets.bottom);
			screenBounds.x += (p.x + screenInsets.left);
			screenBounds.y += (p.y + screenInsets.top);
		}
		else
			screenBounds = new Rectangle(p,toolkit.getScreenSize());

		Rectangle rect = new Rectangle(px,py,pw,ph);
		if(py + ph > screenBounds.y + screenBounds.height && ph < screenBounds.height)
			rect.y = -rect.height;
		return rect;
	}
	/**
	 * Calculates the upper left location of the Popup.
	 */
	private Point getPopupLocation(JTextComponent txt,JPopupMenu popup,JScrollPane scroller)
	{
		Dimension popupSize = txt.getSize();
		Insets insets = popup.getInsets();
		popupSize.setSize(popupSize.width - (insets.right + insets.left),
						  getPopupHeightForRowCount(8));
		Rectangle popupBounds = computePopupBounds(0,txt.getBounds().height,
			popupSize.width,popupSize.height);
		Dimension scrollSize = popupBounds.getSize();
		Point popupLocation = popupBounds.getLocation();
		scroller.setMaximumSize(scrollSize);
		scroller.setPreferredSize(scrollSize);
		scroller.setMinimumSize(scrollSize);
		list.revalidate();
		return popupLocation;
	}
	////////////////////////////////////////////////////////
	class MouseHandler extends MouseAdapter
	{
		private JTextComponent txt;
		public void setTextComponent(JTextComponent txt)
		{
			this.txt = txt;
		}
		public void mouseReleased(MouseEvent anEvent)
		{
			if(txt != null)
			{
				txt.setText(StringUtil.nvl(list.getSelectedValue(),""));
				MenuSelectionManager.defaultManager().clearSelectedPath();
				txt.requestFocusInWindow();
			}
		}
	}
	////////////////////////////////////////////////////////
	class MouseMotionHandler extends MouseMotionAdapter
	{
		public void mouseMoved(MouseEvent anEvent)
		{
			Point location = anEvent.getPoint();
			Rectangle r = new Rectangle();
			list.computeVisibleRect(r);
			if(r.contains(location))
			{
				if(list == null)
					return;
				int index = list.locationToIndex(location);
				if(index == -1)
				{
					if(location.y < 0)
						index = 0;
					else
						index = list.getModel().getSize() - 1;
				}
				if(list.getSelectedIndex() != index)
				{
					list.setSelectedIndex(index);
					list.ensureIndexIsVisible(index);
				}
			}
		}
	}
	////////////////////////////////////////////////////////
	private MouseHandler mousehandler = new MouseHandler();
	private static Hashtable groups;
	private static int max = 50;
	private String name;
	public Vector data;
	public int index;
	public HistoryModelGroup group;
	public JPopupMenu popup;
	public JList list;
}
class VectorListModel extends AbstractListModel
{
	private Vector listData;
	public VectorListModel(Vector vt)
	{
		listData = vt;
	}
	public int getSize()
	{
		return listData.size();
	}
	public Object getElementAt(int i)
	{
		return listData.elementAt(i);
	}
}

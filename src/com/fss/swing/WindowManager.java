package com.fss.swing;

import java.awt.*;
import java.util.*;
import javax.swing.*;

import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class WindowManager
{
	////////////////////////////////////////////////////////
	private static Vector mvtWindowList = new Vector();
	////////////////////////////////////////////////////////
	public static int getWindowCount()
	{
		return mvtWindowList.size();
	}
	////////////////////////////////////////////////////////
	public static Window getWindow(int iIndex)
	{
		if(iIndex >= mvtWindowList.size())
			return null;
		return (Window)mvtWindowList.elementAt(iIndex);
	}
	////////////////////////////////////////////////////////
	public static Window getWindow(Class cls)
	{
		for(int iIndex = 0;iIndex < mvtWindowList.size();iIndex++)
		{
			Object obj = mvtWindowList.elementAt(iIndex);
			if(obj.getClass().equals(cls)) return (Window)obj;
		}
		return null;
	}
	////////////////////////////////////////////////////////
	public static void addWindow(Window wnd)
	{
		mvtWindowList.addElement(wnd);
	}
	////////////////////////////////////////////////////////
	public static void removeWindow(Window wnd)
	{
		mvtWindowList.removeElement(wnd);
	}
	////////////////////////////////////////////////////////
	public static void refresh()
	{
		for(int iIndex = 0;iIndex < mvtWindowList.size();iIndex++)
		{
			Window wnd = (Window)mvtWindowList.elementAt(iIndex);
			if(!wnd.isShowing()) removeWindow(wnd);
		}
	}
	////////////////////////////////////////////////////////
	public static void close(int iIndex)
	{
		Window wnd = getWindow(iIndex);
		if(wnd != null)
			if(wnd.isShowing())
			{
				wnd.dispose();
				removeWindow(wnd);
			}
	}
	////////////////////////////////////////////////////////
	public static void close(Class cls)
	{
		Window wnd = getWindow(cls);
		if(wnd != null)
			if(wnd.isShowing())
			{
				wnd.dispose();
				removeWindow(wnd);
			}
	}
	////////////////////////////////////////////////////////
	public static void closeAll()
	{
		for(int iIndex = 0;iIndex < mvtWindowList.size();iIndex++)
		{
			Window wnd = (Window)mvtWindowList.elementAt(iIndex);
			if(wnd.isVisible()) wnd.dispose();
		}
		mvtWindowList.clear();
	}
	////////////////////////////////////////////////////////
	public static Component getFocusOwner()
	{
		for(int iWindowIndex = 0;iWindowIndex < mvtWindowList.size();iWindowIndex++)
		{
			Window wnd = (Window)mvtWindowList.elementAt(iWindowIndex);
			Component cmpFocused = wnd.getFocusOwner();
			if(cmpFocused != null)
				return cmpFocused;
		}
		return null;
	}
	////////////////////////////////////////////////////////
	public static void updateLookAndField()
	{
		int iIndex = 0;
		while(iIndex < mvtWindowList.size())
		{
			Window wndTemp = (Window)mvtWindowList.elementAt(iIndex);
			if(!wndTemp.isVisible())
				mvtWindowList.removeElementAt(iIndex);
			else
			{
				SwingUtilities.updateComponentTreeUI(wndTemp);
				Skin.applySkin(wndTemp);
				iIndex++;
			}
		}
	}
	////////////////////////////////////////////////////////
	public static void updateLanguage()
	{
		for(int iIndex = 0;iIndex < mvtWindowList.size();iIndex++)
		{
			Window wndTemp = (Window)mvtWindowList.elementAt(iIndex);
			try
			{
				if(wndTemp instanceof LanguageUpdatable)
					((LanguageUpdatable)wndTemp).updateLanguage();
				updateLanguage(wndTemp);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				MessageBox.showMessageDialog(getFocusOwner(),e,Global.APP_NAME,JOptionPane.ERROR_MESSAGE);
				wndTemp.dispose();
			}
		}
	}
	////////////////////////////////////////////////////////
	public static void updateLanguage(Container cmp) throws Exception
	{
		for(int iIndex = 0;iIndex < cmp.getComponentCount();iIndex++)
		{
			Component cmpChild = cmp.getComponent(iIndex);
			if(cmpChild instanceof LanguageUpdatable)
				((LanguageUpdatable)cmpChild).updateLanguage();
			if(cmpChild instanceof Container)
				updateLanguage((Container)cmpChild);
		}
	}
	////////////////////////////////////////////////////////
	public static void centeredWindow(final java.awt.Window wdn)
	{
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int cx = wdn.getWidth();
		int cy = wdn.getHeight();
		wdn.setBounds((dim.width - cx) / 2,(dim.height - cy) / 2,cx,cy);
		wdn.setVisible(true);
	}
	////////////////////////////////////////////////////////
	public static void maximizeWindow(final java.awt.Window wdn)
	{
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int cx = wdn.getWidth();
		int cy = wdn.getHeight();
		wdn.setBounds(0,0,cx,cy);
		wdn.setVisible(true);
	 }
	 ////////////////////////////////////////////////////////
	 /**
	  *
	  * @param cmp Component
	  * @return String
	  */
	 ////////////////////////////////////////////////////////
	 public static String generateName(Component cmp)
	 {
		 Container ctn = cmp.getParent();
		 String strName = cmp.getName();
		 if(strName == null || strName.length() == 0)
		 {
			 if(ctn != null)
				 strName = String.valueOf(getComponentIndex(ctn,cmp));
			 else
				 strName = "";
		 }
		 if(strName.startsWith("null."))
			 strName = "";
		 if(ctn != null)
		 {
			 if(strName.length() > 0)
				 strName = "|" + strName;
			 if(ctn instanceof JDialog ||
				ctn instanceof JFrame)
				 return ctn.getClass().getName() + strName;
			 else
				 return generateName(ctn) + strName;
		 }
		 return strName;
	 }
	 ////////////////////////////////////////////////////////
	 /**
	  *
	  * @param ctn Container
	  * @param cmp Component
	  * @return int
	  */
	 ////////////////////////////////////////////////////////
	 public static int getComponentIndex(Container ctn,Component cmp)
	 {
		 for(int iIndex = 0;iIndex < ctn.getComponentCount();iIndex++)
		 {
			 if(cmp == ctn.getComponent(iIndex))
				 return iIndex;
		 }
		 return -1;
	 }
	 ////////////////////////////////////////////////////////
	 /**
	  * Shows the specified popup menu, ensuring it is displayed within
	  * the bounds of the screen.
	  * @param popup The popup menu
	  * @param comp The component to show it for
	  * @param x The x co-ordinate
	  * @param y The y co-ordinate
	  * @since jEdit 4.0pre1
	  */
	 ////////////////////////////////////////////////////////
	 public static void showPopupMenu(JPopupMenu popup, Component comp,int x, int y)
	 {
		 showPopupMenu(popup,comp,x,y,true);
	 }
	 ////////////////////////////////////////////////////////
	 /**
	  * Shows the specified popup menu, ensuring it is displayed within
	  * the bounds of the screen.
	  * @param popup The popup menu
	  * @param comp The component to show it for
	  * @param x The x co-ordinate
	  * @param y The y co-ordinate
	  * @param point If true, then the popup originates from a single point;
	  * otherwise it will originate from the component itself. This affects
	  * positioning in the case where the popup does not fit onscreen.
	  *
	  * @since jEdit 4.1pre1
	  */
	 ////////////////////////////////////////////////////////
	 public static void showPopupMenu(JPopupMenu popup, Component comp,int x, int y, boolean point)
	 {
		 int offsetX = 0;
		 int offsetY = 0;
		 int extraOffset = (point ? 1 : 0);
		 Component win = comp;
		 while(!(win instanceof Window || win == null))
		 {
			 offsetX += win.getX();
			 offsetY += win.getY();
			 win = win.getParent();
		 }
		 if(win != null)
		 {
			 Dimension size = popup.getPreferredSize();
			 Rectangle screenSize = win.getGraphicsConfiguration().getBounds();
			 if(x + offsetX + size.width + win.getX() > screenSize.width && x + offsetX + win.getX() >= size.width)
			 {
				 if(point)
					 x -= (size.width + extraOffset);
				 else
					 x = (win.getWidth() - size.width - offsetX + extraOffset);
			 }
			 else
				 x += extraOffset;
			 if(y + offsetY + size.height + win.getY() > screenSize.height && y + offsetY + win.getY() >= size.height)
			 {
				 if(point)
					 y = (win.getHeight() - size.height - offsetY + extraOffset);
				 else
					 y = -size.height - 1;
			 }
			 else
				 y += extraOffset;
			 popup.show(comp,x,y);
		 }
		 else
			 popup.show(comp,x + extraOffset,y + extraOffset);
	 }
}

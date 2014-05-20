package com.fss.swing;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class JXText extends JTextField implements TrackChangeListener,UndoableEditListener
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private String strCurrentValue = null;
	private boolean bStored = false;
	private boolean nextFocusOnEnter = false;
	private UndoManager undomanager = new UndoManager();
	private boolean mbHistory = false;
	private Border mbdr;
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public JXText()
	{
		MouseHandler mouseHandler = new MouseHandler();
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		setHistoryEnabled(true);

		setDocument(new MaskedDocument());
		setDisabledTextColor(java.awt.Color.BLACK);
		addKeyListener(new java.awt.event.KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				this_keyPressed(e);
			}
		});
		addFocusListener(new FocusListener()
		{
			public void focusLost(FocusEvent e)
			{
				if(isHistoryEnabled() && historyModel != null)
				{
					if(historyModel.popup != null &&
					   e.getOppositeComponent() != historyModel.popup.getRootPane())
						historyModel.addItem(getText());
				}
			}
			public void focusGained(FocusEvent e)
			{
				if(isHistoryEnabled())
					ensureModelAvailable();
			}
		});
		getDocument().addUndoableEditListener(this);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param e UndoableEditEvent
	 */
	////////////////////////////////////////////////////////
	public void undoableEditHappened(UndoableEditEvent e)
	{
		undomanager.addEdit(e.getEdit());
	}
	////////////////////////////////////////////////////////
	/**
	 * Undo change
	 */
	////////////////////////////////////////////////////////
	public void undo()
	{
		try
		{
			undomanager.undo();
		}
		catch(Exception e)
		{
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Redo change
	 */
	////////////////////////////////////////////////////////
	public void redo()
	{
		try
		{
			undomanager.redo();
		}
		catch(Exception e)
		{
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Filter implementation
	 */
	////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strFilter String
	 */
	////////////////////////////////////////////////////////
	public void setFilter(String strFilter)
	{
		((MaskedDocument)getDocument()).setFilter(strFilter);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param cLowerBound char
	 * @param cUpperBound char
	 */
	////////////////////////////////////////////////////////
	public void addFilterBoundary(char cLowerBound,char cUpperBound)
	{
		((MaskedDocument)getDocument()).addFilterBoundary(cLowerBound,cUpperBound);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iBoudaryIndex int
	 */
	////////////////////////////////////////////////////////
	public void removeFilterBoundary(int iBoudaryIndex)
	{
		((MaskedDocument)getDocument()).removeFilterBoundary(iBoudaryIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getFilterBoundaryCount()
	{
		return((MaskedDocument)getDocument()).getFilterBoundaryCount();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iBoudaryIndex int
	 * @return char
	 */
	////////////////////////////////////////////////////////
	public char getLowerFilterBoundary(int iBoudaryIndex)
	{
		return((MaskedDocument)getDocument()).getLowerFilterBoundary(iBoudaryIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iBoudaryIndex int
	 * @return char
	 */
	////////////////////////////////////////////////////////
	public char getUpperFilterBoundary(int iBoudaryIndex)
	{
		return((MaskedDocument)getDocument()).getUpperFilterBoundary(iBoudaryIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String getFilter()
	{
		return ((MaskedDocument)getDocument()).getFilter();
	}
	////////////////////////////////////////////////////////
	/**
	 * Allowance implementation
	 */
	////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param strAllowance String
	 */
	////////////////////////////////////////////////////////
	public void setAllowance(String strAllowance)
	{
		((MaskedDocument)getDocument()).setAllowance(strAllowance);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param cLowerBound char
	 * @param cUpperBound char
	 */
	////////////////////////////////////////////////////////
	public void addAllowanceBoundary(char cLowerBound,char cUpperBound)
	{
		((MaskedDocument)getDocument()).addAllowanceBoundary(cLowerBound,cUpperBound);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iBoudaryIndex int
	 */
	////////////////////////////////////////////////////////
	public void removeAllowanceBoundary(int iBoudaryIndex)
	{
		((MaskedDocument)getDocument()).removeAllowanceBoundary(iBoudaryIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return int
	 */
	////////////////////////////////////////////////////////
	public int getAllowanceBoundaryCount()
	{
		return((MaskedDocument)getDocument()).getAllowanceBoundaryCount();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iBoudaryIndex int
	 * @return char
	 */
	////////////////////////////////////////////////////////
	public char getLowerAllowanceBoundary(int iBoudaryIndex)
	{
		return((MaskedDocument)getDocument()).getLowerAllowanceBoundary(iBoudaryIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param iBoudaryIndex int
	 * @return char
	 */
	////////////////////////////////////////////////////////
	public char getUpperAllowanceBoundary(int iBoudaryIndex)
	{
		return((MaskedDocument)getDocument()).getUpperAllowanceBoundary(iBoudaryIndex);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public String getAllowance()
	{
		return ((MaskedDocument)getDocument()).getAllowance();
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param value boolean
	 */
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
	// Purpose: Backup current value of text field to variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void backup()
	{
		strCurrentValue = getText();
		bStored = true;
	}
	////////////////////////////////////////////////////////
	// Purpose: backup current value of text field to variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public String getBackupData()
	{
		return strCurrentValue;
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
		setText(strCurrentValue);
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
		Object obj = getText();
		if(obj == null || strCurrentValue == null)
		{
			if(obj == null && strCurrentValue == null)
				return false;
			else
				return true;
		}
		return !strCurrentValue.equals(obj);
	}
	////////////////////////////////////////////////////////
	// Purpose: Clear backup variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void clearBackup()
	{
		strCurrentValue = null;
		bStored = false;
	}
	////////////////////////////////////////////////////////
	// Purpose: Set max length
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void setMaxLength(int iMaxLength)
	{
		Document doc = getDocument();
		if(doc != null && doc instanceof LimitedDocument)
			((LimitedDocument)doc).setMaxLength(iMaxLength);
	}
	////////////////////////////////////////////////////////
	// Purpose: Get max length
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public int getMaxLength()
	{
		Document doc = getDocument();
		if(doc != null && doc instanceof LimitedDocument)
			return ((LimitedDocument)doc).getMaxLength();
		return -1;
	}
	////////////////////////////////////////////////////////
	// Purpose: Set mask
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void setMask(String strMask)
	{
		Document doc = getDocument();
		if(doc != null && doc instanceof MaskedDocument)
			((MaskedDocument)doc).setMask(strMask);
	}
	////////////////////////////////////////////////////////
	// Purpose: Set mask
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public String getMask()
	{
		Document doc = getDocument();
		if(doc != null && doc instanceof MaskedDocument)
			return ((MaskedDocument)doc).getMask();
		return null;
	}
	////////////////////////////////////////////////////////
	private void this_keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == e.VK_ENTER && getNextFocusOnEnter())
		{
			transferFocus();
			e.consume();
		}
		else if(e.getKeyCode() == e.VK_Z && e.isControlDown())
		{
			undo();
			e.consume();
		}
		else if(e.getKeyCode() == e.VK_Y && e.isControlDown())
		{
			redo();
			e.consume();
		}
		else if(e.getKeyCode() == KeyEvent.VK_UP)
		{
			if(isHistoryEnabled() && historyModel != null)
			{
				String str = getText();
				if(str.length() > 0 && !historyModel.isItemExist(str))
				{
					historyModel.addItem(str);
					historyModel.index = 0;
				}
				setText(historyModel.historyPrevious());
				e.consume();
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_DOWN)
		{
			if(isHistoryEnabled() && historyModel != null)
			{
				String str = getText();
				if(str.length() > 0 && !historyModel.isItemExist(str))
				{
					historyModel.addItem(str);
					historyModel.index = 0;
				}
				if(e.isAltDown())
					historyModel.showPopupMenu(this);
				else
					setText(historyModel.historyNext());
				e.consume();
			}
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Implementing history
	 * Modify code from JEdit HistoryTextField
	 */
	////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////
	/**
	 * Sets if all text should be selected when the field gets focus.
	 * @since jEdit 4.0pre3
	 * @return HistoryModel
	 */
	////////////////////////////////////////////////////////
	public HistoryModel getModel()
	{
		return historyModel;
	}
	////////////////////////////////////////////////////////
	/**
	 * Sets the history list model.
	 * @param name The model name
	 * @since jEdit 2.3pre3
	 */
	////////////////////////////////////////////////////////
	public void setModel(String name)
	{
		historyModel = HistoryModel.getModel(name);
		repaint();
	}
	////////////////////////////////////////////////////////
	protected void processMouseEvent(MouseEvent evt)
	{
		if(!isEnabled())
			return;
		switch(evt.getID())
		{
			case MouseEvent.MOUSE_PRESSED:
				if(isHistoryEnabled())
				{
					Border border = getBorder();
					Insets insets = border.getBorderInsets(JXText.this);
					if(evt.getX() >= getWidth() - insets.right || evt.isPopupTrigger())
					{
						ensureModelAvailable();
						historyModel.showPopupMenu(this);
					}
					else
						super.processMouseEvent(evt);
					break;
				}
			case MouseEvent.MOUSE_EXITED:
				if(isHistoryEnabled())
				{
					setCursor(Cursor.getDefaultCursor());
					super.processMouseEvent(evt);
					break;
				}
			default:
				super.processMouseEvent(evt);
				break;
		}
	}
	////////////////////////////////////////////////////////
	private HistoryModel historyModel;
	////////////////////////////////////////////////////////
	class MouseHandler extends MouseInputAdapter
	{
		public void mouseMoved(MouseEvent evt)
		{
			if(isHistoryEnabled())
			{
				Border border = getBorder();
				Insets insets = border.getBorderInsets(JXText.this);
				if(evt.getX() >= getWidth() - insets.right)
					setCursor(Cursor.getDefaultCursor());
				else
					setCursor(Cursor.getPredefinedCursor(
						Cursor.TEXT_CURSOR));
			}
		}
	}
	////////////////////////////////////////////////////////
	static class HistoryBorder extends AbstractBorder
	{
		static final int WIDTH = 16;
		public void paintBorder(Component c, Graphics g,int x, int y, int w, int h)
		{
			g.translate(x+w-WIDTH,y-1);
			int w2 = WIDTH/2;
			int h2 = h/2;
			g.setColor(UIManager.getColor(c.isEnabled()
										  ? "TextField.foreground" : "TextField.disabledForeground"));
			g.drawLine(w2-5,h2-2,w2+4,h2-2);
			g.drawLine(w2-4,h2-1,w2+3,h2-1);
			g.drawLine(w2-3,h2  ,w2+2,h2  );
			g.drawLine(w2-2,h2+1,w2+1,h2+1);
			g.drawLine(w2-1,h2+2,w2  ,h2+2);

			g.translate(-(x+w-WIDTH),-(y-1));
		}
		public Insets getBorderInsets(Component c)
		{
			return new Insets(0,0,0,WIDTH);
		}
	}
	public void ensureModelAvailable()
	{
		if(getModel() == null)
			setModel(WindowManager.generateName(this));
		else
		{
			String strNewName = WindowManager.generateName(this);
			int iIndex = strNewName.indexOf("|");
			if(iIndex >= 0)
				strNewName = strNewName.substring(iIndex + 1,strNewName.length());
			if(!strNewName.equals(getModel().getName()))
				setModel(strNewName);
		}
	}
	public void processKeyEvent(KeyEvent e)
	{
		if(isHistoryEnabled() && historyModel != null)
		{
			if(historyModel.popup != null && historyModel.popup.isVisible())
			{
				int iCode = e.getKeyCode();
				if(iCode == e.VK_ESCAPE || iCode == e.VK_TAB)
					MenuSelectionManager.defaultManager().clearSelectedPath();
				else if(iCode == e.VK_SPACE || iCode == e.VK_ENTER)
				{
					setText(com.fss.util.StringUtil.nvl(historyModel.list.getSelectedValue(),""));
					MenuSelectionManager.defaultManager().clearSelectedPath();
				}
				else
				{
					e.setSource(historyModel.list);
					historyModel.list.dispatchEvent(e);
				}
				return;
			}
			else
			{
				int iCode = e.getKeyCode();
				if(iCode == e.VK_ENTER)
					historyModel.addItem(getText());
			}
		}
		super.processKeyEvent(e);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean isHistoryEnabled()
	{
		return mbHistory;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bHistory boolean
	 */
	////////////////////////////////////////////////////////
	public void setHistoryEnabled(boolean bHistory)
	{
		if(bHistory != mbHistory)
		{
			mbHistory = bHistory;
			if(mbdr == null)
				mbdr = getBorder();
			if(isHistoryEnabled())
			{
				setBorder(new CompoundBorder(mbdr,new HistoryBorder()));
				if(isFocusOwner())
					ensureModelAvailable();
			}
			else
				setBorder(mbdr);
		}
	}
}

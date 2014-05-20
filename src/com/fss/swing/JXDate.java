package com.fss.swing;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.border.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class JXDate extends JTextField implements TrackChangeListener,UndoableEditListener
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private String mstrOldValue = null;
	private String strCurrentValue = null;
	private boolean bStored = false;
	private boolean nextFocusOnEnter = false;
	private UndoManager undomanager = new UndoManager();
	private boolean mbHistory = false;
	private Border mbdr;
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public JXDate()
	{
		MouseHandler mouseHandler = new MouseHandler();
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		setHistoryEnabled(true);

		setDocument(new DateDocument(this));
		setDisabledTextColor(java.awt.Color.BLACK);
		addFocusListener(new FocusListener()
		{
			public void focusLost(FocusEvent e)
			{
				if(isHistoryEnabled() && historyModel != null)
				{
					if(historyModel.popup != null &&
					   e.getOppositeComponent() != historyModel.popup.getRootPane())
					{
						DateDocument doc = (DateDocument)getDocument();
						if(getText().length() > 0 && (doc.iDValue <= 0 || doc.iMValue <= 0 || doc.iYValue <= 0))
							setText(mstrOldValue);
						historyModel.addItem(getText());
					}
				}
			}
			public void focusGained(FocusEvent e)
			{
				if(isHistoryEnabled())
					ensureModelAvailable();
				mstrOldValue = getText();
			}
		});
		this.addKeyListener(new java.awt.event.KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				this_keyPressed(e);
			}
		});
		getDocument().addUndoableEditListener(this);
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
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean getNextFocusOnEnter()
	{
		return nextFocusOnEnter;
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
	// Purpose: Set format
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void setFormat(String strFormat)
	{
		Document doc = getDocument();
		if(doc != null && doc instanceof DateDocument)
			((DateDocument)doc).setFormat(strFormat);
	}
	////////////////////////////////////////////////////////
	// Purpose: Set mask
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public String getFormat()
	{
		Document doc = getDocument();
		if(doc != null && doc instanceof DateDocument)
			return ((DateDocument)doc).getFormat();
		return null;
	}
	////////////////////////////////////////////////////////
	public String getText()
	{
		DateDocument doc = (DateDocument)getDocument();
		if(super.getText().length() > 0 && (doc.iDValue <= 0 || doc.iMValue <= 0 || doc.iYValue <= 0))
			setText(mstrOldValue);
		return super.getText();
	}
	////////////////////////////////////////////////////////
	private void this_keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == e.VK_ENTER && getNextFocusOnEnter())
		{
			this.transferFocus();
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
		historyModel.index = -1;
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
					Insets insets = border.getBorderInsets(JXDate.this);
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
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			int ind = Integer.parseInt(evt.getActionCommand());
			if(ind == -1)
				setText("");
			else
				setText(((JMenuItem)evt.getSource()).getText());
		}
	}
	////////////////////////////////////////////////////////
	class MouseHandler extends MouseInputAdapter
	{
		public void mouseMoved(MouseEvent evt)
		{
			ensureModelAvailable();
			Border border = getBorder();
			Insets insets = border.getBorderInsets(JXDate.this);
			if(evt.getX() >= getWidth() - insets.right)
				setCursor(Cursor.getDefaultCursor());
			else
				setCursor(Cursor.getPredefinedCursor(
					Cursor.TEXT_CURSOR));
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

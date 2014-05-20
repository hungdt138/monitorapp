package com.fss.swing;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.undo.*;
import javax.swing.event.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class JXTextArea extends JTextArea implements
	TrackChangeListener,KeyListener,UndoableEditListener
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private String strCurrentValue = null;
	private boolean bStored = false;
	private UndoManager undomanager = new UndoManager();
	////////////////////////////////////////////////////////
	/**
	 * Constructor
	 */
	////////////////////////////////////////////////////////
	public JXTextArea()
	{
		addKeyListener(this);
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
	/**
	 * Implements key listeneer for tab action
	 * @param e KeyEvent
	 */
	////////////////////////////////////////////////////////
	public void keyTyped(KeyEvent e)
	{
		if(e.getKeyChar() == '\t')
		{
			if(e.isShiftDown())
				this.transferFocusBackward();
			else
				this.transferFocus();
			e.consume();
		}
	}
	////////////////////////////////////////////////////////
	public void keyReleased(KeyEvent e)
	{
	}
	////////////////////////////////////////////////////////
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyChar() == '\t')
			e.consume();
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
	}
}

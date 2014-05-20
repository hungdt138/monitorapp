package com.fss.swing;

import javax.swing.*;
import java.awt.event.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class JXCheckBox extends JCheckBox implements TrackChangeListener
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	public boolean bCurrentValue = false;
	private boolean bStored = false;
	private boolean nextFocusOnEnter = false;
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public JXCheckBox()
	{
		super();
		this.addKeyListener(new java.awt.event.KeyAdapter(){
			public void keyPressed(KeyEvent e)
			{
				this_keyPressed(e);
			}
		});
	}
	////////////////////////////////////////////////////////
	public JXCheckBox(String str)
	{
		super(str);
		this.addKeyListener(new java.awt.event.KeyAdapter(){
			public void keyPressed(KeyEvent e)
			{
				this_keyPressed(e);
			}
		});
	}
	////////////////////////////////////////////////////////
	// Purpose: backup current value of text field to variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void backup()
	{
		bCurrentValue = isSelected();
		bStored = true;
	}
	////////////////////////////////////////////////////////
	// Purpose: backup current value of text field to variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public boolean getBackupData()
	{
		return bCurrentValue;
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
		setSelected(bCurrentValue);
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
		return (bCurrentValue != isSelected());
	}
	////////////////////////////////////////////////////////
	// Purpose: Clear backup variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void clearBackup()
	{
		bStored = false;
	}
	////////////////////////////////////////////////////////
	private void this_keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == e.VK_ENTER && getNextFocusOnEnter())
			this.transferFocus();
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
}

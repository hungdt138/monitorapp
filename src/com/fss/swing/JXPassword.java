package com.fss.swing;

import javax.swing.JPasswordField;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class JXPassword extends JPasswordField implements TrackChangeListener
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	public String strCurrentValue = null;
	private boolean bStored = false;
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public JXPassword()
	{
		setDocument(new LimitedDocument());
	}
	////////////////////////////////////////////////////////
	// Purpose: Backup current value of text field to variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void backup()
	{
		strCurrentValue = new String(getPassword());
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
		Object obj = new String(getPassword());
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
	// Purpose: Clear backup variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void setMaxLength(int iMaxLength)
	{
		((LimitedDocument)getDocument()).setMaxLength(iMaxLength);
	}
}

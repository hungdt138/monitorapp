package com.fss.swing;

import javax.swing.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ExternalEditorComponent extends JButton
{
	public Object getValue()
	{
		return super.getText();
	}

	public void setValue(Object objValue)
	{
		if(objValue == null)
			objValue = "";
		super.setText(objValue.toString());
	}
}
package com.fss.monitor;

import java.util.*;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import com.fss.util.*;
import com.fss.swing.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

class TableParameterEditor extends JButton implements ControlableValue
{
	private Component cmpCaller;
	private boolean mbModal;
	private String mstrTitle;
	private Vector mvtDefinition;
	private Vector mvtValue;
	public TableParameterEditor(Component cmp,boolean bModal,
								String strTitle,Vector vtDefinition)
	{
		cmpCaller = cmp;
		mbModal = bModal;
		mstrTitle = strTitle;
		mvtDefinition = vtDefinition;
		addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onClick();
			}
		});
	}
	private void onClick()
	{
		try
		{
			DialogTableParameter dlg = new DialogTableParameter(cmpCaller,mbModal,mstrTitle,mvtDefinition,mvtValue);
			WindowManager.centeredWindow(dlg);
			cmpCaller.repaint();
			setValue(mvtValue);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(this,e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}
	public Object getValue()
	{
		return mvtValue;
	}
	public void setValue(Object objValue)
	{
		if(objValue == null || !(objValue instanceof Vector))
			mvtValue = new Vector();
		else
			mvtValue = (Vector)objValue;
		setText(mvtValue.toString());
	}
	public Object getDisplayValue()
	{
		return mvtValue;
	}
}

package com.fss.swing;

import java.text.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboPopup;

/**
 * <p>Title: JXDatePopup</p>
 * <p>Copyright: Copyright (c) 2001-2003</p>
 * <p>Company: NCI Projects</p>
 * @author Vadim Crits
 * @version 1.1
 */

public class JXDatePopup extends BasicComboPopup
{
	protected JXCalendar popupEditor;

	public JXDatePopup(JComboBox comboBox,JXCalendar popupEditor)
	{
		super(comboBox);
		this.popupEditor = popupEditor;
		setFocusEnabled(popupEditor,false);
		add(popupEditor);
	}

	protected void setFocusEnabled(Component component,boolean flag)
	{
		if(component == null)
			return;
		if(component instanceof JComponent)
		{
			JComponent jcomponent = (JComponent)component;
			if(flag != jcomponent.isRequestFocusEnabled())
				jcomponent.setRequestFocusEnabled(flag);
		}
		if(component instanceof Container)
		{
			Component components[] = ((Container)component).getComponents();
			for(int i = 0;i < components.length;i++)
				setFocusEnabled(components[i],flag);
		}
	}

	protected void configurePopup()
	{
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setBorderPainted(true);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black),BorderFactory.createEmptyBorder(2,2,2,2)));
		setOpaque(false);
		setDoubleBuffered(true);
		setRequestFocusEnabled(false);
	}

	public void show()
	{
		if(popupEditor == null)
			return;
		syncPopupDataWithPickerData();
		popupEditor.setPreferredSize(null);
		setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
		show(comboBox,0,comboBox.getSize().height);
	}

	public void syncPopupDataWithPickerData()
	{
		Locale l = comboBox.getLocale();
		if(!popupEditor.getLocale().equals(l))
			popupEditor.setLocale(l);
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern(((JXDatePlus)comboBox).getFormat());
		if(!sdf.format(popupEditor.getDate()).equals(comboBox.getSelectedItem()))
			comboBox.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,""));
	}
}
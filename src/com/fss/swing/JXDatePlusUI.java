package com.fss.swing;

import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;

/**
 * <p>Title: JXDatePlusUI</p>
 * <p>Copyright: Copyright (c) 2001-2003</p>
 * <p>Company: NCI Projects</p>
 * @author Vadim Crits
 * @version 1.1
 */

public class JXDatePlusUI extends MetalComboBoxUI
{
  protected JXCalendar popupEditor;

  public static ComponentUI createUI(JComponent c)
  {
	return new JXDatePlusUI();
  }

  protected ComboPopup createPopup()
  {
	popupEditor = new JXCalendar();
	JXDatePopup popup = new JXDatePopup(comboBox, popupEditor);
	popup.getAccessibleContext().setAccessibleParent(comboBox);
	return popup;
  }

  public JXCalendar getPopupEditor()
  {
	return popupEditor;
  }
}
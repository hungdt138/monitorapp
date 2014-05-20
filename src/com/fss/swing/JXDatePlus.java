package com.fss.swing;

import java.beans.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;

/**
 * <p>Title: JXDatePlus</p>
 * <p>Copyright: Copyright (c) 2001-2003</p>
 * <p>Company: NCI Projects</p>
 * @author Vadim Crits
 * @version 1.1
 */

public class JXDatePlus extends JComboBox implements PropertyChangeListener,TrackChangeListener
{
	private String strFormat;
	private SimpleDateFormat sdf;
	protected JXCalendar popupEditor;
	private boolean bEditing = false;
	private String mstrOldValue;
	private boolean nextFocusOnEnter = false;

	public JXDatePlus()
	{
		JTextField txtEditor = ((JTextField)getEditor().getEditorComponent());
		getEditor().getEditorComponent().addKeyListener(new java.awt.event.KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				this_keyPressed(e);
			}
		});

		DateDocument doc = new DateDocument(txtEditor)
		{
			public void insertString(int iOffset,String strInsert,AttributeSet attrib) throws BadLocationException
			{
				super.insertString(iOffset,strInsert,attrib);
				bEditing = true;
				if(iDValue >= 0 && iMValue >= 0 && iYValue >= 0)
				{
					try
					{
						JTextField txtEditor = (JTextField)getEditor().getEditorComponent();
						popupEditor.setDate(sdf.parse(txtEditor.getText()));
					}
					catch(Exception ex)
					{
						setDate(popupEditor.getDate());
					}
				}
				bEditing = false;
			}
		};
		txtEditor.setDocument(doc);
		setEditable(true);
		sdf = new SimpleDateFormat();
		setFormat("dd/MM/yyyy");
		txtEditor.addFocusListener(new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
				mstrOldValue = getText();
			}
			public void focusLost(FocusEvent e)
			{
				JTextField txtEditor = ((JTextField)getEditor().getEditorComponent());
				DateDocument doc = (DateDocument)txtEditor.getDocument();
				if(getText().length() > 0 && ((doc.iDValue <= 0 && doc.getFormat().indexOf("D") > 0) ||
											 (doc.iMValue <= 0 && doc.getFormat().indexOf("M") > 0) ||
											 (doc.iYValue <= 0 && doc.getFormat().indexOf("Y") > 0)))
					setText(mstrOldValue);
			}
		});
		addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent e)
			{
				JTextField txtEditor = (JTextField)getEditor().getEditorComponent();
				txtEditor.requestFocusInWindow();
			}
		});
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
	////////////////////////////////////////////////////////
	public void updateUI()
	{
		setUI(JXDatePlusUI.createUI(this));
		popupEditor = ((JXDatePlusUI)ui).getPopupEditor();
		popupEditor.addPropertyChangeListener(this);
	}
	////////////////////////////////////////////////////////
	public void setLocale(Locale l)
	{
		super.setLocale(l);
		if(ui != null)
			popupEditor.setLocale(l);
	}
	////////////////////////////////////////////////////////
	/**
	 * Returns the date of this component.
	 * @return the value of the date property.
	 * @see #setDate
	 */
	/*public Date getDate()
	{
		return popupEditor.getDate();
	}*/

	/**
	 * Sets the date property.
	 * @param date the new date value for this component
	 * @see #getDate
	 */
	public void setDate(Date date)
	{
		popupEditor.setDate(date);
	}

	/**
	 * Sets the date property.
	 * @param date the new date value for this component
	 * @see #getDate
	 */
	public void setText(String strText)
	{
		((JTextField)this.getEditor().getEditorComponent()).setText(strText);
	}

	/**
	 * Returns the formated value of this component.
	 * @return the value of the date property.
	 * @see #setDate
	 */
	public String getText()
	{
		return ((JTextField)this.getEditor().getEditorComponent()).getText();
	}

	/**
	 * Returns the format.
	 * @return the value of the format property
	 * @see #setFormat
	 */
	public String getFormat()
	{
		return strFormat;
	}

	/**
	 * Sets the format property.
	 * @param format the new date and time pattern for this date format
	 * @see #getFormat
	 */
	public void setFormat(String strFormat)
	{
		String strOldFormat = this.strFormat;
		this.strFormat = strFormat;
		sdf.applyPattern(strFormat);
		if(((JTextField)this.getEditor().getEditorComponent()).getText().length() > 0)
			setText(sdf.format(popupEditor.getDate()));
		firePropertyChange("Format",strOldFormat,strFormat);
		DateDocument doc = (DateDocument)((JTextField)getEditor().getEditorComponent()).getDocument();
		strFormat = strFormat.replaceAll("d","D");
		strFormat = strFormat.replaceAll("m","M");
		strFormat = strFormat.replaceAll("y","Y");
		strFormat = strFormat.replaceAll("DD","D");
		strFormat = strFormat.replaceAll("MM","M");
		strFormat = strFormat.replaceAll("YYYY","Y");
		strFormat = strFormat.replaceAll(doc.mchrSeperator + "","");
		doc.setFormat(strFormat);
	}

	public void actionPerformed(ActionEvent e)
	{
		super.actionPerformed(e);
		try
		{
			JTextField txtEditor = (JTextField)getEditor().getEditorComponent();
			popupEditor.setDate(sdf.parse(txtEditor.getText()));
		}
		catch(Exception ex)
		{
			setDate(popupEditor.getDate());
		}
	}

	public void propertyChange(PropertyChangeEvent e)
	{
		String propertyName = e.getPropertyName();
		if(!bEditing && (propertyName.equals("date") || propertyName.equals("day") || propertyName.equals("month") || propertyName.equals("year")))
			setText(sdf.format(popupEditor.getDate()));
	}

	public Object getSelectedItem()
	{
		return getText();
	}

	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	public String strCurrentValue = null;
	private boolean bStored = false;
	////////////////////////////////////////////////////////
	// Purpose: backup current value of text field to variable
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
	private void this_keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == e.VK_ENTER && getNextFocusOnEnter())
			getEditor().getEditorComponent().transferFocus();
	}
}

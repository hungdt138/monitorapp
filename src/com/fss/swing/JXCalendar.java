package com.fss.swing;

import java.beans.*;
import java.util.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * <p>Title: JXCalendar</p>
 * <p>Copyright: Copyright (c) 2001-2003</p>
 * <p>Company: NCI Projects</p>
 * @author Vadim Crits
 * @version 1.1
 */

/* With special thanks to Kai Toedter */

public class JXCalendar extends JPanel implements PropertyChangeListener,TrackChangeListener
{
	private Locale locale;
	private Calendar calendar;
	private String[] monthNames;
	private String[] dayNames;
	private JPanel panel;
	protected JDayChooser dayChooser;
	protected JMonthChooser monthChooser;
	protected JYearChooser yearChooser;

	public JXCalendar()
	{
		setLayout(new BorderLayout());
		addPropertyChangeListener(this);
		locale = Locale.getDefault();
		calendar = Calendar.getInstance(locale);
		dayChooser = new JDayChooser();
		monthChooser = new JMonthChooser();
		yearChooser = new JYearChooser();
		panel = new JPanel(new GridBagLayout());
		panel.add(monthChooser,new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		panel.add(yearChooser,new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(0,0,0,0),1,0));
		add(panel,BorderLayout.NORTH);
		add(dayChooser,BorderLayout.CENTER);
		init();
	}

	private void init()
	{
		DateFormatSymbols dfs = new DateFormatSymbols(locale);
		dayNames = dfs.getShortWeekdays();
		monthNames = dfs.getMonths();
		setDate(calendar.getTime());
		dayChooser.paintDays();
	}

	/**
	 * Returns the locale.
	 * @return the value of the locale property.
	 * @see #setLocale
	 */
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * Sets the locale of this component.
	 * @param l The locale to become this component's locale.
	 * @see #getLocale
	 */
	public void setLocale(Locale l)
	{
		super.setLocale(l);
		Locale oldLocale = locale;
		locale = l;
		firePropertyChange("locale",oldLocale,l);
	}

	/**
	 * Returns the date.
	 * @return the value of the date property.
	 * @see #setDate
	 */
	public Date getDate()
	{
		return calendar.getTime();
	}

	/**
	 * Sets the date property.
	 * @param date the new date value for this component
	 * @see #getDate
	 */
	public void setDate(Date date)
	{
		Date oldDate = calendar.getTime();
		calendar.setTime(date);
		dayChooser.setValue(calendar.get(Calendar.DAY_OF_MONTH));
		monthChooser.setValue(calendar.get(Calendar.MONTH));
		yearChooser.setValue(calendar.get(Calendar.YEAR));
		firePropertyChange("date",oldDate,date);
	}

	/**
	 * Returns the day.
	 * @return the value of the day property.
	 * @see #setDay
	 */
	public int getDay()
	{
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Sets the day property.
	 * @param day the new day value for this component
	 * @see #getDay
	 */
	public void setDay(int day)
	{
		int oldDay = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.add(Calendar.DAY_OF_MONTH,day - oldDay);
		int newDay = calendar.get(Calendar.DAY_OF_MONTH);
		if(day != newDay)
			day = newDay;
		if(day != dayChooser.getValue())
			dayChooser.setValue(day);
		firePropertyChange("day",oldDay,day);
	}

	/**
	 * Returns the month.
	 * @return the value of the month property.
	 * @see #setMonth
	 */
	public int getMonth()
	{
		return calendar.get(Calendar.MONTH);
	}

	/**
	 * Sets the month property.
	 * @param month the new month value for this component
	 * @see #getMonth
	 */
	public void setMonth(int month)
	{
		int oldMonth = calendar.get(Calendar.MONTH);
		calendar.add(Calendar.MONTH,month - oldMonth);
		int newMonth = calendar.get(Calendar.MONTH);
		if(month != newMonth)
			month = newMonth;
		if(month != monthChooser.getValue())
			monthChooser.setValue(month);
		firePropertyChange("month",oldMonth,month);
	}

	/**
	 * Returns the year.
	 * @return the value of the year property.
	 * @see #setYear
	 */
	public int getYear()
	{
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * Sets the year property.
	 * @param year the new year value for this component
	 * @see #getYear
	 */
	public void setYear(int year)
	{
		int oldyear = calendar.get(Calendar.YEAR);
		calendar.add(Calendar.YEAR,year - oldyear);
		int newYear = calendar.get(Calendar.YEAR);
		if(year != newYear)
			year = newYear;
		if(year != yearChooser.getValue())
			yearChooser.setValue(year);
		firePropertyChange("year",oldyear,year);
	}

	public void propertyChange(PropertyChangeEvent e)
	{
		String propertyName = e.getPropertyName();
		if(propertyName.equals("locale"))
		{
			Date saveDate = calendar.getTime();
			calendar = calendar.getInstance((Locale)e.getNewValue());
			calendar.setTime(saveDate);
			init();
		}
		else if(propertyName.equals("date") || propertyName.equals("month") || propertyName.equals("year"))
			dayChooser.paintDays();
	}

	protected class JDayChooser extends JPanel implements ActionListener
	{
		private JButton[] days = new JButton[49];
		private JButton selectedDay;
		private Color oldDayBackground;

		public JDayChooser()
		{
			setLayout(new GridLayout(7,7));
			for(int y = 0;y < 7;y++)
				for(int x = 0;x < 7;x++)
				{
					int index = x + 7 * y;
					days[index] = new JButton();
					if(y == 0)
						days[index].setBackground(new Color(204,204,255));
					else
					{
						days[index].addActionListener(this);
						days[index].setFont(new Font("Dialog",0,12));
					}
					days[index].setMargin(new Insets(0,0,0,0));
					days[index].setFocusPainted(false);
					days[index].setRequestFocusEnabled(false);
					add(days[index]);
				}
			oldDayBackground = (new JButton()).getBackground();
		}

		protected void paintDays()
		{
			Calendar today = Calendar.getInstance(locale);
			Calendar tmpCalendar = (Calendar)calendar.clone();
			int firstDayOfWeek = tmpCalendar.getFirstDayOfWeek();
			int day = firstDayOfWeek;
			int i;
			for(i = 0;i < 7;i++)
			{
				days[i].setText(dayNames[day]);
				if(day == 1)
					days[i].setForeground(Color.red);
				else
					days[i].setForeground(Color.blue);
				if(day < 7)
					day++;
				else
					day -= 6;
			}
			tmpCalendar.set(Calendar.DAY_OF_MONTH,1);
			int firstDay = tmpCalendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;
			if(firstDay < 0)
				firstDay += 7;
			for(i = 0;i < firstDay;i++)
			{
				days[i + 7].setVisible(false);
				days[i + 7].setText("");
			}
			tmpCalendar.add(Calendar.MONTH,1);
			Date firstDayInNextMonth = tmpCalendar.getTime();
			tmpCalendar.add(Calendar.MONTH, -1);
			Date date = tmpCalendar.getTime();
			Color foreground = getForeground();
			int n = 0;
			while(date.before(firstDayInNextMonth))
			{
				days[i + n + 7].setText(Integer.toString(n + 1));
				days[i + n + 7].setVisible(true);
				if(tmpCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) && tmpCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR))
					days[i + n + 7].setForeground(Color.red);
				else
					days[i + n + 7].setForeground(foreground);
				if(n + 1 == calendar.get(Calendar.DAY_OF_MONTH))
				{
					days[i + n + 7].setBackground(Color.gray);
					selectedDay = days[i + n + 7];
				}
				else
					days[i + n + 7].setBackground(oldDayBackground);
				n++;
				tmpCalendar.add(Calendar.DATE,1);
				date = tmpCalendar.getTime();
			}
			for(int k = n + i + 7;k < 49;k++)
			{
				days[k].setVisible(false);
				days[k].setText("");
			}
		}

		protected int getValue()
		{
			if(selectedDay != null)
				return Integer.parseInt(selectedDay.getText());
			else
				return 1;
		}

		protected void setValue(int value)
		{
			if(selectedDay != null)
				selectedDay.setBackground(oldDayBackground);

			for(int i = 7;i < 49;i++)
			{
				if(days[i].getText().equals(Integer.toString(value)))
				{
					selectedDay = days[i];
					selectedDay.setBackground(Color.gray);
					break;
				}
			}
			if(value != calendar.get(Calendar.DAY_OF_MONTH))
				setDay(value);
		}

		public void actionPerformed(ActionEvent e)
		{
			setValue(Integer.parseInt(((JButton)e.getSource()).getText()));
			int newDay = calendar.get(Calendar.DAY_OF_MONTH);
			fireDayChanged(newDay);
			Container parent = getParent();
			while(parent != null)
			{
				if(parent instanceof JPopupMenu)
				{
					((javax.swing.plaf.basic.BasicComboPopup)parent).hide();
					break;
				}
				parent = parent.getParent();
			}
		}
	}


	protected class JMonthChooser extends JXSpin
	{
		public JMonthChooser()
		{
			setMinimum(Calendar.JANUARY);
			setMaximum(Calendar.DECEMBER);
			getEditor().removeActionListener(this);
		}

		protected void setValue(int value,boolean updateEditor,boolean updateScroller)
		{
			super.setValue(value,false,updateScroller);
			getEditor().setText(monthNames[value]);
			if(value != calendar.get(Calendar.MONTH))
				setMonth(value);
		}
	}


	protected class JYearChooser extends JXSpin
	{
		private class NumberDocument extends PlainDocument
		{
			public void insertString(int offs,String str,AttributeSet a) throws BadLocationException
			{
				char[] source = str.toCharArray();
				char[] result = new char[source.length];
				int j = 0;
				for(int i = 0;i < result.length;i++)
				{
					if(Character.isDigit(source[i]))
						result[j++] = source[i];
					else
						Toolkit.getDefaultToolkit().beep();
				}
				super.insertString(offs,new String(result,0,j),a);
			}
		}


		public JYearChooser()
		{
			setMinimum(calendar.getMinimum(Calendar.YEAR));
			setMaximum(calendar.getMaximum(Calendar.YEAR));
			getEditor().setDocument(new NumberDocument());
		}

		protected void setValue(int value,boolean updateEditor,boolean updateScroller)
		{
			super.setValue(value,updateEditor,updateScroller);
			if(value != calendar.get(Calendar.YEAR))
				setYear(value);
		}
	}
	public void fireDayChanged(int iNewValue)
	{
		firePropertyChange("day",iNewValue - 1,iNewValue);
	}
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	public Date dtCurrentValue = null;
	private boolean bStored = false;
	////////////////////////////////////////////////////////
	// Purpose: backup current value of text field to variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void backup()
	{
		dtCurrentValue = getDate();
		bStored = true;
	}
	////////////////////////////////////////////////////////
	// Purpose: backup current value of text field to variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public Date getBackupData()
	{
		return dtCurrentValue;
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
		setDate(dtCurrentValue);
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
		Object obj = getDate();
		if(obj == null || dtCurrentValue == null)
		{
			if(obj == null && dtCurrentValue == null)
				return false;
			else
				return true;
		}
		return !dtCurrentValue.equals(obj);
	}
	////////////////////////////////////////////////////////
	// Purpose: Clear backup variable
	// Author: Thai Hoang Hiep
	// Date: 12/05/2003
	////////////////////////////////////////////////////////
	public void clearBackup()
	{
		dtCurrentValue = null;
		bStored = false;
	}
}

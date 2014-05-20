package com.fss.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * <p>Title: JXSpin</p>
 * <p>Copyright: Copyright (c) 2001-2003</p>
 * <p>Company: NCI Projects</p>
 * @author Vadim Crits
 * @version 1.1
 */

public class JXSpin extends JPanel implements ActionListener,AdjustmentListener,KeyListener
{
	private int minimum = 0;
	private int maximum = 0;
	private int value = 0;
	private int increment = 1;
	private JTextField editor;
	private JScrollBar scroller;

	public JXSpin()
	{
		setLayout(new BorderLayout());
		editor = new JTextField("0");
		editor.addActionListener(this);
		editor.addKeyListener(this);
		scroller = new JScrollBar();
		scroller.setPreferredSize(new Dimension(scroller.getPreferredSize().width,21));
		scroller.setMinimum(minimum);
		scroller.setMaximum(maximum);
		scroller.setValue(maximum - value);
		scroller.setUnitIncrement(increment);
		scroller.setBlockIncrement(increment);
		scroller.setVisibleAmount(0);
		scroller.setRequestFocusEnabled(false);
		scroller.addAdjustmentListener(this);
		add(editor,BorderLayout.CENTER);
		add(scroller,BorderLayout.EAST);
	}

	protected void setValue(int value,boolean updateEditor,boolean updateScroller)
	{
		int oldValue = this.value;
		if(value < minimum)
			this.value = minimum;
		else if(value > maximum)
			this.value = maximum;
		else
			this.value = value;
		if(updateEditor)
			editor.setText(Integer.toString(this.value));
		if(updateScroller)
		{
			scroller.removeAdjustmentListener(this);
			scroller.setValue(maximum - this.value);
			scroller.addAdjustmentListener(this);
		}
		firePropertyChange("value",oldValue,this.value);
	}

/**
	 * Returns the value.
	 * @return the value property.
	 * @see #setValue
	 */
	public int getValue()
	{
		return value;
	}

/**
	 * Sets the value property.
	 * @param value the new value for this component
	 * @see #getValue
	 */
	public void setValue(int value)
	{
		setValue(value,true,true);
	}

/**
	 * Returns the minimum.
	 * @return the value of the minimum property.
	 * @see #setMinimum
	 */
	public int getMinimum()
	{
		return minimum;
	}

/**
	 * Sets the minimum property.
	 * @param minimum the new minimum value for this component
	 * @see #getMinimum
	 */
	public void setMinimum(int minimum)
	{
		int oldMinimum = this.minimum;
		this.minimum = minimum;
		scroller.removeAdjustmentListener(this);
		scroller.setMinimum(minimum);
		scroller.addAdjustmentListener(this);
		firePropertyChange("minimum",oldMinimum,minimum);
	}

/**
	 * Returns the maximum.
	 * @return the value of the maximum property.
	 * @see #setMaximum
	 */
	public int getMaximum()
	{
		return maximum;
	}

/**
	 * Sets the maximum property.
	 * @param maximum the new maximum value for this component
	 * @see #getMaximum
	 */
	public void setMaximum(int maximum)
	{
		int oldMaximum = this.maximum;
		this.maximum = maximum;
		scroller.removeAdjustmentListener(this);
		scroller.setMaximum(maximum);
		scroller.addAdjustmentListener(this);
		firePropertyChange("maximum",oldMaximum,maximum);
	}

/**
	 * Returns the increment.
	 * @return the value of the increment property.
	 * @see #setIncrement
	 */
	public int getIncrement()
	{
		return increment;
	}

/**
	 * Sets the increment property.
	 * @param increment the new increment value for this component
	 * @see #getIncrement
	 */
	public void setIncrement(int increment)
	{
		int oldIncrement = this.increment;
		this.increment = increment;
		scroller.setUnitIncrement(increment);
		scroller.setBlockIncrement(increment);
		firePropertyChange("increment",oldIncrement,increment);
	}

	public JTextField getEditor()
	{
		return editor;
	}

	public void actionPerformed(ActionEvent e)
	{
		setValue(Integer.parseInt(editor.getText()),false,true);
	}

	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		setValue(maximum - e.getValue(),true,false);
	}

	public void keyTyped(KeyEvent e)
{};

	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_UP)
		{
			if(value != maximum)
				setValue(value + increment);
		}
		else if(e.getKeyCode() == KeyEvent.VK_DOWN)
		{
			if(value != minimum)
				setValue(value - increment);
		}
	}

	public void keyReleased(KeyEvent e)
	{
	};
}
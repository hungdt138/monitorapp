package com.fss.swing;

/*
 * @(#)JXComboButton.java	1.35 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

import javax.swing.plaf.metal.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * JButton subclass to help out JXComboUI
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @see JXComboButton
 * @version 1.35 01/23/03
 * @author Tom Santos
 * @author Thai Hoang Hiep - Change to use with VectorTable
 */
public class JXComboButton extends JButton
{
	protected JXCombo comboBox;
	protected VectorTable listBox;
	protected CellRendererPane rendererPane;
	protected Icon comboIcon;
	protected boolean iconOnly = false;

	public final JXCombo getComboBox()
	{
		return comboBox;
	}

	public final void setComboBox(JXCombo cb)
	{
		comboBox = cb;
	}

	public final Icon getComboIcon()
	{
		return comboIcon;
	}

	public final void setComboIcon(Icon i)
	{
		comboIcon = i;
	}

	public final boolean isIconOnly()
	{
		return iconOnly;
	}

	public final void setIconOnly(boolean isIconOnly)
	{
		iconOnly = isIconOnly;
	}

	JXComboButton()
	{
		super("");
		DefaultButtonModel model = new DefaultButtonModel()
		{
			public void setArmed(boolean armed)
			{
				super.setArmed(isPressed() ? true : armed);
			}
		};
		setModel(model);
	}

	public JXComboButton(JXCombo cb,Icon i,
						 CellRendererPane pane,VectorTable list)
	{
		this();
		comboBox = cb;
		comboIcon = i;
		rendererPane = pane;
		listBox = list;
		setEnabled(comboBox.isEnabled());
	}

	public JXComboButton(JXCombo cb,Icon i,boolean onlyIcon,
						 CellRendererPane pane,VectorTable list)
	{
		this(cb,i,pane,list);
		iconOnly = onlyIcon;
	}

	public boolean isFocusTraversable()
	{
		return false;
	}

	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);

		// Set the background and foreground to the combobox colors.
		if(enabled)
		{
			setBackground(comboBox.getBackground());
			setForeground(comboBox.getForeground());
		}
		else
		{
			setBackground(UIManager.getColor("ComboBox.disabledBackground"));
			setForeground(UIManager.getColor("ComboBox.disabledForeground"));
		}
	}

	public void paintComponent(Graphics g)
	{
		boolean leftToRight = comboBox.getComponentOrientation().isLeftToRight();

		// Paint the button as usual
		super.paintComponent(g);

		Insets insets = getInsets();

		int width = getWidth() - (insets.left + insets.right);
		int height = getHeight() - (insets.top + insets.bottom);

		if(height <= 0 || width <= 0)
		{
			return;
		}

		int left = insets.left;
		int top = insets.top;
		int right = left + (width - 1);
		int bottom = top + (height - 1);

		int iconWidth = 0;
		int iconLeft = (leftToRight) ? right : left;

		// Paint the icon
		if(comboIcon != null)
		{
			iconWidth = comboIcon.getIconWidth();
			int iconHeight = comboIcon.getIconHeight();
			int iconTop = 0;

			if(iconOnly)
			{
				iconLeft = (getWidth() / 2) - (iconWidth / 2);
				iconTop = (getHeight() / 2) - (iconHeight / 2);
			}
			else
			{
				if(leftToRight)
				{
					iconLeft = (left + (width - 1)) - iconWidth;
				}
				else
				{
					iconLeft = left;
				}
				iconTop = (top + ((bottom - top) / 2)) - (iconHeight / 2);
			}

			comboIcon.paintIcon(this,g,iconLeft,iconTop);

			// Paint the focus
			if(comboBox.hasFocus())
			{
				g.setColor(MetalLookAndFeel.getFocusColor());
				g.drawRect(left - 1,top - 1,width + 3,height + 1);
			}
		}

		// Let the renderer paint
		if(!iconOnly && comboBox != null)
		{
			VectorTableColumn col = listBox.getColumnEx(listBox.getDisplayIndex());
			TableCellRenderer renderer = null;
			if(col != null)
				renderer = col.getCellRenderer();
			else
				renderer = new DefaultTableCellRenderer();
			Component c = renderer.getTableCellRendererComponent(listBox,
				comboBox.getSelectedItem(),
				getModel().isPressed(),
				false, -1,0);
			c.setFont(rendererPane.getFont());

			if(model.isArmed() && model.isPressed())
			{
				if(isOpaque())
					c.setBackground(UIManager.getColor("Button.select"));
				c.setForeground(comboBox.getForeground());
			}
			else if(!comboBox.isEnabled())
			{
				if(isOpaque())
					c.setBackground(UIManager.getColor("ComboBox.disabledBackground"));
				c.setForeground(UIManager.getColor("ComboBox.disabledForeground"));
			}
			else
			{
				c.setForeground(comboBox.getForeground());
				c.setBackground(comboBox.getBackground());
			}

			int cWidth = width - (insets.right + iconWidth);

			// Fix for 4238829: should lay out the JPanel.
			boolean shouldValidate = false;
			if(c instanceof JPanel)
			{
				shouldValidate = true;
			}

			if(leftToRight)
			{
				rendererPane.paintComponent(g,c,this,
											left,top,cWidth,height,shouldValidate);
			}
			else
			{
				rendererPane.paintComponent(g,c,this,
											left + iconWidth,top,cWidth,height,shouldValidate);
			}
		}
	}
}

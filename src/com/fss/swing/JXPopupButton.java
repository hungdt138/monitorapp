package com.fss.swing;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

/**
 * This class looks like a normal JButton, but
 * displays a configurable JPopupMenu on a click
 *
 * @author James Abbatiello
 * @author David Ludwig
 * version 1.10, 10/04/99
 */

public class JXPopupButton extends JButton implements ActionListener
{
	/* The popup menu displayed when the button is clicked */
	public JPopupMenu popupMenu = new JPopupMenu();

	/**
	 * Constructor
	 * @param	s	A string which will be displayed on the button
	 */
	public JXPopupButton()
	{
		addActionListener(this);
		setIcon(new PopupIcon());
		setHorizontalTextPosition(SwingConstants.LEFT);
	}

	/**
	 * Constructor
	 * @param	s	A string which will be displayed on the button
	 */
	public JXPopupButton(String s)
	{
		this();
		setText(s);
	}

	/**
	 * Add a new entry into the popup menu with the given label.
	 * @param	label	A string which will be displayed on this menu item
	 * @param	handler	An {@link ActionHandler} which will listen for
	 *			events on this menu item (specifically, mouse clicks)
	 */
	public void addMenuItem(JMenuItem mnu)
	{
		popupMenu.add(mnu);
	}

	/**
	 * Called when button is clicked
	 * @param	e	The {@link ActionEvent} sent to this button (ignored)
	 */
	public void actionPerformed(ActionEvent e)
	{
		popupMenu.show(this,0,getHeight());
	}

	public void setEnabled(boolean bEnabled)
	{
		if(!bEnabled)
			popupMenu.setVisible(false);
		super.setEnabled(bEnabled);
	}


	class PopupIcon implements Icon
	{
		public int getIconWidth()
		{
			return 10;
		}

		public int getIconHeight()
		{
			return 5;
		}

		public void paintIcon(Component c,Graphics g,int x,int y)
		{
			g.translate(x,y);
			g.setColor(Color.black);
			int[] xpoints = {0,4,9};
			int[] ypoints = {0,5,0};
			g.fillPolygon(xpoints,ypoints,3);
			g.translate( -x, -y);
		}
	}
}

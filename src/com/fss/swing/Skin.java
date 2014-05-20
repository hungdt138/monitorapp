package com.fss.swing;

import javax.swing.text.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.border.*;

import com.fss.util.*;
import com.fss.vietnamese.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class Skin
{
	////////////////////////////////////////////////////////
	public static LanguageChangeListener LANGUAGE_CHANGE_LISTENER = null;
	public static KeyListener LANGUAGE_KEY_LISTENER = new KeyAdapter()
	{
		public void keyPressed(KeyEvent e)
		{
			if(e.getModifiers() == 8 && LANGUAGE_CHANGE_LISTENER != null)
			{
				if(String.valueOf(e.getKeyChar()).toUpperCase().equals("W"))
					LANGUAGE_CHANGE_LISTENER.switchKeyboard();
			}
		}
	};
	public static SwingVietKey VIETNAMESE_KEY = new SwingVietKey();
	public static Font FONT_SYSTEM;
	public static Font FONT_COMMON;
	public static Font FONT_BUTTON;
	public static Font FONT_LABEL;
	public static Font FONT_TEXTBOX;
	public static Font FONT_TEXTPANE;
	public static Font FONT_COMBOBOX;
	public static Font FONT_LISTBOX;
	public static Font FONT_TABLE;
	public static Font FONT_TABLEHEADER;
	public static Font FONT_MENU;

	public static Dimension BUTTON_MIN_SIZE = new Dimension(80,25);
	public static Insets BUTTON_MARGIN = new Insets(1,2,1,2);

	public static Border BORDER_RAISED = BorderFactory.createBevelBorder(BevelBorder.RAISED,SystemColor.controlLtHighlight,SystemColor.control,SystemColor.controlDkShadow,SystemColor.control);
	public static Border BORDER_LOWRED = BorderFactory.createBevelBorder(BevelBorder.LOWERED,SystemColor.controlLtHighlight,SystemColor.controlShadow,SystemColor.controlDkShadow,SystemColor.control);
	public static Border BORDER_ETCHED = BorderFactory.createEtchedBorder(SystemColor.white,SystemColor.controlDkShadow);

	static
	{
		Font fntCommon = null;
		InputStream is = null;
		try
		{
			is = Skin.class.getResourceAsStream("/com/fss/resource/tahoma.ttf");
			fntCommon = Font.createFont(Font.TRUETYPE_FONT,is);
		}
		catch(Exception e)
		{
			fntCommon = new Font("Tahoma",Font.PLAIN,12);
		}
		finally
		{
			FileUtil.safeClose(is);
		}

		Font fntFixed = null;
		try
		{
			is = Skin.class.getResourceAsStream("/com/fss/resource/cour.ttf");
			fntFixed = Font.createFont(Font.TRUETYPE_FONT,is);
		}
		catch(Exception e)
		{
			fntFixed = new Font("Courier New",Font.PLAIN,12);
		}
		finally
		{
			FileUtil.safeClose(is);
		}

		FONT_COMMON = fntCommon.deriveFont(Font.PLAIN,12);
		FONT_BUTTON = fntCommon.deriveFont(Font.PLAIN,12);
		FONT_LABEL = fntCommon.deriveFont(Font.PLAIN,12);
		FONT_TEXTBOX = fntCommon.deriveFont(Font.PLAIN,12);
		FONT_COMBOBOX = fntCommon.deriveFont(Font.PLAIN,12);
		FONT_LISTBOX = fntCommon.deriveFont(Font.PLAIN,12);
		FONT_TABLE = fntCommon.deriveFont(Font.PLAIN,12);
		FONT_TABLEHEADER = fntCommon.deriveFont(Font.PLAIN,12);
		FONT_MENU = fntCommon.deriveFont(Font.PLAIN,12);
		FONT_SYSTEM = fntFixed.deriveFont(Font.PLAIN,12);
		FONT_TEXTPANE = fntFixed.deriveFont(Font.PLAIN,12);
	}
	////////////////////////////////////////////////////////
	// Function createTitledBorder
	// Porpuse: create skinned titled border
	////////////////////////////////////////////////////////
	public static Border createTableBorder(String strTitle)
	{
		Border bdrOutter = BorderFactory.createTitledBorder(Skin.BORDER_ETCHED,strTitle,TitledBorder.CENTER,TitledBorder.DEFAULT_POSITION);
		return BorderFactory.createCompoundBorder(bdrOutter,Skin.BORDER_ETCHED);
	}
	////////////////////////////////////////////////////////
	// Function applyBorderSkin
	// Porpuse: Set format for border
	////////////////////////////////////////////////////////
	public static void applyBorderSkin(Border bdr)
	{
		if(bdr instanceof CompoundBorder)
		{
			applyBorderSkin(((CompoundBorder)bdr).getInsideBorder());
			applyBorderSkin(((CompoundBorder)bdr).getOutsideBorder());
		}
		else if(bdr instanceof TitledBorder)
			((TitledBorder)bdr).setTitleFont(FONT_LABEL);
	}
	////////////////////////////////////////////////////////
	// Function applySkin
	// Porpuse: Set format for all component in cmpSrc
	////////////////////////////////////////////////////////
	public static void applySkin(Component cmpSrc)
	{
		////////////////////////////////////////////////////////
		// Process border
		////////////////////////////////////////////////////////
		if(cmpSrc instanceof JComponent)
			applyBorderSkin(((JComponent)cmpSrc).getBorder());
		////////////////////////////////////////////////////////
		// Process component
		////////////////////////////////////////////////////////
		if(cmpSrc instanceof JTable)
		{
			cmpSrc.setFont(FONT_TABLE);
			((JTable)cmpSrc).getTableHeader().setFont(FONT_TABLEHEADER);
			if(cmpSrc instanceof VectorTable)
			{
				VectorTable tblSrc = (VectorTable)cmpSrc;
				for(int iIndex = 0;iIndex < tblSrc.getColumnCount();iIndex++)
				{
					JComponent cmp = tblSrc.getColumnEditor(iIndex);
					if(cmp != null)
						cmp.setFont(FONT_TABLE);
				}
			}
		}
		else if(cmpSrc instanceof JComboBox)
		{
			cmpSrc.removeKeyListener(LANGUAGE_KEY_LISTENER);
			cmpSrc.addKeyListener(LANGUAGE_KEY_LISTENER);
			cmpSrc.setFont(FONT_COMBOBOX);
			((JComponent)cmpSrc).setMinimumSize(new Dimension(cmpSrc.getWidth(),22));
			((JComponent)cmpSrc).setPreferredSize(new Dimension(cmpSrc.getWidth(),22));
		}
		else if(cmpSrc instanceof JList)
		{
			cmpSrc.removeKeyListener(LANGUAGE_KEY_LISTENER);
			cmpSrc.addKeyListener(LANGUAGE_KEY_LISTENER);
			cmpSrc.setFont(FONT_LISTBOX);
		}
		else if(cmpSrc instanceof JTextPane ||
				cmpSrc instanceof JTextArea ||
				cmpSrc instanceof JEditorPane)
		{
			cmpSrc.removeKeyListener(VIETNAMESE_KEY);
			cmpSrc.addKeyListener(VIETNAMESE_KEY);
			cmpSrc.removeKeyListener(LANGUAGE_KEY_LISTENER);
			cmpSrc.addKeyListener(LANGUAGE_KEY_LISTENER);
			cmpSrc.setFont(FONT_TEXTPANE);
		}
		else if(cmpSrc instanceof JTextComponent)
		{
			cmpSrc.removeKeyListener(VIETNAMESE_KEY);
			cmpSrc.addKeyListener(VIETNAMESE_KEY);
			cmpSrc.removeKeyListener(LANGUAGE_KEY_LISTENER);
			cmpSrc.addKeyListener(LANGUAGE_KEY_LISTENER);
			cmpSrc.setFont(FONT_TEXTBOX);
			((JComponent)cmpSrc).setPreferredSize(new Dimension(cmpSrc.getWidth(),22));
			((JComponent)cmpSrc).setMinimumSize(new Dimension(cmpSrc.getWidth(),22));
		}
		else if(cmpSrc instanceof JMenuItem)
			cmpSrc.setFont(FONT_MENU);
		else if(cmpSrc instanceof AbstractButton)
		{
			cmpSrc.setFont(FONT_BUTTON);
			if(cmpSrc instanceof JButton)
			{
				if(!((JComponent)cmpSrc).isMinimumSizeSet())
					((JComponent)cmpSrc).setMinimumSize(BUTTON_MIN_SIZE);
				((AbstractButton)cmpSrc).setMargin(BUTTON_MARGIN);
			}
			cmpSrc.removeKeyListener(LANGUAGE_KEY_LISTENER);
			cmpSrc.addKeyListener(LANGUAGE_KEY_LISTENER);
		}
		else if(cmpSrc instanceof JLabel)
		{
			cmpSrc.setFont(FONT_LABEL);
			cmpSrc.removeKeyListener(LANGUAGE_KEY_LISTENER);
			cmpSrc.addKeyListener(LANGUAGE_KEY_LISTENER);
		}
		else
			cmpSrc.setFont(Skin.FONT_LABEL);
		////////////////////////////////////////////////////////
		// Process container
		////////////////////////////////////////////////////////
		if(cmpSrc instanceof JMenu)
		{
			JMenu mnuSrc = (JMenu)cmpSrc;
			int iCount = mnuSrc.getMenuComponentCount();
			for(int iIndex = 0;iIndex < iCount;iIndex++)
			{
				Component cmpChild = mnuSrc.getMenuComponent(iIndex);
				applySkin(cmpChild);
			}
		}
		else if(cmpSrc instanceof Container)
		{
			Container ctnSrc = (Container)cmpSrc;
			int iCount = ctnSrc.getComponentCount();
			for(int iIndex = 0;iIndex < iCount;iIndex++)
			{
				Component cmpChild = ctnSrc.getComponent(iIndex);
				applySkin(cmpChild);
			}
		}
	}
}

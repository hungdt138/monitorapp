package com.fss.swing;

import javax.swing.text.*;
import javax.swing.*;
import java.awt.*;

import com.fss.util.*;
import com.fss.dictionary.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Nguyen Truong Giang
 * @version 1.0
 */

public class MessageBox //extends JOptionPane
{
	public static final int WARNING_MESSAGE = JOptionPane.WARNING_MESSAGE;
	public static final int ERROR_MESSAGE = JOptionPane.ERROR_MESSAGE;
	public static final int QUESTION_MESSAGE = JOptionPane.QUESTION_MESSAGE;
	public static final int INFORMATION_MESSAGE = JOptionPane.INFORMATION_MESSAGE;
	public static final int PLAIN_MESSAGE = JOptionPane.PLAIN_MESSAGE;

	public static final int DEFAULT_OPTION = JOptionPane.DEFAULT_OPTION;
	public static final int OK_OPTION = JOptionPane.OK_OPTION;
	public static final int CLOSED_OPTION = JOptionPane.CLOSED_OPTION;
	public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;

	public static final int YES_OPTION = JOptionPane.YES_OPTION;
	public static final int NO_OPTION = JOptionPane.NO_OPTION;

	public static final int OK_CANCEL_OPTION = JOptionPane.OK_CANCEL_OPTION;
	public static final int YES_NO_CANCEL_OPTION = JOptionPane.YES_NO_CANCEL_OPTION;
	public static final int YES_NO_OPTION = JOptionPane.YES_NO_OPTION;

	public static final Object      UNINITIALIZED_VALUE = "uninitializedValue";

	static Object objMessage = "";
	static String strTitle = "";
	static int iOptionType = -1;
	static int iMessageType = -1;
	static Component cmpParentComponent = null;

	private static void clearAttributes()
	{
		cmpParentComponent = null;
		objMessage = "";
		strTitle = "Information";
		iMessageType = -1;
		iOptionType = -1;
	}

	private static void showMessageDialog()
	{
		if(objMessage == null) return;
		JOptionPane panMessage = new JOptionPane();
		if (objMessage.toString().length() > 500)
			objMessage = objMessage.toString().substring(0, 99) + "...";
		panMessage.setMessage(objMessage);
		if(iMessageType != -1)
			panMessage.setMessageType(iMessageType);
		JDialog dlgMessage = panMessage.createDialog(cmpParentComponent, strTitle);
		applySkin(dlgMessage);
		dlgMessage.pack();
		dlgMessage.validate();
		dlgMessage.invalidate();
		dlgMessage.setVisible(true);
	}

	public static String showInputDialog(Component parentComponent,
		Object message, String title, int messageType)
		throws HeadlessException {
		return (String)showInputDialog(parentComponent, message, title,
									   messageType, null, null, null);
	}

	public static Object showInputDialog(Component parentComponent,
		Object message, String title, int messageType, Icon icon,
		Object[] selectionValues, Object initialSelectionValue)
		throws HeadlessException {
		JOptionPane pane = new JOptionPane(message, messageType,
											  OK_CANCEL_OPTION, icon,
											  null, null);

		pane.setWantsInput(true);
		pane.setSelectionValues(selectionValues);
		pane.setInitialSelectionValue(initialSelectionValue);
		pane.setComponentOrientation(((parentComponent == null) ? JOptionPane.getRootFrame() : parentComponent).getComponentOrientation());

		JDialog dialog = pane.createDialog(parentComponent, title);
		applySkin(dialog);
		dialog.pack();
		pane.selectInitialValue();
		dialog.setVisible(true);
		dialog.dispose();

		Object value = pane.getInputValue();

		if (value == UNINITIALIZED_VALUE)
			return null;

		return value;
	}

	private static int showConfirmMessage()
	{
		JOptionPane panMessage = new JOptionPane();
		if(objMessage.toString().length() > 500)
		{
				objMessage = objMessage.toString().substring(0,99) + "...";
		}
		panMessage.setMessage(objMessage);
		panMessage.setOptionType(iOptionType);
		panMessage.setMessageType(iMessageType);
		JDialog dlgMessage = panMessage.createDialog(cmpParentComponent, strTitle);
		applySkin(dlgMessage);
		dlgMessage.pack();
		dlgMessage.validate();
		dlgMessage.invalidate();
		dlgMessage.setVisible(true);
		return Integer.parseInt(panMessage.getValue().toString()) ;
	}

	public static int showConfirmDialog(Component parentComponent, Object message, String title)
	{
		clearAttributes();
		cmpParentComponent = parentComponent;
		objMessage = message;
		strTitle = title;
		iMessageType = JOptionPane.QUESTION_MESSAGE;
		iOptionType = JOptionPane.YES_NO_OPTION;
		return showConfirmMessage();
	}

	public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType)
	{
		clearAttributes();
		cmpParentComponent = parentComponent;
		objMessage = message;
		strTitle = title;
		iMessageType = JOptionPane.QUESTION_MESSAGE;
		iOptionType = optionType;
		return showConfirmMessage();
	}

	public static void showMessageDialog(Object message)
	{
		showMessageDialog(null,message);
	}

	public static void showMessageDialog(Component parentComponent, Object message)
	{
		clearAttributes();
		cmpParentComponent = parentComponent;
		objMessage = message;
		iMessageType = JOptionPane.INFORMATION_MESSAGE;
		showMessageDialog();
	}

	public static void showMessageDialog(Component parentComponent, Object message, String title)
	{
		clearAttributes();
		cmpParentComponent = parentComponent;
		objMessage = message;
		iMessageType = JOptionPane.INFORMATION_MESSAGE;
		strTitle = title;
		showMessageDialog();
	}

	public static void showMessageDialog(Component parentComponent, Object message, String title, int messageType)
	{
		clearAttributes();
		cmpParentComponent = parentComponent;
		objMessage = message;
		strTitle = title;
		iMessageType= messageType;
		showMessageDialog();
	}

	public static void showMessageDialog(Exception e)
	{
		showMessageDialog(null,e);
	}

	public static void showMessageDialog(Component parentComponent, Exception e)
	{
		clearAttributes();
		cmpParentComponent = parentComponent;
		if(e instanceof AppException)
			objMessage = ErrorDictionary.getString(((AppException)e).getReason(),
				StringUtil.toStringArray(StringUtil.nvl(((AppException)e).getInfo(),"")));
		else
			objMessage = ErrorDictionary.getString(e);
		iMessageType = JOptionPane.INFORMATION_MESSAGE;
		showMessageDialog();
	}

	public static void showMessageDialog(Component parentComponent, Exception e, String title)
	{
		clearAttributes();
		cmpParentComponent = parentComponent;
		if(e instanceof AppException)
			objMessage = ErrorDictionary.getString(((AppException)e).getReason(),
				StringUtil.toStringArray(StringUtil.nvl(((AppException)e).getInfo(),"")));
		else
			objMessage = ErrorDictionary.getString(e);
		iMessageType = JOptionPane.INFORMATION_MESSAGE;
		strTitle = title;
		showMessageDialog();
	}

	public static void showMessageDialog(Component parentComponent, Exception e, String title, int messageType)
	{
		clearAttributes();
		cmpParentComponent = parentComponent;
		if(e instanceof AppException)
			objMessage = ErrorDictionary.getString(((AppException)e).getReason(),
				StringUtil.toStringArray(StringUtil.nvl(((AppException)e).getInfo(),"")));
		else
			objMessage = ErrorDictionary.getString(e);
		strTitle = title;
		iMessageType= messageType;
		showMessageDialog();
	}

	private static void applySkin(Component cmpSrc)
	{
		Font FONT_CONTROL = new Font("Tahoma",Font.PLAIN,12);
		Dimension BUTTON_SIZE = new Dimension(70,25);
		Insets BUTTON_MARGIN = new Insets(2,2,2,2);

		if(cmpSrc instanceof AbstractButton)
		{
			cmpSrc.setFont(FONT_CONTROL);
			((AbstractButton)cmpSrc).setMinimumSize(BUTTON_SIZE);
			((AbstractButton)cmpSrc).setPreferredSize(BUTTON_SIZE);
			((AbstractButton)cmpSrc).setMargin(BUTTON_MARGIN);
			AbstractButton button = (AbstractButton)cmpSrc;
			DefaultDictionary.applyButton(button,button.getText());
		}
		else if(cmpSrc instanceof JLabel)
		{
			cmpSrc.setFont(FONT_CONTROL);
			cmpSrc.setForeground(Color.black);
		}
		else if(cmpSrc instanceof JTextComponent)
		{
			Skin.applySkin(cmpSrc);
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

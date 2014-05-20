package com.fss.swing;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.undo.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.datatransfer.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Nguyen Truong Giang
 * @version 1.0
 */

public class JXNumber extends JTextField implements KeyListener,TrackChangeListener,UndoableEditListener
{
	private int maxLength = 20;
	private int decimalNumber = 0;
	private int remainNumber = 20;
	private boolean bSeparate = false;
	private boolean bNegative = true;
	private DecimalFormat numberFormat;
	private int pos;
	private String strFirst = "";
	private String strSecond = "";
	private String strNumberFormat = "#,###";
	private String strCurrentValue = null;
	private boolean bStored = false;
	private boolean bSelectOnFocus = false;
	public static int ALIGN_LEFT = JTextField.LEFT;
	public static int ALIGN_RIGHT = JTextField.RIGHT;
	private boolean nextFocusOnEnter = false;
	private UndoManager undomanager = new UndoManager();
	private boolean mbHistory = false;
	private Border mbdr;

	public JXNumber()
	{
		MouseHandler mouseHandler = new MouseHandler();
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseHandler);
		setHistoryEnabled(true);

		initControl();
		getDocument().addUndoableEditListener(this);
	}

	public JXNumber(boolean separator, boolean negative)
	{
		initControl();
		buildNumberFormat();
		show1000Separator(separator);
		setNegative(negative);
	}

	private void initControl()
	{
		setDisabledTextColor(java.awt.Color.BLACK);
		setHorizontalAlignment(JTextField.RIGHT);

		pos = getCaretPosition();

		addKeyListener(this);

		this.addFocusListener(new java.awt.event.FocusListener()
		{
			public void focusLost(FocusEvent e)
			{
				if(isHistoryEnabled() && historyModel != null)
				{
					if(historyModel.popup != null &&
					   e.getOppositeComponent() != historyModel.popup.getRootPane())
						historyModel.addItem(getText());
				}
			}
			public void focusGained(FocusEvent e)
			{
				if(isHistoryEnabled())
					ensureModelAvailable();
				this_focusGained(e);
			}
		});
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param e UndoableEditEvent
	 */
	////////////////////////////////////////////////////////
	public void undoableEditHappened(UndoableEditEvent e)
	{
		undomanager.addEdit(e.getEdit());
	}
	////////////////////////////////////////////////////////
	/**
	 * Undo change
	 */
	////////////////////////////////////////////////////////
	public void undo()
	{
		try
		{
			undomanager.undo();
		}
		catch(Exception e)
		{
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Redo change
	 */
	////////////////////////////////////////////////////////
	public void redo()
	{
		try
		{
			undomanager.redo();
		}
		catch(Exception e)
		{
		}
	}

	public void setSelectionAllOnFocus(boolean value)
	{
		bSelectOnFocus = value;
	}
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
		super.setText(strCurrentValue);
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

	public void setNegative(boolean value)
	{
		bNegative = value;
	}

	public void setDataLength(int length, int scale) throws Exception
	{
		if(length < 1) throw new Exception("Length must be greater than zero");
		if(scale < 0) throw new Exception("Scale must be greater than or equal to zero");
		if(length <= scale) throw new Exception("Length must be greater than number of decimal place");
		if(super.getText().length() > 0 &&
		   !super.getText().equals("0"))
			throw new Exception("You must setDataLength before set value for this component");
		maxLength = length;
		decimalNumber = scale;
		remainNumber = maxLength - decimalNumber;
		buildNumberFormat();
	}

	private void buildNumberFormat()
	{
		String strTemp = "";
		for(int i = 0; i < decimalNumber; i++)
		{
			strTemp += "#";
		}
		if(decimalNumber > 0)
			strNumberFormat = "#,###." + strTemp;
		else
			strNumberFormat = "#,###";
	}

	public void show1000Separator(boolean value)
	{
		bSeparate = value;
		keyReleased(null);
	}

	public String getText()
	{
		return super.getText().replaceAll(",","");
	}

	public String getFullText()
	{
		return super.getText();
	}

	public void setDouble(double value) throws Exception
	{
		if(value < 0 && !bNegative) return;
		numberFormat = new DecimalFormat("###");
		String strTemp = numberFormat.format(value);
		if(strTemp.length() > remainNumber) return;
		if(bSeparate)
			numberFormat = new DecimalFormat(strNumberFormat);
		else
			numberFormat = new DecimalFormat(strNumberFormat.replaceAll(",",""));
		super.setText(numberFormat.format(value));
		if(value < 0)
			this.setForeground(Color.red);
		else
			this.setForeground(Color.black);

	}

	public void setText(String value)
	{
		if(value == null || value.length() == 0)
		{
			super.setText("");
			return;
		}
		try
		{
			if(value.trim().length() > 0)
			{
				double dblTemp = Double.parseDouble(value.replaceAll(",",""));
				setDouble(dblTemp);
			}
		}
		catch(Exception e){}
		keyReleased(null);
	}

	public double getDouble()
	{
		try
		{
			return Double.parseDouble(getText());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return 0;
	}

	public void setInt(int value) throws Exception
	{
		String strTemp = String.valueOf(value);
		if(strTemp.length() > remainNumber)
			throw new Exception("Value input is too big, please check and try again");
		super.setText(strTemp);
		keyReleased(null);
	}

	public int getInt() throws Exception
	{
		return Integer.parseInt(getText());
	}

	public void keyTyped(KeyEvent e)
	{
		String strKeyInput = String.valueOf(e.getKeyChar());
		String strCheck = "-0123456789.";
		String strText = super.getText();
		pos = getCaretPosition();
		if(strText.indexOf(".") >= 0)
		{
			strFirst = strText.substring(0,strText.indexOf("."));
			strSecond = strText.substring(strText.indexOf(".") + 1);
		}
		else
		{
			strFirst = strText;
			strSecond = "";
		}
		strFirst.replaceAll(",","");
		if((e.getKeyChar() != e.VK_DELETE &&
		   e.getKeyChar() != e.VK_HOME &&
		   e.getKeyChar() != e.VK_END &&
		   e.getKeyChar() != e.VK_BACK_SPACE &&
		   e.getKeyChar() != e.VK_LEFT &&
		   e.getKeyChar() != e.VK_RIGHT &&
		   strCheck.indexOf(strKeyInput) >= 0) || strKeyInput.equals("+"))
		{
			//kiem tra xem gia tri nhap vao co phai la dau am hoac duong khong
			if(strKeyInput.equals("-") || strKeyInput.equals("+"))
			{
				int caretPosition = getCaretPosition();
				if(bNegative)
				{
					if(strText.indexOf("-") >=0)
					{
						this.setForeground(Color.black);
						super.setText(strText.substring(1));
						if(caretPosition - 1 < 0)
							setCaretPosition(0);
						else
							setCaretPosition(caretPosition - 1);
					}
					else if(strKeyInput.equals("-"))
					{
						this.setForeground(Color.red);
						super.setText("-" + strText);
						setCaretPosition(caretPosition + 1);
					}
				}
				e.consume();
			}
			//kiem tra xem gia tri nhap vao co phai la dau "." khong
			else if(strKeyInput.equals("."))
			{
				if(decimalNumber == 0)
					e.consume();
				else if(strText.indexOf(".") >= 0)
				{
					setCaretPosition(strText.indexOf(".") + 1);
					e.consume();
				}
				else if(strText.indexOf(".") >= 0 &&
						strText.substring(getText().indexOf(".") + 1).length() == decimalNumber &&
						this.getCaretPosition() == strText.length())
					e.consume();
				else if(strText.indexOf("-") >= 0 && pos == 1)
				{
					super.setText("-" + "0." + strText.replaceAll(",","").substring(1,decimalNumber));
					setCaretPosition(3);
					e.consume();
				}
				else if(strText.indexOf("-") < 0 && pos == 0)
				{
					String strTemp = strText.replaceAll(",","");
					if(strTemp.length() > decimalNumber)
						strTemp = strTemp.substring(0,decimalNumber);
					super.setText("0." + strTemp);
					setCaretPosition(2);
					e.consume();
				}
				else if(strText.indexOf(".") < 0)
				{
					String strTemp = strText.substring(pos);
					strTemp = strTemp.replaceAll(",","");
					if(strTemp.length() > decimalNumber)
					{
						strTemp = strTemp.substring(0,decimalNumber);
					}
					super.setText(strText.substring(0,pos) + "." + strTemp);
					setCaretPosition(pos + 1);
					e.consume();
				}
			}
			else if("0123456789".indexOf(strKeyInput) >= 0)
			{
				if(strText.indexOf(".") >= 0 && getCaretPosition() > strText.indexOf("."))
				{
					if(getCaretPosition() == strText.length())
					{
						if(strText.substring(strText.indexOf(".") + 1).length() == decimalNumber)
							e.consume();
					}
					else
					{
						if(strText.substring(strText.indexOf(".") + 1).length() == decimalNumber)
						{
							int secondPos = pos - strText.indexOf(".") - 1;
							String strTemp = strSecond.subSequence(0,secondPos) + strKeyInput + strSecond.substring(secondPos + 1) ;
							super.setText(strFirst + "." + strTemp.substring(0,decimalNumber));
							setCaretPosition(pos + 1);
							e.consume();
						}
					}
				}
				else
				{
					if(strText.indexOf("-") >= 0)
					{
						if (strFirst.replaceAll(",","").length() == remainNumber + 1 &&
							getSelectionStart() == getSelectionEnd())
						{
							e.consume();
						}
					}
					else
					{
						if (strFirst.replaceAll(",","").length() == remainNumber &&
							getSelectionStart() == getSelectionEnd())
						{
							e.consume();
						}
					}

					String strValueBefore = strText.substring(0, this.getCaretPosition());
					if(strKeyInput.equals("0"))
					{
						if (strValueBefore.length() > 0 && strValueBefore.equals("0"))
							e.consume();
					}
					else
					{
						if(strValueBefore.equals("0"))
						{
							if(strText.indexOf(".") >= 0)
								super.setText(strKeyInput + "." + strSecond);
							else
								super.setText(strKeyInput + strSecond);
							setCaretPosition(pos);
							e.consume();
						}
					}
				}
			}
			else
			{
				e.consume();
			}
		}
		else
		{
			e.consume();
		}
	}

	/**
	 * Invoked when a key has been pressed.
	 * See the class description for {@link KeyEvent} for a definition of
	 * a key pressed event.
	 * @param e KeyEvent
	 */
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_ENTER && getNextFocusOnEnter())
		{
			transferFocus();
			e.consume();
		}
		else if(e.getKeyCode() == e.VK_Z && e.isControlDown())
		{
			undo();
			e.consume();
		}
		else if(e.getKeyCode() == e.VK_Y && e.isControlDown())
		{
			redo();
			e.consume();
		}
		else if(e.getKeyCode() == KeyEvent.VK_UP)
		{
			if(isHistoryEnabled() && historyModel != null)
			{
				String str = getText();
				if(str.length() > 0 && !historyModel.isItemExist(str))
				{
					historyModel.addItem(str);
					historyModel.index = 0;
				}
				setText(historyModel.historyPrevious());
				e.consume();
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_DOWN)
		{
			if(isHistoryEnabled() && historyModel != null)
			{
				String str = getText();
				if(str.length() > 0 && !historyModel.isItemExist(str))
				{
					historyModel.addItem(str);
					historyModel.index = 0;
				}
				if(e.isAltDown())
					historyModel.showPopupMenu(this);
				else
					setText(historyModel.historyNext());
				e.consume();
			}
		}
		else
		{
			String strText = super.getText();
			if(e.getKeyChar() == e.VK_DELETE)
			{
				int caretPosition = getCaretPosition();
				if(strText.indexOf(".") >= 0 && caretPosition == strText.indexOf("."))
				{
					String strFirst = strText.substring(0,strText.indexOf(".") + 1);
					String strSecond = strText.substring(strText.indexOf(".") + 1);
					if(strSecond.length() > 0)
					{
						strSecond = strSecond.substring(1);
						super.setText(strFirst + strSecond);
						setCaretPosition(caretPosition);
						e.consume();
					}
				}
				else if(strText.indexOf(".") >= 0 && caretPosition == strText.indexOf(".") - 1)
				{
					if(strText.substring(0,caretPosition).replaceAll("-","").length() == 0)
					{
						if(strText.indexOf("-") >= 0)
						{
							super.setText("-0" + strText.substring(caretPosition + 1));
							setCaretPosition(2);
						}
						else
						{
							super.setText("0" + strText.substring(caretPosition + 1));
							setCaretPosition(1);
						}
						e.consume();
					}
				}
				else if(caretPosition < strText.length() &&
						strText.substring(caretPosition,caretPosition + 1).equals(","))
				{
					String strFirst = strText.substring(0,caretPosition);
					String strSecond = strText.substring(caretPosition + 2);
					super.setText(strFirst + strSecond);
					setCaretPosition(caretPosition);
					e.consume();
				}
			}
			else if(e.getKeyChar() == e.VK_BACK_SPACE)
			{
				int caretPosition = getCaretPosition();
				if(strText.indexOf(".") >= 0 && caretPosition == strText.indexOf(".") + 1)
				{
					String strFirst = strText.substring(0,strText.indexOf("."));
					String strSecond = strText.substring(strText.indexOf(".") + 1);
					if(strFirst.length() > 0)
					{
						String strTemp = strFirst.replaceAll(",","") + strSecond;
						super.setText(strTemp.substring(0,remainNumber));
						if(caretPosition > super.getText().length())
							setCaretPosition(super.getText().length());
						else
							setCaretPosition(strFirst.length());
						e.consume();
					}
					else
					{
						super.setText(strSecond);
						setCaretPosition(0);
						e.consume();
					}
				}
				else if(caretPosition > 0 && caretPosition < strText.length() &&
						strText.substring(caretPosition - 1,caretPosition).equals(","))
				{
					String strFirst = strText.substring(0,caretPosition - 2);
					String strSecond = strText.substring(caretPosition);
					super.setText(strFirst + strSecond);
					setCaretPosition(strFirst.length());
					e.consume();
				}
				else if(caretPosition > 0)
				{
					String strFirst = strText.substring(0,caretPosition - 1);
					String strSecond = strText.substring(caretPosition);
					super.setText(strFirst + strSecond);
					setCaretPosition(strFirst.length());
					e.consume();
				}
			}
			if(e.isControlDown() && e.getKeyCode() == 86)
			{
				TransferHandler th = this.getTransferHandler();
				Clipboard clipboard = this.getToolkit().getSystemClipboard();
				Transferable trans = clipboard.getContents(null);
				JTextField tf = new JTextField();
				JComponent c = (JComponent)tf;
				if(trans != null)
				{
					th.importData(c,trans);
					this.setText(tf.getText());
				}
				e.consume();
			}
		}
	}

	/**
	 * Invoked when a key has been released.
	 * See the class description for {@link KeyEvent} for a definition of
	 * a key released event.
	 * @param e KeyEvent
	 */
	public void keyReleased(KeyEvent e)
	{
		int numAfterCaret = super.getText().length() - getCaretPosition();
		int selectionStart = super.getSelectionStart();
		int selectionEnd = super.getSelectionEnd();
		if(selectionStart == selectionEnd)
		{
			String strText = super.getText();
			for(int i = 0;i < strText.length();i++)
			{
				if("-,0123456789.".indexOf(strText.substring(i,i + 1)) < 0)
				{
					super.setText(super.getText().replaceAll(strText.substring(i,i + 1),""));
					if(super.getText().length() == 0)
						setCaretPosition(0);
					else if(pos > super.getText().length())
						setCaretPosition(super.getText().length());
					else
						setCaretPosition(pos - 1);
				}
			}
			strText = super.getText();
			if(bSeparate)
			{
				String strFormat = "#,###";
				if(strText.indexOf(".") >= 0)
				{
					strFirst = strText.substring(0,strText.indexOf("."));
					strSecond = strText.substring(strText.indexOf(".") + 1);
				}
				else
				{
					strFirst = strText;
					strSecond = "";
				}
				numberFormat = new DecimalFormat(strFormat);
				if(strFirst.length() > 0 && !strFirst.equals("-"))
					strFirst = numberFormat.format(Double.parseDouble(strFirst.replaceAll(",","")));
				if(strText.indexOf(".") >= 0)
					super.setText(strFirst + "." + strSecond);
				else
				{
					super.setText(strFirst);
				}
				if((super.getText().length() - numAfterCaret) >= 0)
					setCaretPosition(super.getText().length() - numAfterCaret);
				else
					setCaretPosition(0);
			}
			if(super.getText().indexOf("-") < 0)
			{
				this.setForeground(Color.black);
			}
		}
	}

	private void this_focusGained(FocusEvent e)
	{
		if(getText().indexOf("-") >= 0)
		{
			this.setForeground(Color.red);
		}
		else
		{
			this.setForeground(Color.black);
		}
		if(bSelectOnFocus)
		{
			this.setSelectionStart(0);
			this.setSelectionEnd(this.getFullText().length());
		}
	}

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
	/**
	 * Implementing history
	 * Modify code from JEdit HistoryTextField
	 */
	////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////
	/**
	 * Sets if all text should be selected when the field gets focus.
	 * @since jEdit 4.0pre3
	 * @return HistoryModel
	 */
	////////////////////////////////////////////////////////
	public HistoryModel getModel()
	{
		return historyModel;
	}
	////////////////////////////////////////////////////////
	/**
	 * Sets the history list model.
	 * @param name The model name
	 * @since jEdit 2.3pre3
	 */
	////////////////////////////////////////////////////////
	public void setModel(String name)
	{
		historyModel = HistoryModel.getModel(name);
		historyModel.index = -1;
		repaint();
	}
	////////////////////////////////////////////////////////
	protected void processMouseEvent(MouseEvent evt)
	{
		if(!isEnabled())
			return;
		switch(evt.getID())
		{
			case MouseEvent.MOUSE_PRESSED:
				if(isHistoryEnabled())
				{
					Border border = getBorder();
					Insets insets = border.getBorderInsets(JXNumber.this);
					if(evt.getX() >= getWidth() - insets.right || evt.isPopupTrigger())
					{
						ensureModelAvailable();
						historyModel.showPopupMenu(this);
					}
					else
						super.processMouseEvent(evt);
					break;
				}
			case MouseEvent.MOUSE_EXITED:
				if(isHistoryEnabled())
				{
					setCursor(Cursor.getDefaultCursor());
					super.processMouseEvent(evt);
					break;
				}
			default:
				super.processMouseEvent(evt);
				break;
		}
	}
	////////////////////////////////////////////////////////
	private HistoryModel historyModel;
	////////////////////////////////////////////////////////
	class MouseHandler extends MouseInputAdapter
	{
		public void mouseMoved(MouseEvent evt)
		{
			ensureModelAvailable();
			Border border = getBorder();
			Insets insets = border.getBorderInsets(JXNumber.this);
			if(evt.getX() >= getWidth() - insets.right)
				setCursor(Cursor.getDefaultCursor());
			else
				setCursor(Cursor.getPredefinedCursor(
					Cursor.TEXT_CURSOR));
		}
	}
	////////////////////////////////////////////////////////
	static class HistoryBorder extends AbstractBorder
	{
		static final int WIDTH = 16;
		public void paintBorder(Component c, Graphics g,int x, int y, int w, int h)
		{
			g.translate(x+w-WIDTH,y-1);
			int w2 = WIDTH/2;
			int h2 = h/2;
			g.setColor(UIManager.getColor(c.isEnabled()
										  ? "TextField.foreground" : "TextField.disabledForeground"));
			g.drawLine(w2-5,h2-2,w2+4,h2-2);
			g.drawLine(w2-4,h2-1,w2+3,h2-1);
			g.drawLine(w2-3,h2  ,w2+2,h2  );
			g.drawLine(w2-2,h2+1,w2+1,h2+1);
			g.drawLine(w2-1,h2+2,w2  ,h2+2);

			g.translate(-(x+w-WIDTH),-(y-1));
		}
		public Insets getBorderInsets(Component c)
		{
			return new Insets(0,0,0,WIDTH);
		}
	}
	public void ensureModelAvailable()
	{
		if(getModel() == null)
			setModel(WindowManager.generateName(this));
		else
		{
			String strNewName = WindowManager.generateName(this);
			int iIndex = strNewName.indexOf("|");
			if(iIndex >= 0)
				strNewName = strNewName.substring(iIndex + 1,strNewName.length());
			if(!strNewName.equals(getModel().getName()))
				setModel(strNewName);
		}
	}
	public void processKeyEvent(KeyEvent e)
	{
		if(isHistoryEnabled() && historyModel != null)
		{
			if(historyModel.popup != null && historyModel.popup.isVisible())
			{
				int iCode = e.getKeyCode();
				if(iCode == e.VK_ESCAPE || iCode == e.VK_TAB)
					MenuSelectionManager.defaultManager().clearSelectedPath();
				else if(iCode == e.VK_SPACE || iCode == e.VK_ENTER)
				{
					setText(com.fss.util.StringUtil.nvl(historyModel.list.getSelectedValue(),""));
					MenuSelectionManager.defaultManager().clearSelectedPath();
				}
				else
				{
					e.setSource(historyModel.list);
					historyModel.list.dispatchEvent(e);
				}
				return;
			}
			else
			{
				int iCode = e.getKeyCode();
				if(iCode == e.VK_ENTER)
					historyModel.addItem(getText());
			}
		}
		super.processKeyEvent(e);
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @return boolean
	 */
	////////////////////////////////////////////////////////
	public boolean isHistoryEnabled()
	{
		return mbHistory;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param bHistory boolean
	 */
	////////////////////////////////////////////////////////
	public void setHistoryEnabled(boolean bHistory)
	{
		if(bHistory != mbHistory)
		{
			mbHistory = bHistory;
			if(mbdr == null)
				mbdr = getBorder();
			if(isHistoryEnabled())
			{
				setBorder(new CompoundBorder(mbdr,new HistoryBorder()));
				if(isFocusOwner())
					ensureModelAvailable();
			}
			else
				setBorder(mbdr);
		}
	}
}

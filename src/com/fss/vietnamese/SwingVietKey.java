
package com.fss.vietnamese;

import java.util.*;
import java.awt.event.*;
import javax.swing.text.JTextComponent;
/**
 * SwingTelexDriver - The driver for Swing's JTextComponent
 * @version	0.1
 * @author	Ho Ngoc Duc <duc@informatik.uni-leipzig.de>
 */
public class SwingVietKey implements KeyListener
{
	protected Encoding encoding;
	protected boolean enabled = false;
	private BitSet MODIFIERS;
	String fToneMarks;
	String fInputMethod;
	char fBreve, fHorn, fStroke;
	String fCirc;
	/**
	 *
	 */
	public SwingVietKey()
	{
		encoding = new UnicodeEncoding();
		setTelexInput();

		MODIFIERS = new BitSet(128);
		for (int i = 0; i < 128; i++)
		{
			if (fCirc.indexOf((char) i) >= 0) MODIFIERS.set(i);
			else if (isBreve((char)i)) MODIFIERS.set(i);
			else if (isHorn((char)i)) MODIFIERS.set(i);
			else if (isStroke((char)i)) MODIFIERS.set(i);
			else if (isToneMark((char) i)) MODIFIERS.set(i);
		}
	}
	////////////////////////////////////////////////////////
	public void keyPressed(KeyEvent e)
	{
	}
	////////////////////////////////////////////////////////
	public void keyTyped(KeyEvent e)
	{
		if (!enabled) return;
		if(e.getSource() instanceof JTextComponent)
		{
			JTextComponent input = (JTextComponent)e.getSource();
			String strContent = input.getText();

			char c = e.getKeyChar();
			if(!isModifier(c))
				return;
			int pos = input.getCaretPosition();
			if(pos <= 0)
				return;
			int idx = pos - 1;
			char last = strContent.charAt(idx);
			char newVal = last;
			char chrOrigin = UnicodeEncoding.getOriginChar(last);
			boolean bReTouch = false;

			if(isCircumflex(c,chrOrigin))
			{
				if(chrOrigin == last)
					newVal = encoding.addCircumflex(last);
				else if(last == encoding.addCircumflex(chrOrigin))
					bReTouch = true;
			}
			else if(isBreve(c))
			{
				if(chrOrigin == last)
					newVal = encoding.addBreveHorn(last);
				else if(last == encoding.addBreveHorn(chrOrigin))
					bReTouch = true;
			}
			else if(isHorn(c))
			{
				if(chrOrigin == last)
					newVal = encoding.addBreveHorn(last);
				else if(last == encoding.addBreveHorn(chrOrigin))
					bReTouch = true;
			}
			else if(isStroke(c))
			{
				if(chrOrigin == last)
					newVal = encoding.addStroke(last);
				else if(last == encoding.addStroke(chrOrigin))
					bReTouch = true;
			}
			else if(isToneMark(c))
			{
				idx = indexOfToneCarrier(pos,strContent);
				if(idx >= 0)
				{
					last = strContent.charAt(idx);
					int iToneMarkID = getToneMarkId(c);
					int iLastToneMarkID = UnicodeEncoding.DNH[last] % 6;
					if(iToneMarkID == iLastToneMarkID)
					{
						chrOrigin = UnicodeEncoding.HND[UnicodeEncoding.DNH[last] - iLastToneMarkID];
						bReTouch = true;
					}
					else
						newVal = encoding.modifyTone(last,iToneMarkID);
				}
			}

			if(bReTouch)
			{
				int iCaretPos = input.getCaretPosition();
				input.setCaretPosition(idx);
				input.moveCaretPosition(idx + 1);
				input.replaceSelection(chrOrigin + "");
				input.setCaretPosition(iCaretPos);
			}
			else if(last != newVal)
			{
				input.setCaretPosition(idx);
				input.moveCaretPosition(idx + 1);
				input.replaceSelection("" + newVal);
				input.setCaretPosition(pos);
				e.consume();
			}
		}
	}
	////////////////////////////////////////////////////////
	public void keyReleased(KeyEvent e)
	{
	}
	////////////////////////////////////////////////////////
	void setTelexInput()
	{
		fToneMarks = "zfrxsj";
		fHorn = 'w';
		fStroke = 'd';
		fBreve = 'w';
		fCirc = "AaEeOo";
	}
	////////////////////////////////////////////////////////
	void setVIQRInput()
	{
		fToneMarks = "z`?~'.";
		fHorn = '+';
		fStroke = 'd';
		fBreve = '(';
		fCirc = "^";
	}
	////////////////////////////////////////////////////////
	void setVNIInput()
	{
		fToneMarks = "023415";
		fHorn = '7';
		fStroke = '9';
		fBreve = '8';
		fCirc = "6";
	}
	////////////////////////////////////////////////////////
	public boolean isBreve(char c)
	{
		return eq(c,fBreve);
	}
	////////////////////////////////////////////////////////
	public boolean isCircumflex(char c, char ch)
	{
		if (fCirc.indexOf(c) < 0) return false;
		return eq(c,Character.toLowerCase(ch));
	}
	////////////////////////////////////////////////////////
	public boolean isHorn(char c)
	{
		return eq(c,fHorn);
	}
	////////////////////////////////////////////////////////
	protected boolean isModifier(char c)
	{
		return MODIFIERS.get((int)c);
	}
	////////////////////////////////////////////////////////
	public boolean isStroke(char c)
	{
		return eq(c,fStroke);
	}
	////////////////////////////////////////////////////////
	public boolean isToneMark(char c)
	{
		return getToneMarkId(c) >= 0;
	}
	////////////////////////////////////////////////////////
	private static boolean eq(char c1, char c2)
	{
		return Character.toLowerCase(c1) == c2;
	}
	////////////////////////////////////////////////////////
	public int getToneMarkId(char c)
	{
		return fToneMarks.indexOf(c);
	}
	////////////////////////////////////////////////////////
	protected int indexOfToneCarrier(int pos,String strContent)
	{
		int idx = indexOfLastVowel(pos,strContent);
		if (idx <= 0) return idx;
		char c = strContent.charAt(idx-1);
		if (!encoding.isVowel(c) && !eq(c,'q')) return idx;
		if (encoding.hasDiacritic(strContent.charAt(idx))) return idx;
		if (encoding.hasDiacritic(c)) return idx -1;
		if (eq(c,'q') && eq(strContent.charAt(idx),'u')) return -1;
		if (eq(c,'o') && eq(strContent.charAt(idx),'a')) return idx;
		if (eq(c,'o') && eq(strContent.charAt(idx),'e')) return idx;
		if (eq(c,'u') && eq(strContent.charAt(idx),'y')) return idx;
		if ((idx >= 2) && eq(c,'u') && eq(strContent.charAt(idx-2),'q')) return idx;
		if ((idx >= 2) && eq(c,'i') && eq(strContent.charAt(idx-2),'g')) return idx;
		return idx -1;
	}
	////////////////////////////////////////////////////////
	protected int indexOfLastVowel(int pos,String strContent)
	{
		int beg = Math.max(0,pos-3);
		for (int i = pos-1; i>=beg; i--)
		{
			char c = strContent.charAt(i);
			if(c <= ' ' || fToneMarks.indexOf(c) >= 0) return -1;
			if (encoding.isVowel(c)) return i;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	public boolean isEnabled()
	{
		return enabled;
	}
	////////////////////////////////////////////////////////
	public void setEnabled(boolean blnEnabled)
	{
		enabled = blnEnabled;
	}
}

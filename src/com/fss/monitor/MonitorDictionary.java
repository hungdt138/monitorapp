package com.fss.monitor;

import java.util.*;
import java.io.*;

import com.fss.util.*;
import com.fss.dictionary.*;
import com.fss.dictionary.Dictionary;

/**
 * <p>Title: MonitorDictionary</p>
 * <p>Description: Dictionary used for thread monitor</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class MonitorDictionary
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	private static final String DICTIONARY_INFO = "/com/fss/monitor/MonitorDictionary.txt";
	////////////////////////////////////////////////////////
	// Member variable
	////////////////////////////////////////////////////////
	private static Hashtable mprtDictionary = new Hashtable();
	private static Dictionary mdic;
	private static String mstrKey;
	////////////////////////////////////////////////////////
	// Static initialize
	////////////////////////////////////////////////////////
	static
	{
		loadDictionary();
		setCurrentLanguage("VN");
	}
	////////////////////////////////////////////////////////
	/**
	 * Load dictionary form resource
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static void loadDictionary()
	{
		InputStream is = null;
		try
		{
			is = MonitorDictionary.class.getResourceAsStream(DICTIONARY_INFO);
			mprtDictionary = Global.loadHashtable(is);

			Enumeration enm = mprtDictionary.keys();
			while(enm.hasMoreElements())
			{
				Object objKey = enm.nextElement();
				String strValue = StringUtil.nvl(mprtDictionary.get(objKey),"");

				// Get separate part from strValue
				int iFirstIndex = strValue.indexOf(",");
				int iLastIndex = strValue.lastIndexOf(",");

				// Add new dictionary into list
				String strLanguage = strValue.substring(0,iFirstIndex);
				String strIconPath = strValue.substring(iFirstIndex + 1,iLastIndex);
				String strFileName = strValue.substring(iLastIndex + 1);
				addDictionary(strFileName,objKey.toString(),strLanguage,strIconPath);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			FileUtil.safeClose(is);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Add new dictionary into list
	 * @param strFileName name of file contain dictionary data
	 * @param strKey dictinary unique key
	 * @param strLanguage dictionary language
	 * @param strIconPath path to icon image
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static void addDictionary(String strFileName,
									 String strKey,
									 String strLanguage,
									 String strIconPath)
	{
		InputStream is = null;
		try
		{
			// Load dictionary
			is = MonitorDictionary.class.getResourceAsStream(strFileName);
			Dictionary dic = new Dictionary(is);

			// Merge with default
			dic.mndRoot.merge(DefaultDictionary.getDictionary(strKey).mndRoot);

			// Set dictionary Hashtable
			dic.setLanguage(strLanguage);
			dic.setIconPath(strIconPath);
			mprtDictionary.remove(strKey);
			mprtDictionary.put(strKey,dic);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			FileUtil.safeClose(is);
		}
	}
	////////////////////////////////////////////////////////
	/**
	 * Get instance of dictionary
	 * @param strKey key of dictionary language
	 * @return Dictionary contain dictionary data of language strLanguage
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static Dictionary getDictionary(String strKey)
	{
		Dictionary dic = (Dictionary)mprtDictionary.get(strKey);
		if(dic == null)
			dic = new Dictionary();
		return dic;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get supported language
	 * @return list of language supported by this dictionary
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String[] getSupportedLanguage()
	{
		Object[] objLang = mprtDictionary.keySet().toArray();
		String[] strReturn = new String[objLang.length];
		for(int iIndex = 0;iIndex < objLang.length;iIndex++)
			strReturn[iIndex] = objLang[iIndex].toString();
		return strReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Set active language of this dictionary
	 * @param strKey key of active language
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static void setCurrentLanguage(String strKey)
	{
		mstrKey = strKey;
		mdic = getDictionary(mstrKey);
	}
	////////////////////////////////////////////////////////
	/**
	 * Get active language of this dictionary
	 * @return key of active language
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String getCurrentLanguage()
	{
		return mstrKey;
	}
	////////////////////////////////////////////////////////
	/**
	 * Get child dictionary
	 * @param strPath child path
	 * @return Dictionary
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static Dictionary getChildDictionary(String strPath)
	{
		DictionaryNode nd = mdic.getNode(strPath);
		if(nd == null)
			return new Dictionary();
		return new Dictionary(nd);
	}
	////////////////////////////////////////////////////////
	/**
	 * Create general button
	 * @param strKey String
	 * @return JButton
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static javax.swing.JButton createButton(String strKey)
	{
		javax.swing.JButton btn = new javax.swing.JButton();
		applyButton(btn,strKey);
		return btn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Create general menu
	 * @param strKey String
	 * @return JMenu
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static javax.swing.JMenu createMenu(String strKey)
	{
		javax.swing.JMenu mnu = new javax.swing.JMenu();
		applyButton(mnu,strKey);
		return mnu;
	}
	////////////////////////////////////////////////////////
	/**
	 * Create general menu item
	 * @param strKey String
	 * @return JMenuItem
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static javax.swing.JMenuItem createMenuItem(String strKey)
	{
		javax.swing.JMenuItem mnu = new javax.swing.JMenuItem();
		applyButton(mnu,strKey);
		return mnu;
	}
	////////////////////////////////////////////////////////
	/**
	 * Apply general button
	 * @param btn AbstractButton
	 * @param strKey String
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static void applyButton(javax.swing.AbstractButton btn,String strKey)
	{
		btn.setText(getString(strKey));
		String strMnemonic = getString("Mnemonic." + strKey);
		if(strMnemonic.length() > 0)
			btn.setMnemonic(strMnemonic.charAt(0));
	}
	////////////////////////////////////////////////////////
	// Wrap function from Dictionary
	////////////////////////////////////////////////////////
	public static String getString(String strKey)
	{
		return mdic.getString(strKey);
	}
	////////////////////////////////////////////////////////
	public static String getString(String strKey,String strPar)
	{
		return mdic.getString(strKey,strPar);
	}
	////////////////////////////////////////////////////////
	public static String getString(String strKey,String strPar1,String strPar2)
	{
		return mdic.getString(strKey,strPar1,strPar2);
	}
	////////////////////////////////////////////////////////
	public static String getString(String strKey,String strPar[])
	{
		return mdic.getString(strKey,strPar);
	}
}

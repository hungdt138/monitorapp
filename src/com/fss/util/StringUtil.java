package com.fss.util;

import java.util.*;
import java.security.*;

/**
 * <p>Title: StringUtil</p>
 * <p>Description: Utility for string processing</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class StringUtil
{
	public static final String CHARFORM_NOHORN =  "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd" +
												  "AAAAAAAAAAAAAAAAAEEEEEEEEEEEIIIIIOOOOOOOOOOOOOOOOOUUUUUUUUUUUYYYYYD";
	public static final String CHARFORM_UNICODE = "àáảãạâầấẩẫậăằắẳẵặèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ" +
												  "ÀÁẢÃẠÂẦẤẨẪẬĂẰẮẲẴẶÈÉẺẼẸÊỀẾỂỄỆÌÍỈĨỊÒÓỎÕỌÔỒỐỔỖỘƠỜỚỞỠỢÙÚỦŨỤƯỪỨỬỮỰỲÝỶỸỴĐ";
	public static final String CHARFORM_TCVN =    "µ¸¶·¹©ÇÊÈÉË¨»¾¼½ÆÌÐÎÏÑªÒÕÓÔÖ×ÝØÜÞßãáâä«åèæçé¬êíëìîïóñòô­õøö÷ùúýûüþ®" +
											      "µ¸¶·¹©ÇÊÈÉË¨»¾¼½ÆÌÐÎÏÑªÒÕÓÔÖ×ÝØÜÞßãáâä«åèæçé¬êíëìîïóñòô­õøö÷ùúýûüþ®";
	public static final int ALIGN_CENTER = 0;
	public static final int ALIGN_LEFT = 1;
	public static final int ALIGN_RIGHT = 2;
	////////////////////////////////////////////////////////
	/**
	 * Encrypt a string
	 * @param strValue string to encrypt
	 * @param strAlgorithm ecrypt algorithm
	 * @return encrypted string
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String encrypt(String strValue,String strAlgorithm) throws Exception
	{
		return encrypt(strValue.getBytes(),strAlgorithm);
	}
	////////////////////////////////////////////////////////
	/**
	 * Encrypt a byte array
	 * @param btValue array to encrypt
	 * @param strAlgorithm ecrypt algorithm
	 * @return encrypted string
	 * @throws Exception if error occured
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String encrypt(byte[] btValue,String strAlgorithm) throws Exception
	{
		BASE64Encoder enc = new BASE64Encoder();
		MessageDigest md = MessageDigest.getInstance(strAlgorithm);
		return enc.encodeBuffer(md.digest(btValue));
	}
	////////////////////////////////////////////////////////
	/**
	 * Format date object
	 * @param dtImput date to format
	 * @param strPattern format pattern
	 * @return formatted string
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String format(java.util.Date dtImput,String strPattern)
	{
		if(dtImput == null)
			return null;
		java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat(strPattern);
		return fmt.format(dtImput);
	}
	////////////////////////////////////////////////////////
	/**
	 * Format long number
	 * @param lngNumber number to format
	 * @param strPattern format pattern
	 * @return formatted string
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String format(long lngNumber,String strPattern)
	{
		java.text.DecimalFormat fmt = new java.text.DecimalFormat(strPattern);
		return fmt.format(lngNumber);
	}
	////////////////////////////////////////////////////////
	/**
	 * Format double number
	 * @param dblNumber number to format
	 * @param strPattern format pattern
	 * @return formatted string
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String format(double dblNumber,String strPattern)
	{
		java.text.DecimalFormat fmt = new java.text.DecimalFormat(strPattern);
		return fmt.format(dblNumber);
	}
	////////////////////////////////////////////////////////
	/**
	 * Replace all occurred of string with another
	 * @param strSrc main string
	 * @param strFind string to find
	 * @param strReplace string to replace
	 * @return main string after replace
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String replaceAll(String strSrc,String strFind,String strReplace)
	{
		if(strFind == null || strFind.length() == 0)
			return strSrc;
		int iLocation = 0;
		int iPrevLocation = 0;
		StringBuffer strResult = new StringBuffer();
		while((iLocation = strSrc.indexOf(strFind,iLocation)) >= 0)
		{
			strResult.append(strSrc.substring(iPrevLocation,iLocation));
			strResult.append(strReplace);
			iLocation += strFind.length();
			iPrevLocation = iLocation;
		}
		strResult.append(strSrc.substring(iPrevLocation,strSrc.length()));
		return strResult.toString();
	}
	////////////////////////////////////////////////////////
	/**
	 * Replace all occurred of string with another
	 * @param strSrc main string
	 * @param strFind string to find
	 * @param strReplace string to replace
	 * @param iMaxReplacement max replacement allowed
	 * @return main string after replace
	 * @author Nguyen Thi Thu Trang
	 */
	////////////////////////////////////////////////////////
	public static String replaceAll(String strSrc,String strFind,String strReplace,int iMaxReplacement)
	{
		int iLocation = 0;
		if(strFind == null || strFind.length() == 0)
			return strSrc;
		int iPrevLocation = 0;
		int iCount = 0;
		StringBuffer strResult = new StringBuffer();
		while((iLocation = strSrc.indexOf(strFind,iLocation)) >= 0 && iCount < iMaxReplacement)
		{
			strResult.append(strSrc.substring(iPrevLocation,iLocation));
			strResult.append(strReplace);
			iCount++;
			iLocation += strFind.length();
			iPrevLocation = iLocation;
		}
		strResult.append(strSrc.substring(iPrevLocation,strSrc.length()));
		return strResult.toString();
	}
	////////////////////////////////////////////////////////
	/**
	 * Replace all occurred of string ignore case with another
	 * @param strSrc main string
	 * @param strFind string to find
	 * @param strReplace string to replace
	 * @return main string after replace
	 * @author SonNT
	 */
	////////////////////////////////////////////////////////
	public static String replaceAllIgnoreCase(String strSrc,String strFind,String strReplace)
	{
		if(strFind == null || strFind.length() == 0)
			return strSrc;
		String strSrcUpper = strSrc.toUpperCase();
		strFind = strFind.toUpperCase();

		int iLocation = 0;
		int iPrevLocation = 0;
		StringBuffer strResult = new StringBuffer();
		while((iLocation = strSrcUpper.indexOf(strFind,iLocation)) >= 0)
		{
			strResult.append(strSrc.substring(iPrevLocation,iLocation));
			strResult.append(strReplace);
			iLocation += strFind.length();
			iPrevLocation = iLocation;
		}
		strResult.append(strSrc.substring(iPrevLocation,strSrc.length()));
		return strResult.toString();
	}
	////////////////////////////////////////////////////////
	/**
	 * Replace null string
	 * @param objInput object value
	 * @param strNullValue object to replace if objInput is null
	 * @return String value of objInput after replace
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String nvl(Object objInput,String strNullValue)
	{
		if(objInput == null)
			return strNullValue;
		return objInput.toString();
	}
	////////////////////////////////////////////////////////
	/**
	 * Return first position of letter (printable character) in String
	 * @param strSource String to search
	 * @param iOffset start offset
	 * @return first position of letter (printable character)
	 * in strSource, -1 if not found
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int indexOfLetter(String strSource,int iOffset)
	{
		char c;
		while(iOffset < strSource.length())
		{
			c = strSource.charAt(iOffset);
			if(c > ' ')
				return iOffset;
			else
				iOffset++;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return first position of space in String
	 * @param strSource String to search
	 * @param iOffset start offset
	 * @return first position of space (unprintable character)
	 * in strSource, -1 if not found
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int indexOfSpace(String strSource,int iOffset)
	{
		char c;
		while(iOffset < strSource.length())
		{
			c = strSource.charAt(iOffset);
			if(c > ' ')
				iOffset++;
			else
				return iOffset;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	/**
	 * Return number occurence of symbol in a string
	 * @param strSource String to search
	 * @param chrSymbol symbol to count
	 * @param iOffset start offset
	 * @return number occurence of symbol in strSource
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static int countSymbol(String strSource,String chrSymbol,int iOffset)
	{
		if(chrSymbol == null || chrSymbol.length() == 0)
			return 0;
		int iCount = 0;
		while((iOffset = strSource.indexOf(chrSymbol,iOffset) + 1) > 0)
			iCount++;
		return iCount;
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert a string to string array base on separator
	 * @param strSource String to convert
	 * @param strSeparator separator symbol
	 * @return String array after convert
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String[] toStringArray(String strSource,String strSeparator)
	{
		Vector vtReturn = toStringVector(strSource,strSeparator);
		String[] strReturn = new String[vtReturn.size()];
		for(int iIndex = 0;iIndex < strReturn.length;iIndex++)
			strReturn[iIndex] = (String)vtReturn.elementAt(iIndex);
		return strReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert a string to string array base on separator
	 * @param strSource String to convert
	 * @param strSeparator separator symbol
	 * @return String array after convert
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static Vector toStringVector(String strSource,String strSeparator)
	{
		Vector vtReturn = new Vector();
		int iIndex = 0;
		int iLastIndex = 0;
		while((iIndex = strSource.indexOf(strSeparator,iLastIndex)) >= 0)
		{
			vtReturn.addElement(strSource.substring(iLastIndex,iIndex).trim());
			iLastIndex = iIndex + strSeparator.length();
		}
		if(iLastIndex <= strSource.length())
			vtReturn.addElement(strSource.substring(iLastIndex,strSource.length()).trim());
		return vtReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert a string to string array base on ',' symbol
	 * @param strSource String to convert
	 * @return String array after convert
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String[] toStringArray(String strSource)
	{
		return toStringArray(strSource,",");
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert a string to string array base on ',' symbol
	 * @param strSource String to convert
	 * @return String array after convert
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static Vector toStringVector(String strSource)
	{
		return toStringVector(strSource,",");
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert char form
	 * @param strSource String to convert
	 * @param strCharformSource source charform
	 * @param strCharformDestination destination charform
	 * @return String after convert
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String convertCharForm(String strSource,String strCharformSource,String strCharformDestination)
	{
		if(strSource == null)
			return null;
		int iLength = strSource.length();
		int iResult = 0;
		StringBuffer strReturn = new StringBuffer();
		for(int iIndex = 0;iIndex < iLength;iIndex++)
		{
			char c = strSource.charAt(iIndex);
			if((iResult = strCharformSource.indexOf(c)) >= 0)
				strReturn.append(strCharformDestination.charAt(iResult));
			else
				strReturn.append(c);
		}
		return strReturn.toString();
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert an unicode string to tcvn string
	 * @param strSource String to convert
	 * @return String array after convert
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String unicodeToTCVN(String strSource)
	{
		return convertCharForm(strSource,CHARFORM_UNICODE,CHARFORM_TCVN);
	}
	////////////////////////////////////////////////////////
	/**
	 * Convert an tcvn string to unicode string
	 * @param strSource String to convert
	 * @return String array after convert
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String tcvnToUnicode(String strSource)
	{
		return convertCharForm(strSource,CHARFORM_TCVN,CHARFORM_UNICODE);
	}
	////////////////////////////////////////////////////////
	/**
	 * Clear all horn in unicode string
	 * @param strSource String to convert
	 * @return String array after convert
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String clearHornUnicode(String strSource)
	{
		return convertCharForm(strSource,CHARFORM_UNICODE,CHARFORM_NOHORN);
	}
	////////////////////////////////////////////////////////
	/**
	 * Clear all horn in unicode string
	 * @param strSource String to convert
	 * @return String array after convert
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String clearHornTCVN(String strSource)
	{
		return convertCharForm(strSource,CHARFORM_TCVN,CHARFORM_NOHORN);
	}
	////////////////////////////////////////////////////////
	/**
	 * Pronounce a number to vietnamese
	 * @param lNumber nuber to pronoun
	 * @return String contain sound of number
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	public static String pronounceVietnameseNumber(long lNumber)
	{
		String strUnit[] = new String[] {"","nghìn","triệu","tỷ","nghìn tỷ","triệu tỷ","nghìn triệu tỷ","tỷ tỷ"};

		// Analyse the number to array
		byte btDecimalNumber[] = new byte[30];
		byte btDecimalCount = 0;
		boolean bNegative = (lNumber < 0);
		if(bNegative)
			lNumber = -lNumber;
		while(lNumber > 0)
		{
			byte btValue = (byte)(lNumber - 10 * (lNumber / 10));
			lNumber /= 10;
			btDecimalNumber[btDecimalCount++] = btValue;
		}

		// Pronounce array
		String strReturn = "";
		int iUnitIndex = 0;
		while(iUnitIndex < strUnit.length && iUnitIndex * 3 < btDecimalCount)
		{
			String str = pronounceVietnameseNumber(btDecimalNumber[iUnitIndex * 3],
												  btDecimalNumber[iUnitIndex * 3 + 1],
												  btDecimalNumber[iUnitIndex * 3 + 2],
												  iUnitIndex * 3 + 2 < btDecimalCount);
			if(str.length() > 0)
			{
				if(strReturn.length() > 0)
					strReturn = str + " " + strUnit[iUnitIndex] + " " + strReturn;
				else
					strReturn = str + " " + strUnit[iUnitIndex];
			}
			iUnitIndex++;
		}
		if(bNegative)
			strReturn = "âm " + strReturn;
		return strReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 * Pronounce a number to vietnamese
	 * @param bUnit unit part
	 * @param bTen tens part
	 * @param bHundred hundred part
	 * @param bZeroHundred if hundred part = 0
	 * @return String contain sound of number
	 * @author Thai Hoang Hiep
	 */
	////////////////////////////////////////////////////////
	private static String pronounceVietnameseNumber(byte bUnit,byte bTen,byte bHundred,boolean bMax)
	{
		// Return zero
		if(bUnit == 0 && bTen == 0 && bHundred == 0)
			return "";

		String strUnitSuffix[] = new String[] {"","một","hai","ba","tư","lăm","sáu","bảy","tám","chín"};
		String strUnitTen[] = new String[] {"","mười một","mười hai","mười ba","mười bốn","mười lăm","mười sáu","mười bảy","mười tám","mười chín"};
		String strUnit[] = new String[] {"","một","hai","ba","bốn","năm","sáu","bảy","tám","chín"};
		String strTenFirst[] = new String[] {"","mười một","hai mươi mốt","ba mươi mốt","bốn mươi mốt","năm mươi mốt","sáu mươi mốt","bảy mươi mốt","tám mươi mốt","chín mươi mốt"};
		String strTen[] = new String[] {"","mười","hai mươi","ba mươi","bốn mươi","năm mươi","sáu mươi","bảy mươi","tám mươi","chín mươi"};
		String strHundred[] = new String[] {"không trăm","một trăm","hai trăm","ba trăm","bốn trăm","năm trăm","sáu trăm","bảy trăm","tám trăm","chín trăm"};
		String strReturn = "";

		if(bMax || bHundred > 0)
			strReturn = strHundred[bHundred];
		if(bTen > 0)
		{
			if(strReturn.length() > 0)
				strReturn += " ";
			if(bUnit > 0)
			{
				if(bTen == 1)
					strReturn += strUnitTen[bUnit];
				else
				{
					if(bUnit == 1)
						strReturn += strTenFirst[bTen];
					else
						strReturn += strTen[bTen] + " " + strUnitSuffix[bUnit];
				}
			}
			else
				strReturn += strTen[bTen];
		}
		else
		{
			if(bUnit > 0)
			{
				if(strReturn.length() > 0)
					strReturn += " linh " + strUnit[bUnit];
				else
					strReturn = strUnit[bUnit];
			}
		}
		return strReturn;
	}
	////////////////////////////////////////////////////////
	/**
	 *
	 * @param str String
	 * @param iAlignment int
	 * @param iLength int
	 * @return String
	 */
	////////////////////////////////////////////////////////
	public static String align(String str,int iAlignment,int iLength)
	{
		if(str == null)
			return null;
		if(str.length() > iLength)
			return str.substring(0,iLength);
		StringBuffer buf = new StringBuffer();
		if(iAlignment == ALIGN_CENTER)
		{
			int iFirstLength = (iLength - str.length()) / 2;
			for(int iIndex = 0;iIndex < iFirstLength;iIndex++)
				buf.append(" ");
			buf.append(str);
			for(int iIndex = str.length() + iFirstLength;iIndex < iLength;iIndex++)
				buf.append(" ");
		}
		else if(iAlignment == ALIGN_RIGHT)
		{
			iLength = iLength - str.length();
			for(int iIndex = 0;iIndex < iLength;iIndex++)
				buf.append(" ");
			buf.append(str);
		}
		else
		{
			buf.append(str);
			for(int iIndex = str.length();iIndex < iLength;iIndex++)
				buf.append(" ");
		}
		return buf.toString();
	}
}

package com.fss.vietnamese;

/**
 * Insert the type's description here.
 * Creation date: (12.03.2002 16:47:08)
 * @author:
 */
public class UnicodeEncoding implements Encoding
{
	static char UPPER_D_STROKE = '\u0110';
	static char LOWER_D_STROKE = '\u0111';
	static char UPPER_A_BREVE = '\u0102'; // A(
	static char LOWER_A_BREVE = '\u0103'; // a(
	static char UPPER_A_CIRC = '\u00c2'; // Â
	static char LOWER_A_CIRC = '\u00e2'; // â
	static char UPPER_E_CIRC = '\u00ca'; // Ê
	static char LOWER_E_CIRC = '\u00ea'; // ê
	static char UPPER_O_CIRC = '\u00d4'; // Ô
	static char LOWER_O_CIRC = '\u00f4'; // ô
	static char UPPER_O_HORN = '\u01a0'; // O+
	static char LOWER_O_HORN = '\u01a1'; // o+
	static char UPPER_U_HORN = '\u01af'; // O+
	static char LOWER_U_HORN = '\u01b0'; // o+
	public static char[] HND =
	{
		0x0041, 0x00c0, 0x1ea2, 0x00c3, 0x00c1, 0x1ea0,
		0x0102, 0x1eb0, 0x1eb2, 0x1eb4, 0x1eae, 0x1eb6,
		0x00c2, 0x1ea6, 0x1ea8, 0x1eaa, 0x1ea4, 0x1eac,
		0x0045, 0x00c8, 0x1eba, 0x1ebc, 0x00c9, 0x1eb8,
		0x00ca, 0x1ec0, 0x1ec2, 0x1ec4, 0x1ebe, 0x1ec6,
		0x0049, 0x00cc, 0x1ec8, 0x0128, 0x00cd, 0x1eca,
		0x004f, 0x00d2, 0x1ece, 0x00d5, 0x00d3, 0x1ecc,
		0x00d4, 0x1ed2, 0x1ed4, 0x1ed6, 0x1ed0, 0x1ed8,
		0x01a0, 0x1edc, 0x1ede, 0x1ee0, 0x1eda, 0x1ee2,
		0x0055, 0x00d9, 0x1ee6, 0x0168, 0x00da, 0x1ee4,
		0x01af, 0x1eea, 0x1eec, 0x1eee, 0x1ee8, 0x1ef0,
		0x0059, 0x1ef2, 0x1ef6, 0x1ef8, 0x00dd, 0x1ef4,
		0x0061, 0x00e0, 0x1ea3, 0x00e3, 0x00e1, 0x1ea1,
		0x0103, 0x1eb1, 0x1eb3, 0x1eb5, 0x1eaf, 0x1eb7,
		0x00e2, 0x1ea7, 0x1ea9, 0x1eab, 0x1ea5, 0x1ead,
		0x0065, 0x00e8, 0x1ebb, 0x1ebd, 0x00e9, 0x1eb9,
		0x00ea, 0x1ec1, 0x1ec3, 0x1ec5, 0x1ebf, 0x1ec7,
		0x0069, 0x00ec, 0x1ec9, 0x0129, 0x00ed, 0x1ecb,
		0x006f, 0x00f2, 0x1ecf, 0x00f5, 0x00f3, 0x1ecd,
		0x00f4, 0x1ed3, 0x1ed5, 0x1ed7, 0x1ed1, 0x1ed9,
		0x01a1, 0x1edd, 0x1edf, 0x1ee1, 0x1edb, 0x1ee3,
		0x0075, 0x00f9, 0x1ee7, 0x0169, 0x00fa, 0x1ee5,
		0x01b0, 0x1eeb, 0x1eed, 0x1eef, 0x1ee9, 0x1ef1,
		0x0079, 0x1ef3, 0x1ef7, 0x1ef9, 0x00fd, 0x1ef5,
	};
	public static short[] DNH = null;
	static
	{
		initialize();
	}
	static UnicodeEncoding fInstance = new UnicodeEncoding();
	public char addBreveHorn(char c)
	{
		switch(c)
		{
			case 'A':
				return UPPER_A_BREVE;
			case 'O':
				return UPPER_O_HORN;
			case 'U':
				return UPPER_U_HORN;
			case 'a':
				return LOWER_A_BREVE;
			case 'o':
				return LOWER_O_HORN;
			case 'u':
				return LOWER_U_HORN;
			default:
			{
				return c;
			}
		}
	}
	public char addCircumflex(char c)
	{
		switch(c)
		{
			case 'A':
				return UPPER_A_CIRC;
			case 'E':
				return UPPER_E_CIRC;
			case 'O':
				return UPPER_O_CIRC;
			case 'a':
				return LOWER_A_CIRC;
			case 'e':
				return LOWER_E_CIRC;
			case 'o':
				return LOWER_O_CIRC;
			default:
			{
				return c;
			}
		}
	}
	public static char getOriginChar(char chr)
	{
		char chrOrigin = chr;
		if(chr == UPPER_D_STROKE)
			chrOrigin = 'D';
		else if(chr == LOWER_D_STROKE)
			chrOrigin = 'd';
		else if(chr == UPPER_A_BREVE || chr == UPPER_A_CIRC)
			chrOrigin = 'A';
		else if(chr == LOWER_A_BREVE || chr == LOWER_A_CIRC)
			chrOrigin = 'a';
		else if(chr == UPPER_E_CIRC)
			chrOrigin = 'E';
		else if(chr == LOWER_E_CIRC)
			chrOrigin = 'e';
		else if(chr == UPPER_O_CIRC || chr == UPPER_O_HORN)
			chrOrigin = 'O';
		else if(chr == LOWER_O_CIRC || chr == LOWER_O_HORN)
			chrOrigin = 'o';
		else if(chr == UPPER_U_HORN)
			chrOrigin = 'U';
		else if(chr == LOWER_U_HORN)
			chrOrigin = 'u';
		return chrOrigin;
	}
	/**
	 * addStroke method comment.
	 */
	public char addStroke(char c) {
		if (c == 'D') return UPPER_D_STROKE;
		if (c == 'd') return LOWER_D_STROKE;
		return c;
	}

	public String fromUnicode(String str) {
		return str;
	}
	/**
	 * getDescription method comment.
	 */
	public java.lang.String getDescription() {
		return "Unicode encoding";
	}
	/**
	 * getID method comment.
	 */
	public java.lang.String getID() {
		return "ISO10646";
	}
	public static Encoding getInstance() {
		return fInstance;
	}
	/**
	 * hasDiacritic method comment.
	 */
	public boolean hasDiacritic(char c) {
		if (c == UPPER_D_STROKE || c == LOWER_D_STROKE) return true;
		return isVowel(c) && c >= 128;
	}
	private static void initialize() {
		int len = 0;
		for (int i = 0; i < HND.length; i++){
			len = Math.max(HND[i], len);
		}
		DNH = new short[len+1];
		for (int i = 0; i < DNH.length; i++){
			DNH[i] = -1;
		}
		for (int i = 0; i < HND.length; i++){
			DNH[HND[i]] = (short)i;
		}
	}
	/**
	 * isVowel method comment.
	 */
	public boolean isVowel(char c) {
		if (c >= DNH.length) return false;
		return DNH[c] != -1;
	}
	/**
	 * modifyTone method comment.
	 */
	public char modifyTone(char c, int toneMark) {
		if (c >= DNH.length || DNH[c] == -1) {
			return c;
		}
		int i = DNH[c];
		return HND[i - (i % 6) + toneMark];
	}

	/**
	 * toUnicode method comment.
	 */
	public java.lang.String toUnicode(java.lang.String str) {
		return str;
	}
}
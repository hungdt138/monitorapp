package com.fss.asn1;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: FPT - FSS</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public interface ASNDataHandler
{
	int STATUS_CONTINUE = 0;
	int STATUS_IGNORE = 1;
	int STATUS_END = 2;
	////////////////////////////////////////////////////////
	/**
	 * Event occured while begin node symbol found
	 * @param iTagID int
	 * @param btClass byte
	 * @param btConstructed byte
	 * @param def ASNDefinition
	 * @return int
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	int beginNode(int iTagID,byte btClass,byte btConstructed,ASNDefinition def) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Event occured while reading node data
	 * @param btData byte[]
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	void dataNode(byte[] btData) throws Exception;
	////////////////////////////////////////////////////////
	/**
	 * Event occured while end node symbol found
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	void endNode() throws Exception;
}

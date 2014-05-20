package com.fss.asn1;

import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: FPT - FSS</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public abstract class DefaultASNDataHandler implements ASNDataHandler
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	public ASNData mdat;
	public Vector mvtDataStack = new Vector();
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
	public int beginNode(int iTagID,byte btClass,byte btConstructed,ASNDefinition def) throws Exception
	{
		ASNData dat = new ASNData();
		dat.miTagID = iTagID;
		dat.mbtClass = btClass;
		dat.mbtConstructed = btConstructed;
		dat.mpDefinition = def;
		if(mvtDataStack.size() > 0)
			((ASNData)mvtDataStack.elementAt(mvtDataStack.size() - 1)).addChild(dat);
		mdat = dat;
		mvtDataStack.addElement(mdat);
		return STATUS_CONTINUE;
	}
	////////////////////////////////////////////////////////
	/**
	 * Event occured while reading node data
	 * @param btData byte[]
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void dataNode(byte[] btData) throws Exception
	{
		mdat.mbtData = btData;
	}
	////////////////////////////////////////////////////////
	/**
	 * Event occured while end node symbol found
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public void endNode() throws Exception
	{
		if(mvtDataStack.size() == 1)
			processRecord((ASNData)mvtDataStack.elementAt(0));
		mvtDataStack.removeElementAt(mvtDataStack.size() - 1);
	}
	////////////////////////////////////////////////////////
	/**
	 * Process one record
	 * @param dat ASNData
	 * @throws Exception
	 */
	////////////////////////////////////////////////////////
	public abstract void processRecord(ASNData dat) throws Exception;
}

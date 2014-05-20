package com.fss.swing;

import java.io.*;
import java.awt.datatransfer.*;
import javax.swing.tree.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TransferableData implements Transferable
{
	////////////////////////////////////////////////////////
	// Constant
	////////////////////////////////////////////////////////
	public static final DataFlavor TRANSFERABLE_DATA_FLAVOR = new DataFlavor(DefaultMutableTreeNode.class, "DefaultMutableTreeNode");
	public static final DataFlavor TRANSFERABLE_DATA_FLAVORS[] = {TRANSFERABLE_DATA_FLAVOR};
	private Object mobjData;
	////////////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////////////
	public TransferableData(Object obj)
	{
		mobjData = obj;
	}
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public boolean isDataFlavorSupported(DataFlavor df)
	{
		return df.equals(TRANSFERABLE_DATA_FLAVOR);
	}
	////////////////////////////////////////////////////////
	public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException,IOException
	{
		if(df.equals(TRANSFERABLE_DATA_FLAVOR))
			return mobjData;
		throw new UnsupportedFlavorException(df);
	}
	////////////////////////////////////////////////////////
	public DataFlavor[] getTransferDataFlavors()
	{
		return TRANSFERABLE_DATA_FLAVORS;
	}
}

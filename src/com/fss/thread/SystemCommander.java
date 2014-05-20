package com.fss.thread;

import java.util.*;

import com.fss.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class SystemCommander extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected String mstrCommand;
	protected int miTimeout;
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = new Vector();

		vtReturn.addElement(createParameterDefinition("Command","",ParameterType.PARAM_TEXTAREA_MAX,"4000",""));
		vtReturn.addElement(createParameterDefinition("Timeout","",ParameterType.PARAM_TEXTBOX_MASK,"99990",""));
		vtReturn.addAll(super.getParameterDefinition());

		return vtReturn;
	}
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		mstrCommand = loadMandatory("Command");
		miTimeout = loadUnsignedInteger("Timeout") * 1000;
		super.fillParameter();
	}
	////////////////////////////////////////////////////////
	public void processSession() throws Exception
	{
		logMonitor("Start executing command\r\n\t" + mstrCommand);

		// Execute
		int iResult = exec(mstrCommand,miTimeout);
		if(iResult != 0 && iResult != -1)
			throw new Exception("Execution failure");

		logMonitor("Completed");
	}
}

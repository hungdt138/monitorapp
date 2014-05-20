package com.fss.thread;

import java.sql.*;
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

public class SQLCommander extends ManageableThread
{
	////////////////////////////////////////////////////////
	// Member variables
	////////////////////////////////////////////////////////
	protected String mstrCommand;
	////////////////////////////////////////////////////////
	// Override
	////////////////////////////////////////////////////////
	public Vector getParameterDefinition()
	{
		Vector vtReturn = new Vector();

		vtReturn.addElement(createParameterDefinition("SQLCommand","",ParameterType.PARAM_TEXTAREA_MAX,"4000",""));
		vtReturn.addAll(super.getParameterDefinition());

		for(int iIndex = vtReturn.size() - 1;iIndex >= 0;iIndex--)
		{
			String strParameterName = (String)((Vector)vtReturn.elementAt(iIndex)).elementAt(0);
			if(strParameterName.equals("ConnectDB"))
				vtReturn.removeElementAt(iIndex);
		}

		return vtReturn;
	}
	////////////////////////////////////////////////////////
	public void fillParameter() throws AppException
	{
		mstrCommand = loadMandatory("SQLCommand");
		super.fillParameter();
		mbAutoConnectDB = true;
	}
	////////////////////////////////////////////////////////
	public void processSession() throws Exception
	{
		logMonitor("Start executing command\r\n" + mstrCommand);

		// Enable output
		Statement stmt = mcnMain.createStatement();
		stmt.executeUpdate("{ call DBMS_OUTPUT.ENABLE }");

		// Execute
		stmt = mcnMain.createStatement();
		stmt.executeUpdate(mstrCommand);

		// Flush output
		stmt.executeUpdate("{ call DBMS_OUTPUT.ENABLE }");

		// Fill output
		CallableStatement cstmt = mcnMain.prepareCall("{ call DBMS_OUTPUT.GET_LINE(?,?) }");
		cstmt.registerOutParameter(1,java.sql.Types.VARCHAR);
		cstmt.registerOutParameter(2,java.sql.Types.NUMERIC);
		while(true)
		{
			cstmt.executeUpdate();
			if(cstmt.getString(2).equals("0"))
				logMonitor(StringUtil.nvl(cstmt.getString(1),""));
			else
				break;
		}

		// Log completed
		logMonitor("Completed");
	}
}

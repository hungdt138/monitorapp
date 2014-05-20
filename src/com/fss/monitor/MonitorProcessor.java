package com.fss.monitor;

import javax.swing.*;

import com.fss.ddtp.*;
import com.fss.util.*;
import com.fss.swing.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *     MonitorProcessor only read request from server
 *     and pass response for it
 * </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT</p>
 * @author
 *  - Thai Hoang Hiep
 *  - Dang Dinh Trung
 * @version 2.0
 */

public class MonitorProcessor extends SocketProcessor
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	private static Object mobjRoot = null;
	private static Class mclsRoot = MonitorProcessor.class;
	////////////////////////////////////////////////////////
	// RootObject Hashtable
	////////////////////////////////////////////////////////
	public static void setRootObject(Object obj)
	{
		mobjRoot = obj;
		mclsRoot = obj.getClass();
	}
	////////////////////////////////////////////////////////
	public static Object getRootObject()
	{
		return mobjRoot;
	}
	////////////////////////////////////////////////////////
	public static Class getRootClass()
	{
		return mclsRoot;
	}
	///////////////////////////////////////////////////////////
	// Purpose: called from server, show result after start, stop thread
	// Author: TrungDD
	// Date: 09/2003
	///////////////////////////////////////////////////////////
	public void logMonitor()
	{
		try
		{
			String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
			String strResult = StringUtil.nvl(request.getString("LogResult"),"");
			String strStatus = StringUtil.nvl(request.getString("ThreadStatus"),"");
			if(strThreadID.length() > 0)
			{
				PanelThreadManager pnl = ((PanelThreadManager)getRootObject());
				try
				{
					PanelThreadMonitor pnlThrMonitor = pnl.getPanelThreadMonitor(strThreadID);
					if(pnlThrMonitor != null)
					{
						pnl.showResult(pnlThrMonitor.txtMonitor,strResult);
						pnl.updateStatus(strThreadID,strStatus);
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	///////////////////////////////////////////////////////////
	// Purpose: delete one disabled thread from panel
	//          called from server when a thread was disabled
	// Author: TrungDD
	// Date: 09/2003
	///////////////////////////////////////////////////////////
	public void unloadThread()
	{
		try
		{
			String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
			if(strThreadID.length() > 0)
			{
				PanelThreadManager pnl = ((PanelThreadManager)getRootObject());
				pnl.unloadThread(strThreadID);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	///////////////////////////////////////////////////////////
	// Purpose: insert one enabled thread to panel
	//          called from server when a thread was enabled
	// Author: TrungDD
	// Date: 09/2003
	///////////////////////////////////////////////////////////
	public void loadThread()
	{
		try
		{
			String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
			if(strThreadID.length() > 0)
			{
				String strThreadName = StringUtil.nvl(request.getString("ThreadName"),"");
				String strThreadStatus = StringUtil.nvl(request.getString("ThreadStatus"),"");
				PanelThreadManager client = ((PanelThreadManager)getRootObject());
				client.loadThread(strThreadID,strThreadName,strThreadStatus);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	///////////////////////////////////////////////////////////
	// Purpose: show confirm dialog for old user when new user has
	//          entered
	// Author: TrungDD
	// Date: 09/2003
	///////////////////////////////////////////////////////////
	public void confirmClose()
	{
		try
		{
			PanelThreadManager pnl = ((PanelThreadManager)getRootObject());
			DialogConfirmShutdown dlgConfirm = new DialogConfirmShutdown(pnl);
			WindowManager.centeredWindow(dlgConfirm);
			response.setString("AcceptStatus",String.valueOf(dlgConfirm.bAcceptToExit));

			// Dong y thoat thi gui message va dong client
			if(dlgConfirm.bAcceptToExit)
			{
				String strRequestID = StringUtil.nvl(request.getRequestID(),"");
				if(strRequestID.length() > 0 && pnl.isOpen())
				{
					response.setResponseID(strRequestID);
					pnl.channel.sendResponse(response);
					Thread.sleep(100);
					pnl.disconnect();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			MessageBox.showMessageDialog(WindowManager.getFocusOwner(),e,Global.APP_NAME,MessageBox.ERROR_MESSAGE);
		}
	}
	///////////////////////////////////////////////////////////
	// Purpose: rename one thread when its name in server has changed
	// Author: TrungDD
	// Date: 09/2003
	///////////////////////////////////////////////////////////
	public void renameThread()
	{
		try
		{
			String strThreadID = StringUtil.nvl(request.getString("ThreadID"),"");
			if(strThreadID.length() > 0)
			{
				String strThreadName = StringUtil.nvl(request.getString("ThreadName"),"");
				PanelThreadManager client = ((PanelThreadManager)getRootObject());
				client.renameThread(strThreadID,strThreadName);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	///////////////////////////////////////////////////////////
	// Purpose: show actionlog
	// Author: HiepTH
	// Date: 24/03/2004
	///////////////////////////////////////////////////////////
	public void logAction()
	{
		try
		{
			String strResult = StringUtil.nvl(request.getString("strLog"),"");
			PanelThreadManager pnl = ((PanelThreadManager)getRootObject());
			pnl.showResult(pnl.txtBoard,strResult);
			if(!JOptionPane.getFrameForComponent(pnl).isActive())
				JOptionPane.getFrameForComponent(pnl).toFront();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	///////////////////////////////////////////////////////////
	// Purpose: show actionlog
	// Author: HiepTH
	// Date: 24/03/2004
	///////////////////////////////////////////////////////////
	public void userConnected()
	{
		try
		{
			String strChannel = request.getString("strChannel");
			String strUserName = request.getString("strUserName");
			String strStartDate = request.getString("strStartDate");
			String strHost = request.getString("strHost");
			((PanelThreadManager)getRootObject()).addUser(strChannel,strUserName,strStartDate,strHost);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	///////////////////////////////////////////////////////////
	// Purpose: show actionlog
	// Author: HiepTH
	// Date: 24/03/2004
	///////////////////////////////////////////////////////////
	public void userDisconnected()
	{
		try
		{
			String strChannel = request.getString("strChannel");
			((PanelThreadManager)getRootObject()).removeUser(strChannel);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

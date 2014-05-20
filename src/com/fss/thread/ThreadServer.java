package com.fss.thread;

import java.net.*;
import java.util.*;
import com.fss.util.*;

import com.fss.ddtp.*;

/**
 * <p>Title: Listen server socket</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FSS-FPT-BU5</p>
 * @author Dang Dinh Trung
 * @version 1.0
 */

public class ThreadServer implements Runnable
{
	protected ServerSocket serverSocket;
	protected Thread mthrMain = null;
	protected ThreadManager mmgrMain;
	////////////////////////////////////////////////////////
	// Constructor of ThreadServer /////////////////
	////////////////////////////////////////////////////////
	public ThreadServer(ServerSocket aServerSocket,ThreadManager mgr)
	{
		serverSocket = aServerSocket;
        mmgrMain = mgr;
	}
	/////////////////////////////////////////////////////////
	// Purpose: listen from server socket and create new connection
	//          check if there is more than one connection at once
	// Author: TrungDD
	// Date: 09/2003
	/////////////////////////////////////////////////////////
	public void run()
	{
		try
		{
			while(mmgrMain.isOpen())
			{
				Socket sck = null;
				String strUserName = "";
				String strPassword = "";
				String strRequestID = "";

				try
				{
					// Accept a new connection
					sck = serverSocket.accept();
				}
				catch(Exception e)
				{
					e.printStackTrace();
					continue;
				}

				try
				{
					DDTP request = new DDTP(sck.getInputStream());
					strUserName = request.getString("UserName");
					strPassword = request.getString("Password");
					strRequestID = request.getRequestID();

					// Login to system
					if(!request.getFunctionName().equals("login"))
						throw new AppException("FSS-00020","ThreadServer.run");
					String strUserID = null;

					// Check username, password
					ThreadProcessor processor = new ThreadProcessor(mmgrMain);
					Vector vtLogin = processor.login(strUserName,strPassword,sck.getInetAddress());
					strUserID = (String)vtLogin.elementAt(0);

					// If an user already connected
					if(mmgrMain.isFullConnected())
					{
						// Confirm old user
						String strAccept = "false";
						int iIndex = 0;
						while(!strAccept.equals("true") && iIndex < mmgrMain.getChannelCount())
						{
							SocketTransmitter channel = (SocketTransmitter)mmgrMain.mvtChannel.elementAt(iIndex++);
							request = new DDTP();
							request.setRequestID(String.valueOf(System.currentTimeMillis()));
							try
							{
								DDTP response = channel.sendRequest("MonitorProcessor","confirmClose",request);
								strAccept = StringUtil.nvl(response.getString("AcceptStatus"),"");
							}
							catch(Exception e)
							{
								strAccept = "true";
							}
							if(strAccept.equals("true"))
								channel.close();
						}
						if(!strAccept.equals("true"))
							throw new AppException("FSS-00019","ThreadServer.run");
					}

					// Create thread handle new connection
					SocketTransmitter channel = new SocketTransmitter(sck)
					{
						public void close()
						{
							if(msckMain != null)
							{
								mmgrMain.logAction("<FONT color=\"#CC6622\"><U>User '" + mstrUserName + "' disconnected</U></FONT>");
								mmgrMain.notifyUserDisconnected(this);
								super.close();
							}
							mmgrMain.removeChannel(this);
						}
					};
					channel.mobjParent = mmgrMain;
					channel.setUserID(strUserID);
					channel.setUserName(strUserName);
					channel.setPackage("com.fss.monitor.");
					channel.start();
					mmgrMain.addChannel(channel);

					// Reupdate ThreadMonitors and get information about the running threads
					if(strRequestID.length() > 0)
					{
						Vector vtThread = mmgrMain.getThreadInfoList();
						DDTP response = new DDTP();
						response.setResponseID(strRequestID);
						response.setString("strLog",mmgrMain.getLogContent());
						response.setVector("vtThread",vtThread);
						response.setString("strChannel",channel.toString());
						response.setString("strThreadAppName",ThreadConstant.APP_NAME);
						response.setString("strThreadAppVersion",ThreadConstant.APP_VERSION);
						response.setString("strAppName",Global.APP_NAME);
						response.setString("strAppVersion",Global.APP_VERSION);
						if(vtLogin.elementAt(1).equals("0")) // Expired
							response.setString("PasswordExpired","");
						channel.sendResponse(response);
					}
					mmgrMain.logAction("<FONT color=\"#2266CC\"><U>User '" + strUserName + "' connected successfully</U></FONT>");
					mmgrMain.notifyUserConnected(channel);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					mmgrMain.logAction("<FONT color=\"#CC6622\"><U>User '" + strUserName + "' connected failure</U></FONT>");

					try
					{
						// Send response
						if(strRequestID.length() > 0)
						{
							DDTP response = new DDTP();
							response.setResponseID(strRequestID);
							if(e instanceof AppException)
								response.setException((AppException)e);
							else
								response.setException(new AppException(e.getMessage(),"ThreadServer.run",""));

							// Ensure ddtp is sent
							response.store(sck.getOutputStream());
						}
					}
					catch(Exception e1)
					{
						e1.printStackTrace();
					}

					try
					{
						Thread.sleep(1500);
						sck.close();
					}
					catch(Exception e1)
					{
					}
				}
			}
		}
		finally
		{
			try
			{
				serverSocket.close();
			}
			catch(Exception e)
			{
			}
		}
	}
	//////////////////////////////////////////////////////////
	public void start()
	{
		if(mthrMain != null)
			mthrMain.stop();
		mthrMain = new Thread(this);
		mthrMain.start();
	}
}

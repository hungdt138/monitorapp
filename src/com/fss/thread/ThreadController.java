package com.fss.thread;

import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ThreadController implements Runnable
{
	////////////////////////////////////////////////////////
	private Vector mvtThread = new Vector();
	private Thread mthrMain;
	private boolean mbRunning = true;
	protected ThreadManager mmgrMain;
	////////////////////////////////////////////////////////
	public ThreadController(ThreadManager mgr)
	{
		mmgrMain = mgr;
	}
	////////////////////////////////////////////////////////
	public boolean complete()
	{
		return(mvtThread.size() == 0);
	}
	////////////////////////////////////////////////////////
	private int indexOf(ManageableThread thr)
	{
		int iThreadIndex = 0;
		while(iThreadIndex < mvtThread.size())
		{
			Vector vtThreadInfo = (Vector)mvtThread.elementAt(iThreadIndex);
			if(vtThreadInfo.elementAt(0) == thr)
				return iThreadIndex;
			iThreadIndex++;
		}
		return -1;
	}
	////////////////////////////////////////////////////////
	public void removeThread(ManageableThread thr)
	{
		int iThreadIndex = indexOf(thr);
		if(iThreadIndex >= 0)
			mvtThread.removeElementAt(iThreadIndex);
	}
	////////////////////////////////////////////////////////
	public void addCommand(ManageableThread thr,int iCommand)
	{
		int iThreadIndex = indexOf(thr);
		if(iThreadIndex >= 0)
		{
			Vector vtThreadInfo = (Vector)mvtThread.elementAt(iThreadIndex);
			Vector vtCommandList = (Vector)vtThreadInfo.elementAt(1);
			vtCommandList.addElement(new Integer(iCommand));
		}
		else
		{
			Vector vtCommandList = new Vector();
			vtCommandList.addElement(new Integer(iCommand));

			Vector vtThreadInfo = new Vector();
			vtThreadInfo.addElement(thr);
			vtThreadInfo.addElement(vtCommandList);
			mvtThread.addElement(vtThreadInfo);
		}
	}
	////////////////////////////////////////////////////////
	public void start()
	{
		if(mthrMain != null)
			mthrMain.stop();
		mthrMain = new Thread(this);
		mthrMain.start();
	}
	////////////////////////////////////////////////////////
	public void run()
	{
		try
		{
			mbRunning = true;
			while(mbRunning)
			{
				try
				{
					int iThreadIndex = 0;
					while(iThreadIndex < mvtThread.size())
					{
						// Get command list and thread
						Vector vtThreadInfo = (Vector)mvtThread.elementAt(iThreadIndex);
						ManageableThread thr = (ManageableThread)vtThreadInfo.elementAt(0);
						Vector vtCommandList = (Vector)vtThreadInfo.elementAt(1);
						iThreadIndex++;

						// Process command
						if(vtCommandList.size() <= 0)
							this.removeThread(thr);
						else
						{
							// Process first command only
							int iCommand = ((Integer)vtCommandList.elementAt(0)).intValue();
							if(iCommand == ThreadConstant.THREAD_START)
							{
								if(thr.miThreadStatus != iCommand)
								{
									thr.start();
									while(thr.miThreadStatus != iCommand)
									{
										try
										{
											Thread.sleep(100);
										}
										catch(Exception e)
										{
										}
									}
									thr.miThreadCommand = ThreadConstant.THREAD_NONE;
								}
								vtCommandList.removeElementAt(0);
							}
							else if(iCommand == ThreadConstant.THREAD_STOP)
							{
								if(thr.miThreadStatus == iCommand)
								{
									thr.miThreadCommand = ThreadConstant.THREAD_NONE;
									vtCommandList.removeElementAt(0);
								}
								else
									thr.miThreadCommand = iCommand;
							}
							else
								vtCommandList.removeElementAt(0);
						}
					}
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					try
					{
						Thread.sleep(1000);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}

				}
			}
		}
		finally
		{
			try
			{
				mmgrMain.serverSocket.close();
			}
			catch(Exception e)
			{
			}
		}
	}
	////////////////////////////////////////////////////////
	public void close()
	{
		mbRunning = false;
	}
}

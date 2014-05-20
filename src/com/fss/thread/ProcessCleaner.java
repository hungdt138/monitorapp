package com.fss.thread;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ProcessCleaner implements Runnable
{
	private Process mprcMain;
	public ProcessCleaner(Process prc)
	{
		mprcMain = prc;
		new Thread(this).start();
	}
	public void run()
	{
		try
		{
			mprcMain.waitFor();
			Thread.sleep(500);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				mprcMain.getInputStream().close();
				mprcMain.getOutputStream().close();
				mprcMain.getErrorStream().close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
}

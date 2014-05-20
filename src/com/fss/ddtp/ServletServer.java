package com.fss.ddtp;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.fss.util.*;

/**
 * <p>Title: ServletServer</p>
 * <p>Description: Serve ddtp client request</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: FPT</p>
 * @author Thai Hoang Hiep
 * @version 1.0
 */

public class ServletServer extends HttpServlet
{
	////////////////////////////////////////////////////////
	// Variables
	////////////////////////////////////////////////////////
	public HttpServletRequest httpRequest = null;
	public HttpServletResponse httpResponse = null;
	public HttpSession session = null;
	public String sessionUserID = null;
	public String sessionUserName = null;
	////////////////////////////////////////////////////////
	// Purpose: Overide function
	////////////////////////////////////////////////////////
	public void service(HttpServletRequest httpRequest,HttpServletResponse httpResponse) throws ServletException,IOException
	{
		DDTP response = null;
		try
		{
			// Get request, session create response
			ServletServer srv = new ServletServer();
			srv.httpRequest = httpRequest;
			srv.httpResponse = httpResponse;
			srv.session = httpRequest.getSession(true);
			InputStream is = null;
			DDTP request = null;
			try
			{
				is = httpRequest.getInputStream();
				request = new DDTP(is);
			}
			finally
			{
				FileUtil.safeClose(is);
			}

			// Load some importance variable
			srv.sessionUserName = (String)srv.session.getValue("sessionUserName");
			srv.sessionUserID = (String)srv.session.getValue("sessionUserID");

			// Check session expire
			if(isAuthenRequire())
			{
				if(srv.sessionUserName == null && !request.getFunctionName().equals("login"))
					throw new AppException("SESSION_EXPIRED","ServletServer.service");
			}

			// Process request
			response = Processor.processRequest(srv,request);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			response = new DDTP();
			if(e instanceof AppException)
				response.setException((AppException)e);
			else
				response.setException(new AppException(e.getMessage(),"ServletServer.service",""));
		}
		finally
		{
			// Flush data to client
			OutputStream out = httpResponse.getOutputStream();
			response.store(out);
			out.flush();
			out.close();
		}
	}
	///////////////////////////////////////////////////////////
	protected boolean isAuthenRequire()
	{
		return true;
	}
}

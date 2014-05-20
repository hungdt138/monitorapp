
/*---------------------------------------------------------------------------
 * Copyright (C) 1999-2004 Dallas Semiconductor Corporation, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DALLAS SEMICONDUCTOR BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dallas Semiconductor
 * shall not be used except as stated in the Dallas Semiconductor
 * Branding Policy.
 *---------------------------------------------------------------------------
 */

package com.fss.server.ftp;



import com.fss.server.*;
import java.io.*;
import java.net.*;


/**
 * A simple server that uses FTP as described in RFC 959.  This server uses a ServerSocket to listen
 * on the specified port (defaults to port 21) for FTP connection requests.  For each connection made,
 * an FTP session is created.  All command processing is handled by the FTP session, not this server.
 */
public class FTPServer
   extends Server
{
   static final int       PORT = 21;   // 21 for control connection, 20 for data
   private ServerSocket   socket;
   private static final int DEFAULT_TIMEOUT  = 600000;   //10 minutes in milliseconds
   private int            timeout          = DEFAULT_TIMEOUT;

   /**
	* Prepares the FTP server to listen on the well known FTP port (21).  The server will not
	* be started and no connections will be accepted until its <code>run()</code> method is executed.
	*/
   public FTPServer ()
	  throws IOException
   {
	  this(PORT);
   }

   /**
	* Prepares the FTP server to listen on an arbitrary port.  The server will not
	* be started and no connections will be accepted until its <code>run()</code> method is executed.
	*/
   public FTPServer (int port)
	  throws IOException
   {
	  socket            = new ServerSocket(port);
	  connectionMessage = ""; //HiepTH rem//TINIOS.getFromCurrentEnvironment("FTP_CONNECT");
	  welcomeMessage    = ""; //HiepTH rem//TINIOS.getFromCurrentEnvironment("FTP_WELCOME");
	  logAnon           = ""; //HiepTH rem//TINIOS.getFromCurrentEnvironment("FTP_LOG_ANON");

	  if ((connectionMessage != null) && (connectionMessage.length() == 0))
		 connectionMessage = null;

	  if ((welcomeMessage != null) && (welcomeMessage.length() == 0))
		 welcomeMessage = null;

	  if ((logAnon != null) && (logAnon.length() == 0))
		 logAnon = null;

	  String value = null;

	  /*if ((value = TINIOS.getFromCurrentEnvironment("FTP_ALLOW_ANON"))
			  != null)
	  {
		 anonymousAllowed = !value.equals("false");
	  }
	  else*/ //HiepTH rem
		 anonymousAllowed = true;   //Default is allowed

	  /*if ((value = TINIOS.getFromCurrentEnvironment("FTP_ALLOW_ROOT"))
			  != null)
	  {
		 rootLoginAllowed = !value.equals("false");
	  }
	  else*/ //HiepTH rem
		 rootLoginAllowed = true;   //Default is allowed

	  /*if ((value = TINIOS.getFromCurrentEnvironment("FTP_TIMEOUT")) != null)
	  {
		 try
		 {
			timeout = Integer.parseInt(value);
		 }
		 catch (NumberFormatException nfe)
		 {
//            com.dalsemi.system.Debug.debugDump("Invalid timeout: " + value);

			timeout = DEFAULT_TIMEOUT;
		 }
	  }
	  else*/
		 timeout = DEFAULT_TIMEOUT;
   }

   /**
	* Listens on the connection port for connection requests.  Once a
	* request is made, it creates, initializes, and returns a new
	* FTPSession to handle that request.  This method will block until
	* a connection is made.
	*
	* @return  a new <code>FTPSession</code>
	*/
   protected Session acceptNewSession ()
   {
	  FTPSession newSession = null;

	  try
	  {
		 Socket sock = socket.accept();

		 //If the socket was closed (i.e. the server is being shutdown, accept
		 //will return.  So, check for shutdown before we assume a new session
		 //has been requested.
		 if (shutdown)
			return null;

		 try {
		   sock.setSoTimeout(timeout);
		 }
		 catch (IOException ioe) {
		   return null;
		 }

		 SystemPrintStream sout =
			new SystemPrintStream(sock.getOutputStream());
		 FTPInputStream    sin  = new FTPInputStream(sock.getInputStream(),
													 false);

		 newSession = new FTPSession(sin, sout, sout, sock, this);

		 try
		 {
			newSession.start();
		 }
		 catch(Throwable t)
		 {
			//aah! must be over the limit on Threads.
			//no more threads for you!  it's closing time.
			newSession = null;
			sock.close();
		 }
	  }
	  catch (SocketException se)
	  {
/*         com.dalsemi.system.Debug.debugDump("FTPServer Panic: "
											+ se.toString());
*/
		 try
		 {
			if (!shutdown)
			   shutDown();
		 }
		 catch (Throwable t)
		 {
//            com.dalsemi.system.Debug.debugDump(t.toString());   //DEBUG

			shutdown = true;
		 }
	  }
	  catch (InterruptedIOException iioe)
	  {

		 // timeout
	  }
	  catch (IOException ioe)
	  {
/*         com.dalsemi.system.Debug.debugDump("FTPServer Panic: "
											+ ioe.toString());
*/
		 try
		 {
			if (!shutdown)
			   shutDown();
		 }
		 catch (Throwable t)
		 {
//            com.dalsemi.system.Debug.debugDump(t.toString());   //DEBUG

			shutdown = true;
		 }
	  }

	  return newSession;
   }

   /**
	* Closes the ServerSocket used to listen for connections.
	*/
   protected void closeAllPorts ()
	  throws IOException
   {
	  socket.close();
   }

   /**
	* This method does not apply to an FTPServer, so it does nothing.
	*/
   public void broadcast (String sendThis)
   {
	  //Do nothing.
   }
}


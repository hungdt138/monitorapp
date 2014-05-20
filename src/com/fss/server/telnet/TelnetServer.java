
/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000 Dallas Semiconductor Corporation, All Rights Reserved.
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

package com.fss.server.telnet;



import com.fss.server.*;
import java.io.*;
import java.net.*;


/**
 * A simple server that uses the Telnet protocol as described in RFC 854.  This server uses a ServerSocket to listen
 * on the specified port (defaults to port 23) for Telnet connection requests.  For each connection made,
 * a Telnet session is created.  All command processing is handled by the Telnet session, not this server.
 */
public class TelnetServer extends Server
{
   private static final int DEFAULT_TIMEOUT  = 600000;   //10 minutes in milliseconds
   static final int       PORT = 23;
   private int            timeout;
   private ServerSocket   socket;

   /**
	* Prepares the Telnet server to listen on the well known Telnet port (23).  The server will not
	* be started and no connections will be accepted until its <code>run()</code> method is executed.
	* @throws IOException
	*/
   public TelnetServer () throws IOException
   {
	  this(PORT);
   }

   /**
	* Prepares the Telnet server to listen on an arbitrary port.  The server will not
	* be started and no connections will be accepted until its <code>run()</code> method is executed.
	* @param port int
	* @throws IOException
	*/
   public TelnetServer (int port) throws IOException
   {
	  socket = new ServerSocket(port);
	  welcomeMessage = "";
	  rootLoginAllowed = true;
	  timeout = DEFAULT_TIMEOUT;
   }

   /**
	* Listens on the connection port for connection requests.  Once a
	* request is made, it creates, initializes, and returns a new
	* TelnetSession to handle that request.  This method will block until
	* a connection is made.
	* @return  a new <code>TelnetSession</code>
	*/
   private int last_wait = 100;  //wait 100 ms first time we try to wait
   private static final int MAXIMUM_WAIT = 1000 * 60 * 5;  //wait a max of 5 minutes

   protected Session acceptNewSession ()
   {
	  TelnetSession newSession = null;

	  try
	  {
		 Socket sock = null;
		 try
		 {
			sock = socket.accept();
			//if we are succesful, reset the back-off
			last_wait = 100;
			sock.setSoTimeout(timeout);
		 }
		 catch(BindException be)
		 {
			//rethrow it...why would we get this?
			throw be;
		 }
		 catch(IOException ioe)
		 {
			//this means we've reached max number of sockets or the wait queue is full
			//do our exponential backoff
			try
			{
				Thread.sleep(last_wait);
			}
			catch(InterruptedException ie)
			{
			}

			//there won't be multiple threads running around in here, so we don't need to synch
			last_wait = last_wait << 1;
			if (last_wait > MAXIMUM_WAIT)
			{
				last_wait = MAXIMUM_WAIT;
			}

			//and just bail out
			return null;
		 }
		 catch(OutOfMemoryError oome)
		 {
			System.gc();
			//give the system some time to clear out any old threads or connections or whatever
			try
			{
				Thread.sleep(10000);
			}
			catch(InterruptedException ioe)
			{
			}
			return null;
		 }

		 //If the socket was closed (i.e. the server is being shutdown, accept
		 //will return.  So, check for shutdown before we assume a new session
		 //has been requested.
		 if (shutdown)
			return null;

		 SystemPrintStream sout =
			new SystemPrintStream(sock.getOutputStream());
		 TelnetInputStream sin  = new TelnetInputStream(sock.getInputStream(), sout);

		 newSession = new TelnetSession(sin, sout, sout, sock, this);
		 sin.setSession(newSession);
		 //maybe we should just wrap the above in a try catch and return null?
		 //we don't really want to close down the server on an error here!

		 try
		 {
			newSession.start();
		 }
		 catch(Throwable t)
		 {
			//aah! must be over the limit on Threads.
			//no more threads for you!  it's closing time.
			sout.print("Thread limit reached.  Connection Terminated.\r\n");
			sock.close();
			newSession = null;
		 }
	  }
	  catch (IOException ioe)
	  {
		 try
		 {
			if(!shutdown)
			   shutDown();
		 }
		 catch (Throwable t)
		 {
			shutdown = true;
		 }
	  }
	  return newSession;
   }

   /**
	* Closes the ServerSocket used to listen for connections.
	* @throws IOException
	*/
   protected synchronized void closeAllPorts () throws IOException
   {
	  socket.close();
   }
}

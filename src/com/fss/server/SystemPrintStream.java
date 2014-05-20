
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

package com.fss.server;



import java.io.*;


/**
 * A basic implementation of a PrintStream for use as <code>System.out</code>
 * and <code>System.err</code>.  This class allows its root stream to be set,
 * and overrides <i>all</i> of its parent's (PrintStream) methods.
 * This allows special cases to be handled at the expense of some code space.
 * For instance, a <code>SystemPrintStream</code> can be set to protect itself
 * if an <code>IOException</code> occurs.  This is useful, for example,
 * from a telnet session if you run a Java program in the background.
 * The program and the session are both using a socket for output to the user.
 * If the user exits the telnet session, that socket goes away, and the
 * process would receive a <code>SocketException</code> on
 * every write, probably killing the process.  If it is set to protect itself,
 * the <code>SystemPrintStream</code> will set its internal root stream to
 * a <code>NullOutputStream</code> when it detects an <code>IOException</code>.
 * Thus, the process can continue to run.
 *
 * @author Stephen Hess, Lorne Smith
 * @version 1.0
 *
 * @see com.fss.server.SystemInputStream
 * @see com.dalsemi.comm.NullOutputStream
 * @see com.fss.server.Session
 */
public class SystemPrintStream
   extends PrintStream
{
   /**
	* The user session that owns this <code>SystemPrintStream</code>.
	* This could be a session on the serial port, through a telnet or
	* FTP connection, or through any other user created <code>Session</code>.
	*/
   protected Session session;

   /**
	* The name of the file this <code>SystemPrintStream</code> is
	* outputting to, or null if this stream is not redirecting to a file.
	*/
   public String fileOutName = null;

   /**
	* If this <code>SystemPrintStream</code> outputs to a file,
	* this variable determines if the output will be appended
	* to the file or if it will over-write the file.
	*/
   public boolean append = false;

   /**
	* Allows the <code>SystemPrintStream</code> to protect itself.  When a process
	* is run in the background and not redirected to a file, then its <code>OutputStream</code>
	* is in danger of causing problems if the underlying <code>Session</code> is
	* terminated.  If the <code>Session</code> ends and the <code>shieldsUp</code>
	* variable is true, this <code>SystemPrintStream</code>'s internal root
	* stream must be reassigned to a <code>NullOutputStream</code>.
	*/
   public boolean              shieldsUp = false;

   /**
	* Creates a new <code>SystemPrintStream</code> with the specified
	* underlying root <code>OutputStream</code>.
	*
	* @param root the internal root <code>OutputStream</code>
	* for this <code>SystemPrintStream</code>
	*
	*/
   public SystemPrintStream (OutputStream root)
   {
	  super(root);
   }

   /**
	* Creates a new <code>SystemPrintStream</code> with the specified
	* underlying root <code>OutputStream</code>.
	*
	* @param out the internal root <code>OutputStream</code>
	* for this <code>SystemPrintStream</code>
	* @param autoFlush set to true if the <code>SystemPrintStream</code>
	* should flush the internal root stream on every write call
	*
	*/
   public SystemPrintStream (OutputStream out, boolean autoFlush)
   {
	  super(out,autoFlush);
   }

   /**
	* Creates a new <code>SystemPrintStream</code> with the specified
	* underlying root <code>OutputStream</code>.  In this case, the
	* underlying root stream should be for a file.
	*
	* @param root the internal root <code>OutputStream</code>
	* for this <code>SystemPrintStream</code>
	* @param fileOutName name of the file this <code>SystemPrintStream</code>
	* is directing its output towards
	* @param append true if this <code>SystemPrintStream</code> should
	* append to the file, false if it should over-write the file
	*
	*/
   public SystemPrintStream (OutputStream root, String fileOutName,
							 boolean append)
   {
	  super(root);

	  this.fileOutName = fileOutName;
	  this.append      = append;
   }

   /**
	* Sets the underlying root output stream of this stream.
	*
	* @param root the new underlying stream to use
	*
	* @see #getRootOutputStream()
	*/
   public void setRootStream (OutputStream root)
   {
	  out = root;
   }

   /**
	* Returns the underlying root <code>OutputStream</code> of this stream.
	*
	* @return the underlying root stream
	*
	* @see #setRootStream(java.io.OutputStream)
	*/
   public OutputStream getRootOutputStream ()
   {
	  return out;
   }

   /**
	* Informs this stream of its owning session.  This allows this stream
	* to call into the session when needed.
	*
	* @param s the owning session
	*/
   public void setSession (Session s)
   {
	  session = s;
   }

   //this is where we handle our shields if we've got a problem
   // What the heck is Stephen talking about?
   // "Captain, shields failing, She canno' take no more" - Scotty
   private void handleError ()
   {
	  if (shieldsUp)
	  {
/*         if (session != null)
		 {
			session.
		 }*/

		 //redirect output to the big bin-bucket in the sky
		 out = new NullOutputStream();
	  }
	  else
	  {
//         if (session != null)
//            session.forceEndSession();

//         error = true;
	  }

	  /* no matter what we gotta end the session, we don't want headless sessions running around,
		 and make sure we do it AFTER we've redirected the output stream in the shieldsUp
		 case, because otherwise we could have a bad race condition where we get thread-swapped
		 and the socket is closed, and our output stream is no good.
	   */
	  if (session != null)
		  session.forceEndSession();
   }

   /**
	* Writes the byte to the underlying <code>OutputStream</code>.
	*
	* @param b value to be written
	*
	*/
   public void write (int b)
   {
	  try
	  {
		 super.write(b);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Writes a portion of a <code>byte</code> array to the underlying
	* <code>OutputStream</code>.
	*
	* @param buf a <code>byte</code> array
	* @param off offset in the <code>byte</code> array to begin writing bytes from
	* @param len number of bytes to write
	*
	*/
   public void write (byte buf [], int off, int len)
   {
	  try
	  {
		 super.write(buf, off, len);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the value of the <code>boolean</code> argument.  If the argument
	* is <code>true</code>, the <code>String</code> "true" is printed.  If the
	* argument is <code>false</code>, the <code>String</code> "false" is printed.
	*
	* @param b the <code>boolean</code> value to print
	*
	* @see #println(boolean)
	*/
   public void print (boolean b)
   {
	  try
	  {
		 super.print(b);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the value of the <code>char</code> argument according to the
	* default encoding scheme.
	*
	* @param c the <code>char</code> to be printed
	*
	* @see #println(char)
	*/
   public void print (char c)
   {
	  try
	  {
		 super.print(c);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the <code>int</code> argument by calling the
	* <code>Integer.toString(int)</code> method.
	*
	* @param i the <code>int</code> to be printed
	*
	* @see #println(int)
	*/
   public void print (int i)
   {
	  try
	  {
		 super.print(i);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the <code>long</code> argument by calling the
	* <code>Long.toString(long)</code> method.
	*
	* @param l the <code>long</code> to be printed
	*
	* @see #println(long)
	*/
   public void print (long l)
   {
	  try
	  {
		 super.print(l);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the floating point argument by calling the
	* <code>Float.toString(float)</code> method.
	*
	* @param f the <code>float</code> to be printed
	*
	* @see #println(float)
	*/
   public void print (float f)
   {
	  try
	  {
		 super.print(f);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the double precision floating point argument by calling the
	* <code>Double.toString(double)</code> method.
	*
	* @param d the <code>double</code> to be printed
	*
	* @see #println(double)
	*/
   public void print (double d)
   {
	  try
	  {
		 super.print(d);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the <code>char</code> array according to the
	* default encoding scheme.
	*
	* @param s the <code>char</code> array to be printed
	*
	* @see #println(char[])
	*/
   public void print (char[] s)
   {
	  try
	  {
		 super.print(s);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the <code>String</code> argument to the
	* underlying root stream, or the <code>String</code>
	* "null" if the argument is <code>null</code>.
	*
	* @param s the <code>String</code> to print
	*
	* @see #println(java.lang.String)
	*/
   public void print (String s)
   {
	  try
	  {
		 super.print(s);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints a <code>String</code> representation of
	* the argument <code>Object</code> by invoking its
	* <code>toString()</code> method, or prints the
	* <code>String</code> "null" if the argument is
	* <code>null</code>.
	*
	* @param obj the <code>Object</code> to print
	*
	* @see #println(java.lang.Object)
	*/
   public void print (Object obj)
   {
	  try
	  {
		 super.print(obj);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Writes the end of line sequence CRLF to
	* the underlying <code>OutputStream</code>.
	*
	*/
   public void println ()
   {
	  try
	  {
		 super.println();
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the value of the <code>boolean</code> argument, followed
	* by the end of line sequence.  If the argument
	* is <code>true</code>, the <code>String</code> "true" is printed.  If the
	* argument is <code>false</code>, the <code>String</code> "false" is printed.
	*
	* @param x the <code>boolean</code> value to print
	*
	* @see #print(boolean)
	*/
   public void println (boolean x)
   {
	  try
	  {
		 super.println(x);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the value of the <code>char</code> argument according to the
	* default encoding scheme, followed by the end of line sequence.
	*
	* @param x the <code>char</code> to be printed
	*
	* @see #print(char)
	*/
   public void println (char x)
   {
	  try
	  {
		 super.println(x);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the <code>int</code> argument by calling the
	* <code>Integer.toString(int)</code> method,
	* followed by the end of line sequence.
	*
	* @param x the <code>int</code> to be printed
	*
	* @see #print(int)
	*/
   public void println (int x)
   {
	  try
	  {
		 super.println(x);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the <code>long</code> argument by calling the
	* <code>Long.toString(long)</code> method,
	* followed by the end of line sequence.
	*
	* @param x the <code>long</code> to be printed
	*
	* @see #print(long)
	*/
   public void println (long x)
   {
	  try
	  {
		 super.println(x);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the floating point argument by calling the
	* <code>Float.toString(float)</code> method, followed
	* by the end of line sequence.
	*
	* @param x the <code>float</code> to be printed
	*
	* @see #print(float)
	*/
   public void println (float x)
   {
	  try
	  {
		 super.println(x);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the double precision floating point argument by calling the
	* <code>Double.toString(double)</code> method, followed by
	* the end of line sequence.
	*
	* @param x the <code>double</code> to be printed
	*
	* @see #print(double)
	*/
   public void println (double x)
   {
	  try
	  {
		 super.println(x);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the character array according to the
	* default encoding scheme, followed by the end
	* of line sequence.
	*
	* @param x the <code>char</code> array to be printed
	*
	* @see #print(char[])
	*/
   public void println (char x [])
   {
	  try
	  {
		 super.println(x);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints the <code>String</code> argument to the
	* underlying root stream, or the <code>String</code>
	* "null" if the argument is <code>null</code>,
	* followed by the end of line sequence.
	*
	* @param x the <code>String</code> to print
	*
	* @see #print(java.lang.String)
	*/
   public void println (String x)
   {
	  try
	  {
		 super.println(x);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }

   /**
	* Prints a <code>String</code> representation of
	* the argument <code>Object</code> by invoking its
	* <code>toString()</code> method, or prints the
	* <code>String</code> "null" if the argument is
	* <code>null</code>, followed by the end of
	* line sequence.
	*
	* @param x the <code>Object</code> to print
	*
	* @see #print(java.lang.Object)
	*/
   public void println (Object x)
   {
	  try
	  {
		 super.println(x);
		 if (super.checkError())
			handleError();
	  }
	  catch (NullPointerException npe)
	  {
		 handleError();
	  }
   }
}

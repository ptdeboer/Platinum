/*
 * Copyright 2012-2014 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at the following location:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * For the full license, see: LICENSE.txt (located in the root folder of this distribution).
 * ---
 */
// source:

package nl.esciencecenter.ptk.util.vterm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

/* 
 * XSocket forwarder. 
 * redirects IP Socket to X (unix) socket: 
 * 
 */
public class XSocket
{
	private ServerSocket sock;

	public XSocket(int port)
	{
		try
		{
			sock=new java.net.ServerSocket(port);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean terminate=false; 
	
	/** Start accepting connections and redirect each incoming connection */ 
	public void listen(boolean background)
	{
		
		// listen thread:
		
		while (terminate==false)
		{
			Socket clientsock;
			
			try
			{
				clientsock = this.sock.accept();
				redirect(clientsock); 
			}
			catch (IOException e)
			{
				Error("Error in accept:"+e); 
				e.printStackTrace();
			}
			
		}
	}

	private void redirect(final Socket clientsock) throws IOException
	{
		Debug(1,"redirect:"+clientsock);
		
		//final SocketChannel channel = clientsock.getChannel();
		final InputStream inps=clientsock.getInputStream(); 
		File file=new File("/tmp/.X11-unix/X0");
		// fails:
		final RandomAccessFile rafile=new RandomAccessFile(file,"rw"); 

		if (inps==null)
		{
			Error("NULL Channel after getChannel:"+clientsock);
			return; 
		}
		
		Runnable reader=new Runnable()
		{
			public void run()
			{
				Debug(1,"Started reader for:"+clientsock); 
				
				byte buffer[]=new byte[1024]; 
				
				while (terminate==false)
				{
					try
					{
						int val=inps.read(buffer); 
						
						Debug(1,"Reader received val="+val); 
						rafile.write(buffer,0,val); 
					}
					catch (IOException e)
					{
						System.err.println("Exception:e"); 
						e.printStackTrace();
						terminate=true; 
					}
						
				} 
				
				Debug(1,"Stopped redirecting:"+sock); 
			}

		
		};
		
		Thread thread=new Thread(reader,"XSocket reader"); 
		thread.start(); 
		
	}

	private void Debug(int level, String msg)
	{
//		if (level<=1) 
//			Global.infoPrintln(this,msg); 
//		else
//			Global.debugPrintln(this,msg); 
	}
	
	private void Error(String msg)
	{
//		Global.debugPrintln(this+"Error:",msg);
//		Global.errorPrintln(this+"Error:",msg); 
	}

	public static void main(String args[])
	{
		XSocket sock=new XSocket(6001); 
		sock.listen(true); 
		
	}
}

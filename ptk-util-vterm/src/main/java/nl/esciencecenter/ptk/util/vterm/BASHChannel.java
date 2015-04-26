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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import nl.esciencecenter.ptk.GlobalProperties;
import nl.esciencecenter.ptk.exec.ChannelOptions;
import nl.esciencecenter.ptk.exec.ShellChannel;
import nl.esciencecenter.ptk.util.logging.ClassLogger;

/**
 * 
 * Open BASH Shell Channel to local filesystem 
 */
public class BASHChannel implements ShellChannel
{
    // ========================================================================
    
    // ========================================================================
    
    private static ClassLogger logger;
    
    static
    {
        logger=ClassLogger.getLogger(BASHChannel.class);
        //logger.setLevelToDebug(); 
    }
        
    // ========================================================================
    
    // ========================================================================
    private Process shellProcess=null;
    private InputStream inps=null;
    private OutputStream outps=null;
    private InputStream errs=null;
    private int exitValue;

    public BASHChannel(URI uri, ChannelOptions options)
    {
    }

    public BASHChannel()
    {
    }
    

    @Override
    public OutputStream getStdin()
    {
        return outps;
    }

    @Override
    public InputStream getStdout()
    {
        return inps;
    }

    @Override
    public InputStream getStderr()
    {
        return errs;
    }

    @Override
    public void connect() throws IOException
    {
     
        this.shellProcess = null;

        try
        {
            boolean plainbash = false;
            String cmds[] = null;

            // pseudo tty which invokes bash.

            if (GlobalProperties.isLinux())
            {
                cmds = new String[1];
                // linux executable .lxe :-)
                cmds[0] = getExePath("ptty.lxe"); 
            }
            else if (GlobalProperties.isWindows())
            {
                cmds = new String[1];
                cmds[0] = getExePath("ptty.exe");
            }
            else
            {
                //Global.errorPrintf(this,"exec bash: Can't determine OS:%s\n",Global.getOsName());
                return; 
            }

            shellProcess = Runtime.getRuntime().exec(cmds);
            inps = shellProcess.getInputStream();
            outps = shellProcess.getOutputStream();
            errs = shellProcess.getErrorStream();

            // final PseudoTtty ptty;

            if (plainbash)
            {
                errs = shellProcess.getErrorStream();
                // ptty=new PseudoTtty(inps,outps,errs);
                // inps=ptty.getInputStream();
                // outps=ptty.getOutputStream();
            }
            else
            {
                // ptty=null;
            }
        }
        catch (Exception e)
        {
            logger.logException(ClassLogger.ERROR,e,"Couldn't initialize bash session:%s\n",e);
            throw new IOException("Failed to start bash session.\n"+e.getMessage(),e);
        }
    }

    private String getExePath(String file)
    {
        java.net.URL url=Thread.currentThread().getContextClassLoader().getResource(file); 
        
        if (url!=null)
        {
            logger.infoPrintf("Using executable:"+url); 
            return url.getPath(); 
        }
        
        return "bin/"+file;
    }

    @Override
    public void disconnect(boolean wait) 
    {
        if (this.shellProcess!=null)
        {
            this.shellProcess.destroy();
            
            if (wait)
            {
                try
                {
                    shellProcess.waitFor();
                }
                catch (InterruptedException e)
                {
                    logger.logException(ClassLogger.ERROR,e,"Interuppted during waitFor\n");
                } 
                exitValue=shellProcess.exitValue(); 
                this.shellProcess=null; 
            }
        }
        
    }

    @Override
    public String getTermType()
    {
        return null;
    }

    @Override
    public boolean setTermType(String type)
    {
        logger.warnPrintf("Can't set TERM type to:%s\n",type);
        return false;
    }

    @Override
    public boolean setTermSize(int col, int row, int wp, int hp)
    {
        logger.warnPrintf("Can't set TERM type to:%dx%dx%dx%d\n",col,row,wp,hp); 
        return false;
    }

    @Override
    public int[] getTermSize()
    {
        return null;
    }

    public void waitFor() throws InterruptedException
    {
        this.shellProcess.waitFor(); 
    }

    public int exitValue()
    {
        if (shellProcess!=null)
        {
            exitValue=shellProcess.exitValue();
        }
        return exitValue; 
    }

}


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

package nl.esciencecenter.ptk.vbrowser.ui.proxy;

import java.util.Vector;

import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.tasks.UITask;

public class ProxyNodeEventNotifier
{
    // ========================================================================
    // 
    // ========================================================================

    private static ProxyNodeEventNotifier instance;
    
    private static ClassLogger logger; 
    
    static
    {
        logger=ClassLogger.getLogger(ProxyNodeEventNotifier.class); 
        instance=new ProxyNodeEventNotifier(); 
    }
    
    /** Single instance for all ProxyModels! */ 
    public static ProxyNodeEventNotifier getInstance()
    {
        return instance; 
    }

    // ========================================================================
    // 
    // ========================================================================
    
    private Vector<ProxyNodeEventListener> listeners=new Vector<ProxyNodeEventListener>(); 
    
    private Vector<ProxyNodeEvent> events=new Vector<ProxyNodeEvent>();
    
    private UITask notifierTask; 
    
    private volatile boolean doNotify=true; 
    
    protected ProxyNodeEventNotifier()
    {
        startNotifier();
    }
    
    protected void startNotifier()
    {
        this.notifierTask=new UITask(null,"ProxyViewNodeEventNotifier task")
            {
                @Override
                protected void doTask() throws Exception
                {
                    try
                    {
                        doNotifyLoop();
                    }
                    catch (Throwable t)
                    {
                        logger.errorPrintf("Notifyer event thread exception=%s\n",t);
                        t.printStackTrace();
                    }
                }
    

                @Override
                protected void stopTask() throws Exception
                {
                    stopNotifier();  
                }
            };
        
        this.notifierTask.startTask(); 
    }
    
    public void stopNotifier()
    {
        this.doNotify=false;
    }
    
    protected void doNotifyLoop()
    {
        logger.infoPrintf("Starting notifyerloop"); 
        
        while(doNotify)
        {
            ProxyNodeEvent event=getNextEvent(); 
            if (event!=null)
            {
                notifyEvent(event);
            }
            else
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        logger.infoPrintf("Notifyerloop has stopped.");
    }

    private void notifyEvent(ProxyNodeEvent event)
    {
        for(ProxyNodeEventListener listener:getListeners())
        {
            try
            {
                listener.notifyDataSourceEvent(event);
            }
            catch (Throwable t)
            {
                logger.errorPrintf("***Exception during event notifiation:%s\n",t); 
                t.printStackTrace(); 
            }
        }
    }

    private ProxyNodeEventListener[] getListeners()
    {
        // create private copy 
        synchronized(this.listeners)
        {
            ProxyNodeEventListener _arr[]=new ProxyNodeEventListener[this.listeners.size()];
            _arr=this.listeners.toArray(_arr);
            return _arr; 
        }
    }

    private ProxyNodeEvent getNextEvent()
    {
        synchronized(this.events) 
        {
            if (this.events.size()<=0)
                return null; 
            
            ProxyNodeEvent event = this.events.get(0);
            this.events.remove(0);
            return event; 
        }
    }
    
    public void scheduleEvent(ProxyNodeEvent event)
    {
        synchronized(this.events)
        {
            this.events.add(event);
        }
    }
    

    public void addListener(ProxyNodeEventListener listener)
    {
        synchronized(this.listeners)
        {
            this.listeners.add(listener); 
        }
    }
    
    public void removeListener(ProxyNodeEventListener listener)
    {
        synchronized(this.listeners)
        {
            this.listeners.remove(listener); 
        }
    }

}

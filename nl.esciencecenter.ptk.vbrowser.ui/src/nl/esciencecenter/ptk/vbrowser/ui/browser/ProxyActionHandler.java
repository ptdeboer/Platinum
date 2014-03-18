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

package nl.esciencecenter.ptk.vbrowser.ui.browser;

import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.ui.panels.monitoring.TaskMonitorDialog;
import nl.esciencecenter.ptk.ui.panels.monitoring.TransferMonitorDialog;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.Action;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeEvent;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNodeEventNotifier;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Delegated Action Handler class for the Proxy Browser. 
 * Encapsulates Copy, Paste, Create, Delete, Rename, Link and  Drag & Drop. 
 */
public class ProxyActionHandler
{
    final private static ClassLogger logger=ClassLogger.getLogger(ProxyActionHandler.class);
    
    private ProxyBrowserController proxyBrowser;

    public ProxyActionHandler(ProxyBrowserController proxyBrowser)
    {
        this.proxyBrowser=proxyBrowser; 
        logger.setLevelToDebug(); 
    }

    public void handlePaste(Action action,ViewNode node)
    {
        logger.debugPrintf("*** Paste On:%s\n",node); 
    }

    public void handleCopy(Action action,ViewNode node)
    {
        logger.debugPrintf("*** Copy On:%s\n",node);
    }

    public void handleCopySelection(Action action,ViewNode node)
    {
        logger.debugPrintf("*** Copy Selection:%s\n",node);
    }

    public void handleDeleteSelection(Action action,ViewNode node)
    {
        logger.debugPrintf("*** Delete Selection: %s\n",node);
    }

    public void handleCreate(Action action, final ViewNode node, final String type, final String options)
    {
        final String name=proxyBrowser.getUI().askInput("New name for:"+type, "Give new name for "+type); 
        
        if (name==null)
        {
            logger.debugPrintf("Create action cancelled\n"); 
            return;
        }
        
        final VRL locator=node.getVRL(); 
        
        ProxyBrowserTask task = new ProxyBrowserTask(proxyBrowser, "create new:"+type+" at " + locator)
        {
            @Override
            protected void doTask()
            {
                 
                try
                {
                    doCreateNewNode(node.getVRL(),type,name,this.getTaskMonitor());
                }
                catch (Throwable e)
                {
                    proxyBrowser.handleException("Couldn't open location:" + locator, e);
                }
            }
        };

        task.startTask();
        TaskMonitorDialog.showTaskMonitorDialog(null, task, 0); 
        
    }

    public void handleDelete(Action action, ViewNode node)
    {
        boolean result=proxyBrowser.getUI().askOkCancel("Delete resource"+node+"?", "Do you want to delete:"+node.getVRL(), false); 
         
        if (result==false)
            return; 
        
        final VRL locator=node.getVRL(); 
        
        ProxyBrowserTask task = new ProxyBrowserTask(proxyBrowser, "Delete resource:"+locator)
        {
            @Override
            protected void doTask()
            {
                try
                {
                    doDeleteNode(locator,this.getTaskMonitor());
                }
                catch (Throwable e)
                {
                    proxyBrowser.handleException("Couldn't delete:" + locator, e);
                }
            }

        };

        task.startTask();
        TaskMonitorDialog.showTaskMonitorDialog(null, task, 0); 
    }
    
    protected void doCreateNewNode(VRL parentLocation, String type, String name, ITaskMonitor taskMonitor)
    {
        logger.debugPrintf("*** doCreate:<%s>:%s\n",type,name); 

        try
        {
            taskMonitor.startSubTask("Creating new node:"+type,1); 
            ProxyNode parentNode=proxyBrowser.openProxyNode(parentLocation); 
            ProxyNode newNode=parentNode.createNew(type,name);
            fireNewNodeEvent(parentNode,newNode); 
            taskMonitor.endSubTask("Creating new node:"+type);  
        }
        catch (Throwable ex) 
        {
            this.proxyBrowser.handleException("Failed to create new Resource:"+type+":"+name, ex); 
        }
    }

    protected void doDeleteNode(VRL locator, ITaskMonitor taskMonitor)
    {
        
        try
        {
            String taskStr="Deleting node:"+locator; 
            taskMonitor.startSubTask(taskStr,1);
            ProxyNode delNode=proxyBrowser.openProxyNode(locator);
            ProxyNode parentNode=delNode.getParent();
            delNode.delete(false);  
            // must notify parent as well ! 
            fireDeletedNodeEvent(parentNode,delNode);  
            taskMonitor.endSubTask(taskStr);   
        }
        catch (Throwable ex) 
        {
            this.proxyBrowser.handleException("Failed to delete resource:"+locator,ex); 
        }
    }
    
    public void fireNewNodeEvent(ProxyNode parent, ProxyNode childNode)
    {
        ProxyNodeEventNotifier.getInstance().scheduleEvent(
                ProxyNodeEvent.createChildAddedEvent(
                        parent.getVRL(),
                        childNode.getVRL()));
    }
    
    public void fireDeletedNodeEvent(ProxyNode parent, ProxyNode actualNode)
    {
        ProxyNodeEventNotifier.getInstance().scheduleEvent(
                ProxyNodeEvent.createChildDeletedEvent(
                        (parent!=null)?parent.getVRL():null,
                            actualNode.getVRL()));
    }
}

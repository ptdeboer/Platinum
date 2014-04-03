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

import java.awt.Component;
import java.awt.Point;
import java.util.List;

import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.ui.panels.monitoring.TaskMonitorDialog;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.Action;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler.DropAction;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.vbrowser.vrs.event.VRSEvent;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Delegated Action Handler class for the Proxy Browser.<br>
 * Encapsulates Copy, Paste, Create, Delete, Rename, Link and Drag  & Drop actions. 
 */
public class ProxyActionHandler
{
    final private static ClassLogger logger = ClassLogger.getLogger(ProxyActionHandler.class);

    private ProxyBrowserController proxyBrowser;

    public ProxyActionHandler(ProxyBrowserController proxyBrowser)
    {
        this.proxyBrowser = proxyBrowser;
        logger.setLevelToDebug();
    }

    public void handlePaste(Action action, ViewNode node)
    {
        logger.debugPrintf("*** Paste On:%s\n", node);
    }

    public void handleCopy(Action action, ViewNode node)
    {
        logger.debugPrintf("*** Copy On:%s\n", node);
    }

    public void handleCopySelection(Action action, ViewNode node)
    {
        logger.debugPrintf("*** Copy Selection:%s\n", node);
    }

    public void handleDeleteSelection(ViewNodeComponent viewComp, Action action, ViewNode node)
    {
        logger.debugPrintf("Delete Selection: %s\n", node);
        ViewNode selections[] = ((ViewNodeContainer) viewComp).getNodeSelection();

        final VRL vrls[] = ViewNode.toVRLs(selections);

        BrowserTask task = new BrowserTask(proxyBrowser, "Deleting resources")
        {
            @Override
            protected void doTask()
            {
                try
                {
                    doDeleteNodes(vrls, this.getTaskMonitor());
                }
                catch (Throwable e)
                {
                    proxyBrowser.handleException("Couldn't delete resources.", e);
                }
            }

        };

        task.startTask();
        TaskMonitorDialog.showTaskMonitorDialog(null, task, 0);
    }

    public void handleCreate(Action action, final ViewNode node, final String type, final String options)
    {
        final String name = proxyBrowser.getUI().askInput("New name for:" + type, "Give new name for " + type, "New " + type);

        if (name == null)
        {
            logger.debugPrintf("Create action cancelled\n");
            return;
        }

        final VRL locator = node.getVRL();

        BrowserTask task = new BrowserTask(proxyBrowser, "Creating new resource:" + type + ":'" + name + "' at:" + locator)
        {
            @Override
            protected void doTask()
            {

                try
                {
                    doCreateNewNode(node.getVRL(), type, name, this.getTaskMonitor());
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
        boolean result = proxyBrowser.getUI().askOkCancel("Delete resource" + node + "?",
                "Do you want to delete:'" + node.getName() + "'(" + node.getVRL() + "')", false);

        if (result == false)
        {
            return;
        }
        final VRL locator = node.getVRL();

        BrowserTask task = new BrowserTask(proxyBrowser, "Deleting resource:" + locator)
        {
            @Override
            protected void doTask()
            {
                try
                {
                    doDeleteNodes(new VRL[]
                    { locator }, this.getTaskMonitor());
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

    public void handleRename(Action action, ViewNode node)
    {
        String oldName = node.getName();

        final String name = proxyBrowser.getUI().askInput("Enter new Name", "Enter new name for:" + oldName, oldName);

        if (StringUtil.isEmpty(name))
            return;

        final VRL locator = node.getVRL();

        BrowserTask task = new BrowserTask(proxyBrowser, "Renaming '" + oldName + "' to:'" + name + "' for resource:" + locator)
        {
            @Override
            protected void doTask()
            {
                try
                {
                    doRenameNode(locator, name, this.getTaskMonitor());
                }
                catch (Throwable e)
                {
                    proxyBrowser.handleException("Couldn't rename:" + locator, e);
                }
            }

        };

        task.startTask();
        TaskMonitorDialog.showTaskMonitorDialog(null, task, 0);
    }

    public boolean handleDrop(Component uiComponent, Point optPoint, final ViewNode viewNode, final DropAction dropAction,
            final List<VRL> vrls)
    {
        // ===================================
        // Do interactive UI stuff here ...
        // ===================================

        // UIGlobal.assertGuiThread("Interface drop most be called during Swings Event thread!");

        BrowserTask task = new BrowserTask(proxyBrowser, "Performing drop '" + dropAction + "' on resource:" + viewNode)
        {
            @Override
            protected void doTask()
            {
                try
                {
                    doDrop(viewNode, dropAction, vrls, this.getTaskMonitor());
                }
                catch (Throwable e)
                {
                    proxyBrowser.handleException("Failed to drop  open location:" + viewNode.getVRL(), e);
                }
            }
        };

        task.startTask();
        TaskMonitorDialog.showTaskMonitorDialog(null, task, 0);
        return true;
    }

    // ====================
    // Backgrounded actions
    // ====================

    protected void doCreateNewNode(VRL parentLocation, String type, String name, ITaskMonitor taskMonitor)
    {
        logger.debugPrintf("*** doCreate:<%s>:%s\n", type, name);

        try
        {
            String taskStr = "Creating new node:" + type;
            taskMonitor.startSubTask(taskStr, 1);
            ProxyNode parentNode = proxyBrowser.openProxyNode(parentLocation);
            ProxyNode newNode = parentNode.createNew(type, name);
            fireNewNodeEvent(parentNode, newNode);
            taskMonitor.updateSubTaskDone(taskStr, 1);
            taskMonitor.endSubTask("Creating new node:" + type);
        }
        catch (Throwable ex)
        {
            this.proxyBrowser.handleException("Failed to create new Resource:" + type + ":" + name, ex);
        }
    }

    protected void doDeleteNodes(VRL locators[], ITaskMonitor taskMonitor)
    {
        for (VRL vrl : locators)
        {
            try
            {
                String taskStr = "Deleting node:" + vrl;
                taskMonitor.startSubTask(taskStr, 1);
                ProxyNode delNode = proxyBrowser.openProxyNode(vrl);
                ProxyNode parentNode = delNode.getParent();
                delNode.delete(false);
                // must notify parent as well !
                fireDeletedNodeEvent(parentNode, delNode);
                taskMonitor.updateSubTaskDone(taskStr, 1);
                taskMonitor.logPrintf(" - deleted:%s\n", vrl);
                taskMonitor.endSubTask(taskStr);
            }
            catch (Throwable ex)
            {
                this.proxyBrowser.handleException("Failed to delete resource:" + vrl, ex);
                break;
            }
        }
    }

    protected void doRenameNode(VRL locator, String newName, ITaskMonitor taskMonitor)
    {

        try
        {
            String taskStr = "Renaming node:" + locator;
            taskMonitor.startSubTask(taskStr, 1);
            ProxyNode oldNode = proxyBrowser.openProxyNode(locator);
            // will invalidate old ProxyNode and create (duplicate) new Node !
            ProxyNode newNode = oldNode.renameTo(newName);
            // must notify parent as well !
            fireNodeRenamedEvent(oldNode.getParent(), oldNode, newNode);
            taskMonitor.updateSubTaskDone(taskStr, 1);
            taskMonitor.endSubTask(taskStr);
        }
        catch (Throwable ex)
        {
            this.proxyBrowser.handleException("Failed to delete resource:" + locator, ex);
        }
    }

    protected void doDrop(ViewNode viewNode, DropAction dropAction, List<VRL> vrls, ITaskMonitor taskMonitor)
    {
        logger.debugPrintf("*** doDrop %s on:%s\n", viewNode.getVRL(), dropAction);

        try
        {
            ProxyFactory factory = this.proxyBrowser.getProxyFactoryFor(viewNode.getVRL());
            ProxyNodeDnDHandler dndHandler = factory.getProxyDnDHandler(viewNode);
            dndHandler.doDrop(viewNode, dropAction, vrls, taskMonitor);
        }
        catch (Throwable ex)
        {
            this.proxyBrowser.handleException("Failed doDrop() type '" + dropAction + " on :" + viewNode.getVRL(), ex);
        }
    }

    // ====================
    // Asynchronous updates
    // ====================

    public void fireNewNodeEvent(ProxyNode parent, ProxyNode childNode)
    {
        parent.getProxyNodeEventNotifier().scheduleEvent(
                VRSEvent.createChildAddedEvent(
                        parent.getVRL(),
                        childNode.getVRL()));
    }

    public void fireNewNodesEvent(ProxyNode parent, List<ProxyNode> childNodes)
    {
        VRL vrls[] = ProxyNode.toVRLArray(childNodes);

        parent.getProxyNodeEventNotifier().scheduleEvent(
                VRSEvent.createChildsAddedEvent(
                        parent.getVRL(),
                        vrls));
    }

    public void fireDeletedNodeEvent(ProxyNode parent, ProxyNode actualNode)
    {
        parent.getProxyNodeEventNotifier().scheduleEvent(
                VRSEvent.createChildDeletedEvent(
                        (parent != null) ? parent.getVRL() : null,
                        actualNode.getVRL()));
    }

    public void fireNodeRenamedEvent(ProxyNode parent, ProxyNode oldNode, ProxyNode newNode)
    {
        parent.getProxyNodeEventNotifier().scheduleEvent(
                VRSEvent.createNodeRenamedEvent(
                        (parent != null) ? parent.getVRL() : null,
                        oldNode.getVRL(),
                        newNode.getVRL()));
    }

}

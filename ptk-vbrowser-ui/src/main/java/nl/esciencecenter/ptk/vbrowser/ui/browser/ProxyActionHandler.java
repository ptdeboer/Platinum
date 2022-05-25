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

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.task.ITaskMonitor;
import nl.esciencecenter.ptk.ui.panels.monitoring.TaskMonitorDialog;
import nl.esciencecenter.ptk.util.CollectionUtil;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmd;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.CopyBuffer;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler;
import nl.esciencecenter.ptk.vbrowser.ui.model.ProxyNodeDnDHandler.DropAction;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeComponent;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNodeContainer;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyFactory;
import nl.esciencecenter.ptk.vbrowser.ui.proxy.ProxyNode;
import nl.esciencecenter.vbrowser.vrs.event.VRSEvent;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Delegated ActionCmd Handler for the Proxy Browser.<br>
 * Handles Copy, Paste, Create, Delete, Rename, Link and Drag & Drop actions.
 */
@Slf4j
final public class ProxyActionHandler {

    private final ProxyBrowserController proxyBrowser;

    public ProxyActionHandler(ProxyBrowserController proxyBrowser) {
        this.proxyBrowser = proxyBrowser;
    }

    public void handlePaste(ActionCmd action, ViewNode viewNode) {
        log.debug("Paste onto: {}", viewNode);
        if (!proxyBrowser.getCopyBuffer().hasBuffer()) {
            proxyBrowser.showMessage("Empty buffer", "Nothing in copy buffer.");
            log.warn("handlePaste(): Nothing in buffer...");
            return;
        }
        CopyBuffer.CopyBufferElement bufEl = proxyBrowser.getCopyBuffer().getFirst();
        List<VRL> vrls = bufEl.getVrls();
        boolean isCut = bufEl.isCut();

        this.handlePasteOrDrop(null, null, viewNode, DropAction.COPY_PASTE, vrls);
    }

    public void handleCopy(ActionCmd action, ViewNode node, boolean isCut) {
        handleCopySelection(action, node, CollectionUtil.singleton(node), isCut);
    }

    public void handleCopySelection(ActionCmd action, ViewNode node, List<ViewNode> selection, boolean isCut) {
        List<VRL> vrls = new ArrayList<>();
        selection.stream().forEach(nodeEl -> vrls.add(nodeEl.getVRL()));
        proxyBrowser.getCopyBuffer().store(vrls, isCut);
        log.error("Stored: {}", vrls);
    }

    public void handleDeleteSelection(ViewNodeComponent viewComp, ActionCmd action) {
        log.debug("handleDeleteSelection: {}", viewComp);
        List<ViewNode> selections = ((ViewNodeContainer) viewComp).getNodeSelection();

        final VRL[] vrls = ViewNode.toVRLs(selections);

        BrowserTask task = new BrowserTask(proxyBrowser, "Deleting resources") {
            @Override
            protected void doTask() {
                try {
                    doDeleteNodes(vrls, this.getTaskMonitor(), false);
                } catch (Throwable e) {
                    proxyBrowser.handleException("Couldn't delete resources.", e);
                }
            }

        };

        task.startTask();
        TaskMonitorDialog.showTaskMonitorDialog(null, task, 0);
    }

    public void handleCreate(ActionCmd action, final ViewNode node, final String type,
                             final String options) {

        final String name = proxyBrowser.getUI().askInput("New name for:" + type,
                "Give new name for " + type, "New " + type);

        if (name == null) {
            log.debug("Create action cancelled");
            return;
        }

        final VRL locator = node.getVRL();

        BrowserTask task = new BrowserTask(proxyBrowser, "Creating new resource:<" + type + ">:'"
                + name + "' at:" + locator) {
            @Override
            protected void doTask() {

                try {
                    doCreateNewNode(node.getVRL(), type, name, this.getTaskMonitor());
                } catch (Throwable e) {
                    proxyBrowser.handleException("Couldn't open location:" + locator, e);
                }
            }
        };

        task.startTask();
        TaskMonitorDialog.showTaskMonitorDialog(null, task, 0);
    }

    public void handleDelete(ActionCmd action, ViewNode node, boolean recursive) {
        boolean result = proxyBrowser.getUI().askOkCancel("Delete resource" + node + "?",
                "Do you want to delete:'" + node.getName() + "'(" + node.getVRL() + "')", false);

        if (result == false) {
            return;
        }
        final VRL locator = node.getVRL();

        BrowserTask task = new BrowserTask(proxyBrowser, "Deleting resource:" + locator) {
            @Override
            protected void doTask() {
                try {
                    doDeleteNodes(new VRL[]{locator}, this.getTaskMonitor(), recursive);
                } catch (Throwable e) {
                    proxyBrowser.handleException("Couldn't delete:" + locator, e);
                }
            }

        };

        task.startTask();
        TaskMonitorDialog.showTaskMonitorDialog(null, task, 0);
    }

    public void handleRename(ActionCmd action, ViewNode node) {
        String oldName = node.getName();

        final String name = proxyBrowser.getUI().askInput("Enter new Name",
                "Enter new name for:" + oldName, oldName);

        if (StringUtil.isEmpty(name))
            return;

        final VRL locator = node.getVRL();

        BrowserTask task = new BrowserTask(proxyBrowser, "Renaming '" + oldName + "' to:'" + name
                + "' for resource:" + locator) {
            @Override
            protected void doTask() {
                try {
                    doRenameNode(locator, name, this.getTaskMonitor());
                } catch (Throwable e) {
                    proxyBrowser.handleException("Couldn't rename:" + locator, e);
                }
            }

        };

        task.startTask();
        TaskMonitorDialog.showTaskMonitorDialog(null, task, 0);
    }

    public boolean handlePasteOrDrop(Component optComponent, Point optPoint, final ViewNode viewNode,
                                     final DropAction dropAction, final List<VRL> vrls) {
        // ===================================
        // Do interactive UI stuff here ...
        // ===================================

        // UIGlobal.assertGuiThread("Interface drop most be called during Swings Event thread!");

        BrowserTask task = new BrowserTask(proxyBrowser, "Performing drop '" + dropAction
                + "' on resource:" + viewNode) {
            @Override
            protected void doTask() {
                try {
                    doPasteOrDrop(viewNode, dropAction, vrls, this.getTaskMonitor());
                } catch (Throwable e) {
                    proxyBrowser.handleException(
                            "Failed to drop  open location:" + viewNode.getVRL(), e);
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

    private void doCreateNewNode(VRL parentLocation, String type, String name,
                                 ITaskMonitor taskMonitor) {
        log.debug("doCreateNewNode:<{}>:{}", type, name);

        try {
            String taskStr = "Creating new node:" + type;
            taskMonitor.startSubTask(taskStr, 1);
            ProxyNode parentNode = proxyBrowser.openProxyNode(parentLocation);
            ProxyNode newNode = parentNode.createNew(type, name);
            fireNewNodeEvent(parentNode, newNode);
            taskMonitor.updateSubTaskDone(taskStr, 1);
            taskMonitor.endSubTask("Creating new node:" + type);
        } catch (Throwable ex) {
            this.proxyBrowser.handleException("Failed to create new Resource:" + type + ":" + name,
                    ex);
        }
    }

    private void doDeleteNodes(VRL[] locators, ITaskMonitor taskMonitor, boolean recursive) {
        for (VRL vrl : locators) {
            try {
                String taskStr = "Deleting node:" + vrl;
                taskMonitor.startSubTask(taskStr, 1);
                ProxyNode delNode = proxyBrowser.openProxyNode(vrl);
                ProxyNode parentNode = delNode.getParent();
                delNode.delete(recursive, taskMonitor);
                // must notify parent as well !
                fireDeletedNodeEvent(parentNode, delNode);
                taskMonitor.updateSubTaskDone(taskStr, 1);
                taskMonitor.logPrintf("Deleted:<%s>%s\n", delNode.getResourceType(), vrl);
                taskMonitor.endSubTask(taskStr);
            } catch (Throwable ex) {
                this.proxyBrowser.handleException("Failed to delete resource:" + vrl, ex);
                break;
            }
        }
    }

    private void doRenameNode(VRL locator, String newName, ITaskMonitor taskMonitor) {

        try {
            String taskStr = "Renaming node:" + locator;
            taskMonitor.startSubTask(taskStr, 1);
            ProxyNode oldNode = proxyBrowser.openProxyNode(locator);
            // will invalidate old ProxyNode and create (duplicate) new Node !
            ProxyNode newNode = oldNode.renameTo(newName);
            // must notify parent as well !
            fireNodeRenamedEvent(oldNode.getParent(), oldNode, newNode);
            taskMonitor.updateSubTaskDone(taskStr, 1);
            taskMonitor.endSubTask(taskStr);
        } catch (Throwable ex) {
            this.proxyBrowser.handleException("Failed to delete resource:" + locator, ex);
        }
    }

    private void doPasteOrDrop(ViewNode viewNode, DropAction dropAction, List<VRL> vrls,
                               ITaskMonitor taskMonitor) {
        log.debug("doPasteOrDrop {} on:{}", viewNode.getVRL(), dropAction);

        try {
            ProxyFactory factory = this.proxyBrowser.getProxyFactoryFor(viewNode.getVRL());
            ProxyNodeDnDHandler dndHandler = factory.getProxyDnDHandler(viewNode);
            dndHandler.doDrop(viewNode, dropAction, vrls, taskMonitor);
        } catch (Throwable ex) {
            this.proxyBrowser.handleException("Failed doDrop() type '" + dropAction + " on :"
                    + viewNode.getVRL(), ex);
        }
    }

    // ====================
    // Asynchronous updates
    // ====================

    public void fireNewNodeEvent(ProxyNode parent, ProxyNode childNode) {
        parent.getProxyNodeEventNotifier().scheduleEvent(
                VRSEvent.createChildAddedEvent(parent.getVRL(), childNode.getVRL()));
    }

    public void fireNewNodesEvent(ProxyNode parent, List<ProxyNode> childNodes) {
        VRL[] vrls = ProxyNode.toVRLArray(childNodes);

        parent.getProxyNodeEventNotifier().scheduleEvent(
                VRSEvent.createChildsAddedEvent(parent.getVRL(), vrls));
    }

    public void fireDeletedNodeEvent(ProxyNode parent, ProxyNode actualNode) {
        actualNode.getProxyNodeEventNotifier().scheduleEvent(
                VRSEvent.createChildDeletedEvent((parent != null) ? parent.getVRL() : null,
                        actualNode.getVRL()));
    }

    public void fireNodeRenamedEvent(ProxyNode parent, ProxyNode oldNode, ProxyNode newNode) {
        oldNode.getProxyNodeEventNotifier().scheduleEvent(
                VRSEvent.createNodeRenamedEvent((parent != null) ? parent.getVRL() : null,
                        oldNode.getVRL(), newNode.getVRL()));
    }

}

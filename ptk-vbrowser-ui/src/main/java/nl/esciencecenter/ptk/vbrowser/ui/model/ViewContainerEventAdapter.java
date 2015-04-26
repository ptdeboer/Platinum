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

package nl.esciencecenter.ptk.vbrowser.ui.model;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPopupMenu;

import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.Action;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;

/**
 * Generic event handler for ViewComponents. Handles Mouse and Focus events.
 */
public class ViewContainerEventAdapter implements MouseListener, MouseMotionListener, FocusListener
{
    private static final ClassLogger logger = ClassLogger.getLogger(ViewContainerEventAdapter.class);

    private ViewNodeContainer viewComp;

    private ViewNodeActionListener nodeActionListener;

    private ViewNode firstNode;

    private ViewNode lastNode;

    private boolean notifySelectionEvents = true;

    private boolean notifyActionEvents = true;

    public ViewContainerEventAdapter(ViewNodeContainer viewComp,
            ViewNodeActionListener componentController)
    {
        this.viewComp = viewComp;
        this.nodeActionListener = componentController;
        // this.notifySelectionEvents=handleSelectionEvents;
        // this.notifyActionEvents=handleActionEvents;
    }

    public BrowserInterface getBrowserInterface()
    {
        return this.viewComp.getBrowserInterface();
    }

    public void setNotifySelectionEvent(boolean val)
    {
        this.notifySelectionEvents = val;
    }

    @Override
    public void focusGained(FocusEvent event)
    {
        logger.debugPrintf("focusGained:%s\n", event);
    }

    @Override
    public void focusLost(FocusEvent event)
    {
        logger.debugPrintf("focusLost:%s\n", event);
    }

    @Override
    public void mouseDragged(MouseEvent event)
    {
        // debugPrintf("mouseClicked:%s\n",event);
    }

    @Override
    public void mouseMoved(MouseEvent event)
    {
        // debugPrintf("mouseClicked:%s\n",event);
    }

    @Override
    public void mouseEntered(MouseEvent event)
    {
        ViewNode node = this.getViewNode(event);

        if (node == null)
            viewComp.requestFocus(true); // container focus
        else
            viewComp.requestNodeFocus(node, true); // child focus

    }

    @Override
    public void mouseExited(MouseEvent event)
    {
        // logger.debugPrintf("mouseClicked:%s\n", event);
    }

    @Override
    public void mouseReleased(MouseEvent event)
    {
        // logger.debugPrintf("mouseClicked:%s\n", event);
    }

    public void mousePressed(MouseEvent e)
    {
        doMousePressed(e);
    }

    public void mouseClicked(MouseEvent e)
    {
        doMouseClicked(e);
    }

    // =================
    // implementations
    // =================

    protected void doMouseClicked(MouseEvent e)
    {
        logger.debugPrintf("mouseClicked:%s\n", e);

        ViewNode node = getViewNode(e);

        boolean canvasClick = false;

        if (node == null)
        {
            canvasClick = true;
        }

        boolean shift = ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0);
        boolean combine = ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0);

        // When pressed down, no selection is made
        // When clicked, selection is made:
        if (isSelection(e))
        {
            if (canvasClick)
            {
                if (combine == false)
                {
                    notifyClearSelection(viewComp);
                    // clear range select
                    this.firstNode = null;
                    this.lastNode = null;
                    // unselect !
                    fireNodeSelectionAction(null); // nodeActionListener.handleNodeSelection(null);
                }
                else
                {
                    // Is combined selection click on canvas -> handled by tree
                    // selection model
                    notifySetSelection(viewComp, null, true);
                    fireNodeSelectionAction(null); // nodeActionListener.handleNodeSelection(null);
                }
            }
            else
            {
                // handled mouseClicked if NO multi combo click !
                if ((combine == false) && (shift == false))
                {
                    // clear range select
                    this.firstNode = null;
                    this.lastNode = null;
                    notifyClearSelection(viewComp);
                    // single select:
                    notifySetSelection(viewComp, node, true);
                    // is selection action:
                    fireNodeSelectionAction(node); // nodeActionListener.handleNodeSelection(node);
                }
                else if (shift == true)
                {
                    // range select:
                    if (this.firstNode == null)
                    {
                        // first click:
                        this.firstNode = node;
                        notifySetSelection(viewComp, node, true);
                    }
                    else
                    {
                        // unselect previous range:
                        if (this.lastNode != null)
                        {
                            notifySetSelectionRange(viewComp, firstNode, lastNode, false);
                        }

                        // second or third,etc click:
                        this.lastNode = node;
                        notifySetSelectionRange(viewComp, firstNode, lastNode, true);

                        // controller.notifySelectionClick(node,true);
                    }

                }
                else
                // combine=true
                {
                    // add selection
                    notifySetSelection(viewComp, node, true);
                }
            }

        }

        if ((combine == false) && (shift == false))
        {
            if (isAction(e))
            {
                fireNodeDefaultAction(node);// nodeActionListener.handleNodeAction(node);
            }
        }
    }

    protected void doMousePressed(MouseEvent e)
    {
        logger.debugPrintf("mousePressed:%s\n", e);

        ViewNode node = getViewNode(e);
        boolean canvasclick = false;

        if (node == null)
        {
            // no node under mouse click
            node = viewComp.getViewNode();
            canvasclick = true;
        }

        // CTRL and SHIFT modifiers:
        boolean combine = ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0);
        boolean shift = ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0);

        // Right click on node without modifiers is auto unselects all!
        if (canvasclick == false)
        {
            if ((combine == false) && (shift == false) && isPopupTrigger(e))
            {
                // clear selection BEFORE menu popup!
                this.viewComp.clearNodeSelection();
            }
        }

        ViewNode refs[] = this.viewComp.getNodeSelection();

        // check whether more then one nodes are selected
        // If two nodes are selected use Canvas Menu for Multiple Selections !
        if ((refs != null) && (refs.length > 1) && (combine == true))
        {
            canvasclick = true;
        }

        // right click -> Popup
        if (isPopupTrigger(e))
        {
            if (canvasclick == false)
            {
                doShowPopupMenu(viewComp, node, false, e);
            }
            else
            {
                doShowPopupMenu(viewComp, node, true, e);
            }
        }
    }

    private boolean doShowPopupMenu(ViewNodeContainer viewComponent, ViewNode viewNode, boolean canvasMenu, MouseEvent e)
    {
        // get (optional) ViewNode menu */
        JPopupMenu menu = viewComponent.createNodeActionMenuFor(viewNode, canvasMenu);
        if (menu != null)
        {
            menu.show((Component) e.getSource(), e.getX(), e.getY());
            return true;
        }
        else
        {
            logger.warnPrintf("No pop-up menu created for (comp/ViewNode):%s/%s\n", viewComponent, viewNode);
            return false;
        }

    }

    // =========
    // Events
    // =========

    private void fireNodeSelectionAction(ViewNode node)
    {
        this.nodeActionListener.handleNodeActionEvent(node, Action.createSelectionAction(node));
    }

    private void fireNodeDefaultAction(ViewNode node)
    {
        this.nodeActionListener.handleNodeActionEvent(node, Action.createDefaultAction(node));
    }

    protected void notifySetSelectionRange(ViewNodeContainer viewC, ViewNode node1, ViewNode node2, boolean value)
    {
        if (this.notifySelectionEvents)
            viewC.setNodeSelectionRange(node1, node2, value);
    }

    protected void notifyClearSelection(ViewNodeContainer viewC)
    {
        if (this.notifySelectionEvents)
            viewC.clearNodeSelection();
    }

    protected void notifySetSelection(ViewNodeContainer viewC, ViewNode node, boolean isSelected)
    {
        if (this.notifySelectionEvents)
            viewComp.setNodeSelection(node, isSelected);
    }

    /**
     * Get active ViewNode. This might be a child node in ViewContainer or, in the case of a single node, the node
     * itself.
     */
    public ViewNode getViewNode(MouseEvent e)
    {
        // check source:
        Object source = e.getSource();

        if (source instanceof ViewNodeContainer)
        {
            // check container:
            return ((ViewNodeContainer) source).getNodeUnderPoint(e.getPoint());
        }
        else
        {
            // single component:
            if (e.getSource() instanceof ViewNodeComponent)
            {
                return ((ViewNodeComponent) source).getViewNode();
            }
        }

        return null;
    }

    // === misc/todo ===

    public boolean isAction(MouseEvent e)
    {
        return getBrowserInterface().getPlatform().getGuiSettings().isAction(e);
    }

    public boolean isSelection(MouseEvent e)
    {
        return getBrowserInterface().getPlatform().getGuiSettings().isSelection(e);
    }

    public boolean isPopupTrigger(MouseEvent e)
    {
        return getBrowserInterface().getPlatform().getGuiSettings().isPopupTrigger(e);
    }

}

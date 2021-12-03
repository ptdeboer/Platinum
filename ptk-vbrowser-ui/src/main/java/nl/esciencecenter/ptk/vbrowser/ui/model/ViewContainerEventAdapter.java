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

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmd;
import nl.esciencecenter.ptk.vbrowser.ui.actionmenu.ActionCmdType;
import nl.esciencecenter.ptk.vbrowser.ui.actions.UIAction;
import nl.esciencecenter.ptk.vbrowser.ui.actions.UIActionListener;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import static java.awt.event.MouseEvent.NOBUTTON;
import static nl.esciencecenter.ptk.util.CollectionUtil.getFirstOrNull;
import static nl.esciencecenter.ptk.vbrowser.ui.actions.KeyMappings.*;

/**
 * Generic event handler for ViewComponents. Handles Mouse and Actions events.
 * Handles both AWT Actions and VBrowser UIActions.
 */
@Slf4j
public class ViewContainerEventAdapter extends MouseAdapter implements ActionListener, UIActionListener {

    private final ViewNodeContainer viewComp;
    private final ViewNodeActionListener nodeActionListener;
    private ViewNode firstNode;
    private ViewNode lastNode;

    private final boolean notifySelectionEvents = true;

    public ViewContainerEventAdapter(ViewNodeContainer viewComp,
                                     ViewNodeActionListener componentController) {
        this.viewComp = viewComp;
        this.nodeActionListener = componentController;
    }

    public BrowserInterface getBrowserInterface() {
        return this.viewComp.getBrowserInterface();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        doMousePressed(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        doMouseClicked(e);
    }

    // =================
    // implementations
    // =================

    protected void doMouseClicked(MouseEvent e) {
        log.debug("mouseClicked:{}", e);

        ViewNode node = getViewNode(e);

        boolean canvasClick = node == null;

        boolean shift = ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0);
        boolean combine = ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0);

        // When pressed down, no selection is made
        // When clicked, selection is made:
        if (isSelection(e)) {
            if (canvasClick) {
                if ((combine == false) && (shift == false)) {
                    notifyClearSelection(viewComp);
                    // clear range select
                    this.firstNode = null;
                    this.lastNode = null;
                    // unselect !
                    fireNodeSelectionAction(null); // nodeActionListener.handleNodeSelection(null);
                } else {
                    // Is combined selection click on canvas -> handled by tree
                    // selection model
                    notifySetSelection(viewComp, null, true);
                    fireNodeSelectionAction(null); // nodeActionListener.handleNodeSelection(null);
                }
            } else {
                // handled mouseClicked if NO multi combo click !
                if ((combine == false) && (shift == false)) {
                    // clear range select
                    this.firstNode = null;
                    this.lastNode = null;
                    notifyClearSelection(viewComp);
                    // single select:
                    notifySetSelection(viewComp, node, true);
                    // is selection action:
                    fireNodeSelectionAction(node); // nodeActionListener.handleNodeSelection(node);
                } else if (shift == true) {
                    // range select:
                    if (this.firstNode == null) {
                        // first click:
                        this.firstNode = node;
                        notifySetSelection(viewComp, node, true);
                    } else {
                        // unselect previous range:
                        if (this.lastNode != null) {
                            notifySetSelectionRange(viewComp, firstNode, lastNode, false);
                        }

                        // second or third,etc click:
                        this.lastNode = node;
                        notifySetSelectionRange(viewComp, firstNode, lastNode, true);

                        // controller.notifySelectionClick(node,true);
                    }

                } else
                // combine=true
                {
                    // add selection
                    notifySetSelection(viewComp, node, true);
                }
            }

        }

        if ((combine == false) && (shift == false)) {
            if (isActionClick(e)) {
                fireNodeDefaultAction(node);// nodeActionListener.handleNodeAction(node);
            }
        }
    }

    protected void doMousePressed(MouseEvent e) {
        log.debug("mousePressed:{}", e);


        ViewNode node = getViewNode(e);
        boolean canvasclick = false;

        if (node == null) {
            // no node under mouse click
            node = viewComp.getViewNode();
            canvasclick = true;
        }

        // CTRL and SHIFT modifiers:
        boolean combine = ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0);
        boolean shift = ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0);

        // Right click on node without modifiers is auto unselects all!
        if (canvasclick == false) {
            if ((combine == false) && (shift == false) && isPopupTrigger(e)) {
                // clear selection BEFORE menu popup!
                this.viewComp.clearNodeSelection();
            }
        }

        List<ViewNode> refs = this.viewComp.getNodeSelection();

        // check whether more then one nodes are selected
        // If two nodes are selected use Canvas Menu for Multiple Selections !
        if ((refs != null) && (refs.size() > 1) && ((combine == true) || (shift == true))) {
            canvasclick = true;
        }

        // right click -> Popup
        if (isPopupTrigger(e)) {
            doShowPopupMenu(viewComp, node, canvasclick != false, e);
        }
    }

    private boolean doShowPopupMenu(ViewNodeContainer viewComponent, ViewNode viewNode,
                                    boolean canvasMenu, MouseEvent e) {
        // get (optional) ViewNode menu */
        JPopupMenu menu = viewComponent.createNodeActionMenuFor(viewNode, canvasMenu);
        if (menu != null) {
            menu.show((Component) e.getSource(), e.getX(), e.getY());
            return true;
        } else {
            log.warn("No pop-up menu created for (comp/ViewNode):{}/{}", viewComponent,
                    viewNode);
            return false;
        }

    }

    // =========
    // Events
    // =========

    private void fireNodeSelectionAction(ViewNode node) {
        this.nodeActionListener.handleNodeActionEvent(node, ActionCmd.createSelectionAction(node));
    }

    private void fireNodeDefaultAction(ViewNode node) {
        this.nodeActionListener.handleNodeActionEvent(node, ActionCmd.createDefaultAction(node));
    }


    protected void notifySetSelectionRange(ViewNodeContainer viewC, ViewNode node1, ViewNode node2,
                                           boolean value) {
        if (this.notifySelectionEvents)
            viewC.setNodeSelectionRange(node1, node2, value);
    }

    protected void notifyClearSelection(ViewNodeContainer viewC) {
        if (this.notifySelectionEvents)
            viewC.clearNodeSelection();
    }

    protected void notifySetSelection(ViewNodeContainer viewC, ViewNode node, boolean isSelected) {
        if (this.notifySelectionEvents)
            viewComp.setNodeSelection(node, isSelected);
    }

    /**
     * Get active ViewNode. This might be a child node in ViewContainer or, in the case of a single
     * node, the node itself.
     */
    public ViewNode getViewNode(MouseEvent e) {
        // check source:
        Object source = e.getSource();

        if (source instanceof ViewNodeContainer) {
            // check container:
            return ((ViewNodeContainer) source).getNodeUnderPoint(e.getPoint());
        } else {
            // single component:
            if (e.getSource() instanceof ViewNodeComponent) {
                return ((ViewNodeComponent) source).getViewNode();
            }
        }

        return null;
    }

    public boolean isActionClick(MouseEvent e) {
        return getBrowserInterface().getPlatform().getGuiSettings().isActionClick(e);
    }

    public boolean isSelection(MouseEvent e) {
        return getBrowserInterface().getPlatform().getGuiSettings().isSelection(e);
    }

    public boolean isPopupTrigger(MouseEvent e) {
        return getBrowserInterface().getPlatform().getGuiSettings().isPopupTrigger(e);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("Action Performed:{}:{}", e.getActionCommand(), e);
    }

    /**
     * Handles Key Mappings which use registered InputMap and ActionMap.
     */
    @Override
    public void uiActionPerformed(UIAction action, ActionEvent e) {
        JComponent comp = (JComponent) e.getSource();

        List<ViewNode> nodes = this.viewComp.getNodeSelection();
        ViewNode node = getFirstOrNull(nodes);

        if (action == ESCAPE) {
            this.viewComp.clearNodeSelection();
        }

        if (action == OPEN_ACTION) {
            this.nodeActionListener.handleNodeActionEvent(node, ActionCmd.createCustomAction(ActionCmdType.OPEN_LOCATION, node));
        }

        if (action == FULL_OPEN) {
            this.nodeActionListener.handleNodeActionEvent(node, ActionCmd.createCustomAction(ActionCmdType.OPEN_CONTAINER, node));
        }

        if (action == OPEN_MENU) {
            boolean canvasMenu = false;
            Rectangle bounds = this.viewComp.findBoundsOfSelectionNode(node);
            if (bounds == null) {
                bounds = viewComp.getJComponent().getBounds();
                canvasMenu = true;
            }
            int px = bounds.x + bounds.width / 2;
            int py = bounds.y + bounds.height / 2;
            MouseEvent dummyEvent = new MouseEvent(comp, -1, e.getWhen(), 0, px, py, 1, false, NOBUTTON);
            doShowPopupMenu(viewComp, node, canvasMenu, dummyEvent);
        }
    }

}

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

package nl.esciencecenter.ptk.vbrowser.ui.resourcetree;

import lombok.extern.slf4j.Slf4j;
import nl.esciencecenter.ptk.vbrowser.ui.actions.KeyMappings;
import nl.esciencecenter.ptk.vbrowser.ui.actions.UIAction;
import nl.esciencecenter.ptk.vbrowser.ui.actions.UIActionListener;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserInterface;
import nl.esciencecenter.ptk.vbrowser.ui.browser.BrowserPlatform;
import nl.esciencecenter.ptk.vbrowser.ui.dnd.ViewNodeContainerDragListener;
import nl.esciencecenter.ptk.vbrowser.ui.model.*;
import nl.esciencecenter.ptk.vbrowser.ui.properties.UIProperties;

import javax.swing.*;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Actual Swing JTree 'View' Component.
 */
@Slf4j
public class ResourceTree extends JTree implements ViewNodeContainer, Autoscroll, UIActionListener {

    /**
     * Follow mouse movements inside Tree and update node focus+effects.
     */
    public class MouseFocusFollower extends MouseAdapter implements FocusListener {
        private ResourceTreeNode prevNode;

        @Override
        public void mouseEntered(MouseEvent event) {
            log.debug("mouseEntered:{}", event);
            ResourceTree.this.requestFocusInWindow();
        }

        public void mouseMoved(MouseEvent e) {
            if (prevNode != null) {
                ResourceTree.this.toggleFocus(prevNode, false);
            }

            ResourceTreeNode rtnode = ResourceTree.this.getRTNodeUnderPoint(e.getPoint());
            ResourceTree.this.toggleFocus(rtnode, true);
            prevNode = rtnode;
        }

        public void focusGained(FocusEvent e) {
            setBorder(e.getComponent(), true);
        }

        @Override
        public void focusLost(FocusEvent e) {
            setBorder(e.getComponent(), false);
        }

        public void setBorder(Component component, boolean value) {
            ResourceTree.this.setFocusBorder(value);
        }
    }

    // ========================================================================
    //
    // ========================================================================

    private ResourceTreeUpdater dataUpdater;

    // private ViewModel viewModel;

    private ResourceTreeController controller;

    private ViewContainerEventAdapter resourceTreeListener;

    private UIViewModel uiModel;

    private DragSource dragSource;

    private ViewNodeContainerDragListener dgListener;

    /**
     * Create a new ResourceTree using the UI properties from UIModel and the data from DataSource.
     */
    public ResourceTree(BrowserInterface browser, ProxyDataSource viewNodeSource) {
        init(browser, viewNodeSource);
    }

    public BrowserPlatform getPlatform() {
        return this.getBrowserInterface().getPlatform();
    }

    public void populate(ResourceTreeNode node) {
        log.debug("ResourceTree:populate():{}", node.getVRL());
        this.dataUpdater.updateChilds(node);
    }

    private void init(BrowserInterface browser, ProxyDataSource viewNodeSource) {

        // default model
        this.uiModel = UIViewModel.createTreeViewModel();

        // === TreeSelectionModel ===
        TreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
        this.setSelectionModel(selectionModel);

        // === ResourceTreeModel ===
        // empty model
        ResourceTreeModel model = new ResourceTreeModel();
        // data producer users DataSource source to update model
        this.dataUpdater = new ResourceTreeUpdater(this, viewNodeSource);
        // tree model:
        this.setModel(model);

        // === Controllers, etc. ===
        this.controller = new ResourceTreeController(browser, this, model);

        // === Properties ===
        putClientProperty("JTree.lineStyle", "Angled");
        this.setScrollsOnExpand(true);
        this.setExpandsSelectedPaths(true);
        // Keep generating scroll events even when draggin out of window !
        this.setAutoscrolls(true);
        this.setExpandsSelectedPaths(true);
        // start with root node:
        this.setRootVisible(true);

        // === Listeners ===

        resourceTreeListener = new ViewContainerEventAdapter(this, controller);

        // Listen for Tree Selection Events
        addTreeExpansionListener(controller);
        // Mouse Focus follower for ResourceTreeNodes: mouse entered, moved and set border:
        MouseFocusFollower mouseFocusFollower = new MouseFocusFollower();
        this.addMouseListener(mouseFocusFollower);
        this.addMouseMotionListener(mouseFocusFollower);
        this.addFocusListener(mouseFocusFollower);
        //
        this.addMouseListener(resourceTreeListener);
        this.setFocusable(true);

        // custom quick-view actions.
        KeyMappings.addActionKeyMappings(this, false);

        // === ResourceTreeCellRenderer ===
        // Setting the renderer fires (UI) update events !
        ResourceTreeCellRenderer renderer = new ResourceTreeCellRenderer(this);
        setCellRenderer(renderer);

        // update top level !
        dataUpdater.update();

        // Properties
        this.setFocusable(true);

        // [DnD]
        initDND();
    }

    private void initDND() {

        // drop: DropTarget AND Tranferhandler need to be set !
        this.setDropTarget(new ResourceTreeDropTarget(this));
        this.setTransferHandler(getPlatform().getTransferHandler());

        // drag
        this.dragSource = DragSource.getDefaultDragSource();
        this.dgListener = new ViewNodeContainerDragListener();
        // this.dsListener = MyDragSourceListener.fsutil();
        // component, action, listener
        this.dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE,
                this.dgListener);
    }

    public boolean isFocusable() {
        return true;
    }

    public ResourceTreeModel getModel() {
        return (ResourceTreeModel) super.getModel();
    }

    public ResourceTreeUpdater getDataProducer() {
        return this.dataUpdater;
    }

    public int getIconSize() {
        return getUIViewModel().getIconSize();
    }

    public UIViewModel getUIViewModel() {
        return uiModel;
    }

    public JComponent getComponent() {
        return this;
    }

    /**
     * Used for Mouse Events :
     */
    public ViewNode getNodeUnderPoint(Point p) {
        ResourceTreeNode rtnode = this.getRTNodeUnderPoint(p);
        if (rtnode != null)
            return rtnode.getViewItem();

        return null;
    }

    /**
     * Used for Mouse Events :
     */
    public ResourceTreeNode getRTNodeUnderPoint(Point p) {
        if (p == null)
            return null;

        int clickRow = getRowForLocation(p.x, p.y);

        TreePath path = getPathForLocation(p.x, p.y);

        if ((path == null) || (clickRow < 0)) {
            // no node under mouse click
            return null;
        }

        return getNode(path);
    }

    public ResourceTreeNode getNode(TreePath path) {
        Object o = path.getLastPathComponent();

        if (o == null) {
            log.debug("No Node found at tree path:" + path);
        } else if (o instanceof ResourceTreeNode) {
            // instanceof returns false when o==null, so here o exists.
            return (ResourceTreeNode) o;
        }

        return null;
    }

    public ResourceTreeNode getRootRTNode() {
        return this.getModel().getRoot();
    }

    public ViewNode getViewNode() {
        return this.getModel().getRoot().getViewItem();
    }

    public ResourceTreeNode[] getRTSelection() {
        TreePath[] paths = getSelectionPaths();

        if (paths == null)
            return null; // no selection

        ArrayList<ResourceTreeNode> nodes = new ArrayList<ResourceTreeNode>(paths.length);

        for (TreePath path : paths) {
            ResourceTreeNode node = this.getNode(path);
            if (node != null)
                nodes.add(node);
        }

        ResourceTreeNode[] _nodes = new ResourceTreeNode[nodes.size()];

        return nodes.toArray(_nodes);
    }

    public void setRoot(ProxyDataSource viewNodeSource, boolean update, boolean showAsRoot) {
        this.setRootVisible(showAsRoot);
        this.setShowsRootHandles(showAsRoot == false);
        this.setDataSource(viewNodeSource, update);
    }

    protected void setDataSource(ProxyDataSource viewNodeSource, boolean update) {
        this.getUpdater().setDataSource(viewNodeSource, update);
    }

    public ResourceTreeUpdater getUpdater() {
        return this.dataUpdater;
    }

    // === Selection Methods === //

    public void clearSelection() {
        // this is called by SelectionModel
        super.clearSelection();
    }

    public void clearNodeSelection() {
        // this is called by ViewNodeCompoent handler, but selection
        // is already cleared by the JTree's SelectionModel !

        // super.clearSelection();
    }

    public java.util.List<ViewNode> getNodeSelection() {
        ResourceTreeNode[] rtnodes = this.getRTSelection();
        if (rtnodes == null)
            return new ArrayList<>();

        java.util.List<ViewNode> nodes = new ArrayList();

        for (int i = 0; i < rtnodes.length; i++)
            nodes.add(rtnodes[i].getViewItem());

        return nodes;
    }

    @Override
    public void setNodeSelection(ViewNode node, boolean isSelected) {
        // selection already done by selection model
        log.debug("updateSelection {}={}", node, isSelected);
    }

    @Override
    public void setNodeSelectionRange(ViewNode firstNode, ViewNode lastNode, boolean selected) {
        // selection already done by selection model
        log.debug("selection range=[{},{}]={}", firstNode, lastNode, selected);
    }

    public JPopupMenu createNodeActionMenuFor(ViewNode node, boolean canvasMenu) {
        // allowed during testing
        if (this.controller.getBrowserInterface() == null) {
            log.warn("getActionMenuFor() no browser registered.");
            return null;
        }
        // node menu
        return this.controller.getBrowserInterface().createActionMenuFor(this, node, canvasMenu);
    }

    @Override
    public boolean requestFocus(boolean value) {
        if (value)
            return this.requestFocusInWindow();
        return false;
    }

    public void toggleFocus(ResourceTreeNode rtnode, boolean value) {
        if (rtnode == null) {
            // unset focus
        } else {
            rtnode.setHasFocus(value);
            this.getModel().uiFireNodeChanged(rtnode);
        }

    }

    @Override
    public ViewNodeContainer getViewContainer() {
        // ResourceTree has no parent:
        return null;
    }

    public void repaintNode(ResourceTreeNode node) {
        // forward repaint request to model:
        this.getModel().uiFireNodeChanged(node);
    }

    public ViewNode getCurrentSelectedNode() {
        java.util.List<ViewNode> nodes = this.getNodeSelection();

        if ((nodes == null) || (nodes.size() <= 0))
            return null;

        return nodes.get(0);
    }

    private final int autoScrollMargin = 12;

    @Override
    public void autoscroll(Point p) {
        int realrow = getRowForLocation(p.x, p.y);
        Rectangle outer = getBounds();
        realrow = (p.y + outer.y <= autoScrollMargin ? realrow < 1 ? 0 : realrow - 1
                : realrow < getRowCount() - 1 ? realrow + 1 : realrow);
        scrollRowToVisible(realrow);
    }

    @Override
    public Insets getAutoscrollInsets() {
        Rectangle outer = getBounds();
        Rectangle inner = getParent().getBounds();

        return new Insets(inner.y - outer.y + autoScrollMargin, inner.x - outer.x
                + autoScrollMargin, outer.height - inner.height - inner.y + outer.y
                + autoScrollMargin, outer.width - inner.width - inner.x + outer.x
                + autoScrollMargin);
    }

    @Override
    public BrowserInterface getBrowserInterface() {
        return controller.getBrowserInterface();
    }

    @Override
    public Rectangle findBoundsOfSelectionNode(ViewNode node) {
        TreePath treePath = this.findTreePathOfSelection(node);
        if (treePath == null) {
            return null;
        }
        return getPathBounds(treePath);
    }

    public TreePath findTreePathOfSelection(ViewNode node) {
        TreePath[] selPaths = getSelectionPaths();
        if (selPaths == null) {
            return null;
        }
        for (TreePath path : selPaths) {
            Object pathNode = path.getLastPathComponent();
            if ((pathNode instanceof ResourceTreeNode) && ((ResourceTreeNode) pathNode).viewNode.equals(node)) {
                log.debug("Found selected viewNode: {}", path);
                return path;
            }
        }
        return null;
    }


    public ProxyDataSource getDataSource() {
        return dataUpdater.getDataSource();
    }

    public JComponent getJComponent() {
        return this;
    }

    @Override
    public void uiActionPerformed(UIAction action, ActionEvent event) {
        // Delegate. need better api.
        this.resourceTreeListener.uiActionPerformed(action, event);
    }

    public void setFocusBorder(boolean value) {
        if (value) {
            setBorder(BorderFactory.createLineBorder(getUIProperties().getFocusBorderColor(), 1));
        } else {
            // Invisible border to avoid jumping of content.
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }
    }

    private UIProperties getUIProperties() {
        return this.getBrowserInterface().getPlatform().getGuiSettings();
    }

}

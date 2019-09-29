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

import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import nl.esciencecenter.ptk.util.logging.PLogger;
import nl.esciencecenter.ptk.vbrowser.ui.UIGlobal;
import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Generic ResourceTreeModel containing only ViewNodes and Attributes.
 */
public class ResourceTreeModel extends DefaultTreeModel {
       private static PLogger logger;

    static {
        logger = PLogger.getLogger(ResourceTreeModel.class);
    }

    // ========================================================================
    // Constructor/initializers
    // ========================================================================

    public ResourceTreeModel() {
        super(null, false);
    }

    @Override
    public ResourceTreeNode getRoot() {
        return (ResourceTreeNode) super.root;
    }

    public void setRoot(ResourceTreeNode node) {
        super.setRoot(node);
        this.uiFireStructureChanged(node);
    }

    // ========================================================================
    // Super class Object to ResourceTreeNode methods
    // ========================================================================

    @Override
    public ResourceTreeNode getChild(Object parent, int index) {
        return getChild((ResourceTreeNode) parent, index);
    }

    @Override
    public int getChildCount(Object parent) {
        return getChildCount((ResourceTreeNode) parent);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return getIndexOfChild((ResourceTreeNode) parent, (ResourceTreeNode) child);
    }

    @Override
    public boolean isLeaf(Object node) {
        return isLeaf((ResourceTreeNode) node);
    }

    // ========================================================================
    // ResourceTreeNode methods
    // ========================================================================

    public ResourceTreeNode getChild(ResourceTreeNode parent, int index) {
        return parent.getChildAt(index);
    }

    public int getChildCount(ResourceTreeNode parent) {
        return parent.getChildCount();
    }

    public boolean isLeaf(ResourceTreeNode node) {
        return node.isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        logger.debugPrintf(">>> FIXME: valueForPathChanged:%s", newValue);
    }

    public int getIndexOfChild(ResourceTreeNode parent, ResourceTreeNode child) {
        return parent.getIndex(child);
    }

    // public Enumeration<ResourceTreeNode> getChildren(ResourceTreeNode node)
    // {
    // return node.children();
    // }

    // ========================================================================
    // Model updates
    // ========================================================================

    public void setChilds(ResourceTreeNode targetNode, ViewNode[] items) {
        updateChilds(targetNode, items, false);
    }

    public void deleteNode(ResourceTreeNode node, boolean fireEvents) {
        // update model
        ResourceTreeNode parent = node.getParent();
        int index = parent.removeChild(node);

        // fire event:
        if ((index >= 0) && (fireEvents))
            this.uiFireNodeRemoved(parent, index, node);

    }

    public void clearNode(ResourceTreeNode node, boolean fireEvent) {
        // remove previous children, might alread been removed
        node.clear();

        if (fireEvent)
            uiFireStructureChanged(node);
    }

    public void addNodes(ResourceTreeNode node, ViewNode[] childs) {
        updateChilds(node, childs, true);
    }

    /**
     * Set childs or append new ones. If a tree node with similar VRL already exist, the new
     * ViewNode will be updated (merge).
     */
    protected synchronized void updateChilds(ResourceTreeNode targetNode, ViewNode childs[],
            boolean mergeAppend) {
        logger.debugPrintf("+++ updateChilds(append=%s) for:%s,numChilds=#%s\n",
                (mergeAppend == true ? "mergeAppend" : "set"), targetNode.getVRI(),
                ((childs != null) ? "" + childs.length : "?"));

        // possible background thread:

        ResourceTreeNode[] childNodes = null;
        int childIndices[] = null;

        boolean changed = false;

        if ((targetNode.isPopulated() == false) || (mergeAppend == false)) {
            clearNode(targetNode, false);
            changed = true;
        }

        if ((childs == null) || (childs.length <= 0)) {
            targetNode.setPopulated(true);
            // redraw:
            this.uiFireStructureChanged(targetNode);
            return; // nodes already cleared
        }

        int len = childs.length;

        childNodes = new ResourceTreeNode[len];
        childIndices = new int[len];
        // Process the directories
        for (int i = 0; (childs != null) && (i < childs.length); i++) {
            logger.debugPrintf("adding child:(ViewItem)%s\n", childs[i].getVRL());

            ViewNode iconItem = childs[i];
            if (iconItem != null) {
                VRL childLoc = iconItem.getVRL();
                ResourceTreeNode rtnode = null;

                // If child already added:
                // merge the two subsequent calls to setChilds,
                // just update the ViewNode with the same name !

                if ((rtnode = targetNode.getNode(childLoc)) != null) {
                    rtnode.setViewNode(iconItem);
                    continue; // child already exists;
                }

                try {
                    ResourceTreeNode newNode = new ResourceTreeNode(targetNode, iconItem, false);

                    // it now has at least one node:
                    // parentNode.setAllowsChildren(true);

                    // add node, but do not fire event yet;

                    childNodes[i] = newNode;
                    childIndices[i] = targetNode.addNode(newNode);

                    changed = true;
                } catch (Exception e) {
                    logger.logException(PLogger.ERROR, e, "Exception in updatNode:%s\n", e);
                }
            }
        }

        targetNode.setPopulated(true);

        if (changed) {
            if (mergeAppend == true) {
                this.uiFireNodesInserted(targetNode, childIndices); // insert
            } else {
                this.uiFireStructureChanged(targetNode); // redraw
            }
        }
    }

    public int addNode(ResourceTreeNode parent, ResourceTreeNode node, boolean fireEvent) {
        // it now has at least one node:
        // parent.setAllowsChildren(true);
        int index = parent.addNode(node);

        if (fireEvent)
            uiFireNodeInserted(parent, index);

        return index;
    }

    // ========================================================================
    // Events
    // ========================================================================

    public void uiFireStructureChanged(final ResourceTreeNode node) {
        // Check UI Thread:
        if (UIGlobal.isGuiThread() == false) {
            Runnable createTask = new Runnable() {
                public void run() {
                    uiFireStructureChanged(node);
                }
            };

            UIGlobal.swingInvokeLater(createTask);
            return;
        }

        nodeStructureChanged(node);
    }

    // Fire node changed event: updates node itself, not the structure.
    protected void uiFireNodeChanged(final ResourceTreeNode node) {
        // Check UI Thread:
        if (UIGlobal.isGuiThread() == false) {
            Runnable createTask = new Runnable() {
                public void run() {
                    uiFireNodeChanged(node);
                }
            };

            UIGlobal.swingInvokeLater(createTask);
            return;
        }

        this.nodeChanged(node);
    }

    // Fire node changed event: updates node itself, not the structure.
    protected void uiFireNodeRemoved(final ResourceTreeNode parent, int childIndex,
            final ResourceTreeNode child) {
        ResourceTreeNode childs[] = new ResourceTreeNode[1];
        int[] removedChildren = new int[1];
        removedChildren[0] = childIndex;
        childs[0] = child;

        uiFireNodesRemoved(parent, removedChildren, childs);
    }

    protected void uiFireNodesRemoved(final ResourceTreeNode parent, final int childIndices[],
            final ResourceTreeNode childs[]) {
        // Check UI Thread:
        if (UIGlobal.isGuiThread() == false) {
            Runnable createTask = new Runnable() {
                public void run() {
                    uiFireNodesRemoved(parent, childIndices, childs);
                }
            };

            UIGlobal.swingInvokeLater(createTask);
            return;
        }

        this.nodesWereRemoved(parent, childIndices, childs);
    }

    protected void uiFireNodeInserted(final ResourceTreeNode parent, int childIndex) {
        int[] removedChildren = new int[1];
        removedChildren[0] = childIndex;
        uiFireNodesInserted(parent, removedChildren);
    }

    protected void uiFireNodesInserted(final ResourceTreeNode parent, final int childIndices[]) // ,final
                                                                                                // ResourceTreeNode
                                                                                                // childs[])
    {
        // Check UI Thread:
        if (UIGlobal.isGuiThread() == false) {
            Runnable createTask = new Runnable() {
                public void run() {
                    uiFireNodesInserted(parent, childIndices); // ,childs);
                }
            };

            UIGlobal.swingInvokeLater(createTask);
            return;
        }

        this.nodesWereInserted(parent, childIndices); // , childs);

    }

    // =========================================================================
    //
    // =========================================================================

    /**
     * Find nodes which have the specified ProxyLocator. Method peforms a tree walk.
     */
    public List<ResourceTreeNode> findNodes(VRL locator) {
        ResourceTreeNode current = this.getRoot();
        Vector<ResourceTreeNode> nodes = new Vector<ResourceTreeNode>();
        return findNodes(nodes, current, locator);
    }

    protected List<ResourceTreeNode> findNodes(List<ResourceTreeNode> nodes, ResourceTreeNode node,
            VRL locator) {
        // check parent:
        if (node.getVRI().equals(locator))
            nodes.add(node);

        java.util.List<ResourceTreeNode> childs = node.getChilds();
        for (ResourceTreeNode child : childs) {
            // skip one recursion:
            if (child.hasChildren() == false) {
                if (child.getVRI().equals(locator))
                    nodes.add(child);
            } else {
                findNodes(nodes, child, locator);
            }
        }

        return nodes;
    }

}

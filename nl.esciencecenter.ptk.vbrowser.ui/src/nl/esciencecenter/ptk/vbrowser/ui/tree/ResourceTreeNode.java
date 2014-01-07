/*
 * Copyrighted 2012-2013 Netherlands eScience Center.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").  
 * You may not use this file except in compliance with the License. 
 * For details, see the LICENCE.txt file location in the root directory of this 
 * distribution or obtain the Apache License at the following location: 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 * 
 * For the full license, see: LICENCE.txt (located in the root folder of this distribution). 
 * ---
 */
// source: 

package nl.esciencecenter.ptk.vbrowser.ui.tree;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import nl.esciencecenter.ptk.vbrowser.ui.model.ViewNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * ResourceTreeNode holds the childs in the ResourceTree. All updates and node
 * manipulations should go through the ResourceTreeModel.
 */
public class ResourceTreeNode implements TreeNode // , ViewNodeComponent
{
    private ResourceTreeNode parent = null;
    private Vector<ResourceTreeNode> childs = new Vector<ResourceTreeNode>();

    private boolean isRoot = false;
    private boolean isPopulated = false;
    private boolean hasFocus;
    
    protected ViewNode viewNode;

    public ResourceTreeNode(ResourceTreeNode parent,ViewNode item, boolean isRoot)
    {
        if ((parent == null) && (isRoot==false))
        {
            throw new NullPointerException("NULL parent not allowed for non root node.");
        }
        
        this.viewNode = item;
        this.parent = parent;
        this.isRoot = false;
    }

    @Override
    public ResourceTreeNode getChildAt(int childIndex)
    {
        return childs.get(childIndex);
    }

    public boolean isRoot()
    {
        return isRoot;
    }

    /**
     * Creates new TreePath to this node
     */
    public TreePath getTreePath()
    {
        return new TreePath(this.getPath());
    }

    public ResourceTreeNode[] getPath()
    {
        Vector<ResourceTreeNode> paths = new Vector<ResourceTreeNode>();

        ResourceTreeNode current = this;
        while (current != null)
        {
            paths.add(current);

            if (current.isRoot())
                break;

            ResourceTreeNode prev = current;
            current = prev.getParent();

            if (current == null)
                throw new NullPointerException("*** NULL Parent for non root node:" + this);

            // Grr.Arrgg.
            if (current.equals(prev))
                throw new Error("*** Parent points to itself:" + current);
        }

        ResourceTreeNode _arr[] = new ResourceTreeNode[paths.size()];
        int len = paths.size();

        // inverse path
        for (int i = 0; i < len; i++)
            _arr[len - i - 1] = paths.get(i);
        return _arr;
    }

    @Override
    public int getChildCount()
    {
        synchronized (this.childs)
        {
            return childs.size();
        }
    }

    @Override
    public ResourceTreeNode getParent()
    {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node)
    {
        return getIndex((ResourceTreeNode) node);
    }

    public int getIndex(ResourceTreeNode node)
    {
        synchronized (this.childs)
        {
            return this.childs.indexOf(node);
        }
    }

    @Override
    public boolean getAllowsChildren()
    {
        // getAllowsChildren() means if this node can have childs at all...
        return viewNode.isComposite();
    }

    @Override
    public boolean isLeaf()
    {
        // isLeaf() => node has children.
        // Return false also, if node hasn't been populated but can have
        // children!
        // this will trigger the expansion "+" sign on the node !
        if (this.isPopulated == false)
            return (viewNode.isComposite() == false);
        else
            return (this.hasChildren() == false);
    }

    @Override
    public Enumeration<ResourceTreeNode> children()
    {
        return this.childs.elements();
    }

    public List<ResourceTreeNode> getChilds()
    {
        return this.childs; // vector implements List interface
    }

    public String getName()
    {
        return this.viewNode.getName();
    }

    public ViewNode getViewItem()
    {
        return viewNode;
    }

    public boolean hasChildren()
    {
        return (this.childs.size() > 0);
    }

    public boolean isPopulated()
    {
        return this.isPopulated;
    }

    /**
     *  Clears childs and set isPopulated to FALSE ! 
     */
    protected void clear()
    {
        this.childs.clear();
        this.isPopulated = false;
    }

    public ResourceTreeNode getNode(VRL locator)
    {
        // use hash ?
        for (ResourceTreeNode node : this.childs)
            if (node.getVRI().equals(locator))
                return node;

        return null;
    }

    public VRL getVRI()
    {
        return this.viewNode.getVRL();
    }

    protected void setViewNode(ViewNode iconItem)
    {
        this.viewNode = iconItem;
    }

    protected void setPopulated(boolean val)
    {
        this.isPopulated = val;
    }

    /** Atomic add childe node. Returns index of new childs */
    protected int addNode(ResourceTreeNode rtnode)
    {
        synchronized (this.childs)
        {
            int index = this.childs.size();
            this.childs.add(rtnode);
            return index;
        }
    }

    public String toString()
    {
        return "{<ResourceTreeNode>:" + getVRI().toString() + "}";
    }

    public void setHasFocus(boolean value)
    {
        this.hasFocus = value;
    }

    public boolean hasFocus()
    {
        return this.hasFocus;
    }

    /**
     * Atomic remove child. Returns index of deleted child.
     */
    protected int removeChild(ResourceTreeNode node)
    {
        synchronized (this.childs)
        {
            int index = this.childs.indexOf(node);
            if (index >= 0)
                this.childs.remove(index);

            return index;
        }
    }

    public ViewNode getViewNode()
    {
        return viewNode;
    }

}
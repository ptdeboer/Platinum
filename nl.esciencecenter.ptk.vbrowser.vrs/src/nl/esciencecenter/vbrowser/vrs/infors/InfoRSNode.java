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

package nl.esciencecenter.vbrowser.vrs.infors;

import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.data.StringList;
import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VRSContext;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.node.VPathNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/**
 * Super class for all InfoRS Nodes.
 */
public class InfoRSNode extends VPathNode
{
    private static final ClassLogger logger = ClassLogger.getLogger(InfoRSNode.class);

    // ===============
    // Class Constants
    // ===============

    public static List<String> defaultFolderChildTypes = new StringList(InfoRSConstants.RESOURCEFOLDER, InfoRSConstants.RESOURCELINK,
            VRSTypes.VLINK_TYPE);

    // ===============
    // Instance
    // ===============

    /**
     * Store all settings of this node into persistent VRSPoperties object.
     */
    protected AttributeSet attributes = null;

    protected ArrayList<InfoRSNode> subNodes = new ArrayList<InfoRSNode>();

    protected InfoRSNode parent;

    protected String resourceType = null;

    protected InfoRSNode(InfoRS fileSystem, String type, VRL vrl)
    {
        super(fileSystem, vrl);
        this.resourceType = type;
        this.attributes = new AttributeSet(type);
    }

    protected VRSContext getVRSContext()
    {
        return this.getInfoRS().getVRSContext();
    }

    protected InfoRS getInfoRS()
    {
        return (InfoRS) resourceSystem;
    }

    protected InfoRSNode(InfoRSNode parent, String type, VRL vrl)
    {
        super(parent.resourceSystem, vrl);
        this.resourceType = type;
        this.attributes = new AttributeSet(type);
    }

    final public String getResourceType()
    {
        return resourceType;
    }

    final public boolean isRoot()
    {
        return (parent != null);
    }

    public AttributeSet getAttributeSet()
    {
        return this.attributes.duplicate(true);
    }

    public boolean isComposite()
    {
        return true;
    }

    public String getMimeType()
    {
        return VRSTypes.VBROWSER_VRS_MIMETYPE_PREFIX + "-infors-" + getResourceType();
    }

    public InfoRSNode getParent()
    {
        return this.parent;
    }

    public VRL getParentVRL()
    {
        if (this.parent != null)
        {
            return parent.getVRL();
        }

        return new VRL("info", null, 0, "/");
    }

    @Override
    public List<? extends InfoRSNode> list() throws VrsException
    {
        return subNodes;
    }

    public List<? extends InfoResourceNode> listResourceNodes() throws VrsException
    {
        // todo: Generic filter of sub classes.

        ArrayList<InfoResourceNode> filteredNodes = new ArrayList<InfoResourceNode>();

        if ((subNodes == null) || (subNodes.size() < 0))
        {
            return null;
        }

        for (InfoRSNode node : subNodes)
        {
            if (node instanceof InfoResourceNode)
            {
                filteredNodes.add((InfoResourceNode) node);
            }
        }
        return filteredNodes;
    }

    final protected void setParent(InfoRSNode newParent) throws VrsException
    {
        if (newParent == null)
        {
            this.parent = null;
        }
        else if ((parent != null) && (parent != newParent))
        {
            throw new VrsException("Internal Error. Node can not switch parents: current=" + parent + ", new =" + newParent);
        }
        this.parent = newParent;
    }

    final protected void addSubNode(InfoRSNode node) throws VrsException
    {
        synchronized (node)
        {
            node.setParent(this);
        }

        synchronized (subNodes)
        {
            subNodes.add(node);
        }
    }

    final protected void delSubNode(InfoRSNode node) throws VrsException
    {
        synchronized (subNodes)
        {
            subNodes.remove(node);
            node.setParent(null);
        }
    }

    final protected void initSubNodes()
    {
        if (subNodes == null)
        {
            subNodes = new ArrayList<InfoRSNode>();
        }
        else
        {
            // keep object as it is used as mutex.
            synchronized (subNodes)
            {
                subNodes.clear();
            }
        }
    }

    /**
     * Performs optional recursive linear search on ArrayList.
     */
    public InfoRSNode findSubNode(VRL vrl, boolean recursive)
    {
        String subPath = vrl.getPath();
        if (subPath == null)
        {
            return null;
        }

        // unsynchronized access
        for (InfoRSNode node : subNodes)
        {
            if (node.getVRL().equals(vrl))
            {
                return node;
            }

            if (recursive && subPath.startsWith(node.getVRL().getPath()))
            {
                // recursive search:
                InfoRSNode subNode = node.findSubNode(vrl, true);
                if (subNode != null)
                {
                    return subNode;
                }
                // continue;
            }
        }

        return null;
    }

    /**
     * Return node with logical (base)name.
     * 
     * @param name
     *            logical name or basename of node.
     * @return - InfoRSNode or null.
     */
    protected InfoRSNode getSubNodeByName(String name)
    {
        // unsynchronized access:
        for (InfoRSNode node : subNodes)
        {
            if (StringUtil.equals(node.getName(), name))
            {
                return node;
            }
        }

        return null;
    }

    protected VRL createSubPathVRL(String subPath) throws VRLSyntaxException
    {
        return getVRL().resolvePath(subPath);
    }

    protected VRL createNewSubNodeVRL() throws VRLSyntaxException
    {
        return getVRL().resolvePath("v" + this.getNumNodes());
    }

    public int getNumNodes()
    {
        return subNodes.size();
    }

    /**
     * Get top level root resource node.
     */
    protected InfoRootNode getRootNode()
    {
        InfoRSNode node = this;

        while (node.parent != null)
        {
            if (node == node.parent)
            {
                throw new Error("Hierachy error: parent equal to current node:" + node);
            }
            node = node.parent;
        }

        if (node instanceof InfoRootNode)
        {
            return (InfoRootNode) node;
        }
        else
        {
            return null;
        }
    }

    public String toString()
    {
        return "<InfoRSNode:" + this.getResourceType() + ">:" + getVRL();
    }


}

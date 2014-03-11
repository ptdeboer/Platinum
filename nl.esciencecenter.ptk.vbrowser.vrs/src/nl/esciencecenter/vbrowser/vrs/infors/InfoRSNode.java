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
import nl.esciencecenter.vbrowser.vrs.VPath;
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
    
    public static List<String> defaultFolderChildTypes = new StringList( InfoRSConstants.RESOURCEFOLDER,InfoRSConstants.RESOURCELINK,VRSTypes.VLINK_TYPE ); 

    // ===============
    // Instance
    // ===============

    /** 
     * Store all settings of this node into persistent VRSPoperties object. 
     */
    protected AttributeSet attributes=null; 
    
    protected ArrayList<InfoRSNode> nodes = new ArrayList<InfoRSNode>();

    protected InfoRSNode parent;

    protected String resourceType = null;

    protected InfoRSNode(InfoRS fileSystem, String type, VRL vrl)
    {
        super(fileSystem, vrl);
        this.resourceType = type;
        this.attributes=new AttributeSet(type);
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
        this.attributes=new AttributeSet(type);
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
        return VRSTypes.VBROWSER_VRS_MIMETYPE_PREFIX+"-infors-"+getResourceType();
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
    public List<? extends VPath> list() throws VrsException
    {
        return nodes;
    }

    public List<? extends InfoRSNode> listNodes() throws VrsException
    {
        return nodes;
    }

    public List<? extends InfoResourceNode> listResourceNodes() throws VrsException
    {
        ArrayList<InfoResourceNode> subNodes=new ArrayList<InfoResourceNode>();

        if ((nodes==null) || (nodes.size()<0)) 
        {
            return null; 
        }
        
        for (InfoRSNode node:nodes)
        {
            if (node instanceof InfoResourceNode)
            {
                subNodes.add((InfoResourceNode)node);
            }
        }
        return subNodes; 
    }
    
    final protected void setParent(InfoRSNode newParent) throws VrsException
    {
        if (newParent==null)
        {
            this.parent=null; 
        }
        else if ( (parent!=null) && (parent!=newParent)) 
        {
             throw new VrsException("Internal Error. Node can not switch parents: current="+parent+", new ="+newParent);
        }
        this.parent=newParent;  
    }
    
    final protected void addNode(InfoRSNode node) throws VrsException
    {
        synchronized(node)
        {
            node.setParent(this);  
        }
        synchronized (nodes)
        {
            nodes.add(node);
        }
    }

    final protected void delNode(InfoRSNode node) throws VrsException
    {
        synchronized (nodes)
        {
            nodes.remove(node);
            node.setParent(null); 
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
        
        for (InfoRSNode node : nodes)
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
    protected InfoRSNode getSubNode(String name)
    {
        for (InfoRSNode node : nodes)
        {
            if (StringUtil.equals(node.getName(), name))
            {
                return node;
            }
        }

        return null;
    }

    protected VRL createSubNodeVRL(String subPath) throws VRLSyntaxException
    {
        return getVRL().resolvePath(subPath);
    }

    public int getNumNodes()
    {
        return nodes.size();
    }

    /** 
     * Get top level root resource node. 
     */
    protected InfoRootNode getRootNode()
    {
        InfoRSNode node=this; 
        
        while(node.parent!=null)
        {
            if (node==node.parent)
            {
                throw new Error("Hierachy error. parent equal to current node:"+node); 
            }
            node=node.parent; 
        }
        
        if (node instanceof InfoRootNode)
        {
            return (InfoRootNode)node; 
        }
        else
        {
            return null;
        }
    }
    
    
}

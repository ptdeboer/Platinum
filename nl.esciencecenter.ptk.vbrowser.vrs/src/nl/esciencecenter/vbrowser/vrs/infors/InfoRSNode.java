package nl.esciencecenter.vbrowser.vrs.infors;

import java.util.ArrayList;
import java.util.List;

import nl.esciencecenter.ptk.util.StringUtil;
import nl.esciencecenter.ptk.util.logging.ClassLogger;
import nl.esciencecenter.vbrowser.vrs.VPath;
import nl.esciencecenter.vbrowser.vrs.exceptions.VRLSyntaxException;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.node.VPathNode;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class InfoRSNode extends VPathNode
{
    private static final ClassLogger logger = ClassLogger.getLogger(InfoRSNode.class);

    protected ArrayList<InfoRSNode> nodes = new ArrayList<InfoRSNode>();

    protected InfoRSNode parent;

    protected String resourceType = null;

    protected InfoRSNode(InfoRS fileSystem, String type, VRL vrl)
    {
        super(fileSystem, vrl);
        this.resourceType = type;
    }

    protected InfoRS getInfoRS()
    {
        return (InfoRS) resourceSystem;
    }

    protected InfoRSNode(InfoRSNode parent, String type, VRL vrl)
    {
        super(parent.resourceSystem, vrl);
        this.resourceType = type;
    }

    final public String getResourceType()
    {
        return resourceType;
    }

    final public boolean isRoot()
    {
        return (parent != null);
    }

    public boolean isComposite()
    {
        return true;
    }

    public String getMimeType()
    {
        return "vrs-info/" + getResourceType().toLowerCase();
    }

    public InfoRSNode getParent()
    {
        return this.parent;
    }

    public VRL getParentVRL()
    {
        if (this.parent != null)
            return parent.getVRL();

        return new VRL("info", null, 0, "/");
    }

    @Override
    public List<? extends VPath> list() throws VrsException
    {
        return nodes;
    }

    final protected void addNode(InfoRSNode node)
    {
        synchronized (nodes)
        {
            nodes.add(node);
        }
    }

    final protected void delNode(InfoRSNode node)
    {
        synchronized (nodes)
        {
            nodes.remove(node);
        }
    }

    /**
     * Performs optional recursive linear search on ArrayList.
     */
    public InfoRSNode findNode(VRL vrl, boolean recursive)
    {
        String subPath = vrl.getPath();
        if (subPath == null)
            return null;

        for (InfoRSNode node : nodes)
        {
            if (node.getVRL().equals(vrl))
            {
                return node;
            }

            if (recursive && subPath.startsWith(node.getVRL().getPath()))
            {
                // recursive search:
                InfoRSNode subNode = node.findNode(vrl, true);
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
     * @param name logical name or basename of node. 
     * @return - InfoRSNode or null.  
     */
    protected InfoRSNode getSubNode(String name)
    {
        for (InfoRSNode node:nodes)
        {
            if (StringUtil.equals(node.getName(),name)) 
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
    
}

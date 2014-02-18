package nl.esciencecenter.vbrowser.vrs.infors;

import java.util.List;

import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class InfoRootNode extends InfoRSNode
{

    protected InfoRS infors;

    protected LocalSystem localSystem;

    protected InfoConfigNode configNode;

    public InfoRootNode(InfoRS infoRS) throws VrsException
    {
        super(infoRS, InfoRSConstants.INFOROOTNODE, new VRL("info", null, 0, "/"));
        infors = infoRS;
        init();
    }

    protected void init() throws VrsException
    {
        initChilds();
    }

    protected void initChilds() throws VrsException
    {
        this.nodes.clear();
        this.addNode(getConfigNode());
        this.addNode(getLocalSystem());
    }

    public InfoRSNode getNode(VRL vrl) throws VrsException
    {
        String paths[] = vrl.getPathElements();

        if (paths == null)
            return this;

        int n = paths.length;
        if (n == 0)
        {
            return this;
        }

        if (n > 0)
        {
            InfoRSNode node = this.findNode(vrl, true);

            if (node != null)
            {
                return node;
            }
        }

        throw new VrsException("Node not found:" + vrl);
    }

    protected LocalSystem getLocalSystem() throws VrsException
    {
        if (localSystem == null)
        {
            initLocalSystem();
        }

        return localSystem;
    }

    protected InfoConfigNode getConfigNode()
    {
        if (configNode == null)
        {
            initConfigNode();
        }

        return configNode;
    }

    protected void initLocalSystem() throws VrsException
    {
        localSystem = new LocalSystem(this);
    }

    protected void initConfigNode()
    {
        configNode = new InfoConfigNode(this);
    }

    public void addResourceLink(String folderName, String logicalName, VRL targetLink, String optIconURL) throws VrsException
    {
        InfoRSNode parentNode;

        if (folderName != null)
        {
            parentNode = this.getSubNode(folderName);
            if (parentNode == null)
            {
                parentNode = this.createResourceFolder(folderName, null);
            }
        }
        else
        {
            parentNode = this;
        }

        InfoResourceNode node = InfoResourceNode.createLinkNode(parentNode, logicalName, targetLink, optIconURL, true);
        parentNode.addNode(node);
    }

    protected InfoResourceNode createResourceFolder(String folderName, String optIconURL) throws VrsException
    {
        InfoRSNode node = this.getSubNode(folderName);

        if (node instanceof InfoResourceNode)
        {
            return (InfoResourceNode) node;
        }
        else if (node != null)
        {
            throw new VrsException("Type Mismatch: InfoRSNode name'" + folderName + "' already exists, but is not a InfoResourceNode:"
                    + node);
        }
        else
        {
            InfoResourceNode folder = InfoResourceNode.createFolderNode(this, folderName, optIconURL);
            this.addNode(folder);
            return folder;
        }
    }

    public List<String> getChildResourceTypes()
    {
        // Root Node support default InfoRS types: 
       return defaultFolderChildTypes;  
    }
    
}

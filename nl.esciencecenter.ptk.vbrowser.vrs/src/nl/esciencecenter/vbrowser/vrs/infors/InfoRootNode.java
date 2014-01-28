package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class InfoRootNode extends InfoRSNode
{
    protected InfoRS infors;

    protected LocalSystem localSystem;

    protected InfoConfigNode configNode;

    public InfoRootNode(InfoRS infoRS) throws VrsException
    {
        super(infoRS, InfoConstants.INFOROOTNODE, new VRL("info", null, 0, "/"));
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

}

package nl.esciencecenter.vbrowser.vrs.infors;

public class InfoConfigNode extends InfoRSNode
{

    public InfoConfigNode(InfoRootNode infoRootNode)
    {
        super(infoRootNode, InfoRSConstants.INFOCONFIGNODE, InfoRS.createPathVRL(InfoRSConstants.INFOCONFIGNODE));
    }

    public InfoConfigNode(InfoConfigNode parentNode, String subName)
    {
        super(parentNode, InfoRSConstants.INFOCONFIGNODE, InfoRS.createPathVRL(InfoRSConstants.INFOCONFIGNODE + "/" + subName));
    }

    public String getIconURL(int size)
    {
        return "info/configure-128.png";
    }

    public String getName()
    {
        return "Config";
    }

}

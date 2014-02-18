package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public class InfoLinkNode extends InfoResourceNode
{
    protected InfoLinkNode(InfoRSNode parent, VRL logicalVRL, VRL targetVRL)
    {
        super(parent, InfoRSConstants.RESOURCELINK, logicalVRL);
        this.setTargetVRL(targetVRL);
        this.setIconUrl(null); // default ?
        this.setShowLinkIcon(true);
        this.setLogicalName("link:" + logicalVRL.getBasename());
    }

}

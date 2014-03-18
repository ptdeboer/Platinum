package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface VInfoResourceFolder extends VInfoResource
{
    /** 
     * Add node to internal node list. 
     * Either parent of subNode is null or should already be set to this ResourceFolder. 
     */
    public void addInfoNode(InfoRSNode subNode)  throws VrsException; 
    
    public VInfoResourceFolder createFolder(String name) throws VrsException; 
    
    public VInfoResource createResourceLink(VRL targetVRL,String logicalName) throws VrsException;
}

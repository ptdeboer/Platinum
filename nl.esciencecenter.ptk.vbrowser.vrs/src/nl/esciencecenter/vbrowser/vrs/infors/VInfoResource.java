package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Shared interface for ResourceLinks and ResourceFolders 
 */
public interface VInfoResource
{
    public boolean isResourceLink(); 
    
    public boolean isResourceFolder(); 
    
    public VRL getTargetVRL(); 
    
    /** 
     * Add node to internal node list. 
     * Either parent of subNode is null or should already be set to this ResourceFolder. 
     */
    public void addInfoNode(InfoRSNode subNode)  throws VrsException; 
    
    public VInfoResourcePath createFolder(String name) throws VrsException; 
    
    public VInfoResourcePath createResourceLink(VRL targetVRL,String logicalName) throws VrsException;
}

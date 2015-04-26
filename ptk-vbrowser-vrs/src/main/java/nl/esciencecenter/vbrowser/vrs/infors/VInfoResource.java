package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.vbrowser.vrs.data.AttributeSet;
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
     * Create new InfoRS node of specified resourceType from persistent Attributes.  
     */
    public VInfoResourcePath createSubNode(String resourceType, AttributeSet infoAttributes)  throws VrsException; 
    
    public VInfoResourcePath createFolder(String name) throws VrsException; 
    
    public VInfoResourcePath createResourceLink(VRL targetVRL,String logicalName) throws VrsException;
    
    public AttributeSet getInfoAttributes()  throws VrsException;
    
}

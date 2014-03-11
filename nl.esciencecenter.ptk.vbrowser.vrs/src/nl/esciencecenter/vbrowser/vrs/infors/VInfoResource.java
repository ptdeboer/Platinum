package nl.esciencecenter.vbrowser.vrs.infors;

import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Shared interface for ResourceLinks and ResourceFolders 
 */
public interface VInfoResource
{
    public boolean isResourceLink(); 
    
    public boolean isResourceFolder(); 
    
    public VRL getTargetVRL(); 
    
}

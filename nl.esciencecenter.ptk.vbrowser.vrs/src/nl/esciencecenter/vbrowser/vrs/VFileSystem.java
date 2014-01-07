package nl.esciencecenter.vbrowser.vrs;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public interface VFileSystem extends VResourceSystem
{
    // Downcast to VFSPath
    public VFSPath resolvePath(String path) throws VrsException; 
    
    // Downcast to VFSPath 
    public VFSPath resolvePath(VRL vrl) throws VrsException; 

}

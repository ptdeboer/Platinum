package nl.esciencecenter.vbrowser.vrs;

import java.util.List;

import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Explicit file system path. 
 * 
 * @author Piter T. de Boer
 *
 */
public interface VFSPath extends VPath
{

    @Override
    public VRL resolvePathVRL(String path) throws VrsException; 
    
    // Downcast VPath to VFSPath: 
    @Override
    public VFSPath resolvePath(String path) throws VrsException; 

    // Downcast VPath to VFSPath:
    @Override
    public VFSPath getParent() throws VrsException; 
       
    /**
     * @return true if current path is root of this file system, for example "/" or "C:". 
     */
    public abstract boolean isRoot() throws VrsException; 
    
    public abstract boolean isDir() throws VrsException; 
    
    public abstract boolean isFile() throws VrsException; 
 
    public abstract List<? extends VFSPath> list() throws VrsException; 
    
}

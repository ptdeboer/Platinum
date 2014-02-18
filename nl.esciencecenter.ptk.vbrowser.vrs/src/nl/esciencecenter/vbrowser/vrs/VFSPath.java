package nl.esciencecenter.vbrowser.vrs;

import java.nio.file.LinkOption;
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
    // downcast to VFileSystem. 
    public VFileSystem getVFileSystem() throws VrsException; 
    
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
    
    public abstract boolean isDir(LinkOption... linkOptions) throws VrsException; 
    
    public abstract boolean isFile(LinkOption... linkOptions) throws VrsException; 
 
    public abstract boolean exists(LinkOption... linkOptions) throws VrsException; 
    
    public abstract List<? extends VFSPath> list() throws VrsException; 
 
    /** 
     * Create last part of this path as directory. 
     * @param ignoreExisting
     * @throws VrsException
     */
    public abstract void mkdir(boolean ignoreExisting) throws VrsException;

    /** 
     * Create complete path as directory. 
     * @param ignoreExisting
     * @throws VrsException
     */
    public abstract void mkdirs(boolean ignoreExisting) throws VrsException;

    public abstract void createFile(boolean ignoreExisting) throws VrsException;
    
}

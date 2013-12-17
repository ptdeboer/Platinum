package nl.esciencecenter.vbrowser.vrs.node;

import java.util.List;

import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VFileSystem;
import nl.esciencecenter.vbrowser.vrs.VRSTypes;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

public abstract class VFileSystemNode extends VResourceSystemNode implements VFileSystem, VFSPath
{

    protected VFileSystemNode(VRL serverVrl)
    {
        super(serverVrl);
        
    }

    @Override
    public String getResourceType()
    {
        return VRSTypes.FILESYSTEM_TYPE;
    }

    @Override
    public VFSPath getParent() throws VrsException
    {
        // default of FileSystem Root is FileSystem itself. 
        return this; 
    }

    @Override 
    public boolean isRoot() throws VrsException
    {
        return true; 
    }

    @Override
    public boolean isDir() throws VrsException
    {
        return true; 
    }

    @Override
    public boolean isFile() throws VrsException
    {
        return false; 
    }

    @Override
    public List<? extends VFSPath> list() throws VrsException
    {
        return createVFSNode(this.getServerVRL()).list(); 
    }

    @Override
    public VFSPath resolvePath(String relativePath) throws VrsException
    {
        return resolvePath(resolvePathVRL(relativePath)); 
    }
    
    @Override
    public VFSPath resolvePath(VRL vrl) throws VrsException
    {
        return createVFSNode(vrl); 
    }
    
    abstract protected VFSPathNode createVFSNode(VRL vrl) throws VrsException;
    
}

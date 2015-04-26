package nl.esciencecenter.vbrowser.vrs.node;

import java.nio.file.LinkOption;
import java.util.List;

import nl.esciencecenter.vbrowser.vrs.VFSPath;
import nl.esciencecenter.vbrowser.vrs.VFileSystem;
import nl.esciencecenter.vbrowser.vrs.exceptions.VrsException;
import nl.esciencecenter.vbrowser.vrs.vrl.VRL;

/** 
 * Adaptor for the abstract VFSPathNode class. Currently used for testing.  
 */
public class VFSPathNodeAdaptor extends VFSPathNode
{

    protected VFSPathNodeAdaptor(VFileSystem fileSystem, VRL vrl)
    {
        super(fileSystem, vrl);
    }

    @Override
    public long fileLength(LinkOption... linkOptions) throws VrsException
    {
        return -1;
    }

    @Override
    public boolean exists(LinkOption... linkOptions) throws VrsException
    {
        return false;
    }

    @Override
    public FileAttributes getFileAttributes(LinkOption... linkOptions) throws VrsException
    {
        return null;
    }

    @Override
    public boolean isRoot() throws VrsException
    {
        return false;
    }

    @Override
    public List<? extends VFSPath> list() throws VrsException
    {
        return null;
    }

    @Override
    public boolean createFile(boolean ignoreExisting) throws VrsException
    {
        return false;
    }

    @Override
    public boolean mkdir(boolean ignoreExisting) throws VrsException
    {
        return false;
    }

    public boolean delete(LinkOption... linkOptions) throws VrsException
    {
        return false; 
    }

    @Override
    public VFSPath renameTo(VFSPath newPath) throws VrsException
    {
        return null;
    }


}
